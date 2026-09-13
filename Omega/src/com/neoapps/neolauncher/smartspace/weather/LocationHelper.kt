/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neoapps.neolauncher.smartspace.weather

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.android.launcher3.BuildConfig
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.Permissions.checkLocationAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class LocationHelper(private val context: Context) {

    data class ResolvedLocation(
        val latitude: Double,
        val longitude: Double,
        val cityName: String
    )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentOrLastLocation(): ResolvedLocation? = withContext(Dispatchers.IO) {
        if (!context.checkLocationAccess()) return@withContext null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = try {
                    locationManager.getLastKnownLocation(provider)
                } catch (e: SecurityException) {
                    null
                }
                if (loc != null) {
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            }
        }

        val location = bestLocation ?: return@withContext null
        val cityName = reverseGeocode(location.latitude, location.longitude)
            ?: "${String.format(Locale.US, "%.2f", location.latitude)}, ${
                String.format(
                    Locale.US,
                    "%.2f",
                    location.longitude
                )
            }"

        ResolvedLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = cityName
        )
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Utilities.ATLEAST_T) {
                    var result: String? = null
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        result = address.locality ?: address.subAdminArea ?: address.adminArea
                    }
                    result
                } else {
                    @Suppress("DEPRECATION")
                    val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        address.locality ?: address.subAdminArea ?: address.adminArea
                    } else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun getCoordinatesForCity(cityName: String, provider: String): GeoResult? =
        withContext(Dispatchers.IO) {
            val encodedCity = URLEncoder.encode(cityName.trim(), "UTF-8")
            val lang = Locale.getDefault().language

            var url =
                "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=$lang&format=json"
            if (provider == OWMWeatherProvider::class.java.name) {
                val apiKey = NeoPrefs.getInstance().smartspaceWeatherApiKey.getValue().trim()
                url =
                    "https://api.openweathermap.org/geo/1.0/direct?q=$encodedCity&limit=1&appid=$apiKey"
            }

            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "NeoLauncher/${BuildConfig.VERSION_NAME} (Android)"
                )
                .build()
            val client: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body.string()
                    val json = JSONObject(body)
                    if (!json.has("results")) return@withContext null
                    val results = json.getJSONArray("results")
                    if (results.length() == 0) return@withContext null
                    val first = results.getJSONObject(0)
                    GeoResult(
                        name = first.optString("name", cityName),
                        latitude = if (provider == OWMWeatherProvider::class.java.name) first.getDouble(
                            "lat"
                        ) else first.getDouble("latitude"),
                        longitude = if (provider == OWMWeatherProvider::class.java.name) first.getDouble(
                            "lon"
                        ) else first.getDouble("longitude"),
                        country = if (first.has("country")) first.getString("country") else null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}

data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null
)
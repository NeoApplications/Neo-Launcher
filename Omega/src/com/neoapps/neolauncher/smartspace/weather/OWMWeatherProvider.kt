/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import android.widget.Toast
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import com.neoapps.neolauncher.data.models.HourlyWeather
import com.neoapps.neolauncher.data.models.WeatherInfo
import com.neoapps.neolauncher.neoApp
import com.neoapps.neolauncher.smartspace.Temperature
import com.neoapps.neolauncher.smartspace.model.SmartspaceScores
import com.neoapps.neolauncher.smartspace.model.WeatherData
import com.neoapps.neolauncher.smartspace.provider.SmartspaceDataSource
import com.neoapps.neolauncher.smartspace.showWeatherDetailsDialog
import com.neoapps.neolauncher.smartspace.weather.GoogleWeatherProvider.Companion.dummyTarget
import com.neoapps.neolauncher.smartspace.weather.icons.WeatherIconProvider
import com.neoapps.neolauncher.util.Permissions
import com.neoapps.neolauncher.util.Permissions.REQUEST_PERMISSION_LOCATION_ACCESS
import com.neoapps.neolauncher.util.Permissions.checkLocationAccess
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OWMWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.weather_provider_owm
), CurrentWeatherCallback {
    override val isAvailable = true
    override val disabledTargets = listOf(dummyTarget)
    private val _internalTargets = MutableStateFlow(disabledTargets)
    private var lastUpdatedMillis = 0L

    override var internalTargets: Flow<List<SmartspaceTarget>> = _internalTargets.onStart {
        if (System.currentTimeMillis() - lastUpdatedMillis >= UPDATE_INTERVAL_MILLIS || weatherData == null) {
            updateData()
        }
    }

    private fun getOwmHelper(): OpenWeatherMapHelper {
        return OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.getValue().trim())
    }

    private val iconProvider by lazy { WeatherIconProvider(context) }
    private var weatherData: WeatherData? = null
    val scope = CoroutineScope(Dispatchers.IO)
    private val locationAccess get() = context.checkLocationAccess()
    private val currentCity get() = prefs.smartspaceWeatherCity.getValue()
    var weatherInfo: WeatherInfo? = null

    init {
        updateData()
        scope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MILLIS)
                updateData()
            }
        }
        scope.launch {
            combine(
                prefs.smartspaceWeatherCity.get(),
                prefs.smartspaceWeatherUnit.get(),
                prefs.smartspaceWeatherApiKey.get()
            ) { _, _, _ -> }
                .drop(1)
                .collect {
                    updateData()
                }
        }
    }

    private fun updateWeatherData(): List<SmartspaceTarget> {
        val currentWeatherData = weatherData
        if (currentWeatherData != null) {
            Log.d("OWM", "Updating weather data " + currentWeatherData.getTitle())
            val target = SmartspaceTarget(
                smartspaceTargetId = "OWMWeatherMap",
                headerAction = SmartspaceAction(
                    id = "OWMWeatherMap",
                    icon = Icon.createWithBitmap(currentWeatherData.icon),
                    title = "",
                    subtitle = currentWeatherData.getTitle(Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())),
                    pendingIntent = currentWeatherData.pendingIntent,
                    onClick = { view ->
                        val info = weatherInfo
                        if (info != null) {
                            showWeatherDetailsDialog(view.context, info)
                            if (System.currentTimeMillis() - lastUpdatedMillis >= UPDATE_INTERVAL_MILLIS) {
                                updateData()
                            }
                        } else {
                            updateData()
                        }
                    }
                ),
                score = SmartspaceScores.SCORE_WEATHER,
                featureType = SmartspaceTarget.FEATURE_WEATHER,
            )
            return listOf(target)
        } else {
            return disabledTargets
        }
    }

    fun updateWeatherInfo(currentWeather: CurrentWeather) {
        val hourlyList = mutableListOf<HourlyWeather>()

        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val apiKey = prefs.smartspaceWeatherApiKey.getValue().trim()
        val isFahrenheit = prefs.smartspaceWeatherUnit.getValue() == "imperial" ||
                prefs.smartspaceWeatherUnit.getValue() == "fahrenheit"
        val units = if (isFahrenheit) "imperial" else "metric"
        val lang = Locale.getDefault().language

        try {
            var forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?" +
                    "lat=$latitude&lon=$longitude&units=$units&lang=$lang&appid=$apiKey"
            if (currentCity != "##Auto") {
                forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?" +
                        "q=$currentCity&units=$units&lang=$lang&appid=$apiKey"
            }

            Log.d("OWM", "Forecast URL: $forecastUrl")
            val forecastReq = Request.Builder().url(forecastUrl).build()
            client.newCall(forecastReq).execute().use { fResponse ->
                Log.d("OWM", "Forecast response code: ${fResponse.code}")
                if (fResponse.isSuccessful) {
                    val fJson = JSONObject(fResponse.body.string())
                    val list = fJson.optJSONArray("list")
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val nowSec = System.currentTimeMillis() / 1000 - 3600

                    if (list != null) {
                        var count = 0
                        for (i in 0 until list.length()) {
                            if (count >= 12) break
                            val item = list.getJSONObject(i)
                            val dt = item.optLong("dt", 0L)
                            if (dt >= nowSec) {
                                val hMain = item.getJSONObject("main")
                                val hTemp = hMain.optDouble("temp", 0.0)
                                val hWeatherArr = item.optJSONArray("weather")
                                val hFirst = hWeatherArr?.optJSONObject(0)
                                val hOwmId = hFirst?.optInt("id", 800) ?: 800
                                val hIcon = hFirst?.optString("icon", "01d") ?: "01d"

                                hourlyList.add(
                                    HourlyWeather(
                                        time = timeFormat.format(Date(dt * 1000)),
                                        temperature = hTemp,
                                        weatherCode = mapOwmToWmoCode(hOwmId),
                                        isDay = hIcon.endsWith("d")
                                    )
                                )
                                count++
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }

        val targetUnit = Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())
        val owmId = currentWeather.weather.getOrNull(0)?.id?.toInt() ?: 800

        val tempKelvin = (currentWeather.main?.temp ?: 273.15).roundToInt()
        val feelsLikeKelvin = (currentWeather.main?.feelsLike ?: 273.15).roundToInt()
        val maxTempKelvin = (currentWeather.main?.tempMax ?: 273.15).roundToInt()
        val minTempKelvin = (currentWeather.main?.tempMin ?: 273.15).roundToInt()

        val tempConverted =
            Temperature(tempKelvin, Temperature.Unit.Kelvin).inUnit(targetUnit).toDouble()
        val feelsLikeConverted =
            Temperature(feelsLikeKelvin, Temperature.Unit.Kelvin).inUnit(targetUnit).toDouble()
        val maxTempConverted =
            Temperature(maxTempKelvin, Temperature.Unit.Kelvin).inUnit(targetUnit).toDouble()
        val minTempConverted =
            Temperature(minTempKelvin, Temperature.Unit.Kelvin).inUnit(targetUnit).toDouble()

        weatherInfo = WeatherInfo(
            cityName = currentWeather.name ?: cityName,
            temperature = tempConverted,
            apparentTemperature = feelsLikeConverted,
            weatherCode = mapOwmToWmoCode(owmId),
            humidity = currentWeather.main?.humidity ?: 0.0,
            windSpeed = currentWeather.wind?.speed ?: 0.0,
            precipitation = currentWeather.rain?.oneHour ?: 0.0,
            isDay = currentWeather.weather.getOrNull(0)?.icon?.contains("d") ?: true,
            maxTemp = maxTempConverted,
            minTemp = minTempConverted,
            unit = targetUnit.suffix,
            hourly = hourlyList,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun mapOwmToWmoCode(owmId: Int): Int {
        return when (owmId) {
            800 -> 0 // Clear
            801 -> 1 // Mainly clear / Few clouds
            802 -> 2 // Partly cloudy / Scattered clouds
            803, 804 -> 3 // Overcast / Broken clouds
            701, 711, 721, 741 -> 45 // Fog / Mist / Haze
            731, 751, 761, 762, 771, 781 -> 48 // Dust / Sand / Squall
            in 300..321 -> 51 // Drizzle
            500, 501, 520, 521 -> 61 // Rain: slight or moderate
            502, 503, 504, 522, 531 -> 65 // Rain: heavy
            511 -> 66 // Freezing rain
            in 600..601, 620, 621 -> 71 // Snow: slight or moderate
            602, 622 -> 75 // Snow: heavy
            in 611..616 -> 77 // Sleet / snow grains
            in 200..232 -> 95 // Thunderstorm
            else -> 2
        }
    }

    @SuppressLint("MissingPermission")
    fun updateData() {
        val city = currentCity
        if (city == "##Auto") {
            if (!locationAccess) {
                Permissions.requestPermission(
                    context.neoApp.activityHandler.foregroundActivity!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    REQUEST_PERMISSION_LOCATION_ACCESS
                )
                return
            } else {
                scope.launch {
                    val resolvedLoc = LocationHelper(context).getCurrentOrLastLocation()
                    if (resolvedLoc != null) {
                        latitude = resolvedLoc.latitude
                        longitude = resolvedLoc.longitude
                        cityName = resolvedLoc.cityName
                    }
                    getOwmHelper().getCurrentWeatherByGeoCoordinates(
                        latitude,
                        longitude,
                        this@OWMWeatherProvider
                    )
                }
            }
        } else {
            Log.d("OWM", "Updating weather data for $city")
            getOwmHelper().getCurrentWeatherByCityName(city, this)
        }
    }

    override fun onSuccess(currentWeather: CurrentWeather) {
        val temp = currentWeather.main?.temp ?: return
        val icon = currentWeather.weather.getOrNull(0)?.icon ?: return
        lastUpdatedMillis = System.currentTimeMillis()
        weatherData = WeatherData(
            iconProvider.getIcon(icon),
            Temperature(
                temp.roundToInt(),
                Temperature.Unit.Kelvin
            ),
            "https://openweathermap.org/city/${currentWeather.id}"
        )
        currentWeather.coord?.lat?.let { latitude = it }
        currentWeather.coord?.lon?.let { longitude = it }
        _internalTargets.value = updateWeatherData()
        scope.launch {
            updateWeatherInfo(currentWeather)
            _internalTargets.value = updateWeatherData()
        }
    }

    override fun onFailure(throwable: Throwable?) {
        if ((prefs.smartspaceWeatherApiKey.getValue() == context.getString(R.string.default_owm_key)
                    && !BuildConfig.APPLICATION_ID.contains("debug"))
            || throwable?.message == apiKeyError
        ) {
            Toast.makeText(context, R.string.owm_get_your_own_key, Toast.LENGTH_LONG).show()
        } else if (throwable != null) {
            Log.d("OWM", "Updating weather data failed", throwable)
            Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        }
        _internalTargets.value = updateWeatherData()
    }

    companion object {
        private const val UPDATE_INTERVAL_MINUTES = 30L
        private val UPDATE_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(UPDATE_INTERVAL_MINUTES)
        private const val apiKeyError = "UnAuthorized. Please set a valid OpenWeatherMap API KEY" +
                " by using the setApiKey method."
    }
}
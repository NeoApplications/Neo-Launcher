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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class OWMWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.weather_provider_owm
), CurrentWeatherCallback {
    override val isAvailable = true
    override val disabledTargets = listOf(dummyTarget)
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    private val owm by lazy { OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.getValue()) }
    private val iconProvider by lazy { WeatherIconProvider(context) }
    private var weatherData: WeatherData? = null
    val scope = CoroutineScope(Dispatchers.IO)
    private val locationAccess get() = context.checkLocationAccess()
    var weatherInfo: WeatherInfo? = null

    init {
        updateData()
        internalTargets = flow {
            while (true) {
                updateData()
                emit(updateWeatherData())
                delay(TimeUnit.MINUTES.toMillis(25).milliseconds)
            }
        }
    }

    private fun updateWeatherData(): List<SmartspaceTarget> {
        if (weatherData != null) {
            Log.d("OWM", "Updating weather data " + weatherData?.getTitle())
            val target = SmartspaceTarget(
                smartspaceTargetId = "OWMWeatherMap",
                headerAction = SmartspaceAction(
                    id = "OWMWeatherMap",
                    icon = Icon.createWithBitmap(weatherData!!.icon),
                    title = "",
                    subtitle = weatherData?.getTitle(Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())),
                    pendingIntent = weatherData?.pendingIntent
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
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        val apiKey = prefs.smartspaceWeatherApiKey.getValue().trim()
        val isFahrenheit = prefs.smartspaceWeatherUnit.getValue() == "fahrenheit"
        val units = if (isFahrenheit) "imperial" else "metric"
        val lang = Locale.getDefault().language

        try {
            val forecastUrl = "https://api.openweathermap.org/data/4.0/forecast?" +
                    "lat=$latitude&lon=$longitude&units=$units&lang=$lang&appid=$apiKey"
            val forecastReq = Request.Builder().url(forecastUrl).build()
            client.newCall(forecastReq).execute().use { fResponse ->
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

        weatherInfo = WeatherInfo(
            cityName = currentWeather.name,
            temperature = currentWeather.main?.temp ?: 0.0,
            apparentTemperature = currentWeather.main?.feelsLike ?: 0.0,
            weatherCode = currentWeather.weather.getOrNull(0)?.id ?: 0,
            humidity = currentWeather.main?.humidity ?: 0.0,
            windSpeed = currentWeather.wind?.speed ?: 0.0,
            precipitation = currentWeather.rain?.oneHour ?: 0.0,
            isDay = currentWeather.weather.getOrNull(0)?.icon?.contains("d") ?: true,
            maxTemp = currentWeather.main?.tempMax ?: 0.0,
            minTemp = currentWeather.main?.tempMin ?: 0.0,
            unit = Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue()).name,
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
        if (prefs.smartspaceWeatherCity.getValue() == "##Auto") {
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
                        owm.getCurrentWeatherByGeoCoordinates(
                            resolvedLoc.latitude,
                            resolvedLoc.longitude,
                            this@OWMWeatherProvider
                        )
                    }
                }
            }
        } else {
            Log.d("OWM", "Updating weather data for " + prefs.smartspaceWeatherCity.getValue())
            owm.getCurrentWeatherByCityName(prefs.smartspaceWeatherCity.getValue(), this)
        }
    }

    override fun onSuccess(currentWeather: CurrentWeather) {
        val temp = currentWeather.main?.temp ?: return
        val icon = currentWeather.weather.getOrNull(0)?.icon ?: return
        weatherData = WeatherData(
            iconProvider.getIcon(icon),
            Temperature(
                temp.roundToInt(),
                Temperature.Unit.Kelvin
            ),
            "https://openweathermap.org/city/${currentWeather.id}"
        )
        updateWeatherData()
        updateWeatherInfo(currentWeather)
    }

    override fun onFailure(throwable: Throwable?) {
        if ((prefs.smartspaceWeatherApiKey.getValue() == context.getString(R.string.default_owm_key)
                    && !BuildConfig.APPLICATION_ID.contains("debug")
                    && !BuildConfig.APPLICATION_ID.contains("alpha"))
            || throwable?.message == apiKeyError
        ) {
            Toast.makeText(context, R.string.owm_get_your_own_key, Toast.LENGTH_LONG).show()
        } else if (throwable != null) {
            Log.d("OWM", "Updating weather data failed", throwable)
            Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        }
        updateWeatherData()
    }

    companion object {

        private const val apiKeyError = "UnAuthorized. Please set a valid OpenWeatherMap API KEY" +
                " by using the setApiKey method."
    }
}
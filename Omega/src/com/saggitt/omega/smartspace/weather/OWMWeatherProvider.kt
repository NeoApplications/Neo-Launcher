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

package com.saggitt.omega.smartspace.weather

import android.annotation.SuppressLint
import android.app.smartspace.SmartspaceAction
import android.app.smartspace.SmartspaceTarget
import android.content.Context
import android.graphics.drawable.Icon
import android.location.Criteria
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import com.saggitt.omega.neoApp
import com.saggitt.omega.smartspace.model.SmartspaceScores
import com.saggitt.omega.smartspace.model.WeatherData
import com.saggitt.omega.smartspace.provider.SmartspaceDataSource
import com.saggitt.omega.smartspace.weather.GoogleWeatherProvider.Companion.dummyTarget
import com.saggitt.omega.smartspace.weather.icons.WeatherIconProvider
import com.saggitt.omega.util.checkLocationAccess
import com.saggitt.omega.widget.Temperature
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OWMWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.weather_provider_owm
), CurrentWeatherCallback {
    override val isAvailable = true
    override val disabledTargets = listOf(dummyTarget)
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    private val owm by lazy { OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.getValue()) }
    private val iconProvider by lazy { WeatherIconProvider(context) }
    private var weatherData: WeatherData? = null
    private val locationAccess get() = context.checkLocationAccess()
    private val locationManager: LocationManager? by lazy {
        if (locationAccess) {
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        } else null
    }

    init {
        updateData()
        internalTargets = flow {
            while (true) {
                updateData()
                emit(updateWeatherData())
                delay(TimeUnit.MINUTES.toMillis(10))
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

    @SuppressLint("MissingPermission")
    fun updateData() {
        if (prefs.smartspaceWeatherCity.getValue() == "##Auto") {
            if (!locationAccess) {
                Utilities.requestLocationPermission(context.neoApp.activityHandler.foregroundActivity)
                return
            } else {
                val locationProvider = locationManager?.getBestProvider(Criteria(), true)
                val location = locationProvider?.let { locationManager?.getLastKnownLocation(it) }
                if (location != null) {
                    owm.getCurrentWeatherByGeoCoordinates(
                        location.latitude,
                        location.longitude,
                        this
                    )
                }
            }
        } else {
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
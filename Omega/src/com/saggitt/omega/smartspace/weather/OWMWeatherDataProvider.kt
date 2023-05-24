package com.saggitt.omega.smartspace.weather

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.kwabenaberko.openweathermaplib.constant.Units
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import com.saggitt.omega.neoApp
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.smartspace.weather.icons.WeatherIconProvider
import com.saggitt.omega.util.checkLocationAccess
import com.saggitt.omega.widget.Temperature
import kotlin.math.roundToInt

class OWMWeatherDataProvider(controller: OmegaSmartSpaceController) :
    OmegaSmartSpaceController.PeriodicDataProvider(controller), CurrentWeatherCallback {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val owm by lazy { OpenWeatherMapHelper(prefs.smartspaceWeatherApiKey.getValue()) }
    private val iconProvider by lazy { WeatherIconProvider(context) }

    private val locationAccess get() = context.checkLocationAccess()
    private val locationManager: LocationManager? by lazy {
        if (locationAccess) {
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        } else null
    }

    init {
        owm.setUnits(
            when (Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())) {
                Temperature.Unit.Celsius -> Units.METRIC
                Temperature.Unit.Fahrenheit -> Units.IMPERIAL
                else -> Units.METRIC
            }
        )
    }

    @SuppressLint("MissingPermission")
    override fun updateData() {
        // TODO: Create a search/dropdown for cities, make Auto the default
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
        val currentUnit = Temperature.unitFromString(prefs.smartspaceWeatherUnit.getValue())
        updateData(
            OmegaSmartSpaceController.WeatherData(
                iconProvider.getIcon(icon),
                Temperature(
                    temp.roundToInt(),
                    if (currentUnit != Temperature.Unit.Fahrenheit) Temperature.Unit.Celsius
                    else Temperature.Unit.Fahrenheit
                ),
                "https://openweathermap.org/city/${currentWeather.id}"
            ), null
        )
    }

    override fun onFailure(throwable: Throwable?) {
        Log.w("OWM", "Updating weather data failed", throwable)
        if ((prefs.smartspaceWeatherApiKey.getValue() == context.getString(R.string.default_owm_key)
                    && !BuildConfig.APPLICATION_ID.contains("debug"))
            || throwable?.message == apiKeyError
        ) {
            Toast.makeText(context, R.string.owm_get_your_own_key, Toast.LENGTH_LONG).show()
        } else if (throwable != null) {
            Log.d("OWM", "Updating weather data failed", throwable)
            Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        }
        updateData(null, null)
    }

    companion object {

        private const val apiKeyError = "UnAuthorized. Please set a valid OpenWeatherMap API KEY" +
                " by using the setApiKey method."
    }
}
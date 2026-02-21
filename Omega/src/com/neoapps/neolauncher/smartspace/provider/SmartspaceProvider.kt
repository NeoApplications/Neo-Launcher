package com.neoapps.neolauncher.smartspace.provider

import android.app.Activity
import android.content.Context
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PreferenceActivity
import com.neoapps.neolauncher.smartspace.weather.BlankWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.GoogleWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.OWMWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.PixelWeatherProvider
import com.neoapps.neolauncher.util.dropWhileBusy
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class SmartspaceProvider private constructor(context: Context) {
    val prefs = NeoPrefs.getInstance()
    private val currentWeather = prefs.smartspaceWeatherProvider.getValue()
    private val weatherProvider = when (currentWeather) {
        GoogleWeatherProvider::class.java.name -> GoogleWeatherProvider(context)
        OWMWeatherProvider::class.java.name    -> OWMWeatherProvider(context)
        PixelWeatherProvider::class.java.name  -> PixelWeatherProvider(context)
        else                                   -> BlankWeatherProvider(context)
    }

    private val dataSources = arrayListOf(
        weatherProvider
    )

    val providers = prefs.smartspaceEventProviders.getValue().forEach { provider ->
        when (provider) {
            BatteryStatusProvider::class.java.name -> BatteryStatusProvider(context)
            NowPlayingProvider::class.java.name    -> NowPlayingProvider(context)
            CalendarEventProvider::class.java.name -> CalendarEventProvider(context)
            AlarmEventProvider::class.java.name    -> AlarmEventProvider(context)
            NotificationUnreadProvider::class.java.name -> NotificationUnreadProvider(context)
            else                                   -> null
        }?.let { dataSources.add(it) }
    }

    val state = dataSources
        .map { it.targets }
        .reduce { acc, flow -> flow.combine(acc) { a, b -> a + b } }
        .shareIn(
            MainScope(),
            SharingStarted.WhileSubscribed(),
            replay = 1
        )

    val targets = state
        .map {
            if (it.requiresSetup.isNotEmpty()) {
                it.targets + listOf(setupTarget)
            } else {
                it.targets
            }
        }
    val previewTargets = state
        .map { it.targets }

    private val setupTarget = SmartspaceTarget(
        smartspaceTargetId = "smartspaceSetup",
        headerAction = SmartspaceAction(
            id = "smartspaceSetupAction",
            title = context.getString(R.string.smartspace_setup_text),
            intent = PreferenceActivity.navigateIntent(context, Routes.PREFS_WIDGETS)
        ),
        score = -1,
        featureType = SmartspaceTarget.FEATURE_TIPS
    )

    suspend fun startSetup(activity: Activity) { // TODO link to smartspace setup screen
        state
            .map { it.requiresSetup }
            .dropWhileBusy()
            .collect { sources ->
                sources.forEach {
                    it.startSetup(activity)
                    it.onSetupDone()
                }
            }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::SmartspaceProvider)
    }
}

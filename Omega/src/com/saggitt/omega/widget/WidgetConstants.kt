package com.saggitt.omega.widget

import android.content.Context
import com.android.launcher3.R
import com.saggitt.omega.widget.weatherprovider.BlankDataProvider
import com.saggitt.omega.widget.weatherprovider.FakeDataProvider
import com.saggitt.omega.widget.weatherprovider.PEWeatherDataProvider

class WidgetConstants {
    companion object {
        private val displayNames = mapOf(
            Pair(BlankDataProvider::class.java.name, R.string.title_disabled),
            //Pair(SmartSpaceDataWidget::class.java.name, R.string.google_app),
            //Pair(OWMWeatherDataProvider::class.java.name, R.string.weather_provider_owm),
            Pair(PEWeatherDataProvider::class.java.name, R.string.weather_provider_pe),
            //Pair(NowPlayingProvider::class.java.name, R.string.event_provider_now_playing),
            //Pair(
            //    NotificationUnreadProvider::class.java.name,
            //    R.string.event_provider_unread_notifications
            //),
            //Pair(BatteryStatusProvider::class.java.name, R.string.battery_status),
            //Pair(AlarmEventProvider::class.java.name, R.string.name_provider_alarm_events),
            //Pair(PersonalityProvider::class.java.name, R.string.personality_provider),
            //Pair(OnboardingProvider::class.java.name, R.string.onbording),
            //Pair(CalendarEventProvider::class.java.name, R.string.smartspace_provider_calendar),
            Pair(FakeDataProvider::class.java.name, R.string.weather_provider_testing)
        )

        fun getDisplayName(providerName: String): Int {
            return displayNames[providerName] ?: error("No display name for provider $providerName")
        }

        fun getDisplayName(context: Context, providerName: String): String {
            return context.getString(getDisplayName(providerName))
        }
    }
}

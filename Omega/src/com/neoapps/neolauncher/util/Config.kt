/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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

package com.neoapps.neolauncher.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.res.Resources
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.neoapps.neolauncher.allapps.HiddenAppFilter
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PrefKey
import com.neoapps.neolauncher.smartspace.provider.AlarmEventProvider
import com.neoapps.neolauncher.smartspace.provider.BatteryStatusProvider
import com.neoapps.neolauncher.smartspace.provider.CalendarEventProvider
import com.neoapps.neolauncher.smartspace.provider.NotificationUnreadProvider
import com.neoapps.neolauncher.smartspace.provider.NowPlayingProvider
import com.neoapps.neolauncher.smartspace.weather.BlankWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.GoogleWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.OWMWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.PixelWeatherProvider
import com.neoapps.neolauncher.theme.ThemeOverride
import java.util.Locale

class Config(val context: Context) {

    //TODO: Use ContextWrapper instead of UpdateConfiguration
    fun setAppLanguage(languageCode: String) {
        val locale = getLocaleByAndroidCode(languageCode)
        val config = context.resources.configuration
        val mLocale =
            if (languageCode.isNotEmpty()) locale else Resources.getSystem().configuration.locales[0]
        config.setLocale(mLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLocaleByAndroidCode(languageCode: String): Locale {
        return if (!TextUtils.isEmpty(languageCode)) {
            if (languageCode.contains("-r")) Locale(
                languageCode.substring(0, 2),
                languageCode.substring(4, 6)
            ) // de-rAt
            else Locale(languageCode) // de
        } else Resources.getSystem().configuration.locales[0]
    }

    fun getAppsList(filter: HiddenAppFilter?): MutableList<LauncherActivityInfo> {
        val apps = ArrayList<LauncherActivityInfo>()
        val profiles = UserCache.INSTANCE[context].userProfiles
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        profiles.forEach { apps += launcherApps.getActivityList(null, it) }
        return if (filter != null) {
            apps.filter { filter.shouldShowApp(it.componentName) }.toMutableList()
        } else {
            apps
        }
    }

    companion object {
        //PERMISSION FLAGS
        const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
        const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
        const val REQUEST_PERMISSION_READ_CONTACTS = 668

        const val GOOGLE_QSB = "com.google.android.googlequicksearchbox"
        const val GOOGLE_TEXT_ASSIST = "com.google.android.googlequicksearchbox.TEXT_ASSIST"

        //APP DRAWER SORT MODE
        const val SORT_AZ = 0
        const val SORT_ZA = 1
        const val SORT_MOST_USED = 2
        const val SORT_BY_COLOR = 3
        const val SORT_BY_INSTALL_DATE = 4

        //COMPOSE THEME COLORS
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_BLACK = 2

        //CATEGORY BOTTOM SHEET
        const val BS_NONE = -1
        const val BS_SELECT_TAB_TYPE = 0
        const val BS_CREATE_GROUP = 1
        const val BS_EDIT_GROUP = 2

        val drawerSortOptions = mutableMapOf(
            SORT_AZ to R.string.title__sort_alphabetical_az,
            SORT_ZA to R.string.title__sort_alphabetical_za,
            SORT_MOST_USED to R.string.title__sort_most_used,
            SORT_BY_COLOR to R.string.title__sort_by_color,
            SORT_BY_INSTALL_DATE to R.string.title__sort_last_installed,
        )

        const val LAWNICONS_PACKAGE_NAME = "app.lawnchair.lawnicons"

        val ICON_INTENTS = arrayOf(
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.THEMES"),
            Intent("org.adw.launcher.icons.ACTION_PICK_ICON"),
            Intent("com.anddoes.launcher.THEME"),
            Intent("com.teslacoilsw.launcher.THEME"),
            Intent("com.fede.launcher.THEME_ICONPACK"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("com.dlto.atom.launcher.THEME"),
        )

        val smartspaceEventProviders = mapOf(
            BatteryStatusProvider::class.java.name to R.string.battery_status,
            NowPlayingProvider::class.java.name to R.string.event_provider_now_playing,
            CalendarEventProvider::class.java.name to R.string.smartspace_provider_calendar,
            AlarmEventProvider::class.java.name to R.string.name_provider_alarm_events,
            NotificationUnreadProvider::class.java.name to R.string.event_provider_unread_notifications,
        )

        fun smartspaceWeatherProviders(context: Context) = mapOf(
            BlankWeatherProvider::class.java.name to context.resources.getString(R.string.title_disabled),
            GoogleWeatherProvider::class.java.name to context.resources.getString(R.string.google),
            OWMWeatherProvider::class.java.name to context.resources.getString(R.string.weather_provider_owm),
            if (PixelWeatherProvider.isAvailable(context))
                PixelWeatherProvider::class.java.name to context.resources.getString(R.string.weather_provider_pe)
            else {
                "none" to "none"
            }
        )

        fun calendarOptions(context: Context) = mapOf(
            context.resources.getString(R.string.smartspace_calendar_gregorian) to context.resources.getString(
                R.string.title_calendar_gregorian
            ),
            context.resources.getString(R.string.smartspace_calendar_persian) to context.resources.getString(
                R.string.title_calendar_persian
            )
        )

        fun getCurrentTheme(context: Context): Int {
            val themeSet = ThemeOverride.Settings()
            var currentTheme = THEME_LIGHT
            if (themeSet.getTheme(context) == R.style.SettingsTheme_Dark) {
                currentTheme = THEME_DARK
            } else if (themeSet.getTheme(context) == R.style.SettingsTheme_Black) {
                currentTheme = THEME_BLACK
            }
            return currentTheme
        }

        fun getIdpDefaultValue(context: Context, key: Preferences.Key<Int>): Int {
            var originalIdp = 0
            val idp = LauncherAppState.getIDP(context)
            if (key == PrefKey.DOCK_COLUMNS) {
                originalIdp = idp.numColumns
            } else if (key == PrefKey.DRAWER_GRID_COLUMNS) {
                originalIdp = idp.numAllAppsColumns
            }
            return originalIdp
        }

        fun hasWorkApps(context: Context): Boolean {
            return context.prefs.drawerSeparateWorkApps.getValue() &&
                    UserCache.INSTANCE.get(context).userProfiles.size > 1
        }

        /**
         * Shows authentication screen to confirm credentials (pin, pattern or password) for the current
         * user of the device.
         *
         * @param context The {@code Context} used to get {@code KeyguardManager} service
         * @param title the {@code String} which will be shown as the pompt title
         * @param successRunnable The {@code Runnable} which will be executed if the user does not setup
         *                        device security or if lock screen is unlocked
         */
        @RequiresApi(Build.VERSION_CODES.R)
        fun showLockScreen(context: Context, title: String, successRunnable: Runnable) {
            if (hasSecureKeyguard(context)) {

                val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        successRunnable.run()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        //Do nothing
                    }
                }

                val bp = BiometricPrompt.Builder(context)
                    .setTitle(title)
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()

                val handler = MAIN_EXECUTOR.handler
                bp.authenticate(
                    CancellationSignal(), { runnable: Runnable ->
                        handler.post(runnable)
                    },
                    authenticationCallback
                )
            } else {
                // Notify the user a secure keyguard is required for protected apps,
                // but allow to set hidden apps
                Toast.makeText(context, R.string.trust_apps_no_lock_error, Toast.LENGTH_LONG)
                    .show()
                successRunnable.run()
            }
        }

        fun activeCategories(context: Context): Map<String, Int> {
            val activeCategories = Flowerpot.Manager.getInstance(context).getAllPots().filter {
                it.ensureLoaded()
                it.apps.matches.isNotEmpty()
            }

            val categories: Map<String, Int> = activeCategories.associate {
                it.name to it.resId
            }
            return categories
        }

        private fun hasSecureKeyguard(context: Context): Boolean {
            val keyguardManager = context.getSystemService(
                KeyguardManager::class.java
            )
            return keyguardManager != null && keyguardManager.isKeyguardSecure
        }

        /*
            Check is the app is protected
        */
        fun isAppProtected(context: Context, componentKey: ComponentKey): Boolean {
            var result = false
            val protectedApps = ArrayList(
                NeoPrefs.getInstance().drawerProtectedAppsSet.getValue()
                    .map { Utilities.makeComponentKey(context, it) })

            if (protectedApps.contains(componentKey)) {
                result = true
            }
            return result
        }
    }
}
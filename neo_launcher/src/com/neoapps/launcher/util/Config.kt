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
 */

package com.neoapps.launcher.util

import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR

class Config(val context: Context) {
    companion object {

        //APP DRAWER SORT MODE
        const val SORT_AZ = 0
        const val SORT_ZA = 1
        const val SORT_MOST_USED = 2
        const val SORT_BY_COLOR = 3
        const val SORT_BY_INSTALL_DATE = 4

        val drawerSortOptions = mutableMapOf(
            SORT_AZ to R.string.title__sort_alphabetical_az,
            SORT_ZA to R.string.title__sort_alphabetical_za,
            SORT_MOST_USED to R.string.title__sort_most_used,
            SORT_BY_COLOR to R.string.title__sort_by_color,
            SORT_BY_INSTALL_DATE to R.string.title__sort_last_installed,
        )

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
                CoreUtils.getNeoPrefs().drawerProtectedAppsSet.getValue()
                    .map { CoreUtils.makeComponentKey(context, it) })

            if (protectedApps.contains(componentKey)) {
                result = true
            }
            return result
        }
    }
}
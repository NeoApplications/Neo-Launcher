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

package com.neoapps.neolauncher.compose.components.preferences

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.util.SettingsCache
import com.android.launcher3.util.SettingsCache.NOTIFICATION_BADGING_URI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun notificationDotsEnabled(context: Context) = callbackFlow {
    val observer = SettingsCache.OnChangeListener {
        val enabled = SettingsCache.INSTANCE.get(context).getValue(NOTIFICATION_BADGING_URI)
        trySend(enabled)
    }
    val settingsCache = SettingsCache.INSTANCE.get(context)
    observer.onSettingsChanged(false)
    settingsCache.register(NOTIFICATION_BADGING_URI, observer)
    awaitClose { settingsCache.unregister(NOTIFICATION_BADGING_URI, observer) }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    val myListener = ComponentName(context, NotificationListener::class.java)
    return enabledListeners != null &&
            (enabledListeners.contains(myListener.flattenToString()) ||
                    enabledListeners.contains(myListener.flattenToShortString()))
}
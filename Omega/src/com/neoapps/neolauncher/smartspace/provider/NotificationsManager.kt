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

package com.neoapps.neolauncher.smartspace.provider

import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.util.checkPackagePermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsManager(@Suppress("UNUSED_PARAMETER") context: Context) {

    private val scope = MainScope()
    private val notificationsMap = mutableMapOf<String, StatusBarNotification>()
    private val _notifications = MutableStateFlow(emptyList<StatusBarNotification>())
    val notifications: Flow<List<StatusBarNotification>> get() = _notifications

    fun onNotificationPosted(sbn: StatusBarNotification) {
        notificationsMap[sbn.key] = sbn
        onChange()
    }

    fun onNotificationRemoved(sbn: StatusBarNotification) {
        notificationsMap.remove(sbn.key)
        onChange()
    }

    fun onNotificationFullRefresh() {
        scope.launch(Dispatchers.IO) {
            val tmpMap = runCatching {
                NotificationListener.getInstanceIfConnected()?.activeNotifications?.associateBy { it.key }
            }.getOrNull()
            withContext(Dispatchers.Main) {
                notificationsMap.clear()
                if (tmpMap != null) {
                    notificationsMap.putAll(tmpMap)
                }
                onChange()
            }
        }
    }

    private fun onChange() {
        _notifications.value = notificationsMap.values.toList()
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::NotificationsManager)
    }
}

private const val PERM_SUBSTITUTE_APP_NAME = "android.permission.SUBSTITUTE_NOTIFICATION_APP_NAME"
private const val EXTRA_SUBSTITUTE_APP_NAME = "android.substName"

fun StatusBarNotification.getAppName(context: Context): CharSequence {
    val subName = notification.extras.getString(EXTRA_SUBSTITUTE_APP_NAME)
    if (subName != null) {
        if (context.checkPackagePermission(packageName, PERM_SUBSTITUTE_APP_NAME)) {
            return subName
        }
    }
    return context.getAppName(packageName)
}

fun Context.getAppName(name: String): CharSequence {
    try {
        return packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA)
        )
    } catch (_: PackageManager.NameNotFoundException) {
    }
    return name
}

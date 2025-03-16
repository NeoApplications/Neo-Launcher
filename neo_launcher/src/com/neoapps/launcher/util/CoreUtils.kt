/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.UserHandle
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.neoapps.launcher.preferences.NeoPrefs
import kotlin.system.exitProcess


class CoreUtils {


    companion object {

        fun minSDK(sdk: Int): Boolean {
            return Build.VERSION.SDK_INT >= sdk
        }

        fun killLauncher() {
            exitProcess(0)
        }

        fun getNeoPrefs(): NeoPrefs {
            return NeoPrefs.getInstance()
        }

        /**
         * Creates a new component key from an encoded component key string in the form of
         * [flattenedComponentString#userId].  If the userId is not present, then it defaults
         * to the current user.
         */
        fun makeComponentKey(context: Context, componentKeyStr: String): ComponentKey {
            val componentName: ComponentName?
            val user: UserHandle
            val userDelimiterIndex = componentKeyStr.indexOf("#")
            if (userDelimiterIndex != -1) {
                val componentStr = componentKeyStr.substring(0, userDelimiterIndex)
                val componentUser =
                    componentKeyStr.substring(userDelimiterIndex + 1, componentKeyStr.length)
                        .toLong()
                componentName = ComponentName.unflattenFromString(componentStr)
                user = notNullOrDefault(
                    UserCache.INSTANCE[context]
                        .getUserForSerialNumber(componentUser), Process.myUserHandle()
                )
            } else {
                // No user provided, default to the current user
                componentName = ComponentName.unflattenFromString(componentKeyStr)
                user = Process.myUserHandle()
            }
            try {
                return ComponentKey(componentName, user)
            } catch (e: NullPointerException) {
                throw NullPointerException("Trying to create invalid component key: $componentKeyStr")
            }
        }

        private fun <T> notNullOrDefault(value: T?, defValue: T): T {
            return value ?: defValue
        }
    }
}
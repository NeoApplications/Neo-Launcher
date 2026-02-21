/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.icons.cache.CacheLookupFlag.Companion.DEFAULT_LOOKUP_FLAG
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import java.util.Locale

@Composable
fun appsState(
    comparator: Comparator<App> = appComparator
): State<List<App>> {
    val context = LocalContext.current
    val appsState = remember { mutableStateOf(emptyList<App>()) }
    LaunchedEffect(true) {
        Utilities.postAsyncCallback(Handler(MODEL_EXECUTOR.looper)) {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val appsList = UserCache.INSTANCE.get(context).userProfiles.asSequence()
                    .flatMap { launcherApps!!.getActivityList(null, it) }
                    .map { App(context, it) }
                    .sortedWith(comparator)
                    .toList()
            appsState.value = appsList
        }
    }
    return appsState
}

class App(context: Context, private val info: LauncherActivityInfo) {

    val label get() = info.label.toString()
    val packageName get() = info.componentName.packageName
    val icon: Bitmap
    val key = ComponentKey(info.componentName, info.user)

    init {
        val appInfo = AppInfo(context, info, info.user)
        LauncherAppState.getInstance(context).iconCache.getTitleAndIcon(appInfo, DEFAULT_LOOKUP_FLAG)
        icon = appInfo.bitmap.icon
    }
}

val appComparator: Comparator<App> = comparing { it.label.lowercase(Locale.getDefault()) }

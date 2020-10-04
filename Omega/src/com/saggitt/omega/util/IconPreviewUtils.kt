/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.saggitt.omega.util

import android.content.Context
import android.content.res.Resources
import android.os.Process
import com.android.launcher3.AppInfo
import com.android.launcher3.R
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.util.PackageManagerHelper
import com.saggitt.omega.allapps.CustomAppFilter

object IconPreviewUtils {

    private fun getPreviewPackages(resources: Resources): Array<String> {
        return resources.getStringArray(R.array.icon_shape_preview_packages)
    }

    fun getPreviewAppInfos(context: Context): List<AppInfo> {
        val launcherApps = LauncherAppsCompat.getInstance(context)
        val user = Process.myUserHandle()
        val appFilter = CustomAppFilter(context)
        val predefined = getPreviewPackages(context.resources)
                .filter { PackageManagerHelper.isAppInstalled(context.packageManager, it, 0) }
                .mapNotNull { launcherApps.getActivityList(it, user).firstOrNull() }
                .asSequence()
        val randomized = launcherApps.getActivityList(null, Process.myUserHandle())
                .shuffled()
                .asSequence()
        return (predefined + randomized)
                .filter { appFilter.shouldShowApp(it.componentName, it.user) }
                .take(20)
                .map { AppInfo(it, it.user, false) }
                .toList()
    }
}
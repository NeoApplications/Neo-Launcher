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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.android.launcher3.R
import com.neoapps.launcher.util.CoreUtils.Companion.minSDK

fun PackageManager.isAppEnabled(packageName: String?, flags: Int): Boolean {
    return try {
        val info = getApplicationInfo(packageName!!, flags)
        info.enabled
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun PackageManager.isPackageInstalled(packageName: String) =
    try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

fun PackageManager.getPackageVersionCode(packageName: String) =
    try {
        val info = getPackageInfo(packageName, 0)
        when {
            minSDK(Build.VERSION_CODES.P) -> info.longVersionCode
            else -> info.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        -1L
    }

fun PackageManager.getPackageVersion(packageName: String) =
    try {
        val packageInfo = getPackageInfo(packageName, 0)
        packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }

fun PackageManager.getThemedIconPacksInstalled(context: Context): List<String> =
    try {
        queryIntentActivityOptions(
            ComponentName(context.applicationInfo.packageName, context.applicationInfo.className),
            null,
            Intent(context.resources.getString(R.string.icon_packs_intent_name)),
            PackageManager.GET_RESOLVED_FILTER,
        ).map { it.activityInfo.packageName }
    } catch (_: PackageManager.NameNotFoundException) {
        emptyList()
    }
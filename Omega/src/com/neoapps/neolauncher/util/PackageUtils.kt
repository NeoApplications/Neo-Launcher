package com.neoapps.neolauncher.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.android.launcher3.R

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
        info.longVersionCode
    } catch (e: PackageManager.NameNotFoundException) {
        -1L
    }

fun PackageManager.getPackageVersion(packageName: String) =
    try {
        val packageInfo = getPackageInfo(packageName, 0)
        packageInfo.versionName.orEmpty()
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
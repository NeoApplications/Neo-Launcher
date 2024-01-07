package com.saulhdev.neolauncher.util

import android.content.pm.PackageManager
import com.android.launcher3.Utilities
import com.android.launcher3.util.PackageManagerHelper

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
            Utilities.ATLEAST_P -> info.longVersionCode
            else                -> info.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        -1L
    }

fun PackageManager.getPackageVersion(packageName: String) =
    try {
        val packageInfo = getPackageInfo(packageName, 0)
        packageInfo.versionName
    }
    catch (e: PackageManager.NameNotFoundException) {
        ""
    }
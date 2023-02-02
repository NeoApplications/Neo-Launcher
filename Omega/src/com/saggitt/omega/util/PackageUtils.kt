package com.saggitt.omega.util

import android.content.pm.PackageManager

fun PackageManager.isAppEnabled(packageName: String?, flags: Int): Boolean {
    return try {
        val info = getApplicationInfo(packageName!!, flags)
        info.enabled
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
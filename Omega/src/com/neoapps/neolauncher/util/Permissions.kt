package com.neoapps.neolauncher.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object Permissions {
    const val READ_EXTERNAL_STORAGE = "android.Manifest.permission.READ_EXTERNAL_STORAGE"

    const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
    const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
    const val REQUEST_PERMISSION_READ_CONTACTS = 668

    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(permission),
            requestCode
        )
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }
}


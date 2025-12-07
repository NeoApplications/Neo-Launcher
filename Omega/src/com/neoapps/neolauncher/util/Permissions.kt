package com.neoapps.neolauncher.util

import android.app.Activity
import androidx.core.app.ActivityCompat

object Permissions {
    const val READ_EXTERNAL_STORAGE = "android.Manifest.permission.READ_EXTERNAL_STORAGE"
    const val WRITE_EXTERNAL_STORAGE = "android.Manifest.permission.WRITE_EXTERNAL_STORAGE"

    fun requestStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            666
        )
    }
}


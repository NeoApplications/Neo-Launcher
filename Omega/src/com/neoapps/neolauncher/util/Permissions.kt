package com.neoapps.neolauncher.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissions {

    const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
    const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
    const val REQUEST_PERMISSION_READ_CONTACTS = 668
    const val REQUEST_PERMISSION_WALLPAPER_ACCESS = 669

    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(permission),
            requestCode
        )
    }

    @JvmStatic
    fun hasPermission(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Returns the appropriate permission for accessing wallpaper based on the API level.
     * - API 33+: READ_MEDIA_IMAGES
     * - API < 33: READ_EXTERNAL_STORAGE
     */
    fun getWallpaperPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}

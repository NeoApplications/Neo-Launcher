package com.neoapps.neolauncher.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.launcher3.Utilities

object Permissions {

    const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
    const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
    const val REQUEST_PERMISSION_READ_CONTACTS = 668
    const val REQUEST_PERMISSION_WALLPAPER_ACCESS = 669

    fun Context.checkPackagePermission(packageName: String, permissionName: String): Boolean {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            info.requestedPermissions!!.forEachIndexed { index, s ->
                if (s == permissionName) {
                    return info.requestedPermissionsFlags?.get(index)!!
                        .hasFlag(PackageInfo.REQUESTED_PERMISSION_GRANTED)
                }
            }
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return false
    }

    fun Context.checkLocationAccess(): Boolean {
        return hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

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

    val Context.hasWallpaperAccess: Boolean
        get() = if (Utilities.ATLEAST_T) {
            hasPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Utilities.ATLEAST_R) {
            hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    Environment.isExternalStorageManager()
        } else {
            hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

}

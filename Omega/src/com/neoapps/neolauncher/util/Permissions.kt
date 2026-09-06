package com.neoapps.neolauncher.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.android.launcher3.R
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
        return Permissions.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                Permissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val Context.hasStoragePermission
        get() = if (Utilities.ATLEAST_T) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
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
        get() = if (Utilities.ATLEAST_R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

    /**
     * Returns the appropriate permission for accessing wallpaper based on the API level.
     * - API 33+: READ_MEDIA_IMAGES
     * - API < 33: READ_EXTERNAL_STORAGE
     */
    fun getWallpaperPermission(
        activity: Activity,
        onGranted: () -> Unit = {},
        onCancelled: () -> Unit = {}
    ): Boolean {
        if (activity.hasWallpaperAccess) {
            onGranted()
            return true
        }

        if (Utilities.ATLEAST_R) {
            var actionTaken = false
            val dialog = AlertDialog.Builder(activity)
                .setTitle(activity.resources.getString(R.string.permission_wallpaper_title))
                .setMessage(activity.resources.getString(R.string.permission_wallpaper_message))
                .setPositiveButton(activity.resources.getString(R.string.permission_grant)) { dialogInterface, _ ->
                    actionTaken = true
                    dialogInterface.dismiss()
                    onGranted()
                    try {
                        val intent =
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = "package:${activity.packageName}".toUri()
                            }
                        activity.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        activity.startActivity(intent)
                    }
                }
                .setNegativeButton(activity.resources.getString(android.R.string.cancel)) { dialogInterface, _ ->
                    actionTaken = true
                    dialogInterface.dismiss()
                    onCancelled()
                }
                .setOnDismissListener {
                    if (!actionTaken) {
                        onCancelled()
                    }
                }
                .create()
            dialog.show()
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            val requestCode = Permissions.REQUEST_PERMISSION_WALLPAPER_ACCESS

            var actionTaken = false
            val dialog = AlertDialog.Builder(activity)
                .setTitle(activity.resources.getString(R.string.permission_wallpaper_title))
                .setMessage(activity.resources.getString(R.string.permission_wallpaper_message))
                .setPositiveButton(activity.resources.getString(R.string.permission_grant)) { dialogInterface, _ ->
                    actionTaken = true
                    dialogInterface.dismiss()
                    onGranted()
                    Permissions.requestPermission(activity, permission, requestCode)
                }
                .setNegativeButton(activity.resources.getString(android.R.string.cancel)) { dialogInterface, _ ->
                    actionTaken = true
                    dialogInterface.dismiss()
                    onCancelled()
                }
                .setOnDismissListener {
                    if (!actionTaken) {
                        onCancelled()
                    }
                }
                .create()
            dialog.show()
        }

        return false
    }
}

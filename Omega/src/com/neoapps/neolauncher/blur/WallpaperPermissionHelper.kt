/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neoapps.neolauncher.blur

import android.app.Activity
import android.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.neoapps.neolauncher.util.Permissions
import com.neoapps.neolauncher.util.hasWallpaperAccess

object WallpaperPermissionHelper {

    fun requestIfNeeded(activity: Activity): Boolean {
        if (activity.hasWallpaperAccess) {
            return true
        }

        val permission = Permissions.getWallpaperPermission()
        val requestCode = Permissions.REQUEST_PERMISSION_WALLPAPER_ACCESS

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            AlertDialog.Builder(activity)
                .setTitle(activity.resources.getString(R.string.permission_wallpaper_title))
                .setMessage(activity.resources.getString(R.string.permission_wallpaper_message))
                .setPositiveButton(activity.resources.getString(R.string.permission_grant)) { dialog, _ ->
                    dialog.dismiss()
                    Permissions.requestPermission(activity, permission, requestCode)
                }
                .setNegativeButton(activity.resources.getString(android.R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Permissions.requestPermission(activity, permission, requestCode)
        }

        return false
    }
}

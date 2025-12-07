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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.util

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Point
import android.graphics.drawable.Drawable
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings
import com.android.launcher3.dragndrop.FolderAdaptiveIcon
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.CacheableShortcutCachingLogic
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.shortcuts.ShortcutKey
import com.android.launcher3.shortcuts.ShortcutRequest
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.widget.PendingAddShortcutInfo

object DrawableUtils {
    fun getFullDrawable(
        context: Context?, info: ItemInfo, width: Int, height: Int,
        outObj: Array<Any?>?, useTheme: Boolean
    ): Drawable? {
        var icon = loadFullDrawableWithoutTheme(context!!, info, width, height, outObj!!)
        if (useTheme && icon is BitmapInfo.Extender) {
            icon = (icon as BitmapInfo.Extender).getThemedDrawable(context)
        }
        return icon
    }


    fun loadFullDrawableWithoutTheme(
        context: Context,
        info: ItemInfo,
        width: Int,
        height: Int,
        outObj: Array<Any?>
    ): Drawable? {
        val activity = ActivityContext.lookupContext(context)
        val appState = LauncherAppState.getInstance(context)

        return when {
            info is PendingAddShortcutInfo -> {
                val activityInfo = info.getActivityInfo(context)
                outObj[0] = activityInfo
                activityInfo.getFullResIcon(appState.iconCache)
            }

            info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION -> {
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val activityInfo = launcherApps.resolveActivity(info.intent, info.user)
                outObj[0] = activityInfo
                activityInfo?.let {
                    LauncherAppState.getInstance(context)
                        .iconProvider
                        .getIcon(it, activity.deviceProfile.inv.fillResIconDpi)
                }
            }

            info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT -> {
                val si = ShortcutKey.fromItemInfo(info)
                    .buildRequest(context)
                    .query(ShortcutRequest.ALL)

                if (si.isEmpty()) {
                    null
                } else {
                    outObj[0] = si[0]
                    ShortcutCachingLogic.getIcon(
                        context, si[0],
                        appState.invariantDeviceProfile.fillResIconDpi
                    )
                }
            }

            info.itemType == LauncherSettings.Favorites.ITEM_TYPE_FOLDER -> {
                val folderInfo = info as FolderInfo
                if (folderInfo.isCoverMode()) {
                    return getFullDrawable(
                        context,
                        folderInfo.coverInfo,
                        width,
                        height,
                        outObj,
                        true
                    )
                }
                val icon = FolderAdaptiveIcon.createFolderAdaptiveIcon(
                    activity, info.id, Point(width, height)
                )
                if (icon == null) {
                    null
                } else {
                    outObj[0] = icon
                    icon
                }
            }

            info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SEARCH_ACTION
                    && info is ItemInfoWithIcon -> {
                info.bitmap.newIcon(context)
            }

            else -> null
        }
    }
}
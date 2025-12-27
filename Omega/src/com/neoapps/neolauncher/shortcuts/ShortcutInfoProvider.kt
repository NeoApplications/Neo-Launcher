/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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
 */

package com.neoapps.neolauncher.shortcuts

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
import com.android.launcher3.R
import com.android.launcher3.icons.LauncherIcons
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.data.models.IconPickerItem
import com.saggitt.omega.folder.CustomInfoProvider
import com.saggitt.omega.iconpack.IconEntry
import com.saggitt.omega.iconpack.IconType
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ShortcutInfoProvider(context: Context) : CustomInfoProvider<WorkspaceItemInfo>(context) {

    private val launcherApps by lazy { context.getSystemService(LauncherApps::class.java) }

    override fun getTitle(info: WorkspaceItemInfo): String {
        return if (info.title == null || info.title == "")
            getDefaultTitle(info)
        else
            info.title.toString()
    }

    override fun getDefaultTitle(info: WorkspaceItemInfo): String =
        context.getString(R.string.folder_hint_text)

    override fun getCustomTitle(info: WorkspaceItemInfo): String = if (info.title == null) ""
    else info.title.toString()

    override fun setTitle(info: WorkspaceItemInfo, title: String?, modelWriter: ModelWriter) {
        //TODO: Fix this
        //info.setTitle(context, title ?: "")
    }

    override fun setIcon(info: WorkspaceItemInfo, iconEntry: IconEntry) {
        if (iconEntry != null) {
            val launcherActivityInfo = getLauncherActivityInfo(info)
            val iconCache = LauncherAppState.getInstance(context).iconCache
            val drawable = iconCache.getFullResIcon(launcherActivityInfo as ActivityInfo)!!
            val bitmap = LauncherIcons.obtain(context)
                .createBadgedIconBitmap(drawable)
            val repository = IconOverrideRepository(context)
            val scope = MainScope()
            scope.launch {
                val iconPicker = IconPickerItem(
                    packPackageName = "System",
                    drawableName = bitmap.icon.toString(),
                    label = info.title.toString(),
                    type = IconType.Normal

                )
                repository.setOverride(info.componentKey!!, iconPicker)
            }
        }
    }

    override fun showBadge(info: WorkspaceItemInfo) = when (info.itemType) {
        ITEM_TYPE_SHORTCUT, ITEM_TYPE_DEEP_SHORTCUT -> {
            NeoPrefs.getInstance().notificationCount.getValue()
        }

        else -> false
    }

    override fun setSwipeUpAction(info: WorkspaceItemInfo, action: String?) {
        //TODO: Fix this
        //info.setSwipeUpAction(context, action)
    }

    /*override fun getSwipeUpAction(info: WorkspaceItemInfo): String? {
        return info.getSwipeUpAction(context)
    }*/

    private fun getLauncherActivityInfo(info: WorkspaceItemInfo): LauncherActivityInfo? {
        return launcherApps!!.resolveActivity(info.getIntent(), info.user)
    }

    companion object :
        SingletonHolder<ShortcutInfoProvider, Context>(ensureOnMainThread(useApplicationContext(::ShortcutInfoProvider)))
}
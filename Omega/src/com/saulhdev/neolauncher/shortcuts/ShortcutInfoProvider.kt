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

package com.saulhdev.neolauncher.shortcuts

import android.content.Context
import com.android.launcher3.R
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.saggitt.omega.folder.CustomInfoProvider
import com.saggitt.omega.iconpack.IconEntry

class ShortcutInfoProvider(context: Context) : CustomInfoProvider<WorkspaceItemInfo>(context) {

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
        info.setTitle(title ?: "", modelWriter)
    }

    override fun setIcon(info: WorkspaceItemInfo, iconEntry: IconEntry) {
    }

    override fun setSwipeUpAction(info: WorkspaceItemInfo, action: String?) {
        info.setSwipeUpAction(context, action)
    }

    override fun getSwipeUpAction(info: WorkspaceItemInfo): String? {
        return info.getSwipeUpAction(context)
    }
}
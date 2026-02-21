/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

package com.neoapps.neolauncher.groups.category

import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.neoapps.neolauncher.compose.components.ComposeBottomSheet
import com.neoapps.neolauncher.groups.ui.EditGroupBottomSheet
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.prefs

class DrawerFolderInfo(private val drawerFolder: DrawerFolders.Folder) : FolderInfo() {

    private var changed = false
    lateinit var appsStore: AllAppsStore

    override fun setTitle(title: CharSequence?, modelWriter: ModelWriter?) {
        super.setTitle(title, modelWriter)
        changed = true
        drawerFolder.title = title.toString()
    }

    /*
    override fun onIconChanged() {
        super.onIconChanged()
        drawerFolder.context.prefs.withChangeCallback {
            it.reloadGrid()
        }
    }*/

    fun onCloseComplete() {
        if (changed) {
            changed = false
            drawerFolder.context.prefs.drawerFolders.saveToJson()
        }
    }

    fun showEdit(launcher: Launcher) {
        val prefs = NeoPrefs.getInstance()
        ComposeBottomSheet.show(launcher) {
            EditGroupBottomSheet(
                category = prefs.drawerGroupsType!!,
                group = drawerFolder,
                onClose = { AbstractFloatingView.closeAllOpenViews(launcher) }
            )
        }
    }
}

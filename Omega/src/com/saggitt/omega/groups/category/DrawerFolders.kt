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

package com.saggitt.omega.groups.category

import android.content.Context
import com.android.launcher3.R
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.AppGroups
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.CustomFilter
import com.saggitt.omega.groups.Filter
import com.saggitt.omega.groups.GroupCreator
import com.saggitt.omega.groups.ShortcutInfoComparator
import com.saggitt.omega.preferences.PreferencesChangeCallback
import com.saggitt.omega.util.random

class DrawerFolders(val manager: AppGroupsManager) :
    AppGroups<DrawerFolders.Folder>(manager, AppGroupsManager.Category.FOLDER) {

    init {
        loadGroups()
    }

    override fun onGroupsChanged(changeCallback: PreferencesChangeCallback) {
        changeCallback.reloadGrid()
    }

    override fun getGroupCreator(type: String): GroupCreator<Folder> {
        return when (type) {
            TYPE_CUSTOM -> object : GroupCreator<Folder> {
                override fun createGroup(context: Context): Folder {
                    return CustomFolder(context)
                }
            }

            else -> object : GroupCreator<Folder> {
                override fun createGroup(context: Context): Folder? {
                    return null
                }
            }
        }
    }

    override fun getDefaultCreators(): List<GroupCreator<Folder>> {
        return emptyList()
    }

    fun getFolderInfos(apps: AlphabeticalAppsList<*>, modelWriter: ModelWriter) =
        getFolderInfos(buildAppsMap(apps)::get, modelWriter)

    private fun buildAppsMap(apps: AlphabeticalAppsList<*>): Map<ComponentKey, AppInfo> {
        // Copy the list before accessing it to prevent concurrent list access
        return apps.apps.toList().associateBy { it.toComponentKey() }
    }

    private fun getFolderInfos(
        getAppInfo: (ComponentKey) -> AppInfo?, modelWriter: ModelWriter,
    ): List<DrawerFolderInfo> = getGroups()
        .asSequence()
        .filter { !it.isEmpty }
        .map { it.toFolderInfo(getAppInfo, modelWriter) }
        .toList()

    fun getHiddenComponents() = getGroups()
        .asSequence()
        .filterIsInstance<CustomFolder>()
        .filter { it.hideFromAllApps.value() }
        .mapNotNull { it.contents.value }
        .flatMapTo(mutableSetOf()) { it.asSequence() }

    abstract class Folder(context: Context, type: String, titleRes: Int) :
        Group(type, context, context.getString(titleRes)) {
        val id = LongCustomization(KEY_ID, Long.random + 9999L)
        open val isEmpty = true

        init {
            addCustomization(id)
        }

        open fun toFolderInfo(getAppInfo: (ComponentKey) -> AppInfo?, modelWriter: ModelWriter) =
            DrawerFolderInfo(
                this
            ).apply {
                setTitle(this@Folder.title, modelWriter)
                id = this@Folder.id.value().toInt()
                contents = ArrayList()
            }
    }

    class CustomFolder(context: Context) :
        Folder(context, TYPE_CUSTOM, R.string.default_folder_name) {
        val hideFromAllApps = BooleanCustomization(KEY_HIDE_FROM_ALL_APPS, true)
        val contents = ComponentsCustomization(KEY_ITEMS, mutableSetOf())
        override val isEmpty get() = contents.value.isNullOrEmpty()

        val comparator = ShortcutInfoComparator(context)

        init {
            addCustomization(hideFromAllApps)
            addCustomization(contents)

            customizations.setOrder(KEY_TITLE, KEY_HIDE_FROM_ALL_APPS, KEY_ITEMS)
        }

        override val summary: String
            get() {
                val size = getFilter(context).size
                return context.resources.getQuantityString(R.plurals.tab_apps_count, size, size)
            }

        fun getFilter(context: Context): Filter<*> = CustomFilter(context, contents.value())

        override fun toFolderInfo(
            getAppInfo: (ComponentKey) -> AppInfo?,
            modelWriter: ModelWriter,
        ) = super
            .toFolderInfo(getAppInfo, modelWriter).apply {
                this@CustomFolder.contents.value?.mapNotNullTo(getContents()) { key ->
                    getAppInfo(key)?.makeWorkspaceItem(context)
                }?.sortWith(comparator)
            }
    }

    companion object {
        const val TYPE_CUSTOM = "0"
    }
}
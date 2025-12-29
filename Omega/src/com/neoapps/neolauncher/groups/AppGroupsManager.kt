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

package com.neoapps.neolauncher.groups

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.launcher3.R
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.neoapps.neolauncher.groups.category.CustomTabs
import com.neoapps.neolauncher.groups.category.DrawerFolders
import com.neoapps.neolauncher.groups.category.FlowerpotTabs
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PrefKey
import com.neoapps.neolauncher.preferences.StringSelectionPref
import com.neoapps.neolauncher.util.drawerCategorizationOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


typealias CategoryKey = String

class AppGroupsManager(val prefs: NeoPrefs, val dataStore: DataStore<Preferences>) {
    var categorizationType = StringSelectionPref(
        key = PrefKey.DRAWER_CATEGORIZATION_TYPE,
        dataStore = dataStore,
        titleId = R.string.pref_appcategorization_style_text,
        defaultValue = Category.NONE.key,
        entries = prefs.context.drawerCategorizationOptions,
        onChange = {
            MAIN_EXECUTOR.execute {
                onPrefsChanged()
            }
        }
    )

    val categorizationEnabled: Flow<Boolean> = categorizationType.get()
        .map { it != Category.NONE.key }


    val drawerTabs by lazy { CustomTabs(this) }
    private val flowerpotTabs by lazy { FlowerpotTabs(this) }
    val drawerFolders by lazy { DrawerFolders(this) }

    fun getCurrentCategory(): Category {
        return categories.firstOrNull { it.key == categorizationType.getValue() } ?: Category.NONE
    }

    private fun onPrefsChanged() {
        prefs.getOnChangeCallback()!!.let {
            drawerTabs.checkIsEnabled(it)
            flowerpotTabs.checkIsEnabled(it)
            drawerFolders.checkIsEnabled(it)
        }
    }

    fun getEnabledType(): Category? {
        return categories.firstOrNull { getModel(it)?.isEnabled ?: false }
    }

    fun getEnabledModel(): AppGroups<*>? {
        return categories.map { getModel(it) }.firstOrNull { it?.isEnabled ?: false }
    }

    fun getModel(type: Category): AppGroups<*>? {
        return when (type) {
            Category.FLOWERPOT -> flowerpotTabs
            Category.TAB -> drawerTabs
            Category.FOLDER -> drawerFolders
            Category.NONE -> null
            else -> null
        }
    }


    private val categories =
        listOf(Category.FLOWERPOT, Category.TAB, Category.FOLDER, Category.NONE)

    class Category(
        @StringRes val titleId: Int,
        @StringRes val summaryId: Int = -1,
        @DrawableRes val iconId: Int = -1,
        val key: CategoryKey,
        val type: Int = -1,
    ) {
        companion object {
            val NONE = Category(
                titleId = R.string.none,
                key = "pref_drawer_no_categorization",
                type = 0
            )
            val TAB = Category(
                titleId = R.string.app_categorization_tabs,
                summaryId = R.string.pref_appcategorization_tabs_summary,
                iconId = R.drawable.ic_category,
                key = "pref_drawer_tabs",
                type = 2
            )
            val FOLDER = Category(
                titleId = R.string.app_categorization_folders,
                summaryId = R.string.pref_appcategorization_folders_summary,
                iconId = R.drawable.ic_folder_outline,
                key = "pref_drawer_folders",
                type = 1
            )
            val FLOWERPOT = Category(
                titleId = R.string.pref_appcategorization_flowerpot_title,
                summaryId = R.string.pref_appcategorization_flowerpot_summary,
                iconId = R.drawable.ic_category,
                key = "pref_drawer_flowerpot",
                type = 4
            )
        }
    }

}
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

package com.saggitt.omega.groups

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android.launcher3.R
import com.saggitt.omega.groups.category.CustomTabs
import com.saggitt.omega.groups.category.DrawerFolders
import com.saggitt.omega.groups.category.FlowerpotTabs
import com.saggitt.omega.preferences.BooleanPref
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.preferences.PrefKey
import com.saggitt.omega.preferences.StringPref

class AppGroupsManager(val prefs: NeoPrefs, val dataStore: DataStore<Preferences>) {
    var categorizationEnabled = BooleanPref(
        key = PrefKey.DRAWER_CATEGORIZATION_ENABLED,
        dataStore = dataStore,
        titleId = R.string.title_app_categorization_enable,
        summaryId = R.string.summary_app_categorization_enable,
        defaultValue = false,
            onChange = { onPrefsChanged() }
    )

    var categorizationType = StringPref(
        key = PrefKey.DRAWER_CATEGORIZATION_TYPE,
        dataStore = dataStore,
        titleId = R.string.pref_appcategorization_style_text,
        defaultValue = "categorization_type_tabs",
        onChange = {
            onPrefsChanged()
        }
    )

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
        val key: String,
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
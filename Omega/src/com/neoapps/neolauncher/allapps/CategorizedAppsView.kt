/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.allapps

import android.content.Context
import android.content.res.Resources
import android.os.Process
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.android.launcher3.R
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.ItemInfoMatcher
import com.neoapps.neolauncher.NeoLauncher
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.theme.OmegaAppTheme
import com.neoapps.neolauncher.util.Config
import java.util.function.Predicate

class CategorizedAppsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val all = Pair(
        context.getString(R.string.title_drawer_all_apps),
        R.string.title_drawer_all_apps
    )

    private val prefs = NeoPrefs.getInstance()
    private val categories: MutableList<Pair<String, Int>> =
        Config.activeCategories(context).toList().toMutableList()
    private val enabledCategories = prefs.categoriesLayout.getValue()

    private val currentApps = ArrayList<ComponentKey>()
    private var currentCategory by mutableStateOf(categories[0])

    private var mAppsView: ActivityAllAppsContainerView<*>? = null
    private val personalMatcher: Predicate<ItemInfo?> =
        ItemInfoMatcher.ofUser(Process.myUserHandle())

    init {
        categories.sortBy {
            context.getString(it.second)
        }
        categories.removeAll { !enabledCategories.contains(it.first) }
        categories.add(0, all)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<ComposeView>(R.id.categories_bar).apply {
            setContent {
                OmegaAppTheme {
                    AllAppsCategories(
                        categories = categories,
                    ) {
                        if (currentCategory != it) {
                            currentCategory = it
                            updateApps()
                        }
                    }
                }
            }
            setPadding(0, getSearchBarTop(), 0, getScrollBarMarginBottom())
        }
    }

    private fun updateApps() {
        val launcher = NeoLauncher.getLauncher(context)
        val flowerpotManager = Flowerpot.Manager.getInstance(context)

        mAppsView = launcher.appsView
        if (currentCategory.second == R.string.title_drawer_all_apps) {
            currentApps.clear()
            mAppsView?.getActiveRecyclerView()?.apps?.updateItemFilter(personalMatcher)
        } else {
            val pot = flowerpotManager.getPot(currentCategory.first, true)
            currentApps.clear()
            pot!!.apps.matches.forEach {
                currentApps.add(it)
            }
            mAppsView?.appsStore!!.apps.filter {
                currentApps.containsAll(currentApps)
            }
            mAppsView?.getActiveRecyclerView()?.apps?.updateItemFilter { itemInfo: ItemInfo ->
                currentApps.contains(ComponentKey(itemInfo.targetComponent, itemInfo.user))
            }
            mAppsView?.getActiveRecyclerView()?.adapter?.notifyDataSetChanged()
        }
    }

    private fun getSearchBarTop(): Int {
        return paddingTop + resources.getDimensionPixelOffset(R.dimen.all_apps_header_top_padding)
    }

    private fun getScrollBarMarginBottom(): Int {
        if (hasNavigationBar()) {
            val resources = Resources.getSystem()
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId)
            }
            return 0
        }

        return 0
    }

    private fun hasNavigationBar(): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }
}

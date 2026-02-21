/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.neoapps.neolauncher.allapps

import android.content.Context
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.groups.category.DrawerTabs
import com.neoapps.neolauncher.groups.category.FlowerpotTabs
import com.neoapps.neolauncher.util.prefs
import java.util.function.Predicate

class AllAppsTabs(private val context: Context) : Iterable<AllAppsTabs.Tab> {

    val tabs = ArrayList<Tab>()
    val count get() = tabs.size

    var hasWorkApps = false
        set(value) {
            if (value != field) {
                field = value
                reloadTabs()
            }
        }

    private val addedApps = ArrayList<ComponentKey>()

    init {
        reloadTabs()
    }

    fun reloadTabs() {
        addedApps.clear()
        tabs.clear()
        context.prefs.drawerEnabledGroupsModel.getGroups().mapNotNullTo(tabs) {
            when (it) {
                is DrawerTabs.ProfileTab -> {
                    if (hasWorkApps != it.profile.matchesAll) {
                        ProfileTab(createMatcher(addedApps, it.profile.matcher), it)
                    } else null
                }

                is DrawerTabs.CustomTab -> {
                    if (it.hideFromAllApps.value()) {
                        addedApps.addAll(it.contents.value())
                    }
                    Tab(it.title, it.filter.matcher, drawerTab = it)
                }

                is FlowerpotTabs.FlowerpotTab if it.getMatches().isNotEmpty() -> {
                    addedApps.addAll(it.getMatches())
                    Tab(it.title, it.getFilter(context).matcher, drawerTab = it)
                }

                else -> null
            }
        }
    }

    private fun createMatcher(
        components: List<ComponentKey>,
        base: Predicate<ItemInfo>? = null,
    ): Predicate<ItemInfo> {
        return Predicate<ItemInfo> { info ->
            if (base?.test(info) == false) return@Predicate false
            return@Predicate !components.contains(ComponentKey(info.targetComponent, info.user))
        }
    }

    override fun iterator(): Iterator<Tab> {
        return tabs.iterator()
    }

    operator fun get(index: Int) = tabs[index]

    class ProfileTab(matcher: Predicate<ItemInfo>, drawerTab: DrawerTabs.ProfileTab) :
        Tab(drawerTab.title, matcher, drawerTab.profile.isWork, drawerTab)

    open class Tab(
        val name: String,
        val matcher: Predicate<ItemInfo>,
        val isWork: Boolean = false,
        val drawerTab: DrawerTabs.Tab,
    )
}

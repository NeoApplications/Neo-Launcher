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

import android.content.Context
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.CustomFilter
import com.neoapps.neolauncher.groups.Filter
import com.neoapps.neolauncher.groups.GroupCreator

class FlowerpotTabs(manager: AppGroupsManager) :
    DrawerTabs(manager, AppGroupsManager.Category.FLOWERPOT) {
    private val flowerpotManager = Flowerpot.Manager.getInstance(context)

    init {
        val pots = flowerpotManager.getAllPots().toMutableSet()
        val existingGroups = getGroups().filter { group ->
            if (group !is FlowerpotTab) {
                true
            } else {
                val pot = pots.firstOrNull { pot -> pot.name == group.potName.value }
                pot?.let { pots.remove(it) }
                pot != null
            }
        }.toMutableList()
        existingGroups.addAll(pots.map {
            FlowerpotTab(context).apply {
                title = it.displayName
                potName.value = it.name
            }
        })
        setGroups(existingGroups)
        saveToJson()
    }

    override fun getGroupCreator(type: String): GroupCreator<Tab> {
        return when (type) {
            TYPE_FLOWERPOT -> object : GroupCreator<Tab> {
                override fun createGroup(context: Context): Tab {
                    return FlowerpotTab(context)
                }
            }

            else -> super.getGroupCreator(type)
        }
    }

    class FlowerpotTab(context: Context) :
        Tab(context, TYPE_FLOWERPOT, context.getString(R.string.default_tab_name)) {
        val potName: FlowerpotCustomization = FlowerpotCustomization(
            KEY_FLOWERPOT,
            DEFAULT,
            context,
            customizations.entries.first { it is StringCustomization } as StringCustomization)

        private val pot
            get() = Flowerpot.Manager.getInstance(context).getPot(potName.value ?: DEFAULT, true)!!

        init {
            addCustomization(potName)
        }

        override val summary: String
            get() {
                val size = getFilter(context).size
                return context.resources.getQuantityString(R.plurals.tab_apps_count, size, size)
            }

        fun getMatches(): Set<ComponentKey> {
            pot.ensureLoaded()
            return pot.apps.matches
        }

        fun getFilter(context: Context): Filter<*> {
            return CustomFilter(context, getMatches())
        }

        companion object {
            const val DEFAULT = "PERSONALIZATION"
            const val KEY_FLOWERPOT = "potName"
        }
    }

    class FlowerpotCustomization(
        key: String,
        default: String,
        private val context: Context,
        private val title: StringCustomization,
    ) : StringCustomization(key, default) {
        private val flowerpotManager = Flowerpot.Manager.getInstance(context)
        private val displayName
            get() = flowerpotManager.getPot(value ?: default)?.displayName


        override fun saveToJson(context: Context): String {
            return value ?: default
        }


        override fun clone(): Customization<String, String> {
            return FlowerpotCustomization(key, default, context, title).also { it.value = value }
        }
    }

    companion object {
        const val KEY_FLOWERPOT = "potName"
        const val TYPE_FLOWERPOT = "4"
    }
}
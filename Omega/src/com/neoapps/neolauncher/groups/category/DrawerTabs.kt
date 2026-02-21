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
import android.os.Parcel
import android.os.Parcelable
import android.os.Process
import android.os.UserHandle
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.ItemInfoMatcher
import com.neoapps.neolauncher.groups.AppGroups
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.CustomFilter
import com.neoapps.neolauncher.groups.Filter
import com.neoapps.neolauncher.groups.GroupCreator
import com.neoapps.neolauncher.preferences.PreferencesChangeCallback
import com.neoapps.neolauncher.util.prefs
import org.json.JSONObject
import java.util.function.Predicate

abstract class DrawerTabs(manager: AppGroupsManager, type: AppGroupsManager.Category) :
    AppGroups<DrawerTabs.Tab>(manager, type) {

    private val personalTabCreator = ProfileTabCreator(Profile(null))

    private val workTabCreator by lazy {
        val workUser =
            UserCache.INSTANCE.get(context).userProfiles.firstOrNull { it != Process.myUserHandle() }
        if (workUser != null) ProfileTabCreator(Profile(workUser)) else object : GroupCreator<Tab> {
            override fun createGroup(context: Context): Tab? {
                return null
            }
        }
    }

    private val allAppsTabCreator = ProfileTabCreator(Profile())

    private val customTabCreator = object : GroupCreator<Tab> {
        override fun createGroup(context: Context): Tab {
            return CustomTab(context)
        }
    }

    private val flowerpotTabCreator = object : GroupCreator<Tab> {
        override fun createGroup(context: Context): Tab {
            return FlowerpotTabs.FlowerpotTab(context)
        }
    }

    init {
        loadGroups()
    }

    override fun getDefaultCreators(): List<GroupCreator<Tab>> {
        return listOf(allAppsTabCreator) + personalTabCreator +
                UserCache.INSTANCE.get(context).userProfiles.mapNotNull {
                    if (it != Process.myUserHandle()) ProfileTabCreator(Profile(it)) else null
                }
    }

    override fun getGroupCreator(type: String): GroupCreator<Tab> {
        if (type.startsWith(TYPE_PROFILE_PREFIX)) {
            val profile = Profile.fromString(context, type.substring(TYPE_PROFILE_PREFIX.length))
                ?: return object : GroupCreator<Tab> {
                    override fun createGroup(context: Context): Tab? {
                        return null
                    }
                }
            return ProfileTabCreator(profile)
        }
        return when (type) {
            TYPE_CUSTOM, TYPE_UNDEFINED -> customTabCreator
            TYPE_PERSONAL -> personalTabCreator
            TYPE_WORK -> workTabCreator
            TYPE_ALL_APPS -> allAppsTabCreator
            FlowerpotTabs.TYPE_FLOWERPOT -> flowerpotTabCreator
            else -> object : GroupCreator<Tab> {
                override fun createGroup(context: Context): Tab? {
                    return null
                }
            }
        }
    }

    override fun onGroupsChanged(changeCallback: PreferencesChangeCallback) {
        changeCallback.reloadTabs()
    }

    abstract class Tab(context: Context, type: String, title: String) :
        Group(type, context, title) {
        var color = StringCustomization(
            KEY_COLOR,
            context.prefs.profileAccentColor.defaultValue
        )

        init {
            addCustomization(color)
        }
    }

    class CustomTab(context: Context) :
        Tab(context, TYPE_CUSTOM, context.getString(R.string.default_tab_name)) {
        val hideFromAllApps = BooleanCustomization(KEY_HIDE_FROM_ALL_APPS, true)

        val contents = ComponentsCustomization(KEY_ITEMS, mutableSetOf())

        init {
            addCustomization(hideFromAllApps)
            addCustomization(contents)
        }

        override val summary: String
            get() {
                val size = filter.size
                return context.resources.getQuantityString(R.plurals.tab_apps_count, size, size)
            }

        val filter: Filter<*>
            get() = CustomFilter(context, contents.value())
    }

    data class Profile(val user: UserHandle?, val matchesAll: Boolean = false) : Parcelable {

        val isWork = user != null && user != Process.myUserHandle()
        val matcher: Predicate<ItemInfo>? = if (user != null) {
            ItemInfoMatcher.ofUser(user)
        } else if (!matchesAll) {
            ItemInfoMatcher.ofUser(Process.myUserHandle())
        } else {
            null
        }

        constructor() : this(null, true)

        constructor(parcel: Parcel) : this(
            parcel.readParcelable<UserHandle?>(UserHandle::class.java.classLoader),
            parcel.readBoolean()
        )

        fun matches(user: UserHandle): Boolean {
            if (matchesAll) return true
            return if (this.user == null) {
                user == Process.myUserHandle()
            } else {
                user == this.user
            }
        }

        fun filter(key: ComponentKey): Boolean {
            return matches(key.user)
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(user, flags)
            dest.writeBoolean(matchesAll)
        }

        override fun describeContents() = 0

        override fun toString(): String {
            val obj = JSONObject()
            if (user != null) {
                obj.put(KEY_ID, user.toString().replace("\\D+".toRegex(), "").toLong())
            }
            obj.put(KEY_MATCHES_ALL, matchesAll)
            return obj.toString()
        }

        companion object {
            private const val KEY_ID = "id"
            private const val KEY_MATCHES_ALL = "matchesAll"

            @JvmField
            val CREATOR = object : Parcelable.Creator<Profile> {
                override fun createFromParcel(parcel: Parcel): Profile {
                    return Profile(parcel)
                }

                override fun newArray(size: Int): Array<Profile?> {
                    return arrayOfNulls(size)
                }
            }

            fun fromString(context: Context, profile: String): Profile? {
                val obj = JSONObject(profile)
                val user = if (obj.has(KEY_ID)) {
                    UserCache.INSTANCE.get(context)
                        .getUserForSerialNumber(obj.getLong(KEY_ID)) ?: return null
                } else null
                val matchesAll = obj.getBoolean(KEY_MATCHES_ALL)
                return Profile(user, matchesAll)
            }
        }
    }

    data class ProfileTabCreator(private val profile: Profile) : GroupCreator<Tab> {

        override fun createGroup(context: Context): Tab {
            return ProfileTab(context, profile)
        }
    }

    open class ProfileTab(context: Context, val profile: Profile) :
        Tab(context, "$TYPE_PROFILE_PREFIX$profile}", getTitle(context, profile)) {

        init {
            addCustomization(HiddenAppsRow(profile))
        }

        override val summary: String?
            get() {
                val hidden = context.prefs.drawerHiddenAppSet.getValue()
                    .map { Utilities.makeComponentKey(context, it) }
                    .filter(getWorkFilter(profile))
                val size = hidden.size
                if (size == 0) {
                    return null
                }
                return context.resources.getQuantityString(R.plurals.hidden_apps_count, size, size)
            }

        companion object {

            private fun getTitle(context: Context, profile: Profile): String = when {
                profile.matchesAll -> context.getString(R.string.apps_label)
                profile.isWork -> context.getString(R.string.all_apps_work_tab)
                else -> context.getString(R.string.all_apps_personal_tab)
            }
        }
    }

    class HiddenAppsRow(private val profile: Profile) :
        Customization<Collection<ComponentKey>, Boolean>(KEY_HIDDEN, emptySet()) {

        private val predicate get() = getWorkFilter(profile)


        override fun loadFromJson(context: Context, obj: Boolean?) {}

        override fun saveToJson(context: Context): Boolean? {
            val value = value ?: return null
            setHiddenApps(context, value)
            this.value = null
            return null
        }

        private fun setHiddenApps(context: Context, hidden: Collection<ComponentKey>) {
            val prefs = context.prefs
            val hiddenSet = ArrayList(prefs.drawerHiddenAppSet.getValue()
                .map { Utilities.makeComponentKey(context, it) }
                .filter { !predicate(it) })
            hiddenSet.addAll(hidden)
            prefs.drawerHiddenAppSet.setValue(hiddenSet.map(ComponentKey::toString).toSet())
        }

        override fun clone(): Customization<Collection<ComponentKey>, Boolean> {
            return HiddenAppsRow(profile).also { it.value = value }
        }
    }

    companion object {

        const val TYPE_PERSONAL = "0"
        const val TYPE_WORK = "1"
        const val TYPE_CUSTOM = "2"
        const val TYPE_ALL_APPS = "3"
        const val TYPE_PROFILE_PREFIX = "profile"

        const val KEY_HIDDEN = "hidden"

        fun getWorkFilter(profile: Profile): (ComponentKey) -> Boolean {
            return profile::filter
        }
    }
}
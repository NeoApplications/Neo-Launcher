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

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.preferences.PreferencesChangeCallback
import com.neoapps.neolauncher.preferences.StringPref
import com.neoapps.neolauncher.util.asMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

abstract class AppGroups<T : AppGroups.Group>(
    private val manager: AppGroupsManager,
    private val category: AppGroupsManager.Category,
) {
    private val prefs = manager.prefs
    val context = prefs.context
    var groups = ArrayList<T>()

    private var groupsDataJson = StringPref(
        titleId = -1,
        key = stringPreferencesKey(category.key),
        dataStore = manager.dataStore,
        defaultValue = "{}",
        onChange = {
            prefs.legacyPrefs.savePreference(key = category.key, value = it)
            prefs.withChangeCallback { callback ->
                onGroupsChanged(callback)
            }
        }
    )

    val isEnabled: Boolean
        get() = manager.categorizationType.getValue() == category.key

    private val defaultGroups by lazy { getDefaultCreators().mapNotNull { it.createGroup(context) } }

    fun checkIsEnabled(changeCallback: PreferencesChangeCallback) {
        if (isEnabled) {
            onGroupsChanged(changeCallback)
        }
    }

    abstract fun getDefaultCreators(): List<GroupCreator<T>>

    abstract fun getGroupCreator(type: String): GroupCreator<T>

    abstract fun onGroupsChanged(changeCallback: PreferencesChangeCallback)

    fun saveToJson() {
        val arr = JSONArray()
        groups.forEach { group ->
            arr.put(JSONObject(group.saveCustomizationsInternal(context)))
        }

        val obj = JSONObject()
        obj.put(KEY_VERSION, currentVersion)
        obj.put(KEY_GROUPS, arr)
        groupsDataJson.setValue(obj.toString())
    }

    fun getGroups(): List<T> {
        if (!isEnabled) {
            return defaultGroups
        }
        return groups
    }

    fun setGroups(groups: List<T>) {
        this.groups.clear()
        this.groups.addAll(groups)

        val used = mutableSetOf<GroupCreator<T>>()
        groups.forEach {
            val creator = getGroupCreator(it.type)
            used.add(creator)
        }
        getDefaultCreators().asReversed().forEach { creator ->
            if (creator !in used) {
                creator.createGroup(context)?.let { this.groups.add(0, it) }
            }
        }
    }

    fun addGroup(group: T) {
        setGroups(this.groups.plus(group))
    }

    fun removeGroup(group: T) {
        this.groups.remove(group)
    }

    private fun loadGroupsArray(): JSONArray {
        try {
            val obj = JSONObject(groupsDataJson.getValue())
            val version = if (obj.has(KEY_VERSION)) obj.getInt(KEY_VERSION) else 0
            if (version > currentVersion) throw IllegalArgumentException("Version $version is higher than supported ($currentVersion)")

            val groups = obj.getJSONArray(KEY_GROUPS)

            // Change the "type" value to string
            if (version < 2) {
                for (i in 0 until groups.length()) {
                    val group = groups.getJSONObject(i)
                    if (group.has(KEY_TYPE)) {
                        group.put(KEY_TYPE, "${group.getInt(KEY_TYPE)}")
                    }
                }
            }

            return groups
        } catch (_: IllegalArgumentException) {
        } catch (_: JSONException) {
        }
        return JSONArray()
    }

    protected fun loadGroups() {
        groups.clear()
        val arr = loadGroupsArray()
        val used = mutableSetOf<GroupCreator<T>>()
        (0 until arr.length())
            .map { arr.getJSONObject(it) }
            .mapNotNullTo(groups) { group ->
                val type = if (group.has(KEY_TYPE)) group.getString(KEY_TYPE) else TYPE_UNDEFINED
                val creator = getGroupCreator(type)
                used.add(creator)
                creator.createGroup(context)!!.apply { loadCustomizations(context, group.asMap()) }
            }
        getDefaultCreators().asReversed().forEach { creator ->
            if (creator !in used) {
                creator.createGroup(context)?.let { groups.add(0, it) }
            }
        }
    }

    open class Group(val type: String, val context: Context, title: String) {
        private val defaultTitle = title

        val customizations = CustomizationMap()
        private val _title = StringCustomization(KEY_TITLE, defaultTitle)
        open var title: String
            get() =
                _title.value ?: defaultTitle
            set(value) {
                _title.value = value
            }

        open val summary: String?
            get() = null

        init {
            addCustomization(_title)
        }

        fun addCustomization(customization: Customization<*, *>) {
            customizations.add(customization)
        }

        open fun loadCustomizations(context: Context, obj: Map<String, Any>) {
            customizations.entries.forEach { it.loadFromJsonInternal(context, obj[it.key]) }
        }

        fun saveCustomizationsInternal(context: Context): Map<String, Any> {
            val obj = HashMap<String, Any>()
            saveCustomizations(context, obj)
            return obj
        }

        open fun saveCustomizations(context: Context, obj: MutableMap<String, Any>) {
            obj[KEY_TYPE] = type
            customizations.entries.forEach { entry ->
                entry.saveToJson(context)?.let { obj[entry.key] = it }
            }
        }

        fun cloneCustomizations(): CustomizationMap {
            return CustomizationMap(customizations)
        }
    }

    abstract class Customization<T : Any, S : Any>(val key: String, protected val default: T) {

        var value: T? = null

        fun value() = value ?: default

        @Suppress("UNCHECKED_CAST")
        fun loadFromJsonInternal(context: Context, obj: Any?) {
            loadFromJson(context, obj as S?)
        }

        abstract fun loadFromJson(context: Context, obj: S?)

        abstract fun saveToJson(context: Context): S?

        abstract fun clone(): Customization<T, S>

        @Suppress("UNCHECKED_CAST")
        open fun applyFrom(other: Customization<*, *>) {
            value = other.value as? T
        }
    }

    open class StringCustomization(key: String, default: String) :
        Customization<String, String>(key, default) {
        override fun loadFromJson(context: Context, obj: String?) {
            value = obj
        }

        override fun saveToJson(context: Context): String? {
            return value
        }

        override fun clone(): Customization<String, String> {
            return StringCustomization(key, default).also { it.value = value }
        }
    }

    open class BooleanCustomization(key: String, default: Boolean) :
        Customization<Boolean, Boolean>(key, default) {

        override fun loadFromJson(context: Context, obj: Boolean?) {
            value = obj
        }

        override fun saveToJson(context: Context): Boolean? {
            return value
        }

        override fun clone(): Customization<Boolean, Boolean> {
            return BooleanCustomization(key, default).also { it.value = value }
        }
    }

    open class LongCustomization(key: String, default: Long) :
        Customization<Long, Long>(key, default) {
        override fun loadFromJson(context: Context, obj: Long?) {
            value = obj
        }

        override fun saveToJson(context: Context): Long? {
            return value
        }

        override fun clone(): Customization<Long, Long> {
            return LongCustomization(key, default).also { it.value = value }
        }
    }

    abstract class SetCustomization<T : Any, S : Any>(key: String, default: MutableSet<T>) :
        Customization<MutableSet<T>, JSONArray>(key, default) {

        @Suppress("UNCHECKED_CAST")
        override fun loadFromJson(context: Context, obj: JSONArray?) {
            value = if (obj == null) {
                null
            } else {
                val set = HashSet<T>()
                for (i in (0 until obj.length())) {
                    set.add(unflatten(context, obj.get(i) as S))
                }
                set
            }
        }

        override fun saveToJson(context: Context): JSONArray? {
            val list = value ?: return null
            val array = JSONArray()
            list.forEach { array.put(flatten(it)) }
            return array
        }

        abstract fun unflatten(context: Context, value: S): T

        abstract fun flatten(value: T): S
    }

    open class ComponentsCustomization(key: String, default: MutableSet<ComponentKey>) :
        SetCustomization<ComponentKey, String>(key, default) {

        override fun loadFromJson(context: Context, obj: JSONArray?) {
            super.loadFromJson(context, obj)
            if (value == null) {
                value = HashSet(default)
            }
        }

        override fun unflatten(context: Context, value: String): ComponentKey {
            return Utilities.makeComponentKey(context, value)
        }

        override fun flatten(value: ComponentKey): String {
            return value.toString()
        }

        override fun clone(): Customization<MutableSet<ComponentKey>, JSONArray> {
            return ComponentsCustomization(key, default).also { newInstance ->
                value?.let { newInstance.value = HashSet(it) }
            }
        }
    }

    class CustomizationMap(old: CustomizationMap? = null) {

        private val map = HashMap<String, Customization<*, *>>()
        private val order = HashMap<String, Int>()

        init {
            old?.map?.mapValuesTo(map) { it.value.clone() }
            old?.order?.entries?.forEach { order[it.key] = it.value }
        }

        fun add(customization: Customization<*, *>) {
            map[customization.key] = customization
        }

        operator fun get(key: String): Customization<*, *>? {
            return map[key]
        }

        fun setOrder(vararg keys: String) {
            keys.forEachIndexed { index, s -> order[s] = index }
        }

        fun applyFrom(config: CustomizationMap) {
            map.values.forEach { entry ->
                val other = config.map[entry.key] ?: return@forEach
                entry.applyFrom(other)
            }
        }

        val entries get() = map.values

        override fun equals(other: Any?): Boolean {
            if (other !is CustomizationMap) return false
            return map == other.map
        }

        override fun hashCode(): Int {
            return map.hashCode()
        }
    }

    companion object {

        const val currentVersion = 2

        const val KEY_VERSION = "version"
        const val KEY_GROUPS = "tabs"

        const val KEY_ID = "id"
        const val KEY_TYPE = "type"
        const val KEY_COLOR = "color"
        const val KEY_TITLE = "title"
        const val KEY_HIDE_FROM_ALL_APPS = "hideFromAllApps"
        const val KEY_ITEMS = "items"
        const val KEY_FLOWERPOT_DEFAULT = "PERSONALIZATION"

        const val TYPE_UNDEFINED = "-1"
    }
}
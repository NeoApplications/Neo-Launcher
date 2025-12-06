package com.neoapps.launcher

import android.app.Application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class NeoPrefs {
    private val gson: Gson = Gson()
    private val prefs: Application
    private val type: Type

    constructor(application: Application, type: Type) {
        this.prefs = application
        this.type = type
    }

    fun putString(key: String, value: String) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun getString(key: String): String? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getString(key, "")
    }

    fun putInt(key: String, value: Int) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }

    fun getInt(key: String): Int {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getInt(key, 0)
    }

    fun putLong(key: String, value: Long) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putLong(key, value)
            .apply()
    }

    fun getLong(key: String): Long {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getLong(key, 0)
    }

    fun putFloat(key: String, value: Float) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putFloat(key, value)
            .apply()
    }

    fun getFloat(key: String): Float {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getFloat(key, 0f)
    }

    fun putDouble(key: String, value: Double) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putDouble(key, value)
            .apply()
    }

    fun getDouble(key: String): Double {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getDouble(key, 0.0)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getBoolean(key: String): Boolean {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getBoolean(key, false)
    }

    fun putObject(key: String, value: Any) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObject(key, value)
            .apply()
    }

    fun getObject(key: String): Any? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObject(key, null)
    }

    fun putList(key: String, value: List<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putList(key, value)
            .apply()
    }

    fun getList(key: String): List<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getList(key, null)
    }

    fun putMap(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putMap(key, value)
            .apply()
    }

    fun getMap(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getMap(key, null)
    }

    fun putStringSet(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putStringSet(key, value)
            .apply()
    }

    fun getStringSet(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getStringSet(key, null)
    }

    fun putStringArray(key: String, value: Array<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putStringArray(key, value)
            .apply()
    }

    fun getStringArray(key: String): Array<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getStringArray(key, null)
    }

    fun putObjectArray(key: String, value: Array<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectArray(key, value)
            .apply()
    }

    fun getObjectArray(key: String): Array<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectArray(key, null)
    }

    fun putObjectMap(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMap(key, value)
            .apply()
    }

    fun getObjectMap(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMap(key, null)
    }

    fun putObjectSet(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSet(key, value)
            .apply()
    }

    fun getObjectSet(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSet(key, null)
    }

    fun putObjectList(key: String, value: List<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectList(key, value)
            .apply()
    }

    fun getObjectList(key: String): List<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectList(key, null)
    }

    fun putObjectMapSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSet(key, value)
            .apply()
    }

    fun getObjectMapSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSet(key, null)
    }

    fun putObjectMapList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapList(key, value)
            .apply()
    }

    fun getObjectMapList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapList(key, null)
    }

    fun putObjectListMap(key: String, value: List<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectListMap(key, value)
            .apply()
    }

    fun getObjectListMap(key: String): List<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectListMap(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMap(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMap(key, value)
            .apply()
    }

    fun getObjectSetMap(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMap(key, null)
    }

    fun putObjectListMapSet(key: String, value: List<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectListMapSet(key, value)
            .apply()
    }

    fun getObjectListMapSet(key: String): List<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectListMapSet(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetList(key, value)
            .apply()
    }

    fun getObjectSetList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetListMap(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetListMap(key, value)
            .apply()
    }

    fun getObjectSetListMap(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetListMap(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapListSet(key, value)
            .apply()
    }

    fun getObjectMapListSet(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapListSet(key, null)
    }

    fun putObjectSetMapList(key: String, value: Set<*>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectSetMapList(key, value)
            .apply()
    }

    fun getObjectSetMapList(key: String): Set<*>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectSetMapList(key, null)
    }

    fun putObjectMapSetList(key: String, value: Map<*, *>?) {
        prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .edit()
            .putObjectMapSetList(key, value)
            .apply()
    }

    fun getObjectMapSetList(key: String): Map<*, *>? {
        return prefs.getSharedPreferences("com.neoapps.launcher", Application.MODE_PRIVATE)
            .getObjectMapSetList(key, null)
    }

    fun putObjectMapListSet(key: String, value: Map<*, *>?) {
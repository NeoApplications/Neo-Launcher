/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.icons.cache

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteException
import android.os.Handler
import android.os.SystemClock
import android.os.UserHandle
import android.util.ArrayMap
import android.util.Log
import com.android.launcher3.icons.cache.BaseIconCache.IconDB
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.SQLiteCacheHelper
import java.util.ArrayDeque

/** Utility class to handle updating the Icon cache */
class IconCacheUpdateHandler(
    private val iconCache: BaseIconCache,
    private val cacheDb: SQLiteCacheHelper,
    private val workerHandler: Handler,
) {

    private val packagesToIgnore = ArrayMap<UserHandle, MutableSet<String>>()
    // Map of packageKey to ApplicationInfo, dynamically created based on all incoming data
    private val packageAppInfoMap = HashMap<ComponentKey, ApplicationInfo?>()

    private val itemsToDelete = HashSet<UpdateRow>()

    // During the first pass, we load all the items from DB and add all invalid items to
    // mItemsToDelete. In follow up passes, we  go through the items in mItemsToDelete, and if the
    // item is valid, removes it from the list, or leave it there.
    private var firstPass = true

    /** Sets a package to ignore for processing */
    fun addPackagesToIgnore(userHandle: UserHandle, packageName: String) {
        packagesToIgnore.getOrPut(userHandle) { HashSet() }.add(packageName)
    }

    /**
     * Updates the persistent DB, such that only entries corresponding to {@param apps} remain in
     * the DB and are updated.
     *
     * @return The set of packages for which icons have updated.
     */
    fun <T : Any> updateIcons(
        apps: List<T>,
        cachingLogic: CachingLogic<T>,
        onUpdateCallback: OnUpdateCallback,
    ) {
        // Filter the list per user
        val userComponentMap = HashMap<UserHandle, HashMap<ComponentName, T>>()
        for (app in apps) {
            val userHandle = cachingLogic.getUser(app)
            val cn = cachingLogic.getComponent(app)
            userComponentMap.getOrPut(userHandle) { HashMap() }[cn] = app

            // Populate application info map
            val packageKey = BaseIconCache.getPackageKey(cn.packageName, userHandle)
            packageAppInfoMap.getOrPut(packageKey) { cachingLogic.getApplicationInfo(app) }
        }

        if (firstPass) {
            userComponentMap.forEach { (user, componentMap) ->
                updateIconsPerUserForFirstPass(user, componentMap, cachingLogic, onUpdateCallback)
            }
        } else {
            userComponentMap.forEach { (user, componentMap) ->
                updateIconsPerUserForSecondPass(user, componentMap, cachingLogic, onUpdateCallback)
            }
        }

        // From now on, clear every valid item from the global valid map.
        firstPass = false
    }

    /**
     * During the first pass, all the items from the cache are verified one-by-one and any entry
     * with no corresponding entry in {@code componentMap} is added to {@code itemsToDelete}
     *
     * Also starts a SerializedIconUpdateTask for all updated entries
     */
    private fun <T : Any> updateIconsPerUserForFirstPass(
        user: UserHandle,
        componentMap: MutableMap<ComponentName, T>,
        cachingLogic: CachingLogic<T>,
        onUpdateCallback: OnUpdateCallback,
    ) {
        val appsToUpdate = ArrayDeque<T>()

        val userSerial = iconCache.getSerialNumberForUser(user)
        try {
            cacheDb
                .query(
                    arrayOf(
                        IconDB.COLUMN_ROWID,
                        IconDB.COLUMN_COMPONENT,
                        IconDB.COLUMN_FRESHNESS_ID,
                    ),
                    "${IconDB.COLUMN_USER} = ? ",
                    arrayOf(userSerial.toString()),
                )
                .use { c ->
                    var ignorePackages = packagesToIgnore[user] ?: emptySet()

                    val indexComponent = c.getColumnIndex(IconDB.COLUMN_COMPONENT)
                    val indexFreshnessId = c.getColumnIndex(IconDB.COLUMN_FRESHNESS_ID)
                    val rowIndex = c.getColumnIndex(IconDB.COLUMN_ROWID)

                    while (c.moveToNext()) {
                        val rowId = c.getInt(rowIndex)
                        val cn = c.getString(indexComponent)
                        val freshnessId = c.getString(indexFreshnessId) ?: ""

                        val component = ComponentName.unflattenFromString(cn)
                        if (component == null) {
                            // b/357725795
                            Log.e(TAG, "Invalid component name while updating icon cache: $cn")
                            itemsToDelete.add(
                                UpdateRow(rowId, ComponentName("", ""), user, freshnessId)
                            )
                            continue
                        }

                        val app = componentMap.remove(component)
                        if (app == null) {
                            if (!ignorePackages.contains(component.packageName)) {
                                iconCache.remove(component, user)
                                itemsToDelete.add(UpdateRow(rowId, component, user, freshnessId))
                            }
                            continue
                        }

                        if (
                            freshnessId ==
                                cachingLogic.getFreshnessIdentifier(app, iconCache.iconProvider)
                        ) {
                            // Item is up-to-date
                            continue
                        }
                        appsToUpdate.add(app)
                    }
                }
        } catch (e: SQLiteException) {
            Log.d(TAG, "Error reading icon cache", e)
            // Continue updating whatever we have read so far
        }

        // Insert remaining apps.
        if (componentMap.isNotEmpty() || appsToUpdate.isNotEmpty()) {
            val appsToAdd = ArrayDeque(componentMap.values)
            SerializedIconUpdateTask(
                    userSerial,
                    user,
                    appsToAdd,
                    appsToUpdate,
                    cachingLogic,
                    onUpdateCallback,
                )
                .scheduleNext()
        }
    }

    /**
     * During the second pass, we go through the items in {@code itemsToDelete}, and remove any item
     * with corresponding entry in {@code componentMap}.
     */
    private fun <T : Any> updateIconsPerUserForSecondPass(
        user: UserHandle,
        componentMap: MutableMap<ComponentName, T>,
        cachingLogic: CachingLogic<T>,
        onUpdateCallback: OnUpdateCallback,
    ) {
        val userSerial = iconCache.getSerialNumberForUser(user)
        val appsToUpdate = ArrayDeque<T>()

        val itr = itemsToDelete.iterator()
        while (itr.hasNext()) {
            val row = itr.next()
            if (user != row.user) continue
            val app = componentMap.remove(row.componentName) ?: continue

            itr.remove()
            if (
                row.freshnessId != cachingLogic.getFreshnessIdentifier(app, iconCache.iconProvider)
            ) {
                appsToUpdate.add(app)
            }
        }

        // Insert remaining apps.
        if (componentMap.isNotEmpty() || appsToUpdate.isNotEmpty()) {
            val appsToAdd = ArrayDeque<T>()
            appsToAdd.addAll(componentMap.values)
            SerializedIconUpdateTask(
                    userSerial,
                    user,
                    appsToAdd,
                    appsToUpdate,
                    cachingLogic,
                    onUpdateCallback,
                )
                .scheduleNext()
        }
    }

    /**
     * Commits all updates as part of the update handler to disk. Not more calls should be made to
     * this class after this.
     */
    fun finish() {
        // Ignore any application info entries which are already correct
        itemsToDelete.removeIf { row ->
            val info = packageAppInfoMap[ComponentKey(row.componentName, row.user)]
            info != null && row.freshnessId == iconCache.iconProvider.getStateForApp(info)
        }

        // Commit all deletes
        if (itemsToDelete.isNotEmpty()) {
            val r = itemsToDelete.joinToString { it.rowId.toString() }
            cacheDb.delete("${IconDB.COLUMN_ROWID} IN ($r)", null)
            Log.d(TAG, "Deleting obsolete entries, count=" + itemsToDelete.size)
        }
    }

    data class UpdateRow(
        val rowId: Int,
        val componentName: ComponentName,
        val user: UserHandle,
        val freshnessId: String,
    )

    /**
     * A runnable that updates invalid icons and adds missing icons in the DB for the provided
     * LauncherActivityInfo list. Items are updated/added one at a time, so that the worker thread
     * doesn't get blocked.
     */
    private inner class SerializedIconUpdateTask<T : Any>(
        private val userSerial: Long,
        private val userHandle: UserHandle,
        private val appsToAdd: ArrayDeque<T>,
        private val appsToUpdate: ArrayDeque<T>,
        private val cachingLogic: CachingLogic<T>,
        private val onUpdateCallback: OnUpdateCallback,
    ) : Runnable {
        private val updatedPackages = HashSet<String>()

        override fun run() {
            if (appsToUpdate.isNotEmpty()) {
                val app = appsToUpdate.removeLast()
                val pkg = cachingLogic.getComponent(app).packageName

                iconCache.addIconToDBAndMemCache(app, cachingLogic, userSerial)
                updatedPackages.add(pkg)

                if (appsToUpdate.isEmpty() && updatedPackages.isNotEmpty()) {
                    // No more app to update. Notify callback.
                    onUpdateCallback.onPackageIconsUpdated(updatedPackages, userHandle)
                }

                // Let it run one more time.
                scheduleNext()
            } else if (appsToAdd.isNotEmpty()) {
                iconCache.addIconToDBAndMemCache(appsToAdd.removeLast(), cachingLogic, userSerial)

                // Let it run one more time.
                scheduleNext()
            }
        }

        fun scheduleNext() {
            workerHandler.postAtTime(
                this,
                iconCache.iconUpdateToken,
                SystemClock.uptimeMillis() + 1,
            )
        }
    }

    fun interface OnUpdateCallback {
        fun onPackageIconsUpdated(updatedPackages: HashSet<String>, user: UserHandle)
    }

    companion object {
        private const val TAG = "IconCacheUpdateHandler"
    }
}

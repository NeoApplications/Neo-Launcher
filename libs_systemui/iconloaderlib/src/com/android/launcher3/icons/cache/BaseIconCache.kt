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
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteReadOnlyDatabaseException
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.HARDWARE
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Trace
import android.os.UserHandle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.launcher3.Flags
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BaseIconFactory.IconOptions
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.GraphicsUtils
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.SourceHint
import com.android.launcher3.icons.cache.CacheLookupFlag.Companion.DEFAULT_LOOKUP_FLAG
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.FlagOp
import com.android.launcher3.util.SQLiteCacheHelper
import java.util.function.Supplier
import kotlin.collections.MutableMap.MutableEntry

abstract class BaseIconCache
@JvmOverloads
constructor(
    @JvmField protected val context: Context,
    private val dbFileName: String?,
    private val bgLooper: Looper,
    private var iconDpi: Int,
    iconPixelSize: Int,
    inMemoryCache: Boolean,
    val iconProvider: IconProvider = IconProvider(context),
) {
    class CacheEntry {
        @JvmField var bitmap: BitmapInfo = BitmapInfo.LOW_RES_INFO
        @JvmField var title: CharSequence = ""
        @JvmField var contentDescription: CharSequence = ""
    }

    private val packageManager: PackageManager = context.packageManager

    private val cache: MutableMap<ComponentKey, CacheEntry?> =
        if (inMemoryCache) {
            HashMap(INITIAL_ICON_CACHE_CAPACITY)
        } else {
            object : AbstractMutableMap<ComponentKey, CacheEntry?>() {
                override fun put(key: ComponentKey, value: CacheEntry?): CacheEntry? = value

                override val entries: MutableSet<MutableEntry<ComponentKey, CacheEntry?>> =
                    mutableSetOf()
            }
        }

    val iconUpdateToken = Any()

    @JvmField val workerHandler = Handler(bgLooper)

    @JvmField protected var iconDb = IconDB(context, dbFileName, iconPixelSize)

    private var defaultIcon: BitmapInfo? = null
    private val userFlagOpMap = SparseArray<FlagOp>()
    private val userFormatString = SparseArray<String?>()

    private val appInfoCachingLogic =
        AppInfoCachingLogic(
            pm = context.packageManager,
            instantAppResolver = this::isInstantApp,
            errorLogger = this::logPersistently,
        )

    init {
        updateSystemState()
    }

    /**
     * Returns the persistable serial number for {@param user}. Subclass should implement proper
     * caching strategy to avoid making binder call every time.
     */
    abstract fun getSerialNumberForUser(user: UserHandle): Long

    /** Return true if the given app is an instant app and should be badged appropriately. */
    protected abstract fun isInstantApp(info: ApplicationInfo): Boolean

    /** Opens and returns an icon factory. The factory is recycled by the caller. */
    abstract val iconFactory: BaseIconFactory

    fun updateIconParams(iconDpi: Int, iconPixelSize: Int) =
        workerHandler.post { updateIconParamsBg(iconDpi, iconPixelSize) }

    @Synchronized
    private fun updateIconParamsBg(iconDpi: Int, iconPixelSize: Int) {
        try {
            this.iconDpi = iconDpi
            defaultIcon = null
            userFlagOpMap.clear()
            iconDb.clear()
            iconDb.close()
            iconDb = IconDB(context, dbFileName, iconPixelSize)
            cache.clear()
        } catch (e: SQLiteReadOnlyDatabaseException) {
            // This is known to happen during repeated backup and restores, if the Launcher is in
            // restricted mode. When the launcher is loading and the backup restore is being cleared
            // there can be a conflict where one DB is trying to delete the DB file, and the other
            // is attempting to write to it. The effect is that launcher crashes, then the backup /
            // restore process fails, then the user's home screen icons fail to restore. Adding this
            // try / catch will stop the crash, and LoaderTask will sanitize any residual icon data,
            // leading to a completed backup / restore and a better experience for our customers.
            Log.e(TAG, "failed to clear the launcher's icon db or cache.", e)
        }
    }

    fun getFullResIcon(info: ActivityInfo): Drawable? = iconProvider.getIcon(info, iconDpi)

    /** Remove any records for the supplied ComponentName. */
    @Synchronized
    fun remove(componentName: ComponentName, user: UserHandle) =
        cache.remove(ComponentKey(componentName, user))

    /** Remove any records for the supplied package name from memory. */
    private fun removeFromMemCacheLocked(packageName: String, user: UserHandle) =
        cache.keys.removeIf { it.componentName.packageName == packageName && it.user == user }

    /** Removes the entries related to the given package in memory and persistent DB. */
    @Synchronized
    fun removeIconsForPkg(packageName: String, user: UserHandle) {
        removeFromMemCacheLocked(packageName, user)
        iconDb.delete(
            "$COLUMN_COMPONENT LIKE ? AND $COLUMN_USER = ?",
            arrayOf("$packageName/%", getSerialNumberForUser(user).toString()),
        )
    }

    fun getUpdateHandler(): IconCacheUpdateHandler {
        updateSystemState()
        // Remove all active icon update tasks.
        workerHandler.removeCallbacksAndMessages(iconUpdateToken)
        return IconCacheUpdateHandler(this, iconDb, workerHandler)
    }

    /**
     * Refreshes the system state definition used to check the validity of the cache. It
     * incorporates all the properties that can affect the cache like the list of enabled locale and
     * system-version.
     */
    private fun updateSystemState() {
        iconProvider.updateSystemState()
        userFormatString.clear()
    }

    fun getUserBadgedLabel(label: CharSequence, user: UserHandle): CharSequence {
        val key = user.hashCode()
        val index = userFormatString.indexOfKey(key)
        var format: String?
        if (index < 0) {
            format = packageManager.getUserBadgedLabel(IDENTITY_FORMAT_STRING, user).toString()
            if (TextUtils.equals(IDENTITY_FORMAT_STRING, format)) {
                format = null
            }
            userFormatString.put(key, format)
        } else {
            format = userFormatString.valueAt(index)
        }
        return if (format == null) label else String.format(format, label)
    }

    /**
     * Adds/updates an entry into the DB and the in-memory cache. The update is skipped if the entry
     * fails to load
     */
    @Synchronized
    fun <T : Any> addIconToDBAndMemCache(obj: T, cachingLogic: CachingLogic<T>, userSerial: Long) {
        val user = cachingLogic.getUser(obj)
        val componentName = cachingLogic.getComponent(obj)
        val key = ComponentKey(componentName, user)
        val bitmapInfo = cachingLogic.loadIcon(context, this, obj)

        // Icon can't be loaded from cachingLogic, which implies alternative icon was loaded
        // (e.g. fallback icon, default icon). So we drop here since there's no point in caching
        // an empty entry.
        if (bitmapInfo.isNullOrLowRes || isDefaultIcon(bitmapInfo, user)) {
            return
        }
        val entryTitle =
            cachingLogic.getLabel(obj).let {
                if (it.isNullOrEmpty()) componentName.packageName else it
            }

        // Only add an entry in memory, if there was already something previously
        if (cache[key] != null) {
            val entry = CacheEntry()
            entry.bitmap = bitmapInfo
            entry.title = entryTitle
            entry.contentDescription = getUserBadgedLabel(entryTitle, user)
            cache[key] = entry
        }

        val freshnessId = cachingLogic.getFreshnessIdentifier(obj, iconProvider)
        if (freshnessId != null) {
            addOrUpdateCacheDbEntry(bitmapInfo, entryTitle, componentName, userSerial, freshnessId)
        }
    }

    @Synchronized
    fun getDefaultIcon(user: UserHandle): BitmapInfo {
        if (defaultIcon == null) {
            iconFactory.use { li -> defaultIcon = li.makeDefaultIcon(iconProvider) }
        }
        return defaultIcon!!.withFlags(getUserFlagOpLocked(user))
    }

    protected fun getUserFlagOpLocked(user: UserHandle): FlagOp {
        val key = user.hashCode()
        val index = userFlagOpMap.indexOfKey(key)
        if (index >= 0) {
            return userFlagOpMap.valueAt(index)
        } else {
            iconFactory.use { li ->
                val op = li.getBitmapFlagOp(IconOptions().setUser(user))
                userFlagOpMap.put(key, op)
                return op
            }
        }
    }

    fun isDefaultIcon(icon: BitmapInfo, user: UserHandle) = getDefaultIcon(user).icon == icon.icon

    /**
     * Retrieves the entry from the cache. If the entry is not present, it creates a new entry. This
     * method is not thread safe, it must be called from a synchronized method.
     */
    @JvmOverloads
    protected fun <T : Any> cacheLocked(
        componentName: ComponentName,
        user: UserHandle,
        infoProvider: Supplier<T?>,
        cachingLogic: CachingLogic<T>,
        lookupFlags: CacheLookupFlag,
        cursor: Cursor? = null,
    ): CacheEntry {
        assertWorkerThread()
        val cacheKey = ComponentKey(componentName, user)
        var entry = cache[cacheKey]
        if (entry == null || entry.bitmap.matchingLookupFlag.isVisuallyLessThan(lookupFlags)) {
            val addToMemCache = entry != null || !lookupFlags.skipAddToMemCache()
            entry = CacheEntry()
            if (addToMemCache) cache[cacheKey] = entry
            // Check the DB first.
            val cacheEntryUpdated =
                if (cursor == null) getEntryFromDBLocked(cacheKey, entry, lookupFlags, cachingLogic)
                else updateTitleAndIconLocked(cacheKey, entry, cursor, lookupFlags, cachingLogic)

            val obj: T? by lazy { infoProvider.get() }
            if (!cacheEntryUpdated) {
                loadFallbackIcon(
                    obj,
                    entry,
                    cachingLogic,
                    lookupFlags.usePackageIcon(),
                    /* usePackageTitle= */ true,
                    componentName,
                    user,
                )
            }

            if (TextUtils.isEmpty(entry.title)) {
                obj?.let { loadFallbackTitle(it, entry, cachingLogic, user) }
            }
        }
        return entry
    }

    /** Fallback method for loading an icon bitmap. */
    protected fun <T : Any> loadFallbackIcon(
        obj: T?,
        entry: CacheEntry,
        cachingLogic: CachingLogic<T>,
        usePackageIcon: Boolean,
        usePackageTitle: Boolean,
        componentName: ComponentName,
        user: UserHandle,
    ) {
        if (obj != null) {
            entry.bitmap = cachingLogic.loadIcon(context, this, obj)
        } else {
            if (usePackageIcon) {
                val packageEntry = getEntryForPackageLocked(componentName.packageName, user)
                if (DEBUG) {
                    Log.d(TAG, "using package default icon for " + componentName.toShortString())
                }
                entry.bitmap = packageEntry.bitmap
                entry.contentDescription = packageEntry.contentDescription

                if (usePackageTitle) {
                    entry.title = packageEntry.title
                }
            }
        }
    }

    /** Fallback method for loading an app title. */
    protected fun <T : Any> loadFallbackTitle(
        obj: T,
        entry: CacheEntry,
        cachingLogic: CachingLogic<T>,
        user: UserHandle,
    ) {
        entry.title =
            cachingLogic.getLabel(obj).let {
                if (it.isNullOrEmpty()) cachingLogic.getComponent(obj).packageName else it
            }
        entry.contentDescription = getUserBadgedLabel(entry.title, user)
    }

    @Synchronized
    fun clearMemoryCache() {
        assertWorkerThread()
        cache.clear()
    }

    /**
     * Adds a default package entry in the cache. This entry is not persisted and will be removed
     * when the cache is flushed.
     */
    @Synchronized
    protected fun cachePackageInstallInfo(
        packageName: String,
        user: UserHandle,
        icon: Bitmap?,
        title: CharSequence?,
    ) {
        removeFromMemCacheLocked(packageName, user)
        val cacheKey = getPackageKey(packageName, user)

        // For icon caching, do not go through DB. Just update the in-memory entry.
        val entry = cache[cacheKey] ?: CacheEntry()
        if (!title.isNullOrEmpty()) {
            entry.title = title
        }

        if (icon != null) {
            iconFactory.use { li ->
                entry.bitmap =
                    li.createBadgedIconBitmap(
                        li.createShapedAdaptiveIcon(icon),
                        IconOptions().setUser(user),
                    )
            }
        }
        if (!TextUtils.isEmpty(title) && entry.bitmap.icon != null) {
            cache[cacheKey] = entry
        }
    }

    /** Returns the package entry if it has already been cached in memory, null otherwise */
    protected fun getInMemoryPackageEntryLocked(
        packageName: String,
        user: UserHandle,
    ): CacheEntry? = getInMemoryEntryLocked(getPackageKey(packageName, user))

    @VisibleForTesting
    fun getInMemoryEntryLocked(key: ComponentKey): CacheEntry? {
        assertWorkerThread()
        return cache[key]
    }

    /**
     * Gets an entry for the package, which can be used as a fallback entry for various components.
     * This method is not thread safe, it must be called from a synchronized method.
     */
    @WorkerThread
    protected fun getEntryForPackageLocked(
        packageName: String,
        user: UserHandle,
        lookupFlags: CacheLookupFlag = DEFAULT_LOOKUP_FLAG,
    ): CacheEntry {
        assertWorkerThread()
        val cacheKey = getPackageKey(packageName, user)
        var entry = cache[cacheKey]

        if (entry == null || entry.bitmap.matchingLookupFlag.isVisuallyLessThan(lookupFlags)) {
            entry = CacheEntry()
            var entryUpdated = true

            // Check the DB first.
            if (!getEntryFromDBLocked(cacheKey, entry, lookupFlags, appInfoCachingLogic)) {
                try {
                    val appInfo =
                        context
                            .getSystemService(LauncherApps::class.java)!!
                            .getApplicationInfo(
                                packageName,
                                PackageManager.MATCH_UNINSTALLED_PACKAGES,
                                user,
                            )
                    if (appInfo == null) {
                        throw NameNotFoundException("ApplicationInfo is null").also {
                            logPersistently(
                                String.format("ApplicationInfo is null for %s", packageName),
                                it,
                            )
                        }
                    }

                    // Load the full res icon for the application, but if useLowResIcon is set, then
                    // only keep the low resolution icon instead of the larger full-sized icon
                    val iconInfo = appInfoCachingLogic.loadIcon(context, this, appInfo)
                    entry.bitmap =
                        if (lookupFlags.useLowRes())
                            BitmapInfo.of(BitmapInfo.LOW_RES_ICON, iconInfo.color)
                        else iconInfo

                    loadFallbackTitle(appInfo, entry, appInfoCachingLogic, user)

                    // Add the icon in the DB here, since these do not get written during
                    // package updates.
                    appInfoCachingLogic.getFreshnessIdentifier(appInfo, iconProvider)?.let {
                        freshnessId ->
                        addOrUpdateCacheDbEntry(
                            iconInfo,
                            entry.title,
                            cacheKey.componentName,
                            getSerialNumberForUser(user),
                            freshnessId,
                        )
                    }
                } catch (e: NameNotFoundException) {
                    if (DEBUG) Log.d(TAG, "Application not installed $packageName")
                    entryUpdated = false
                }
            }

            val shouldAddToCache =
                !(lookupFlags.skipAddToMemCache() && Flags.restoreArchivedAppIconsFromDb())
            // Only add a filled-out entry to the cache
            if (entryUpdated && shouldAddToCache) {
                cache[cacheKey] = entry
            }
        }
        return entry
    }

    protected fun getEntryFromDBLocked(
        cacheKey: ComponentKey,
        entry: CacheEntry,
        lookupFlags: CacheLookupFlag,
        cachingLogic: CachingLogic<*>,
    ): Boolean {
        var c: Cursor? = null
        Trace.beginSection("loadIconIndividually")
        try {
            c =
                iconDb.query(
                    lookupFlags.toLookupColumns(),
                    "$COLUMN_COMPONENT = ? AND $COLUMN_USER = ?",
                    arrayOf(
                        cacheKey.componentName.flattenToString(),
                        getSerialNumberForUser(cacheKey.user).toString(),
                    ),
                )
            if (c.moveToNext()) {
                return updateTitleAndIconLocked(cacheKey, entry, c, lookupFlags, cachingLogic)
            }
        } catch (e: SQLiteException) {
            Log.d(TAG, "Error reading icon cache", e)
        } finally {
            c?.close()
            Trace.endSection()
        }
        return false
    }

    private fun updateTitleAndIconLocked(
        cacheKey: ComponentKey,
        entry: CacheEntry,
        c: Cursor,
        lookupFlags: CacheLookupFlag,
        logic: CachingLogic<*>,
    ): Boolean {
        // Set the alpha to be 255, so that we never have a wrong color
        entry.bitmap =
            BitmapInfo.of(
                BitmapInfo.LOW_RES_ICON,
                GraphicsUtils.setColorAlphaBound(c.getInt(INDEX_COLOR), 255),
            )
        c.getString(INDEX_TITLE).let {
            if (it.isNullOrEmpty()) {
                entry.title = ""
                entry.contentDescription = ""
            } else {
                entry.title = it
                entry.contentDescription = getUserBadgedLabel(it, cacheKey.user)
            }
        }

        if (!lookupFlags.useLowRes()) {
            try {
                val data: ByteArray = c.getBlob(INDEX_ICON) ?: return false
                entry.bitmap =
                    BitmapInfo.of(
                        BitmapFactory.decodeByteArray(
                            data,
                            0,
                            data.size,
                            Options().apply { inPreferredConfig = HARDWARE },
                        )!!,
                        entry.bitmap.color,
                    )
            } catch (e: Exception) {
                return false
            }

            iconFactory.use { factory ->
                val themeController = factory.themeController
                val monoIconData = c.getBlob(INDEX_MONO_ICON)
                if (themeController != null && monoIconData != null) {
                    entry.bitmap.themedBitmap =
                        themeController.decode(
                            data = monoIconData,
                            info = entry.bitmap,
                            factory = factory,
                            sourceHint =
                                SourceHint(cacheKey, logic, c.getString(INDEX_FRESHNESS_ID)),
                        )
                }
            }
        }
        entry.bitmap.flags = c.getInt(INDEX_FLAGS)
        entry.bitmap = entry.bitmap.withFlags(getUserFlagOpLocked(cacheKey.user))
        return true
    }

    private fun addOrUpdateCacheDbEntry(
        bitmapInfo: BitmapInfo,
        label: CharSequence,
        key: ComponentName,
        userSerial: Long,
        freshnessId: String,
    ) {
        val values = ContentValues()
        if (bitmapInfo.canPersist()) {
            values.put(COLUMN_ICON, GraphicsUtils.flattenBitmap(bitmapInfo.icon))
            values.put(COLUMN_MONO_ICON, bitmapInfo.themedBitmap?.serialize())
        } else {
            values.put(COLUMN_ICON, null as ByteArray?)
            values.put(COLUMN_MONO_ICON, null as ByteArray?)
        }

        values.put(COLUMN_ICON_COLOR, bitmapInfo.color)
        values.put(COLUMN_FLAGS, bitmapInfo.flags)
        values.put(COLUMN_LABEL, label.toString())

        values.put(COLUMN_COMPONENT, key.flattenToString())
        values.put(COLUMN_USER, userSerial)
        values.put(COLUMN_FRESHNESS_ID, freshnessId)
        iconDb.insertOrReplace(values)
    }

    private fun assertWorkerThread() {
        check(Looper.myLooper() == bgLooper) {
            "Cache accessed on wrong thread " + Looper.myLooper()
        }
    }

    /** Log to Log.d. Subclasses can override this method to log persistently for debugging. */
    protected open fun logPersistently(message: String, e: Exception?) {
        Log.d(TAG, message, e)
    }

    /** Cache class to store the actual entries on disk */
    class IconDB(context: Context, dbFileName: String?, iconPixelSize: Int) :
        SQLiteCacheHelper(
            context,
            dbFileName,
            (RELEASE_VERSION shl 16) + iconPixelSize,
            TABLE_NAME,
        ) {

        override fun onCreateTable(db: SQLiteDatabase) {
            db.execSQL(
                ("CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_COMPONENT TEXT NOT NULL, " +
                    "$COLUMN_USER INTEGER NOT NULL, " +
                    "$COLUMN_FRESHNESS_ID TEXT, " +
                    "$COLUMN_ICON BLOB, " +
                    "$COLUMN_MONO_ICON BLOB, " +
                    "$COLUMN_ICON_COLOR INTEGER NOT NULL DEFAULT 0, " +
                    "$COLUMN_FLAGS INTEGER NOT NULL DEFAULT 0, " +
                    "$COLUMN_LABEL TEXT, " +
                    "PRIMARY KEY ($COLUMN_COMPONENT, $COLUMN_USER) " +
                    ");")
            )
        }
    }

    companion object {
        protected const val TAG = "BaseIconCache"
        private const val DEBUG = false

        private const val INITIAL_ICON_CACHE_CAPACITY = 50

        // A format string which returns the original string as is.
        private const val IDENTITY_FORMAT_STRING = "%1\$s"

        // Empty class name is used for storing package default entry.
        const val EMPTY_CLASS_NAME: String = "."

        fun getPackageKey(packageName: String, user: UserHandle) =
            ComponentKey(ComponentName(packageName, packageName + EMPTY_CLASS_NAME), user)

        // Ensures themed bitmaps in the icon cache are invalidated
        @JvmField val RELEASE_VERSION = if (Flags.forceMonochromeAppIcons()) 10 else 9

        @JvmField val TABLE_NAME = "icons"
        @JvmField val COLUMN_ROWID = "rowid"
        @JvmField val COLUMN_COMPONENT = "componentName"
        @JvmField val COLUMN_USER = "profileId"
        @JvmField val COLUMN_FRESHNESS_ID = "freshnessId"
        @JvmField val COLUMN_ICON = "icon"
        @JvmField val COLUMN_ICON_COLOR = "icon_color"
        @JvmField val COLUMN_MONO_ICON = "mono_icon"
        @JvmField val COLUMN_FLAGS = "flags"
        @JvmField val COLUMN_LABEL = "label"

        @JvmField
        val COLUMNS_LOW_RES =
            arrayOf(COLUMN_COMPONENT, COLUMN_LABEL, COLUMN_ICON_COLOR, COLUMN_FLAGS)

        @JvmField
        val COLUMNS_HIGH_RES =
            COLUMNS_LOW_RES.copyOf(COLUMNS_LOW_RES.size + 3).apply {
                this[size - 3] = COLUMN_ICON
                this[size - 2] = COLUMN_MONO_ICON
                this[size - 1] = COLUMN_FRESHNESS_ID
            }

        @JvmField val INDEX_TITLE = COLUMNS_HIGH_RES.indexOf(COLUMN_LABEL)
        @JvmField val INDEX_COLOR = COLUMNS_HIGH_RES.indexOf(COLUMN_ICON_COLOR)
        @JvmField val INDEX_FLAGS = COLUMNS_HIGH_RES.indexOf(COLUMN_FLAGS)
        @JvmField val INDEX_ICON = COLUMNS_HIGH_RES.indexOf(COLUMN_ICON)
        @JvmField val INDEX_MONO_ICON = COLUMNS_HIGH_RES.indexOf(COLUMN_MONO_ICON)
        @JvmField val INDEX_FRESHNESS_ID = COLUMNS_HIGH_RES.indexOf(COLUMN_FRESHNESS_ID)

        @JvmStatic
        fun CacheLookupFlag.toLookupColumns() =
            if (useLowRes()) COLUMNS_LOW_RES else COLUMNS_HIGH_RES
    }
}

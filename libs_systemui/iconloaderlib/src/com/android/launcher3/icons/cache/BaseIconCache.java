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
package com.android.launcher3.icons.cache;

import static android.graphics.BitmapFactory.decodeByteArray;

import static com.android.launcher3.icons.BaseIconFactory.getFullResDefaultActivityIcon;
import static com.android.launcher3.icons.BitmapInfo.LOW_RES_ICON;
import static com.android.launcher3.icons.GraphicsUtils.flattenBitmap;
import static com.android.launcher3.icons.GraphicsUtils.setColorAlphaBound;

import static java.util.Objects.requireNonNull;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import com.android.launcher3.icons.BaseIconFactory;
import com.android.launcher3.icons.BaseIconFactory.IconOptions;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.FlagOp;
import com.android.launcher3.util.SQLiteCacheHelper;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class BaseIconCache {

    private static final String TAG = "BaseIconCache";
    private static final boolean DEBUG = false;

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    // A format string which returns the original string as is.
    private static final String IDENTITY_FORMAT_STRING = "%1$s";

    // Empty class name is used for storing package default entry.
    public static final String EMPTY_CLASS_NAME = ".";

    public static class CacheEntry {

        @NonNull
        public BitmapInfo bitmap = BitmapInfo.LOW_RES_INFO;
        @NonNull
        public CharSequence title = "";
        @NonNull
        public CharSequence contentDescription = "";
    }

    @NonNull
    protected final Context mContext;

    @NonNull
    protected final PackageManager mPackageManager;

    @NonNull
    private final Map<ComponentKey, CacheEntry> mCache;

    @NonNull
    protected final Handler mWorkerHandler;

    protected int mIconDpi;

    @NonNull
    protected IconDB mIconDb;

    @NonNull
    protected LocaleList mLocaleList = LocaleList.getEmptyLocaleList();

    @NonNull
    protected String mSystemState = "";

    @Nullable
    private BitmapInfo mDefaultIcon;

    @NonNull
    private final SparseArray<FlagOp> mUserFlagOpMap = new SparseArray<>();

    private final SparseArray<String> mUserFormatString = new SparseArray<>();

    @Nullable
    private final String mDbFileName;

    @NonNull
    private final Looper mBgLooper;

    public BaseIconCache(@NonNull final Context context, @Nullable final String dbFileName,
            @NonNull final Looper bgLooper, final int iconDpi, final int iconPixelSize,
            final boolean inMemoryCache) {
        mContext = context;
        mDbFileName = dbFileName;
        mPackageManager = context.getPackageManager();
        mBgLooper = bgLooper;
        mWorkerHandler = new Handler(mBgLooper);

        if (inMemoryCache) {
            mCache = new HashMap<>(INITIAL_ICON_CACHE_CAPACITY);
        } else {
            // Use a dummy cache
            mCache = new AbstractMap<ComponentKey, CacheEntry>() {
                @Override
                public Set<Entry<ComponentKey, CacheEntry>> entrySet() {
                    return Collections.emptySet();
                }

                @Override
                public CacheEntry put(ComponentKey key, CacheEntry value) {
                    return value;
                }
            };
        }

        updateSystemState();
        mIconDpi = iconDpi;
        mIconDb = new IconDB(context, dbFileName, iconPixelSize);
    }

    /**
     * Returns the persistable serial number for {@param user}. Subclass should implement proper
     * caching strategy to avoid making binder call every time.
     */
    protected abstract long getSerialNumberForUser(@NonNull final UserHandle user);

    /**
     * Return true if the given app is an instant app and should be badged appropriately.
     */
    protected abstract boolean isInstantApp(@NonNull final ApplicationInfo info);

    /**
     * Opens and returns an icon factory. The factory is recycled by the caller.
     */
    @NonNull
    public abstract BaseIconFactory getIconFactory();

    public void updateIconParams(final int iconDpi, final int iconPixelSize) {
        mWorkerHandler.post(() -> updateIconParamsBg(iconDpi, iconPixelSize));
    }

    private synchronized void updateIconParamsBg(final int iconDpi, final int iconPixelSize) {
        mIconDpi = iconDpi;
        mDefaultIcon = null;
        mUserFlagOpMap.clear();
        mIconDb.clear();
        mIconDb.close();
        mIconDb = new IconDB(mContext, mDbFileName, iconPixelSize);
        mCache.clear();
    }

    @Nullable
    private Drawable getFullResIcon(@Nullable final Resources resources, final int iconId) {
        if (resources != null && iconId != 0) {
            try {
                return resources.getDrawableForDensity(iconId, mIconDpi);
            } catch (Resources.NotFoundException e) { }
        }
        return getFullResDefaultActivityIcon(mIconDpi);
    }

    @Nullable
    public Drawable getFullResIcon(@NonNull final String packageName, final int iconId) {
        try {
            return getFullResIcon(mPackageManager.getResourcesForApplication(packageName), iconId);
        } catch (PackageManager.NameNotFoundException e) { }
        return getFullResDefaultActivityIcon(mIconDpi);
    }

    @Nullable
    public Drawable getFullResIcon(@NonNull final ActivityInfo info) {
        try {
            return getFullResIcon(mPackageManager.getResourcesForApplication(info.applicationInfo),
                    info.getIconResource());
        } catch (PackageManager.NameNotFoundException e) { }
        return getFullResDefaultActivityIcon(mIconDpi);
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public synchronized void remove(@NonNull final ComponentName componentName,
            @NonNull final UserHandle user) {
        mCache.remove(new ComponentKey(componentName, user));
    }

    /**
     * Remove any records for the supplied package name from memory.
     */
    private void removeFromMemCacheLocked(@Nullable final String packageName,
            @Nullable final UserHandle user) {
        HashSet<ComponentKey> forDeletion = new HashSet<>();
        for (ComponentKey key: mCache.keySet()) {
            if (key.componentName.getPackageName().equals(packageName)
                    && key.user.equals(user)) {
                forDeletion.add(key);
            }
        }
        for (ComponentKey condemned: forDeletion) {
            mCache.remove(condemned);
        }
    }

    /**
     * Removes the entries related to the given package in memory and persistent DB.
     */
    public synchronized void removeIconsForPkg(@NonNull final String packageName,
            @NonNull final UserHandle user) {
        removeFromMemCacheLocked(packageName, user);
        long userSerial = getSerialNumberForUser(user);
        mIconDb.delete(
                IconDB.COLUMN_COMPONENT + " LIKE ? AND " + IconDB.COLUMN_USER + " = ?",
                new String[]{packageName + "/%", Long.toString(userSerial)});
    }

    @NonNull
    public IconCacheUpdateHandler getUpdateHandler() {
        updateSystemState();
        return new IconCacheUpdateHandler(this);
    }

    /**
     * Refreshes the system state definition used to check the validity of the cache. It
     * incorporates all the properties that can affect the cache like the list of enabled locale
     * and system-version.
     */
    private void updateSystemState() {
        mLocaleList = mContext.getResources().getConfiguration().getLocales();
        mSystemState = mLocaleList.toLanguageTags() + "," + Build.VERSION.SDK_INT;
        mUserFormatString.clear();
    }

    @NonNull
    protected String getIconSystemState(@Nullable final String packageName) {
        return mSystemState;
    }

    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        int key = user.hashCode();
        int index = mUserFormatString.indexOfKey(key);
        String format;
        if (index < 0) {
            format = mPackageManager.getUserBadgedLabel(IDENTITY_FORMAT_STRING, user).toString();
            if (TextUtils.equals(IDENTITY_FORMAT_STRING, format)) {
                format = null;
            }
            mUserFormatString.put(key, format);
        } else {
            format = mUserFormatString.valueAt(index);
        }
        return format == null ? label : String.format(format, label);
    }

    /**
     * Adds an entry into the DB and the in-memory cache.
     * @param replaceExisting if true, it will recreate the bitmap even if it already exists in
     *                        the memory. This is useful then the previous bitmap was created using
     *                        old data.
     */
    @VisibleForTesting
    public synchronized <T> void addIconToDBAndMemCache(@NonNull final T object,
            @NonNull final CachingLogic<T> cachingLogic, @NonNull final PackageInfo info,
            final long userSerial, final boolean replaceExisting) {
        UserHandle user = cachingLogic.getUser(object);
        ComponentName componentName = cachingLogic.getComponent(object);

        final ComponentKey key = new ComponentKey(componentName, user);
        CacheEntry entry = null;
        if (!replaceExisting) {
            entry = mCache.get(key);
            // We can't reuse the entry if the high-res icon is not present.
            if (entry == null || entry.bitmap.isNullOrLowRes()) {
                entry = null;
            }
        }
        if (entry == null) {
            entry = new CacheEntry();
            entry.bitmap = cachingLogic.loadIcon(mContext, object);
        }
        // Icon can't be loaded from cachingLogic, which implies alternative icon was loaded
        // (e.g. fallback icon, default icon). So we drop here since there's no point in caching
        // an empty entry.
        if (entry.bitmap.isNullOrLowRes()) return;

        CharSequence entryTitle = cachingLogic.getLabel(object);
        if (entryTitle == null) {
            Log.wtf(TAG, "No label returned from caching logic instance: " + cachingLogic);
            entryTitle = "";
        }
        entry.title = entryTitle;

        entry.contentDescription = getUserBadgedLabel(entry.title, user);
        if (cachingLogic.addToMemCache()) mCache.put(key, entry);

        ContentValues values = newContentValues(entry.bitmap, entry.title.toString(),
                componentName.getPackageName(), cachingLogic.getKeywords(object, mLocaleList));
        addIconToDB(values, componentName, info, userSerial,
                cachingLogic.getLastUpdatedTime(object, info));
    }

    /**
     * Updates {@param values} to contain versioning information and adds it to the DB.
     * @param values {@link ContentValues} containing icon & title
     */
    private void addIconToDB(@NonNull final ContentValues values, @NonNull final ComponentName key,
            @NonNull final PackageInfo info, final long userSerial, final long lastUpdateTime) {
        values.put(IconDB.COLUMN_COMPONENT, key.flattenToString());
        values.put(IconDB.COLUMN_USER, userSerial);
        values.put(IconDB.COLUMN_LAST_UPDATED, lastUpdateTime);
        values.put(IconDB.COLUMN_VERSION, info.versionCode);
        mIconDb.insertOrReplace(values);
    }

    @NonNull
    public synchronized BitmapInfo getDefaultIcon(@NonNull final UserHandle user) {
        if (mDefaultIcon == null) {
            try (BaseIconFactory li = getIconFactory()) {
                mDefaultIcon = li.makeDefaultIcon();
            }
        }
        return mDefaultIcon.withFlags(getUserFlagOpLocked(user));
    }

    @NonNull
    protected FlagOp getUserFlagOpLocked(@NonNull final UserHandle user) {
        int key = user.hashCode();
        int index;
        if ((index = mUserFlagOpMap.indexOfKey(key)) >= 0) {
            return mUserFlagOpMap.valueAt(index);
        } else {
            try (BaseIconFactory li = getIconFactory()) {
                FlagOp op = li.getBitmapFlagOp(new IconOptions().setUser(user));
                mUserFlagOpMap.put(key, op);
                return op;
            }
        }
    }

    public boolean isDefaultIcon(@NonNull final BitmapInfo icon, @NonNull final UserHandle user) {
        return getDefaultIcon(user).icon == icon.icon;
    }

    /**
     * Retrieves the entry from the cache. If the entry is not present, it creates a new entry.
     * This method is not thread safe, it must be called from a synchronized method.
     */
    @NonNull
    protected <T> CacheEntry cacheLocked(
            @NonNull final ComponentName componentName, @NonNull final UserHandle user,
            @NonNull final Supplier<T> infoProvider, @NonNull final CachingLogic<T> cachingLogic,
            final boolean usePackageIcon, final boolean useLowResIcon) {
        return cacheLocked(
                componentName,
                user,
                infoProvider,
                cachingLogic,
                null,
                usePackageIcon,
                useLowResIcon);
    }

    @NonNull
    protected <T> CacheEntry cacheLocked(
            @NonNull final ComponentName componentName, @NonNull final UserHandle user,
            @NonNull final Supplier<T> infoProvider, @NonNull final CachingLogic<T> cachingLogic,
            @Nullable final Cursor cursor, final boolean usePackageIcon,
            final boolean useLowResIcon) {
        assertWorkerThread();
        ComponentKey cacheKey = new ComponentKey(componentName, user);
        CacheEntry entry = mCache.get(cacheKey);
        if (entry == null || (entry.bitmap.isLowRes() && !useLowResIcon)) {
            entry = new CacheEntry();
            if (cachingLogic.addToMemCache()) {
                mCache.put(cacheKey, entry);
            }

            // Check the DB first.
            T object = null;
            boolean providerFetchedOnce = false;
            boolean cacheEntryUpdated = cursor == null
                    ? getEntryFromDBLocked(cacheKey, entry, useLowResIcon)
                    : updateTitleAndIconLocked(cacheKey, entry, cursor, useLowResIcon);
            if (!cacheEntryUpdated) {
                object = infoProvider.get();
                providerFetchedOnce = true;

                loadFallbackIcon(
                        object,
                        entry,
                        cachingLogic,
                        usePackageIcon,
                        /* usePackageTitle= */ true,
                        componentName,
                        user);
            }

            if (TextUtils.isEmpty(entry.title)) {
                if (object == null && !providerFetchedOnce) {
                    object = infoProvider.get();
                    providerFetchedOnce = true;
                }
                if (object != null) {
                    loadFallbackTitle(object, entry, cachingLogic, user);
                }
            }
        }
        return entry;
    }

    /**
     * Fallback method for loading an icon bitmap.
     */
    protected <T> void loadFallbackIcon(@Nullable final T object, @NonNull final CacheEntry entry,
            @NonNull final CachingLogic<T> cachingLogic, final boolean usePackageIcon,
            final boolean usePackageTitle, @NonNull final ComponentName componentName,
            @NonNull final UserHandle user) {
        if (object != null) {
            entry.bitmap = cachingLogic.loadIcon(mContext, object);
        } else {
            if (usePackageIcon) {
                CacheEntry packageEntry = getEntryForPackageLocked(
                        componentName.getPackageName(), user, false);
                if (DEBUG) Log.d(TAG, "using package default icon for " +
                        componentName.toShortString());
                entry.bitmap = packageEntry.bitmap;
                entry.contentDescription = packageEntry.contentDescription;

                if (usePackageTitle) {
                    entry.title = packageEntry.title;
                }
            }
            if (entry.bitmap == null) {
                // TODO: entry.bitmap can never be null, so this should not happen at all.
                Log.wtf(TAG, "using default icon for " + componentName.toShortString());
                entry.bitmap = getDefaultIcon(user);
            }
        }
    }

    /**
     * Fallback method for loading an app title.
     */
    protected <T> void loadFallbackTitle(
            @NonNull final T object, @NonNull final CacheEntry entry,
            @NonNull final CachingLogic<T> cachingLogic, @NonNull final UserHandle user) {
        entry.title = cachingLogic.getLabel(object);
        entry.contentDescription = getUserBadgedLabel(
                cachingLogic.getDescription(object, entry.title), user);
    }

    public synchronized void clear() {
        assertWorkerThread();
        mIconDb.clear();
    }

    /**
     * Adds a default package entry in the cache. This entry is not persisted and will be removed
     * when the cache is flushed.
     */
    protected synchronized void cachePackageInstallInfo(@NonNull final String packageName,
            @NonNull final UserHandle user, @Nullable final Bitmap icon,
            @Nullable final CharSequence title) {
        removeFromMemCacheLocked(packageName, user);

        ComponentKey cacheKey = getPackageKey(packageName, user);
        CacheEntry entry = mCache.get(cacheKey);

        // For icon caching, do not go through DB. Just update the in-memory entry.
        if (entry == null) {
            entry = new CacheEntry();
        }
        if (!TextUtils.isEmpty(title)) {
            entry.title = title;
        }
        if (icon != null) {
            BaseIconFactory li = getIconFactory();
            entry.bitmap = li.createShapedIconBitmap(icon, new IconOptions().setUser(user));
            li.close();
        }
        if (!TextUtils.isEmpty(title) && entry.bitmap.icon != null) {
            mCache.put(cacheKey, entry);
        }
    }

    @NonNull
    private static ComponentKey getPackageKey(@NonNull final String packageName,
            @NonNull final UserHandle user) {
        ComponentName cn = new ComponentName(packageName, packageName + EMPTY_CLASS_NAME);
        return new ComponentKey(cn, user);
    }

    /**
     * Gets an entry for the package, which can be used as a fallback entry for various components.
     * This method is not thread safe, it must be called from a synchronized method.
     */
    @WorkerThread
    @NonNull
    protected CacheEntry getEntryForPackageLocked(@NonNull final String packageName,
            @NonNull final UserHandle user, final boolean useLowResIcon) {
        assertWorkerThread();
        ComponentKey cacheKey = getPackageKey(packageName, user);
        CacheEntry entry = mCache.get(cacheKey);

        if (entry == null || (entry.bitmap.isLowRes() && !useLowResIcon)) {
            entry = new CacheEntry();
            boolean entryUpdated = true;

            // Check the DB first.
            if (!getEntryFromDBLocked(cacheKey, entry, useLowResIcon)) {
                try {
                    int flags = Process.myUserHandle().equals(user) ? 0 :
                            PackageManager.GET_UNINSTALLED_PACKAGES;
                    PackageInfo info = mPackageManager.getPackageInfo(packageName, flags);
                    ApplicationInfo appInfo = info.applicationInfo;
                    if (appInfo == null) {
                        throw new NameNotFoundException("ApplicationInfo is null");
                    }

                    BaseIconFactory li = getIconFactory();
                    // Load the full res icon for the application, but if useLowResIcon is set, then
                    // only keep the low resolution icon instead of the larger full-sized icon
                    BitmapInfo iconInfo = li.createBadgedIconBitmap(
                            appInfo.loadIcon(mPackageManager),
                            new IconOptions().setUser(user).setInstantApp(isInstantApp(appInfo)));
                    li.close();

                    entry.title = appInfo.loadLabel(mPackageManager);
                    entry.contentDescription = getUserBadgedLabel(entry.title, user);
                    entry.bitmap = BitmapInfo.of(
                            useLowResIcon ? LOW_RES_ICON : iconInfo.icon, iconInfo.color);

                    // Add the icon in the DB here, since these do not get written during
                    // package updates.
                    ContentValues values = newContentValues(
                            iconInfo, entry.title.toString(), packageName, null);
                    addIconToDB(values, cacheKey.componentName, info, getSerialNumberForUser(user),
                            info.lastUpdateTime);

                } catch (NameNotFoundException e) {
                    if (DEBUG) Log.d(TAG, "Application not installed " + packageName);
                    entryUpdated = false;
                }
            }

            // Only add a filled-out entry to the cache
            if (entryUpdated) {
                mCache.put(cacheKey, entry);
            }
        }
        return entry;
    }

    protected boolean getEntryFromDBLocked(@NonNull final ComponentKey cacheKey,
            @NonNull final CacheEntry entry, final boolean lowRes) {
        Cursor c = null;
        Trace.beginSection("loadIconIndividually");
        try {
            c = mIconDb.query(
                    lowRes ? IconDB.COLUMNS_LOW_RES : IconDB.COLUMNS_HIGH_RES,
                    IconDB.COLUMN_COMPONENT + " = ? AND " + IconDB.COLUMN_USER + " = ?",
                    new String[]{
                            cacheKey.componentName.flattenToString(),
                            Long.toString(getSerialNumberForUser(cacheKey.user))});
            if (c.moveToNext()) {
                return updateTitleAndIconLocked(cacheKey, entry, c, lowRes);
            }
        } catch (SQLiteException e) {
            Log.d(TAG, "Error reading icon cache", e);
        } finally {
            if (c != null) {
                c.close();
            }
            Trace.endSection();
        }
        return false;
    }

    private boolean updateTitleAndIconLocked(
            @NonNull final ComponentKey cacheKey, @NonNull final CacheEntry entry,
            @NonNull final Cursor c, final boolean lowRes) {
        // Set the alpha to be 255, so that we never have a wrong color
        entry.bitmap = BitmapInfo.of(LOW_RES_ICON,
                setColorAlphaBound(c.getInt(IconDB.INDEX_COLOR), 255));
        entry.title = c.getString(IconDB.INDEX_TITLE);
        if (entry.title == null) {
            entry.title = "";
            entry.contentDescription = "";
        } else {
            entry.contentDescription = getUserBadgedLabel(entry.title, cacheKey.user);
        }

        if (!lowRes) {
            byte[] data = c.getBlob(IconDB.INDEX_ICON);
            if (data == null) {
                return false;
            }
            try {
                BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
                decodeOptions.inPreferredConfig = Config.HARDWARE;
                entry.bitmap = BitmapInfo.of(
                        requireNonNull(decodeByteArray(data, 0, data.length, decodeOptions)),
                        entry.bitmap.color);
            } catch (Exception e) {
                return false;
            }

            // Decode mono bitmap
            data = c.getBlob(IconDB.INDEX_MONO_ICON);
            Bitmap icon = entry.bitmap.icon;
            if (data != null && data.length == icon.getHeight() * icon.getWidth()) {
                Bitmap monoBitmap = Bitmap.createBitmap(
                        icon.getWidth(), icon.getHeight(), Config.ALPHA_8);
                monoBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
                Bitmap hwMonoBitmap = monoBitmap.copy(Config.HARDWARE, false /*isMutable*/);
                if (hwMonoBitmap != null) {
                    monoBitmap.recycle();
                    monoBitmap = hwMonoBitmap;
                }
                try (BaseIconFactory factory = getIconFactory()) {
                    entry.bitmap.setMonoIcon(monoBitmap, factory);
                }
            }
        }
        entry.bitmap.flags = c.getInt(IconDB.INDEX_FLAGS);
        entry.bitmap = entry.bitmap.withFlags(getUserFlagOpLocked(cacheKey.user));
        return entry.bitmap != null;
    }

    /**
     * Returns a cursor for an arbitrary query to the cache db
     */
    public synchronized Cursor queryCacheDb(String[] columns, String selection,
            String[] selectionArgs) {
        return mIconDb.query(columns, selection, selectionArgs);
    }

    /**
     * Cache class to store the actual entries on disk
     */
    public static final class IconDB extends SQLiteCacheHelper {
        private static final int RELEASE_VERSION = 34;

        public static final String TABLE_NAME = "icons";
        public static final String COLUMN_ROWID = "rowid";
        public static final String COLUMN_COMPONENT = "componentName";
        public static final String COLUMN_USER = "profileId";
        public static final String COLUMN_LAST_UPDATED = "lastUpdated";
        public static final String COLUMN_VERSION = "version";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_ICON_COLOR = "icon_color";
        public static final String COLUMN_MONO_ICON = "mono_icon";
        public static final String COLUMN_FLAGS = "flags";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_SYSTEM_STATE = "system_state";
        public static final String COLUMN_KEYWORDS = "keywords";

        public static final String[] COLUMNS_LOW_RES = new String[] {
                COLUMN_COMPONENT,
                COLUMN_LABEL,
                COLUMN_ICON_COLOR,
                COLUMN_FLAGS};
        public static final String[] COLUMNS_HIGH_RES = Arrays.copyOf(COLUMNS_LOW_RES,
                COLUMNS_LOW_RES.length + 2, String[].class);
        static {
            COLUMNS_HIGH_RES[COLUMNS_LOW_RES.length] = COLUMN_ICON;
            COLUMNS_HIGH_RES[COLUMNS_LOW_RES.length + 1] = COLUMN_MONO_ICON;
        }
        private static final int INDEX_TITLE = Arrays.asList(COLUMNS_LOW_RES).indexOf(COLUMN_LABEL);
        private static final int INDEX_COLOR = Arrays.asList(COLUMNS_LOW_RES)
                .indexOf(COLUMN_ICON_COLOR);
        private static final int INDEX_FLAGS = Arrays.asList(COLUMNS_LOW_RES).indexOf(COLUMN_FLAGS);
        private static final int INDEX_ICON = COLUMNS_LOW_RES.length;
        private static final int INDEX_MONO_ICON = INDEX_ICON + 1;

        public IconDB(Context context, String dbFileName, int iconPixelSize) {
            super(context, dbFileName, (RELEASE_VERSION << 16) + iconPixelSize, TABLE_NAME);
        }

        @Override
        protected void onCreateTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_COMPONENT + " TEXT NOT NULL, "
                    + COLUMN_USER + " INTEGER NOT NULL, "
                    + COLUMN_LAST_UPDATED + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_VERSION + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_ICON + " BLOB, "
                    + COLUMN_MONO_ICON + " BLOB, "
                    + COLUMN_ICON_COLOR + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_FLAGS + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_LABEL + " TEXT, "
                    + COLUMN_SYSTEM_STATE + " TEXT, "
                    + COLUMN_KEYWORDS + " TEXT, "
                    + "PRIMARY KEY (" + COLUMN_COMPONENT + ", " + COLUMN_USER + ") "
                    + ");");
        }
    }

    @NonNull
    private ContentValues newContentValues(@NonNull final BitmapInfo bitmapInfo,
            @NonNull final String label, @NonNull final String packageName,
            @Nullable final String keywords) {
        ContentValues values = new ContentValues();
        if (bitmapInfo.canPersist()) {
            values.put(IconDB.COLUMN_ICON, flattenBitmap(bitmapInfo.icon));

            // Persist mono bitmap as alpha channel
            Bitmap mono = bitmapInfo.getMono();
            if (mono != null && mono.getHeight() == bitmapInfo.icon.getHeight()
                    && mono.getWidth() == bitmapInfo.icon.getWidth()
                    && mono.getConfig() == Config.ALPHA_8) {
                byte[] pixels = new byte[mono.getWidth() * mono.getHeight()];
                mono.copyPixelsToBuffer(ByteBuffer.wrap(pixels));
                values.put(IconDB.COLUMN_MONO_ICON, pixels);
            } else {
                values.put(IconDB.COLUMN_MONO_ICON, (byte[]) null);
            }
        } else {
            values.put(IconDB.COLUMN_ICON, (byte[]) null);
            values.put(IconDB.COLUMN_MONO_ICON, (byte[]) null);
        }
        values.put(IconDB.COLUMN_ICON_COLOR, bitmapInfo.color);
        values.put(IconDB.COLUMN_FLAGS, bitmapInfo.flags);

        values.put(IconDB.COLUMN_LABEL, label);
        values.put(IconDB.COLUMN_SYSTEM_STATE, getIconSystemState(packageName));
        values.put(IconDB.COLUMN_KEYWORDS, keywords);
        return values;
    }

    private void assertWorkerThread() {
        if (Looper.myLooper() != mBgLooper) {
            throw new IllegalStateException("Cache accessed on wrong thread " + Looper.myLooper());
        }
    }
}

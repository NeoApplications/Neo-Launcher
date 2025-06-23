/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3.icons;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT;
import static com.android.launcher3.icons.cache.CacheLookupFlag.DEFAULT_LOOKUP_FLAG;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;
import static com.android.launcher3.util.LooperExecutor.CALLER_ICON_CACHE;
import static com.android.launcher3.widget.WidgetSections.NO_CATEGORY;

import static java.util.stream.Collectors.groupingBy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInstaller;
import android.content.pm.ShortcutInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import com.android.launcher3.Flags;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.Utilities;
import com.android.launcher3.dagger.ApplicationContext;
import com.android.launcher3.dagger.LauncherAppSingleton;
import com.android.launcher3.icons.cache.BaseIconCache;
import com.android.launcher3.icons.cache.CacheLookupFlag;
import com.android.launcher3.icons.cache.CachedObject;
import com.android.launcher3.icons.cache.CachedObjectCachingLogic;
import com.android.launcher3.icons.cache.LauncherActivityCachingLogic;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.IconRequestInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.ItemInfoWithIcon;
import com.android.launcher3.model.data.PackageItemInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.pm.InstallSessionHelper;
import com.android.launcher3.pm.UserCache;
import com.android.launcher3.util.CancellableTask;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.DaggerSingletonTracker;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.WidgetSections;
import com.android.launcher3.widget.WidgetSections.WidgetSection;
import com.saggitt.omega.icons.CustomIconProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
@LauncherAppSingleton
public class IconCache extends BaseIconCache {

    // Shortcut extra which can point to a packageName and can be used to indicate an alternate
    // badge info. Launcher only reads this if the shortcut comes from a system app.
    public static final String EXTRA_SHORTCUT_BADGE_OVERRIDE_PACKAGE =
            "extra_shortcut_badge_override_package";

    private static final String TAG = "Launcher.IconCache";

    private final Predicate<ItemInfoWithIcon> mIsUsingFallbackOrNonDefaultIconCheck = w ->
            w.bitmap != null && (w.bitmap.isNullOrLowRes() || !isDefaultIcon(w.bitmap, w.user));

    private final LauncherApps mLauncherApps;
    private final UserCache mUserManager;
    private final InstallSessionHelper mInstallSessionHelper;
    private final InstantAppResolver mInstantAppResolver;
    private final CancellableTask mCancelledTask;
    private final LauncherIcons.IconPool mIconPool;

    private final SparseArray<BitmapInfo> mWidgetCategoryBitmapInfos;

    private int mPendingIconRequestCount = 0;

    @Inject
    public IconCache(
            @ApplicationContext Context context,
            InvariantDeviceProfile idp,
            @Nullable @Named("ICONS_DB") String dbFileName,
            UserCache userCache,
            LauncherIconProvider iconProvider,
            InstallSessionHelper installSessionHelper,
            LauncherIcons.IconPool iconPool,
            DaggerSingletonTracker lifecycle) {
        super(context, dbFileName, MODEL_EXECUTOR.getLooper(),
                idp.fillResIconDpi, idp.iconBitmapSize, true /* inMemoryCache */, iconProvider);
        mLauncherApps = context.getSystemService(LauncherApps.class);
        mUserManager = userCache;
        mInstallSessionHelper = installSessionHelper;
        mIconPool = iconPool;

        mInstantAppResolver = InstantAppResolver.newInstance(context);
        mWidgetCategoryBitmapInfos = new SparseArray<>();

        mCancelledTask = new CancellableTask(() -> null, MAIN_EXECUTOR, c -> { });
        mCancelledTask.cancel();

        lifecycle.addCloseable(this::close);
    }

    @Override
    public long getSerialNumberForUser(@NonNull UserHandle user) {
        return mUserManager.getSerialNumberForUser(user);
    }

    @Override
    protected boolean isInstantApp(@NonNull ApplicationInfo info) {
        return mInstantAppResolver.isInstantApp(info);
    }

    @NonNull
    @Override
    public BaseIconFactory getIconFactory() {
        return mIconPool.obtain();
    }

    /**
     * Updates the entries related to the given package in memory and persistent DB.
     */
    public synchronized void updateIconsForPkg(@NonNull final String packageName,
            @NonNull final UserHandle user) {
        List<LauncherActivityInfo> apps = mLauncherApps.getActivityList(packageName, user);
        if (Flags.restoreArchivedAppIconsFromDb()
                && apps.stream().anyMatch(app -> app.getApplicationInfo().isArchived)) {
            // When archiving app icon, don't delete old icon so it can be re-used.
            return;
        }
        removeIconsForPkg(packageName, user);
        long userSerial = mUserManager.getSerialNumberForUser(user);
        for (LauncherActivityInfo app : apps) {
            addIconToDBAndMemCache(app, LauncherActivityCachingLogic.INSTANCE, userSerial);
        }
    }

    /**
     * Closes the cache DB. This will clear any in-memory cache.
     */
    public void close() {
        // This will clear all pending updates
        getUpdateHandler();

        mIconDb.close();
    }

    /**
     * Fetches high-res icon for the provided ItemInfo and updates the caller when done.
     *
     * @return a request ID that can be used to cancel the request.
     */
    @AnyThread
    public CancellableTask updateIconInBackground(final ItemInfoUpdateReceiver caller,
                                                  final ItemInfoWithIcon info) {
        Supplier<ItemInfoWithIcon> task;
        if (info instanceof AppInfo || info instanceof WorkspaceItemInfo) {
            task = () -> {
                getTitleAndIcon(info, DEFAULT_LOOKUP_FLAG);
                return info;
            };
        } else if (info instanceof PackageItemInfo pii) {
            task = () -> {
                getTitleAndIconForApp(pii, DEFAULT_LOOKUP_FLAG);
                return pii;
            };
        } else {
            Log.i(TAG, "Icon update not supported for "
                    + info == null ? "null" : info.getClass().getName());
            return mCancelledTask;
        }

        Runnable endRunnable;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mPendingIconRequestCount <= 0) {
                MODEL_EXECUTOR.elevatePriority(CALLER_ICON_CACHE);
            }
            mPendingIconRequestCount++;
            endRunnable = this::onIconRequestEnd;
        } else {
            endRunnable = () -> { };
        }

        CancellableTask<ItemInfoWithIcon> request = new CancellableTask<>(
                task, MAIN_EXECUTOR, caller::reapplyItemInfo, endRunnable);
        Utilities.postAsyncCallback(workerHandler, request);
        return request;
    }

    private void onIconRequestEnd() {
        mPendingIconRequestCount--;
        if (mPendingIconRequestCount <= 0) {
            MODEL_EXECUTOR.restorePriority(CALLER_ICON_CACHE);
        }
    }

    /**
     * Updates {@param application} only if a valid entry is found.
     */
    public synchronized void updateTitleAndIcon(AppInfo application) {
        CacheEntry entry = cacheLocked(application.componentName,
                application.user, () -> null, LauncherActivityCachingLogic.INSTANCE,
                application.getMatchingLookupFlag());
        if (entry.bitmap != null || !isDefaultIcon(entry.bitmap, application.user)) {
            applyCacheEntry(entry, application);
        }
    }

    /**
     * Fill in {@code info} with the icon and label for {@code activityInfo}
     */
    @SuppressWarnings("NewApi")
    public synchronized void getTitleAndIcon(ItemInfoWithIcon info,
                                             LauncherActivityInfo activityInfo, @NonNull CacheLookupFlag lookupFlag) {
        boolean isAppArchived = Flags.enableSupportForArchiving() && activityInfo != null
                && activityInfo.getActivityInfo().isArchived;
        // If we already have activity info, no need to use package icon
        getTitleAndIcon(info, () -> activityInfo, lookupFlag.withUsePackageIcon(isAppArchived));
    }

    /**
     * Fill in {@code info} with the icon for {@code si}
     */
    public void getShortcutIcon(ItemInfoWithIcon info, ShortcutInfo si) {
        getShortcutIcon(info, new CacheableShortcutInfo(si, mContext));
    }

    /**
     * Fill in {@code info} with the icon for {@code si}
     */
    public void getShortcutIcon(ItemInfoWithIcon info, CacheableShortcutInfo si) {
        getShortcutIcon(info, si, mIsUsingFallbackOrNonDefaultIconCheck);
    }

    /**
     * Fill in {@code info} with the icon and label for {@code si}. If the icon is not
     * available, and fallback check returns true, it keeps the old icon.
     * Shortcut entries are not kept in memory since they are not frequently used
     */
    public <T extends ItemInfoWithIcon> void getShortcutIcon(T info, CacheableShortcutInfo si,
                                                             @NonNull Predicate<T> fallbackIconCheck) {
        UserHandle user = CacheableShortcutCachingLogic.INSTANCE.getUser(si);
        BitmapInfo bitmapInfo = cacheLocked(
                CacheableShortcutCachingLogic.INSTANCE.getComponent(si),
                user,
                () -> si,
                CacheableShortcutCachingLogic.INSTANCE,
                DEFAULT_LOOKUP_FLAG.withSkipAddToMemCache()).bitmap;
        if (bitmapInfo.isNullOrLowRes()) {
            bitmapInfo = getDefaultIcon(user);
        }

        if (isDefaultIcon(bitmapInfo, user) && fallbackIconCheck.test(info)) {
            return;
        }
        info.bitmap = bitmapInfo.withBadgeInfo(getShortcutInfoBadge(si.getShortcutInfo()));
    }

    /**
     * Returns the badging info for the shortcut
     */
    public BitmapInfo getShortcutInfoBadge(ShortcutInfo shortcutInfo) {
        return getShortcutInfoBadgeItem(shortcutInfo).bitmap;
    }

    @VisibleForTesting
    protected ItemInfoWithIcon getShortcutInfoBadgeItem(ShortcutInfo shortcutInfo) {
        // Check for badge override first.
        String pkg = shortcutInfo.getPackage();
        String override = shortcutInfo.getExtras() == null ? null
                : shortcutInfo.getExtras().getString(EXTRA_SHORTCUT_BADGE_OVERRIDE_PACKAGE);
        if (!TextUtils.isEmpty(override)
                && mInstallSessionHelper.isTrustedPackage(pkg, shortcutInfo.getUserHandle())) {
            pkg = override;
        } else {
            // Try component based badge before trying the normal package badge
            ComponentName cn = shortcutInfo.getActivity();
            if (cn != null) {
                // Get the app info for the source activity.
                AppInfo appInfo = new AppInfo();
                appInfo.user = shortcutInfo.getUserHandle();
                appInfo.componentName = cn;
                appInfo.intent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setComponent(cn);
                getTitleAndIcon(appInfo, DEFAULT_LOOKUP_FLAG);
                return appInfo;
            }
        }
        PackageItemInfo pkgInfo = new PackageItemInfo(pkg, shortcutInfo.getUserHandle());
        getTitleAndIconForApp(pkgInfo, DEFAULT_LOOKUP_FLAG);
        return pkgInfo;
    }

    /**
     * Fill in {@param info} with the icon and label. If the
     * corresponding activity is not found, it reverts to the package icon.
     */
    public synchronized void getTitleAndIcon(
            @NonNull ItemInfoWithIcon info,
            @NonNull CacheLookupFlag lookupFlag) {
        // null info means not installed, but if we have a component from the intent then
        // we should still look in the cache for restored app icons.
        if (info.getTargetComponent() == null) {
            info.bitmap = getDefaultIcon(info.user);
            info.title = "";
            info.contentDescription = "";
        } else {
            Intent intent = info.getIntent();
            getTitleAndIcon(info, () -> mLauncherApps.resolveActivity(intent, info.user),
                    lookupFlag.withUsePackageIcon());
        }
    }

    /**
     * Loads and returns the icon for the provided object without adding it to memCache
     */
    public synchronized String getTitleNoCache(CachedObject info) {
        CacheEntry entry = cacheLocked(info.getComponent(), info.getUser(), () -> info,
                CachedObjectCachingLogic.INSTANCE,
                DEFAULT_LOOKUP_FLAG.withUseLowRes().withSkipAddToMemCache());
        return Utilities.trim(entry.title);
    }

    /**
     * Fill in {@param mWorkspaceItemInfo} with the icon and label for {@param info}
     */
    public synchronized void getTitleAndIcon(
            @NonNull ItemInfoWithIcon infoInOut,
            @NonNull Supplier<LauncherActivityInfo> activityInfoProvider,
            @NonNull CacheLookupFlag lookupFlag) {
        CacheEntry entry = cacheLocked(infoInOut.getTargetComponent(), infoInOut.user,
                activityInfoProvider, LauncherActivityCachingLogic.INSTANCE, lookupFlag);
        applyCacheEntry(entry, infoInOut);
    }

    /**
     * Creates an sql cursor for a query of a set of ItemInfoWithIcon icons and titles.
     *
     * @param iconRequestInfos List of IconRequestInfos representing titles and icons to query.
     * @param user UserHandle all the given iconRequestInfos share
     * @param lookupFlag what flags to use when loading the icon.
     */
    private <T extends ItemInfoWithIcon> Cursor createBulkQueryCursor(
            List<IconRequestInfo<T>> iconRequestInfos, UserHandle user, CacheLookupFlag lookupFlag)
            throws SQLiteException {
        String[] queryParams = Stream.concat(
                iconRequestInfos.stream()
                        .map(r -> r.itemInfo.getTargetComponent())
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(ComponentName::flattenToString),
                Stream.of(Long.toString(getSerialNumberForUser(user)))).toArray(String[]::new);
        String componentNameQuery = TextUtils.join(
                ",", Collections.nCopies(queryParams.length - 1, "?"));

        return mIconDb.query(
                toLookupColumns(lookupFlag),
                COLUMN_COMPONENT
                        + " IN ( " + componentNameQuery + " )"
                        + " AND " + COLUMN_USER + " = ?",
                queryParams);
    }

    /**
     * Load and fill icons requested in iconRequestInfos using a single bulk sql query.
     */
    public synchronized <T extends ItemInfoWithIcon> void getTitlesAndIconsInBulk(
            List<IconRequestInfo<T>> iconRequestInfos) {
        Map<Pair<UserHandle, Boolean>, List<IconRequestInfo<T>>> iconLoadSubsectionsMap =
                iconRequestInfos.stream()
                        .filter(iconRequest -> {
                            if (iconRequest.itemInfo.getTargetComponent() == null) {
                                Log.i(TAG,
                                        "Skipping Item info with null component name: "
                                                + iconRequest.itemInfo);
                                iconRequest.itemInfo.bitmap = getDefaultIcon(
                                        iconRequest.itemInfo.user);
                                return false;
                            }
                            return true;
                        })
                        .collect(groupingBy(iconRequest ->
                                Pair.create(iconRequest.itemInfo.user, iconRequest.useLowResIcon)));

        Trace.beginSection("loadIconsInBulk");
        iconLoadSubsectionsMap.forEach((sectionKey, filteredList) -> {
            Map<ComponentName, List<IconRequestInfo<T>>> duplicateIconRequestsMap =
                    filteredList.stream()
                            .filter(iconRequest -> {
                                // Filter out icons that should not share the same bitmap and title
                                if (iconRequest.itemInfo.itemType == ITEM_TYPE_DEEP_SHORTCUT) {
                                    Log.e(TAG,
                                            "Skipping Item info for deep shortcut: "
                                                    + iconRequest.itemInfo,
                                            new IllegalStateException());
                                    return false;
                                }
                                return true;
                            })
                            .collect(groupingBy(iconRequest ->
                                    iconRequest.itemInfo.getTargetComponent()));

            Trace.beginSection("loadIconSubsectionInBulk");
            loadIconSubsection(sectionKey, filteredList, duplicateIconRequestsMap);
            Trace.endSection();
        });
        Trace.endSection();
    }

    private <T extends ItemInfoWithIcon> void loadIconSubsection(
            Pair<UserHandle, Boolean> sectionKey,
            List<IconRequestInfo<T>> filteredList,
            Map<ComponentName, List<IconRequestInfo<T>>> duplicateIconRequestsMap) {
        Trace.beginSection("loadIconSubsectionWithDatabase");
        CacheLookupFlag lookupFlag = DEFAULT_LOOKUP_FLAG.withUseLowRes(sectionKey.second);
        try (Cursor c = createBulkQueryCursor(
                filteredList,
                /* user = */ sectionKey.first,
                lookupFlag)) {
            // Database title and icon loading
            int componentNameColumnIndex = c.getColumnIndexOrThrow(COLUMN_COMPONENT);
            while (c.moveToNext()) {
                ComponentName cn = ComponentName.unflattenFromString(
                        c.getString(componentNameColumnIndex));
                List<IconRequestInfo<T>> duplicateIconRequests =
                        duplicateIconRequestsMap.get(cn);

                if (cn != null) {
                    if (duplicateIconRequests != null) {
                        CacheEntry entry = cacheLocked(
                                cn,
                                /* user = */ sectionKey.first,
                                () -> duplicateIconRequests.get(0).launcherActivityInfo,
                                LauncherActivityCachingLogic.INSTANCE,
                                lookupFlag,
                                c);

                        for (IconRequestInfo<T> iconRequest : duplicateIconRequests) {
                            applyCacheEntry(entry, iconRequest.itemInfo);
                        }
                    } else {
                        Log.e(TAG, "Found entry in icon database but no main activity "
                                + "entry for cn: " + cn);
                    }
                }
            }
        } catch (SQLiteException e) {
            Log.d(TAG, "Error reading icon cache", e);
        } finally {
            Trace.endSection();
        }

        Trace.beginSection("loadIconSubsectionWithFallback");
        // Fallback title and icon loading
        for (ComponentName cn : duplicateIconRequestsMap.keySet()) {
            IconRequestInfo<T> iconRequestInfo = duplicateIconRequestsMap.get(cn).get(0);
            ItemInfoWithIcon itemInfo = iconRequestInfo.itemInfo;
            BitmapInfo icon = itemInfo.bitmap;
            boolean loadFallbackTitle = TextUtils.isEmpty(itemInfo.title);
            boolean loadFallbackIcon = icon == null
                    || isDefaultIcon(icon, itemInfo.user)
                    || icon == BitmapInfo.LOW_RES_INFO;

            if (loadFallbackTitle || loadFallbackIcon) {
                Log.i(TAG,
                        "Database bulk icon loading failed, using fallback bulk icon loading "
                                + "for: " + cn);
                CacheEntry entry = new CacheEntry();
                LauncherActivityInfo lai = iconRequestInfo.launcherActivityInfo;

                // Fill fields that are not updated below so they are not subsequently
                // deleted.
                entry.title = itemInfo.title;
                if (icon != null) {
                    entry.bitmap = icon;
                }
                entry.contentDescription = itemInfo.contentDescription;

                if (loadFallbackIcon) {
                    loadFallbackIcon(
                            lai,
                            entry,
                            LauncherActivityCachingLogic.INSTANCE,
                            /* usePackageIcon= */ false,
                            /* usePackageTitle= */ loadFallbackTitle,
                            cn,
                            sectionKey.first);
                }
                if (loadFallbackTitle && TextUtils.isEmpty(entry.title) && lai != null) {
                    loadFallbackTitle(
                            lai,
                            entry,
                            LauncherActivityCachingLogic.INSTANCE,
                            sectionKey.first);
                }

                for (IconRequestInfo<T> iconRequest : duplicateIconRequestsMap.get(cn)) {
                    applyCacheEntry(entry, iconRequest.itemInfo);
                }
            }
        }
        Trace.endSection();
    }

    /**
     * Fill in {@param infoInOut} with the corresponding icon and label.
     */
    public synchronized void getTitleAndIconForApp(
            @NonNull final PackageItemInfo infoInOut,
            @NonNull CacheLookupFlag lookupFlag) {
        CacheEntry entry = getEntryForPackageLocked(
                infoInOut.packageName, infoInOut.user, lookupFlag);
        applyCacheEntry(entry, infoInOut);
        if (infoInOut.widgetCategory == NO_CATEGORY) {
            return;
        }

        WidgetSection widgetSection = WidgetSections.getWidgetSections(mContext)
                .get(infoInOut.widgetCategory);
        infoInOut.title = mContext.getString(widgetSection.mSectionTitle);
        infoInOut.contentDescription = getUserBadgedLabel(infoInOut.title, infoInOut.user);
        final BitmapInfo cachedBitmap = mWidgetCategoryBitmapInfos.get(infoInOut.widgetCategory);
        if (cachedBitmap != null) {
            infoInOut.bitmap = getBadgedIcon(cachedBitmap, infoInOut.user);
            return;
        }

        try (LauncherIcons li = mIconPool.obtain()) {
            final BitmapInfo tempBitmap = li.createBadgedIconBitmap(
                    mContext.getDrawable(widgetSection.mSectionDrawable),
                    new BaseIconFactory.IconOptions());
            mWidgetCategoryBitmapInfos.put(infoInOut.widgetCategory, tempBitmap);
            infoInOut.bitmap = getBadgedIcon(tempBitmap, infoInOut.user);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bitmap for icons with widget category", e);
        }

    }

    private synchronized BitmapInfo getBadgedIcon(@Nullable final BitmapInfo bitmap,
            @NonNull final UserHandle user) {
        if (bitmap == null) {
            return getDefaultIcon(user);
        }
        return bitmap.withFlags(getUserFlagOpLocked(user));
    }

    protected void applyCacheEntry(@NonNull final CacheEntry entry,
            @NonNull final ItemInfoWithIcon info) {
        info.title = Utilities.trim(entry.title);
        info.contentDescription = entry.contentDescription;
        info.bitmap = entry.bitmap;
        // Clear any previously set appTitle, if the packageOverride is no longer valid
        info.appTitle = null;
        if (entry.bitmap == null) {
            // TODO: entry.bitmap can never be null, so this should not happen at all.
            Log.wtf(TAG, "Cannot find bitmap from the cache, default icon was loaded.");
            info.bitmap = getDefaultIcon(info.user);
        }

        // apply package override
        if (!Flags.enableSupportForArchiving() || !info.isArchived()) {
            return;
        }
        String targetPackage = info.getTargetPackage();
        if (targetPackage == null) {
            return;
        }
        CacheEntry packageEntry = getInMemoryPackageEntryLocked(targetPackage, info.user);
        if (packageEntry == null || packageEntry.bitmap.isLowRes()) {
            return;
        }
        info.appTitle = Utilities.trim(info.title);
        info.title = Utilities.trim(packageEntry.title);
        info.contentDescription = packageEntry.contentDescription;
        info.bitmap = packageEntry.bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Drawable getFullResIcon(LauncherActivityInfo info, boolean flattenDrawable) {
        return mIconProvider.getIcon(info, mIconDpi);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Drawable getFullResIcon(LauncherActivityInfo info, ItemInfo itemInfo, boolean flattenDrawable) {
        if (mIconProvider instanceof CustomIconProvider)
            return ((CustomIconProvider) mIconProvider).getIcon(info, mIconDpi);
        return getFullResIcon(info, flattenDrawable);
    }

    public void updateSessionCache(PackageUserKey key, PackageInstaller.SessionInfo info) {
        cachePackageInstallInfo(key.mPackageName, key.mUser, info.getAppIcon(),
                info.getAppLabel());
    }

    @VisibleForTesting
    synchronized boolean isItemInDb(ComponentKey cacheKey) {
        return getEntryFromDBLocked(cacheKey, new CacheEntry(), DEFAULT_LOOKUP_FLAG,
                LauncherActivityCachingLogic.INSTANCE);
    }

    /**
     * Interface for receiving itemInfo with high-res icon.
     */
    public interface ItemInfoUpdateReceiver {

        void reapplyItemInfo(ItemInfoWithIcon info);
    }

    // Edited
    /* Log persistently to FileLog.d for debugging. */
    /*@Override
    protected void logPersistently(@NonNull String message, @Nullable Exception e) {
        FileLog.d(BaseIconCache.TAG, message, e);
    }*/
}

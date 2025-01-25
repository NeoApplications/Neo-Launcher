/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;
import static android.content.res.Resources.ID_NULL;
import static android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;

import com.android.launcher3.util.SafeCloseable;

import java.util.Calendar;
import java.util.function.Supplier;

/**
 * Class to handle icon loading from different packages
 */
public class IconProvider {

    private final String ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED";
    static final int CONFIG_ICON_MASK_RES_ID = Resources.getSystem().getIdentifier(
            "config_icon_mask", "string", "android");

    private static final String TAG = "IconProvider";
    private static final boolean DEBUG = false;
    public static final boolean ATLEAST_T = BuildCompat.isAtLeastT();

    private static final String ICON_METADATA_KEY_PREFIX = ".dynamic_icons";

    private static final String SYSTEM_STATE_SEPARATOR = " ";

    protected final Context mContext;
    private final ComponentName mCalendar;
    private final ComponentName mClock;

    public IconProvider(Context context) {
        mContext = context;
        mCalendar = parseComponentOrNull(context, R.string.calendar_component_name);
        mClock = parseComponentOrNull(context, R.string.clock_component_name);
    }

    /**
     * Adds any modification to the provided systemState for dynamic icons. This system state
     * is used by caches to check for icon invalidation.
     */
    public String getSystemStateForPackage(String systemState, String packageName) {
        if (mCalendar != null && mCalendar.getPackageName().equals(packageName)) {
            return systemState + SYSTEM_STATE_SEPARATOR + getDay();
        } else {
            return systemState;
        }
    }

    /**
     * Loads the icon for the provided LauncherActivityInfo
     */
    public Drawable getIcon(LauncherActivityInfo info, int iconDpi) {
        return getIconWithOverrides(info.getApplicationInfo().packageName, iconDpi,
                () -> info.getIcon(iconDpi));
    }

    /**
     * Loads the icon for the provided activity info
     */
    public Drawable getIcon(ActivityInfo info) {
        return getIcon(info, mContext.getResources().getConfiguration().densityDpi);
    }

    /**
     * Loads the icon for the provided activity info
     */
    public Drawable getIcon(ActivityInfo info, int iconDpi) {
        return getIconWithOverrides(info.applicationInfo.packageName, iconDpi,
                () -> loadActivityInfoIcon(info, iconDpi));
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private Drawable getIconWithOverrides(String packageName, int iconDpi,
            Supplier<Drawable> fallback) {
        ThemeData td = getThemeDataForPackage(packageName);

        Drawable icon = null;
        if (mCalendar != null && mCalendar.getPackageName().equals(packageName)) {
            icon = loadCalendarDrawable(iconDpi, td);
        } else if (mClock != null && mClock.getPackageName().equals(packageName)) {
            icon = ClockDrawableWrapper.forPackage(mContext, mClock.getPackageName(), iconDpi, td);
        }
        if (icon == null) {
            icon = fallback.get();
            if (ATLEAST_T && icon instanceof AdaptiveIconDrawable && td != null) {
                AdaptiveIconDrawable aid = (AdaptiveIconDrawable) icon;
                if  (aid.getMonochrome() == null) {
                    icon = new AdaptiveIconDrawable(aid.getBackground(),
                            aid.getForeground(), td.loadPaddedDrawable());
                }
            }
        }
        return icon;
    }

    protected ThemeData getThemeDataForPackage(String packageName) {
        return null;
    }

    private Drawable loadActivityInfoIcon(ActivityInfo ai, int density) {
        final int iconRes = ai.getIconResource();
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                final Resources resources = mContext.getPackageManager()
                        .getResourcesForApplication(ai.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (NameNotFoundException | Resources.NotFoundException exc) { }
        }
        // Get the default density icon
        if (icon == null) {
            icon = ai.loadIcon(mContext.getPackageManager());
        }
        return icon;
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private Drawable loadCalendarDrawable(int iconDpi, @Nullable ThemeData td) {
        PackageManager pm = mContext.getPackageManager();
        try {
            final Bundle metadata = pm.getActivityInfo(
                    mCalendar,
                    PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_META_DATA)
                    .metaData;
            final Resources resources = pm.getResourcesForApplication(mCalendar.getPackageName());
            final int id = getDynamicIconId(metadata, resources);
            if (id != ID_NULL) {
                if (DEBUG) Log.d(TAG, "Got icon #" + id);
                Drawable drawable = resources.getDrawableForDensity(id, iconDpi, null /* theme */);
                if (ATLEAST_T && drawable instanceof AdaptiveIconDrawable && td != null) {
                    AdaptiveIconDrawable aid = (AdaptiveIconDrawable) drawable;
                    if  (aid.getMonochrome() != null) {
                        return drawable;
                    }
                    if ("array".equals(td.mResources.getResourceTypeName(td.mResID))) {
                        TypedArray ta = td.mResources.obtainTypedArray(td.mResID);
                        int monoId = ta.getResourceId(IconProvider.getDay(), ID_NULL);
                        ta.recycle();
                        return monoId == ID_NULL ? drawable
                                : new AdaptiveIconDrawable(aid.getBackground(), aid.getForeground(),
                                        new ThemeData(td.mResources, monoId).loadPaddedDrawable());
                    }
                }
                return drawable;
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "Could not get activityinfo or resources for package: "
                        + mCalendar.getPackageName());
            }
        }
        return null;
    }

    /**
     * @param metadata metadata of the default activity of Calendar
     * @param resources from the Calendar package
     * @return the resource id for today's Calendar icon; 0 if resources cannot be found.
     */
    private int getDynamicIconId(Bundle metadata, Resources resources) {
        if (metadata == null) {
            return ID_NULL;
        }
        String key = mCalendar.getPackageName() + ICON_METADATA_KEY_PREFIX;
        final int arrayId = metadata.getInt(key, ID_NULL);
        if (arrayId == ID_NULL) {
            return ID_NULL;
        }
        try {
            return resources.obtainTypedArray(arrayId).getResourceId(getDay(), ID_NULL);
        } catch (Resources.NotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "package defines '" + key + "' but corresponding array not found");
            }
            return ID_NULL;
        }
    }

    /**
     * @return Today's day of the month, zero-indexed.
     */
    private static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
    }

    private static ComponentName parseComponentOrNull(Context context, int resId) {
        String cn = context.getString(resId);
        return TextUtils.isEmpty(cn) ? null : ComponentName.unflattenFromString(cn);
    }

    /**
     * Returns a string representation of the current system icon state
     */
    public String getSystemIconState() {
        return (CONFIG_ICON_MASK_RES_ID == ID_NULL
                ? "" : mContext.getResources().getString(CONFIG_ICON_MASK_RES_ID));
    }

    /**
     * Registers a callback to listen for various system dependent icon changes.
     */
    public SafeCloseable registerIconChangeListener(IconChangeListener listener, Handler handler) {
        return new IconChangeReceiver(listener, handler);
    }

    public static class ThemeData {

        final Resources mResources;
        final int mResID;

        public ThemeData(Resources resources, int resID) {
            mResources = resources;
            mResID = resID;
        }

        Drawable loadPaddedDrawable() {
            if (!"drawable".equals(mResources.getResourceTypeName(mResID))) {
                return null;
            }
            Drawable d = mResources.getDrawable(mResID).mutate();
            d = new InsetDrawable(d, .2f);
            float inset = getExtraInsetFraction() / (1 + 2 * getExtraInsetFraction());
            Drawable fg = new InsetDrawable(d, inset);
            return fg;
        }
    }

    private class IconChangeReceiver extends BroadcastReceiver implements SafeCloseable {

        private final IconChangeListener mCallback;
        private String mIconState;

        IconChangeReceiver(IconChangeListener callback, Handler handler) {
            mCallback = callback;
            mIconState = getSystemIconState();


            IntentFilter packageFilter = new IntentFilter(ACTION_OVERLAY_CHANGED);
            packageFilter.addDataScheme("package");
            packageFilter.addDataSchemeSpecificPart("android", PatternMatcher.PATTERN_LITERAL);
            mContext.registerReceiver(this, packageFilter, null, handler);

            if (mCalendar != null || mClock != null) {
                final IntentFilter filter = new IntentFilter(ACTION_TIMEZONE_CHANGED);
                if (mCalendar != null) {
                    filter.addAction(Intent.ACTION_TIME_CHANGED);
                    filter.addAction(ACTION_DATE_CHANGED);
                }
                mContext.registerReceiver(this, filter, null, handler);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_TIMEZONE_CHANGED:
                    if (mClock != null) {
                        mCallback.onAppIconChanged(mClock.getPackageName(), Process.myUserHandle());
                    }
                    // follow through
                case ACTION_DATE_CHANGED:
                case ACTION_TIME_CHANGED:
                    if (mCalendar != null) {
                        for (UserHandle user
                                : context.getSystemService(UserManager.class).getUserProfiles()) {
                            mCallback.onAppIconChanged(mCalendar.getPackageName(), user);
                        }
                    }
                    break;
                case ACTION_OVERLAY_CHANGED: {
                    String newState = getSystemIconState();
                    if (!mIconState.equals(newState)) {
                        mIconState = newState;
                        mCallback.onSystemIconStateChanged(mIconState);
                    }
                    break;
                }
            }
        }

        @Override
        public void close() {
            mContext.unregisterReceiver(this);
        }
    }

    /**
     * Listener for receiving icon changes
     */
    public interface IconChangeListener {

        /**
         * Called when the icon for a particular app changes
         */
        void onAppIconChanged(String packageName, UserHandle user);

        /**
         * Called when the global icon state changed, which can typically affect all icons
         */
        void onSystemIconStateChanged(String iconState);
    }
}

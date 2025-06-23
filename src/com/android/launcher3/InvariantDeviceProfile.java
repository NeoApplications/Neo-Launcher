/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.launcher3;

import static com.android.launcher3.GridType.GRID_TYPE_ANY;
import static com.android.launcher3.GridType.GRID_TYPE_NON_ONE_GRID;
import static com.android.launcher3.GridType.GRID_TYPE_ONE_GRID;
import static com.android.launcher3.LauncherPrefs.DB_FILE;
import static com.android.launcher3.LauncherPrefs.ENABLE_TWOLINE_ALLAPPS_TOGGLE;
import static com.android.launcher3.LauncherPrefs.FIXED_LANDSCAPE_MODE;
import static com.android.launcher3.LauncherPrefs.GRID_NAME;
import static com.android.launcher3.LauncherPrefs.NON_FIXED_LANDSCAPE_GRID_NAME;
import static com.android.launcher3.Utilities.dpiFromPx;
import static com.android.launcher3.testing.shared.ResourceUtils.INVALID_RESOURCE_HANDLE;
import static com.android.launcher3.util.DisplayController.CHANGE_DENSITY;
import static com.android.launcher3.util.DisplayController.CHANGE_DESKTOP_MODE;
import static com.android.launcher3.util.DisplayController.CHANGE_NAVIGATION_MODE;
import static com.android.launcher3.util.DisplayController.CHANGE_SUPPORTED_BOUNDS;
import static com.android.launcher3.util.DisplayController.CHANGE_TASKBAR_PINNING;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Trace;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import androidx.annotation.DimenRes;
import androidx.annotation.IntDef;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.XmlRes;
import androidx.core.content.res.ResourcesCompat;

import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dagger.ApplicationContext;
import com.android.launcher3.dagger.LauncherAppComponent;
import com.android.launcher3.dagger.LauncherAppSingleton;
import com.android.launcher3.graphics.ThemeManager;
import com.android.launcher3.icons.DotRenderer;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.model.DeviceGridState;
import com.android.launcher3.provider.RestoreDbTask;
import com.android.launcher3.testing.shared.ResourceUtils;
import com.android.launcher3.util.DaggerSingletonObject;
import com.android.launcher3.util.DaggerSingletonTracker;
import com.android.launcher3.util.DisplayController;
import com.android.launcher3.util.DisplayController.Info;
import com.android.launcher3.util.Partner;
import com.android.launcher3.util.ResourceHelper;
import com.android.launcher3.util.SimpleBroadcastReceiver;
import com.android.launcher3.util.WindowBounds;
import com.android.launcher3.util.window.CachedDisplayInfo;
import com.android.launcher3.util.window.WindowManagerProxy;
import com.saggitt.omega.DeviceProfileOverrides;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.inject.Inject;

@LauncherAppSingleton
public class InvariantDeviceProfile {

    public static final String TAG = "IDP";
    // We do not need any synchronization for this variable as its only written on UI thread.
    public static final DaggerSingletonObject<InvariantDeviceProfile> INSTANCE =
            new DaggerSingletonObject<>(LauncherAppComponent::getIDP);

    public static final String GRID_NAME_PREFS_KEY = "idp_grid_name";
    public static final String NON_FIXED_LANDSCAPE_GRID_NAME_PREFS_KEY =
            "idp_non_fixed_landscape_grid_name";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_PHONE, TYPE_MULTI_DISPLAY, TYPE_TABLET})
    public @interface DeviceType {
    }

    public static final int TYPE_PHONE = 0;
    public static final int TYPE_MULTI_DISPLAY = 1;
    public static final int TYPE_TABLET = 2;

    private static final float ICON_SIZE_DEFINED_IN_APP_DP = 48;

    // Constants that affects the interpolation curve between statically defined device profile
    // buckets.
    private static final float KNEARESTNEIGHBOR = 3;
    private static final float WEIGHT_POWER = 5;

    // used to offset float not being able to express extremely small weights in extreme cases.
    private static final float WEIGHT_EFFICIENT = 100000f;

    // Used for arrays to specify different sizes (e.g. border spaces, width/height) in different
    // constraints
    static final int COUNT_SIZES = 4;
    public static final int INDEX_DEFAULT = 0;
    public static final int INDEX_LANDSCAPE = 1;
    public static final int INDEX_TWO_PANEL_PORTRAIT = 2;
    public static final int INDEX_TWO_PANEL_LANDSCAPE = 3;

    /** These resources are used to override the device profile */
    private static final String RES_GRID_NUM_ROWS = "grid_num_rows";
    private static final String RES_GRID_NUM_COLUMNS = "grid_num_columns";
    private static final String RES_GRID_ICON_SIZE_DP = "grid_icon_size_dp";

    private final DisplayController mDisplayController;
    private final WindowManagerProxy mWMProxy;
    private final LauncherPrefs mPrefs;
    private final ThemeManager mThemeManager;

    /**
     * Number of icons per row and column in the workspace.
     */
    public int numRows;
    public int numRowsOriginal;
    public int numColumns;
    public int numColumnsOriginal;
    /**
     * Number of icons inside the hotseat area.
     */
    public int numHotseatIcons;
    public int numHotseatIconsOriginal;

    public int numSearchContainerColumns;

    /**
     * Number of icons per row and column in the folder.
     */
    public int[] numFolderRows;
    public int[] numFolderColumns;
    public float[] iconSize;
    public float[] iconTextSize;
    public int iconBitmapSize;
    public int fillResIconDpi;
    public @DeviceType int deviceType;
    public Info displayInfo;

    public PointF[] minCellSize;

    public PointF[] borderSpaces;
    public @DimenRes int inlineNavButtonsEndSpacing;

    public @StyleRes int folderStyle;

    public @StyleRes int cellStyle;

    public float[] horizontalMargin;

    public PointF[] allAppsCellSize;
    public float[] allAppsIconSize;
    public float[] allAppsIconTextSize;
    public PointF[] allAppsBorderSpaces;

    public float[] transientTaskbarIconSize;

    public boolean[] startAlignTaskbar;

    /**
     * Number of icons inside the hotseat area.
     */
    public int numShownHotseatIcons;

    /**
     * Number of icons inside the hotseat area that is stored in the database. This is greater than
     * or equal to numnShownHotseatIcons, allowing for a seamless transition between two hotseat
     * sizes that share the same DB.
     */
    public int numDatabaseHotseatIcons;

    public float[] hotseatBarBottomSpace;
    public float[] hotseatQsbSpace;

    /**
     * Number of columns in the all apps list.
     */
    public int numAllAppsColumns;
    public int numAllAppsRowsForCellHeightCalculation;
    public int numDatabaseAllAppsColumns;
    public @StyleRes int allAppsStyle;
    // Edited
    public int numAllAppsColumnsOriginal;

    /**
     * Do not query directly. see {@link DeviceProfile#isScalableGrid}.
     */
    protected boolean isScalable;
    @XmlRes
    public int devicePaddingId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int workspaceSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int gridSizeSpecsId = INVALID_RESOURCE_HANDLE;;
    @XmlRes
    public int workspaceSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int allAppsSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int allAppsSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int folderSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int folderSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int hotseatSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int hotseatSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int workspaceCellSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int workspaceCellSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int allAppsCellSpecsId = INVALID_RESOURCE_HANDLE;
    @XmlRes
    public int allAppsCellSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;

    private String mLocale = "";
    public boolean enableTwoLinesInAllApps = false;
    /**
     * Fixed landscape mode is the landscape on the phones.
     */
    public boolean isFixedLandscape = false;

    @GridType
    public int gridType;
    public String dbFile;
    public int defaultLayoutId;
    public int demoModeLayoutId;
    public boolean[] inlineQsb = new boolean[COUNT_SIZES];

    /**
     * An immutable list of supported profiles.
     */
    public List<DeviceProfile> supportedProfiles = Collections.EMPTY_LIST;

    public Point defaultWallpaperSize;

    private final List<OnIDPChangeListener> mChangeListeners = new CopyOnWriteArrayList<>();

    @Inject
    InvariantDeviceProfile(
            @ApplicationContext Context context,
            LauncherPrefs prefs,
            DisplayController dc,
            WindowManagerProxy wmProxy,
            ThemeManager themeManager,
            DaggerSingletonTracker lifeCycle) {
        mDisplayController = dc;
        mWMProxy = wmProxy;
        mPrefs = prefs;
        mThemeManager = themeManager;

        String gridName = prefs.get(GRID_NAME);
        initGrid(context, gridName);

        dc.setPriorityListener(
                (displayContext, info, flags) -> {
                    if ((flags & (CHANGE_DENSITY | CHANGE_SUPPORTED_BOUNDS
                            | CHANGE_NAVIGATION_MODE | CHANGE_TASKBAR_PINNING
                            | CHANGE_DESKTOP_MODE)) != 0) {
                        onConfigChanged(displayContext);
                    }
                });
        lifeCycle.addCloseable(() -> dc.setPriorityListener(null));

        LauncherPrefChangeListener prefListener = key -> {
            if (FIXED_LANDSCAPE_MODE.getSharedPrefKey().equals(key)
                    && isFixedLandscape != prefs.get(FIXED_LANDSCAPE_MODE)) {
                Trace.beginSection("InvariantDeviceProfile#setFixedLandscape");
                if (isFixedLandscape) {
                    setCurrentGrid(context, prefs.get(NON_FIXED_LANDSCAPE_GRID_NAME));
                } else {
                    prefs.put(NON_FIXED_LANDSCAPE_GRID_NAME, mPrefs.get(GRID_NAME));
                    onConfigChanged(context);
                }
                Trace.endSection();
            } else if (ENABLE_TWOLINE_ALLAPPS_TOGGLE.getSharedPrefKey().equals(key)
                    && enableTwoLinesInAllApps != prefs.get(ENABLE_TWOLINE_ALLAPPS_TOGGLE)) {
                onConfigChanged(context);
            }
        };
        prefs.addListener(prefListener, FIXED_LANDSCAPE_MODE, ENABLE_TWOLINE_ALLAPPS_TOGGLE);
        lifeCycle.addCloseable(() -> prefs.removeListener(prefListener,
                FIXED_LANDSCAPE_MODE, ENABLE_TWOLINE_ALLAPPS_TOGGLE));

        SimpleBroadcastReceiver localeReceiver = new SimpleBroadcastReceiver(context,
                MAIN_EXECUTOR, i -> onConfigChanged(context));
        localeReceiver.register(Intent.ACTION_LOCALE_CHANGED);
        lifeCycle.addCloseable(() -> localeReceiver.unregisterReceiverSafely());
    }

    // Edited
    private String initGrid(Context context, String gridName, DeviceProfileOverrides.DBGridInfo dbGridInfo) {
        Info displayInfo = mDisplayController.getInfo();
        List<DisplayOption> allOptions = getPredefinedDeviceProfiles(
                context,
                gridName,
                displayInfo,
                (RestoreDbTask.isPending(mPrefs) && !Flags.oneGridSpecs()),
                mPrefs.get(FIXED_LANDSCAPE_MODE)
        );

        // Filter out options that don't have the same number of columns as the grid
        DeviceGridState deviceGridState = new DeviceGridState(mPrefs);
        List<DisplayOption> allOptionsFilteredByColCount =
                filterByColumnCount(allOptions, deviceGridState.getColumns());

        DisplayOption displayOption =
                invDistWeightedInterpolate(displayInfo, allOptionsFilteredByColCount.isEmpty()
                                ? new ArrayList<>(allOptions)
                                : new ArrayList<>(allOptionsFilteredByColCount),
                        displayInfo.getDeviceType());

        if (!displayOption.grid.name.equals(gridName)) {
            mPrefs.put(GRID_NAME, displayOption.grid.name);
        }

        initGrid(context, displayInfo, displayOption);
        FileLog.d(TAG, "After initGrid:"
                + "gridName:" + gridName
                + ", dbFile:" + dbFile
                + ", LauncherPrefs GRID_NAME:" + mPrefs.get(GRID_NAME)
                + ", LauncherPrefs DB_FILE:" + mPrefs.get(DB_FILE));
        return displayOption.grid.name;
    }

    private List<DisplayOption> filterByColumnCount(
            List<DisplayOption> allOptions, int numColumns) {
        return allOptions.stream()
                .filter(option -> option.grid.numColumns == numColumns)
                .collect(Collectors.toList());
    }

    /**
     * @deprecated This is a temporary solution because on the backup and restore case we modify the
     * IDP, this resets it. b/332974074
     */
    @Deprecated
    public void reset(Context context) {
        initGrid(context, mPrefs.get(GRID_NAME));
    }

    // Edited
    private String initGrid(Context context, String gridName) {
        DeviceProfileOverrides dpo = DeviceProfileOverrides.INSTANCE.get(context);
        return initGrid(context, dpo.getCurrentGridName(), dpo.getGridInfo());
    }

    // Edited
    // TODO review & clean up handling of overrides (dbGridInfo vs. overrideOptions vs. closestProfile)
    private void initGrid(Context context, Info displayInfo, DisplayOption displayOption,
            DeviceProfileOverrides.DBGridInfo dbGridInfo) {
        enableTwoLinesInAllApps = Flags.enableTwolineToggle()
                && Utilities.isEnglishLanguage(context)
                && mPrefs.get(ENABLE_TWOLINE_ALLAPPS_TOGGLE);
        mLocale = context.getResources().getConfiguration().locale.toString();

        DeviceProfileOverrides.Options overrideOptions = DeviceProfileOverrides.INSTANCE.get(context)
                .getOverrides(displayOption.grid);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        GridOption closestProfile = displayOption.grid;
        numRows = dbGridInfo.getNumRows();
        numRowsOriginal = numRows;
        numColumns = dbGridInfo.getNumColumns();
        numColumnsOriginal = numColumns;
        numSearchContainerColumns = dbGridInfo.getNumColumns();
        numHotseatIcons = dbGridInfo.getNumHotseatIcons();
        numHotseatIconsOriginal = numHotseatIcons;
        dbFile = dbGridInfo.getDbFile();
        gridType = closestProfile.gridType;
        defaultLayoutId = closestProfile.defaultLayoutId;
        demoModeLayoutId = closestProfile.demoModeLayoutId;

        numFolderRows = closestProfile.numFolderRows;
        numFolderColumns = closestProfile.numFolderColumns;
        folderStyle = closestProfile.folderStyle;

        cellStyle = closestProfile.cellStyle;

        isScalable = closestProfile.isScalable;
        devicePaddingId = closestProfile.devicePaddingId;
        workspaceSpecsId = closestProfile.mWorkspaceSpecsId;
        gridSizeSpecsId = closestProfile.mGridSizeSpecsId;
        workspaceSpecsTwoPanelId = closestProfile.mWorkspaceSpecsTwoPanelId;
        allAppsSpecsId = closestProfile.mAllAppsSpecsId;
        allAppsSpecsTwoPanelId = closestProfile.mAllAppsSpecsTwoPanelId;
        folderSpecsId = closestProfile.mFolderSpecsId;
        folderSpecsTwoPanelId = closestProfile.mFolderSpecsTwoPanelId;
        hotseatSpecsId = closestProfile.mHotseatSpecsId;
        hotseatSpecsTwoPanelId = closestProfile.mHotseatSpecsTwoPanelId;
        workspaceCellSpecsId = closestProfile.mWorkspaceCellSpecsId;
        workspaceCellSpecsTwoPanelId = closestProfile.mWorkspaceCellSpecsTwoPanelId;
        allAppsCellSpecsId = closestProfile.mAllAppsCellSpecsId;
        allAppsCellSpecsTwoPanelId = closestProfile.mAllAppsCellSpecsTwoPanelId;
        numAllAppsRowsForCellHeightCalculation =
                closestProfile.mNumAllAppsRowsForCellHeightCalculation;
        this.deviceType = displayInfo.getDeviceType();
        this.displayInfo = displayInfo;

        inlineNavButtonsEndSpacing = closestProfile.inlineNavButtonsEndSpacing;

        iconSize = displayOption.iconSizes;
        float maxIconSize = iconSize[0];
        for (int i = 1; i < iconSize.length; i++) {
            maxIconSize = Math.max(maxIconSize, iconSize[i]);
        }
        iconBitmapSize = ResourceUtils.pxFromDp(maxIconSize, metrics);
        fillResIconDpi = getLauncherIconDensity(iconBitmapSize);

        iconTextSize = displayOption.textSizes;

        minCellSize = displayOption.minCellSize;

        borderSpaces = displayOption.borderSpaces;

        horizontalMargin = displayOption.horizontalMargin;

        numShownHotseatIcons = dbGridInfo.getNumHotseatIcons();
        numDatabaseHotseatIcons = deviceType == TYPE_MULTI_DISPLAY
                ? closestProfile.numDatabaseHotseatIcons : numShownHotseatIcons;
        hotseatBarBottomSpace = displayOption.hotseatBarBottomSpace;
        hotseatQsbSpace = displayOption.hotseatQsbSpace;

        allAppsStyle = closestProfile.allAppsStyle;

        numAllAppsColumns = closestProfile.numAllAppsColumns;
        numAllAppsColumnsOriginal = numAllAppsColumns;

        numDatabaseAllAppsColumns = deviceType == TYPE_MULTI_DISPLAY
                ? closestProfile.numDatabaseAllAppsColumns : closestProfile.numAllAppsColumns;

        allAppsCellSize = displayOption.allAppsCellSize;
        allAppsBorderSpaces = displayOption.allAppsBorderSpaces;
        allAppsIconSize = displayOption.allAppsIconSizes;
        allAppsIconTextSize = displayOption.allAppsIconTextSizes;

        inlineQsb = closestProfile.inlineQsb;

        transientTaskbarIconSize = displayOption.transientTaskbarIconSize;

        startAlignTaskbar = displayOption.startAlignTaskbar;

        // Fixed Landscape mode
        isFixedLandscape = closestProfile.mIsFixedLandscape;

        // If the partner customization apk contains any grid overrides, apply them
        // Supported overrides: numRows, numColumns, iconSize
        applyPartnerDeviceProfileOverrides(context, metrics);

        overrideOptions.applyUi(this);

        final List<DeviceProfile> localSupportedProfiles = new ArrayList<>();
        defaultWallpaperSize = new Point(displayInfo.currentSize);
        SparseArray<DotRenderer> dotRendererCache = new SparseArray<>();
        for (WindowBounds bounds : displayInfo.supportedBounds) {
            localSupportedProfiles.add(newDPBuilder(context, displayInfo)
                    .setIsMultiDisplay(deviceType == TYPE_MULTI_DISPLAY)
                    .setWindowBounds(bounds)
                    .setDotRendererCache(dotRendererCache)
                    .build());

            // Wallpaper size should be the maximum of the all possible sizes Launcher expects
            int displayWidth = bounds.bounds.width();
            int displayHeight = bounds.bounds.height();
            defaultWallpaperSize.y = Math.max(defaultWallpaperSize.y, displayHeight);

            // We need to ensure that there is enough extra space in the wallpaper
            // for the intended parallax effects
            float parallaxFactor =
                    dpiFromPx(Math.min(displayWidth, displayHeight), displayInfo.getDensityDpi())
                            < 720
                            ? 2
                            : wallpaperTravelToScreenWidthRatio(displayWidth, displayHeight);
            defaultWallpaperSize.x =
                    Math.max(defaultWallpaperSize.x, Math.round(parallaxFactor * displayWidth));
        }
        supportedProfiles = Collections.unmodifiableList(localSupportedProfiles);

        int numMinShownHotseatIconsForTablet = supportedProfiles
                .stream()
                .filter(deviceProfile -> deviceProfile.isTablet)
                .mapToInt(deviceProfile -> deviceProfile.numShownHotseatIcons)
                .min()
                .orElse(0);

        supportedProfiles
                .stream()
                .filter(deviceProfile -> deviceProfile.isTablet)
                .forEach(deviceProfile -> {
                    deviceProfile.numShownHotseatIcons = numMinShownHotseatIconsForTablet;
                    deviceProfile.recalculateHotseatWidthAndBorderSpace();
                });
    }

    DeviceProfile.Builder newDPBuilder(Context context, Info info) {
        return new DeviceProfile.Builder(context, this, info, mWMProxy, mThemeManager);
    }

    public void addOnChangeListener(OnIDPChangeListener listener) {
        mChangeListeners.add(listener);
    }

    public void removeOnChangeListener(OnIDPChangeListener listener) {
        mChangeListeners.remove(listener);
    }


    // Edited
    /**
     * Updates the current grid, this triggers a new IDP, reloads the database and triggers a grid
     * migration.
     */
    @VisibleForTesting
    public void setCurrentGrid(Context context, String newGridName) {
        mPrefs.put(GRID_NAME, newGridName);
        DeviceProfileOverrides.INSTANCE.get(context).setCurrentGrid(newGridName);
        MAIN_EXECUTOR.execute(() -> {
            Trace.beginSection("InvariantDeviceProfile#setCurrentGrid");
            onConfigChanged(context.getApplicationContext());
            Trace.endSection();
        });
    }

    private Object[] toModelState() {
        return new Object[]{
                numColumns, numRows, numSearchContainerColumns, numDatabaseHotseatIcons,
                iconBitmapSize, fillResIconDpi, numDatabaseAllAppsColumns, dbFile, mLocale};
    }

    // Edited
    public void onPreferencesChanged(Context context) {
        Context appContext = context.getApplicationContext();
        MAIN_EXECUTOR.execute(() -> onConfigChanged(appContext));
    }

    // Edited
    /** Updates IDP using the provided context. Notifies listeners of change. */
    @VisibleForTesting
    public void onConfigChanged(Context context) {
        Object[] oldState = toModelState();

        // Re-init grid
        initGrid(context, "");

        boolean modelPropsChanged = !Arrays.equals(oldState, toModelState());
        for (OnIDPChangeListener listener : mChangeListeners) {
            listener.onIdpChanged(modelPropsChanged);
        }
    }

    private static boolean firstGridFilter(GridOption gridOption, int deviceType,
                                           boolean allowDisabledGrid, boolean isFixedLandscapeMode) {
        return (gridOption.isEnabled(deviceType) || allowDisabledGrid)
                && gridOption.filterByFlag(deviceType, isFixedLandscapeMode);
    }

    private static List<DisplayOption> getPredefinedDeviceProfiles(
            Context context,
            String gridName,
            Info displayInfo,
            boolean allowDisabledGrid,
            boolean isFixedLandscapeMode
    ) {
        ArrayList<DisplayOption> profiles = new ArrayList<>();

        try (XmlResourceParser parser = context.getResources().getXml(R.xml.device_profiles)) {
            final int depth = parser.getDepth();
            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if ((type == XmlPullParser.START_TAG)
                        && GridOption.TAG_NAME.equals(parser.getName())) {
                    GridOption gridOption = new GridOption(
                            context, Xml.asAttributeSet(parser), displayInfo);
                    if (firstGridFilter(gridOption, displayInfo.getDeviceType(), allowDisabledGrid,
                            isFixedLandscapeMode)) {
                        final int displayDepth = parser.getDepth();
                        while (((type = parser.next()) != XmlPullParser.END_TAG
                                || parser.getDepth() > displayDepth)
                                && type != XmlPullParser.END_DOCUMENT) {
                            if ((type == XmlPullParser.START_TAG) && "display-option".equals(
                                    parser.getName())) {
                                profiles.add(new DisplayOption(gridOption, context,
                                        Xml.asAttributeSet(parser)));
                            }
                        }
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
        ArrayList<DisplayOption> filteredProfiles = new ArrayList<>();
        if (!TextUtils.isEmpty(gridName)) {
            for (DisplayOption option : profiles) {
                if (gridName.equals(option.grid.name) && (option.grid.isEnabled(
                        displayInfo.getDeviceType()) || allowDisabledGrid)) {
                    filteredProfiles.add(option);
                }
            }
        }
        if (filteredProfiles.isEmpty() && TextUtils.isEmpty(gridName)) {
            // Use the default options since gridName is empty and there's no valid grids.
            for (DisplayOption option : profiles) {
                if (option.canBeDefault) {
                    filteredProfiles.add(option);
                }
            }
        } else if (filteredProfiles.isEmpty()) {
            // In this case we had a grid selected but we couldn't find it.
            filteredProfiles.addAll(profiles);
        }
        if (filteredProfiles.isEmpty()) {
            throw new RuntimeException("No display option with canBeDefault=true");
        }
        return filteredProfiles;
    }

    /**
     * Parses through the xml to find GridSize specs. Then calls findBestGridSize to get the
     * correct grid size for this GridOption.
     *
     * @return the result of {@link #findBestGridSize(List, int, int)}.
     */
    private static GridSize getGridSize(ResourceHelper resourceHelper, Context context,
                                        Info displayInfo) {
        ArrayList<GridSize> gridSizes = new ArrayList<>();

        try (XmlResourceParser parser = resourceHelper.getXml()) {
            final int depth = parser.getDepth();
            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG
                    || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if ((type == XmlPullParser.START_TAG)
                        && "GridSize".equals(parser.getName())) {
                    gridSizes.add(new GridSize(context, Xml.asAttributeSet(parser)));
                }
            }
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }

        // Finds the min width and height in dp for all displays.
        int[] dimens = findMinWidthAndHeightPxForDevice(displayInfo);

        return findBestGridSize(gridSizes, dimens[0], dimens[1]);
    }

    /**
     * @return the biggest grid size that fits the display dimensions.
     * If no best grid size is found, return null.
     */
    private static GridSize findBestGridSize(List<GridSize> list, int minWidthPx,
                                             int minHeightPx) {
        GridSize selectedGridSize = null;
        for (GridSize item: list) {
            if (minWidthPx >= item.mMinDeviceWidthPx && minHeightPx >= item.mMinDeviceHeightPx) {
                if (selectedGridSize == null
                        || (selectedGridSize.mNumColumns <= item.mNumColumns
                        && selectedGridSize.mNumRows <= item.mNumRows)) {
                    selectedGridSize = item;
                }
            }
        }
        return selectedGridSize;
    }

    private static int[] findMinWidthAndHeightPxForDevice(Info displayInfo) {
        int minDisplayWidthPx = Integer.MAX_VALUE;
        int minDisplayHeightPx = Integer.MAX_VALUE;
        for (CachedDisplayInfo display: displayInfo.getAllDisplays()) {
            minDisplayWidthPx = Math.min(minDisplayWidthPx, display.size.x);
            minDisplayHeightPx = Math.min(minDisplayHeightPx, display.size.y);
        }
        return new int[]{minDisplayWidthPx, minDisplayHeightPx};
    }

    /**
     * Returns the GridOption associated to the given file name or null if the fileName is not
     * supported.
     * Ej, launcher.db -> "normal grid", launcher_4_by_4.db -> "practical grid"
     */
    public GridOption getGridOptionFromFileName(Context context, String fileName) {
        return parseAllGridOptions(context).stream()
                .filter(gridOption -> Objects.equals(gridOption.dbFile, fileName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the name of the given size on the current device or empty string if the size is not
     * supported. Ej. 4x4 -> normal, 5x4 -> practical, etc.
     * (Note: the name of the grid can be different for the same grid size depending of
     * the values of the InvariantDeviceProfile)
     */
    public String getGridNameFromSize(Context context, Point size) {
        return parseAllGridOptions(context).stream()
                .filter(gridOption -> gridOption.numColumns == size.x
                        && gridOption.numRows == size.y)
                .map(gridOption -> gridOption.name)
                .findFirst()
                .orElse("");
    }

    /**
     * Returns the grid option for the given gridName on the current device (Note: the gridOption
     * be different for the same gridName depending on the values of the InvariantDeviceProfile).
     */
    public GridOption getGridOptionFromName(Context context, String gridName) {
        return parseAllGridOptions(context).stream()
                .filter(gridOption -> Objects.equals(gridOption.name, gridName))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return all the grid options that can be shown on the device
     */
    public List<GridOption> parseAllGridOptions(Context context) {
        return parseAllDefinedGridOptions(context, displayInfo)
                .stream()
                .filter(go -> go.isEnabled(deviceType))
                .filter(go -> go.filterByFlag(deviceType, isFixedLandscape))
                .collect(Collectors.toList());
    }

    /**
     * @return all the grid options that can be shown on the device
     */
    public static List<GridOption> parseAllDefinedGridOptions(Context context, Info displayInfo) {
        List<GridOption> result = new ArrayList<>();
        try (XmlResourceParser parser = context.getResources().getXml(R.xml.device_profiles)) {
            final int depth = parser.getDepth();
            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG
                    || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if ((type == XmlPullParser.START_TAG)
                        && GridOption.TAG_NAME.equals(parser.getName())) {
                    result.add(new GridOption(context, Xml.asAttributeSet(parser), displayInfo));
                }
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Error parsing device profile", e);
            return Collections.emptyList();
        }
        return result;
    }

    private int getLauncherIconDensity(int requiredSize) {
        // Densities typically defined by an app.
        int[] densityBuckets = new int[]{
                DisplayMetrics.DENSITY_LOW,
                DisplayMetrics.DENSITY_MEDIUM,
                DisplayMetrics.DENSITY_TV,
                DisplayMetrics.DENSITY_HIGH,
                DisplayMetrics.DENSITY_XHIGH,
                DisplayMetrics.DENSITY_XXHIGH,
                DisplayMetrics.DENSITY_XXXHIGH
        };

        int density = DisplayMetrics.DENSITY_XXXHIGH;
        for (int i = densityBuckets.length - 1; i >= 0; i--) {
            float expectedSize = ICON_SIZE_DEFINED_IN_APP_DP * densityBuckets[i]
                    / DisplayMetrics.DENSITY_DEFAULT;
            if (expectedSize >= requiredSize) {
                density = densityBuckets[i];
            }
        }

        return density;
    }

    /**
     * Apply any Partner customization grid overrides.
     *
     * Currently we support: all apps row / column count.
     */
    private void applyPartnerDeviceProfileOverrides(Context context, DisplayMetrics dm) {
        Partner p = Partner.get(context.getPackageManager());
        if (p == null) {
            return;
        }
        try {
            int numRows = p.getIntValue(RES_GRID_NUM_ROWS, -1);
            int numColumns = p.getIntValue(RES_GRID_NUM_COLUMNS, -1);
            float iconSizePx = p.getDimenValue(RES_GRID_ICON_SIZE_DP, -1);

            if (numRows > 0 && numColumns > 0) {
                this.numRows = numRows;
                this.numColumns = numColumns;
            }
            if (iconSizePx > 0) {
                this.iconSize[InvariantDeviceProfile.INDEX_DEFAULT] =
                        Utilities.dpiFromPx(iconSizePx, dm.densityDpi);
            }
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "Invalid Partner grid resource!", ex);
        }
    }

    private static float dist(float x0, float y0, float x1, float y1) {
        return (float) Math.hypot(x1 - x0, y1 - y0);
    }

    private static DisplayOption invDistWeightedInterpolate(
            Info displayInfo, List<DisplayOption> points, @DeviceType int deviceType) {
        int minWidthPx = Integer.MAX_VALUE;
        int minHeightPx = Integer.MAX_VALUE;
        for (WindowBounds bounds : displayInfo.supportedBounds) {
            boolean isTablet = displayInfo.isTablet(bounds);
            if (isTablet && deviceType == TYPE_MULTI_DISPLAY) {
                // For split displays, take half width per page
                minWidthPx = Math.min(minWidthPx, bounds.availableSize.x / 2);
                minHeightPx = Math.min(minHeightPx, bounds.availableSize.y);

            } else if (!isTablet && bounds.isLandscape()) {
                // We will use transposed layout in this case
                minWidthPx = Math.min(minWidthPx, bounds.availableSize.y);
                minHeightPx = Math.min(minHeightPx, bounds.availableSize.x);
            } else {
                minWidthPx = Math.min(minWidthPx, bounds.availableSize.x);
                minHeightPx = Math.min(minHeightPx, bounds.availableSize.y);
            }
        }

        float width = dpiFromPx(minWidthPx, displayInfo.getDensityDpi());
        float height = dpiFromPx(minHeightPx, displayInfo.getDensityDpi());

        // Sort the profiles based on the closeness to the device size
        points.sort((a, b) ->
                Float.compare(dist(width, height, a.minWidthDps, a.minHeightDps),
                        dist(width, height, b.minWidthDps, b.minHeightDps)));

        DisplayOption closestPoint = points.get(0);
        GridOption closestOption = closestPoint.grid;
        float weights = 0;

        if (dist(width, height, closestPoint.minWidthDps, closestPoint.minHeightDps) == 0) {
            return closestPoint;
        }

        DisplayOption out = new DisplayOption(closestOption);
        for (int i = 0; i < points.size() && i < KNEARESTNEIGHBOR; ++i) {
            DisplayOption p = points.get(i);
            float w = weight(width, height, p.minWidthDps, p.minHeightDps, WEIGHT_POWER);
            weights += w;
            out.add(new DisplayOption().add(p).multiply(w));
        }
        out.multiply(1.0f / weights);

        // Since the bitmaps are persisted, ensure that all bitmap sizes are not larger than
        // predefined size to avoid cache invalidation
        for (int i = INDEX_DEFAULT; i < COUNT_SIZES; i++) {
            out.iconSizes[i] = Math.min(out.iconSizes[i], closestPoint.iconSizes[i]);
        }

        return out;
    }

    public DeviceProfile createDeviceProfileForSecondaryDisplay(Context displayContext) {
        // Disable transpose layout and use multi-window mode so that the icons are scaled properly
        return newDPBuilder(displayContext, new Info(displayContext))
                .setIsMultiDisplay(false)
                .setMultiWindowMode(true)
                .setWindowBounds(mWMProxy.getRealBounds(
                        displayContext, mWMProxy.getDisplayInfo(displayContext)))
                .setTransposeLayoutWithOrientation(false)
                .build();
    }

    public DeviceProfile getDeviceProfile(Context context) {
        Rect bounds = mWMProxy.getCurrentBounds(context);
        int rotation = mWMProxy.getRotation(context);
        return getBestMatch(bounds.width(), bounds.height(), rotation);
    }

    /**
     * Returns the device profile matching the provided screen configuration
     */
    public DeviceProfile getBestMatch(float screenWidth, float screenHeight, int rotation) {
        DeviceProfile bestMatch = supportedProfiles.get(0);
        float minDiff = Float.MAX_VALUE;

        for (DeviceProfile profile : supportedProfiles) {
            float diff = Math.abs(profile.widthPx - screenWidth)
                    + Math.abs(profile.heightPx - screenHeight);
            if (diff < minDiff) {
                minDiff = diff;
                bestMatch = profile;
            } else if (diff == minDiff && profile.rotationHint == rotation) {
                bestMatch = profile;
            }
        }
        return bestMatch;
    }

    private static float weight(float x0, float y0, float x1, float y1, float pow) {
        float d = dist(x0, y0, x1, y1);
        if (Float.compare(d, 0f) == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (WEIGHT_EFFICIENT / Math.pow(d, pow));
    }

    /**
     * As a ratio of screen height, the total distance we want the parallax effect to span
     * horizontally
     */
    private static float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16 / 10f;
        final float ASPECT_RATIO_PORTRAIT = 10 / 16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
                (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE
                        - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
                        (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    public interface OnIDPChangeListener {

        /**
         * Called when the device provide changes
         */
        void onIdpChanged(boolean modelPropertiesChanged);
    }


    public static final class GridOption {

        public static final String TAG_NAME = "grid-option";

        private static final int DEVICE_CATEGORY_PHONE = 1 << 0;
        private static final int DEVICE_CATEGORY_TABLET = 1 << 1;
        private static final int DEVICE_CATEGORY_MULTI_DISPLAY = 1 << 2;
        private static final int DEVICE_CATEGORY_ANY =
                DEVICE_CATEGORY_PHONE | DEVICE_CATEGORY_TABLET | DEVICE_CATEGORY_MULTI_DISPLAY;

        private static final int INLINE_QSB_FOR_PORTRAIT = 1 << 0;
        private static final int INLINE_QSB_FOR_LANDSCAPE = 1 << 1;
        private static final int INLINE_QSB_FOR_TWO_PANEL_PORTRAIT = 1 << 2;
        private static final int INLINE_QSB_FOR_TWO_PANEL_LANDSCAPE = 1 << 3;
        private static final int DONT_INLINE_QSB = 0;

        public final String name;
        public final String gridTitle;
        public final int gridIconId;
        public final int numRows;
        public final int numColumns;
        public final int numSearchContainerColumns;
        public final int deviceCategory;
        @GridType
        public final int gridType;

        private final int[] numFolderRows = new int[COUNT_SIZES];
        private final int[] numFolderColumns = new int[COUNT_SIZES];
        private final @StyleRes int folderStyle;
        private final @StyleRes int cellStyle;

        private final @StyleRes int allAppsStyle;
        public final int numAllAppsColumns;
        private final int mNumAllAppsRowsForCellHeightCalculation;
        private final int numDatabaseAllAppsColumns;
        public final int numHotseatIcons;
        public final int numHotseatRows;
        private final int numDatabaseHotseatIcons;

        private final boolean[] inlineQsb = new boolean[COUNT_SIZES];

        private @DimenRes int inlineNavButtonsEndSpacing;
        private final String dbFile;

        private final int defaultLayoutId;
        private final int demoModeLayoutId;

        private final boolean isScalable;
        private final boolean mIsDualGrid;
        private final int devicePaddingId;
        private final int mWorkspaceSpecsId;
        private final int mWorkspaceSpecsTwoPanelId;
        private final int mAllAppsSpecsId;
        private final int mAllAppsSpecsTwoPanelId;
        private final int mFolderSpecsId;
        private final int mFolderSpecsTwoPanelId;
        private final int mHotseatSpecsId;
        private final int mHotseatSpecsTwoPanelId;
        private final int mWorkspaceCellSpecsId;
        private final int mWorkspaceCellSpecsTwoPanelId;
        private final int mAllAppsCellSpecsId;
        private final int mAllAppsCellSpecsTwoPanelId;
        private final int mGridSizeSpecsId;
        private final boolean mIsFixedLandscape;

        public GridOption(Context context, AttributeSet attrs, Info displayInfo) {
            TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.GridDisplayOption);
            name = a.getString(R.styleable.GridDisplayOption_name);
            gridTitle = a.getString(R.styleable.GridDisplayOption_gridTitle);
            gridIconId = a.getResourceId(
                    R.styleable.GridDisplayOption_gridIconId, INVALID_RESOURCE_HANDLE);
            deviceCategory = a.getInt(R.styleable.GridDisplayOption_deviceCategory,
                    DEVICE_CATEGORY_ANY);
            mGridSizeSpecsId = a.getResourceId(
                    R.styleable.GridDisplayOption_gridSizeSpecsId, INVALID_RESOURCE_HANDLE);
            mIsDualGrid = a.getBoolean(R.styleable.GridDisplayOption_isDualGrid, false);
            if (mGridSizeSpecsId != INVALID_RESOURCE_HANDLE) {
                ResourceHelper resourceHelper = new ResourceHelper(context, mGridSizeSpecsId);
                GridSize gridSize = getGridSize(resourceHelper, context, displayInfo);
                numColumns = gridSize.mNumColumns;
                numRows = gridSize.mNumRows;
                dbFile = gridSize.mDbFile;
                defaultLayoutId = gridSize.mDefaultLayoutId;
                demoModeLayoutId = gridSize.mDemoModeLayoutId;
            } else {
                numRows = a.getInt(R.styleable.GridDisplayOption_numRows, 0);
                numColumns = a.getInt(R.styleable.GridDisplayOption_numColumns, 0);
                dbFile = a.getString(R.styleable.GridDisplayOption_dbFile);
                defaultLayoutId = a.getResourceId(
                        R.styleable.GridDisplayOption_defaultLayoutId, 0);
                demoModeLayoutId = a.getResourceId(
                        R.styleable.GridDisplayOption_demoModeLayoutId, defaultLayoutId);
            }

            numSearchContainerColumns = a.getInt(
                    R.styleable.GridDisplayOption_numSearchContainerColumns, numColumns);

            allAppsStyle = a.getResourceId(R.styleable.GridDisplayOption_allAppsStyle,
                    R.style.AllAppsStyleDefault);
            numAllAppsColumns = a.getInt(
                    R.styleable.GridDisplayOption_numAllAppsColumns, numColumns);
            numDatabaseAllAppsColumns = a.getInt(
                    R.styleable.GridDisplayOption_numExtendedAllAppsColumns, 2 * numAllAppsColumns);

            numHotseatIcons = a.getInt(
                    R.styleable.GridDisplayOption_numHotseatIcons, numColumns);
            numHotseatRows = a.getInt(
                    R.styleable.GridDisplayOption_numHotseatRows, 1);
            numDatabaseHotseatIcons = a.getInt(
                    R.styleable.GridDisplayOption_numExtendedHotseatIcons, 2 * numHotseatIcons);

            inlineNavButtonsEndSpacing =
                    a.getResourceId(R.styleable.GridDisplayOption_inlineNavButtonsEndSpacing,
                            R.dimen.taskbar_button_margin_default);

            numFolderRows[INDEX_DEFAULT] = a.getInt(
                    R.styleable.GridDisplayOption_numFolderRows, numRows);
            numFolderColumns[INDEX_DEFAULT] = a.getInt(
                    R.styleable.GridDisplayOption_numFolderColumns, numColumns);

            if (FeatureFlags.enableResponsiveWorkspace()) {
                numFolderRows[INDEX_LANDSCAPE] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderRowsLandscape,
                        numFolderRows[INDEX_DEFAULT]);
                numFolderColumns[INDEX_LANDSCAPE] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderColumnsLandscape,
                        numFolderColumns[INDEX_DEFAULT]);
                numFolderRows[INDEX_TWO_PANEL_PORTRAIT] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderRowsTwoPanelPortrait,
                        numFolderRows[INDEX_DEFAULT]);
                numFolderColumns[INDEX_TWO_PANEL_PORTRAIT] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderColumnsTwoPanelPortrait,
                        numFolderColumns[INDEX_DEFAULT]);
                numFolderRows[INDEX_TWO_PANEL_LANDSCAPE] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderRowsTwoPanelLandscape,
                        numFolderRows[INDEX_DEFAULT]);
                numFolderColumns[INDEX_TWO_PANEL_LANDSCAPE] = a.getInt(
                        R.styleable.GridDisplayOption_numFolderColumnsTwoPanelLandscape,
                        numFolderColumns[INDEX_DEFAULT]);
            } else {
                numFolderRows[INDEX_LANDSCAPE] = numFolderRows[INDEX_DEFAULT];
                numFolderColumns[INDEX_LANDSCAPE] = numFolderColumns[INDEX_DEFAULT];
                numFolderRows[INDEX_TWO_PANEL_PORTRAIT] = numFolderRows[INDEX_DEFAULT];
                numFolderColumns[INDEX_TWO_PANEL_PORTRAIT] = numFolderColumns[INDEX_DEFAULT];
                numFolderRows[INDEX_TWO_PANEL_LANDSCAPE] = numFolderRows[INDEX_DEFAULT];
                numFolderColumns[INDEX_TWO_PANEL_LANDSCAPE] = numFolderColumns[INDEX_DEFAULT];
            }

            folderStyle = a.getResourceId(R.styleable.GridDisplayOption_folderStyle,
                    INVALID_RESOURCE_HANDLE);

            cellStyle = a.getResourceId(R.styleable.GridDisplayOption_cellStyle,
                    R.style.CellStyleDefault);

            isScalable = a.getBoolean(
                    R.styleable.GridDisplayOption_isScalable, false);
            devicePaddingId = a.getResourceId(
                    R.styleable.GridDisplayOption_devicePaddingId, INVALID_RESOURCE_HANDLE);

            if (FeatureFlags.enableResponsiveWorkspace()) {
                mWorkspaceSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_workspaceSpecsId, INVALID_RESOURCE_HANDLE);
                mWorkspaceSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_workspaceSpecsTwoPanelId,
                        mWorkspaceSpecsId);
                mAllAppsSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_allAppsSpecsId, INVALID_RESOURCE_HANDLE);
                mAllAppsSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_allAppsSpecsTwoPanelId,
                        mWorkspaceSpecsId);
                mFolderSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_folderSpecsId, INVALID_RESOURCE_HANDLE);
                mFolderSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_folderSpecsTwoPanelId,
                        mWorkspaceSpecsId);
                mHotseatSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_hotseatSpecsId, INVALID_RESOURCE_HANDLE);
                mHotseatSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_hotseatSpecsTwoPanelId,
                        mWorkspaceSpecsId);
                mWorkspaceCellSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_workspaceCellSpecsId,
                        INVALID_RESOURCE_HANDLE);
                mWorkspaceCellSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_workspaceCellSpecsTwoPanelId,
                        mWorkspaceCellSpecsId);
                mAllAppsCellSpecsId = a.getResourceId(
                        R.styleable.GridDisplayOption_allAppsCellSpecsId,
                        INVALID_RESOURCE_HANDLE);
                mAllAppsCellSpecsTwoPanelId = a.getResourceId(
                        R.styleable.GridDisplayOption_allAppsCellSpecsTwoPanelId,
                        mAllAppsCellSpecsId);
                mNumAllAppsRowsForCellHeightCalculation = a.getInt(
                        R.styleable.GridDisplayOption_numAllAppsRowsForCellHeightCalculation,
                        numRows);
            } else {
                mWorkspaceSpecsId = INVALID_RESOURCE_HANDLE;
                mWorkspaceSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mAllAppsSpecsId = INVALID_RESOURCE_HANDLE;
                mAllAppsSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mFolderSpecsId = INVALID_RESOURCE_HANDLE;
                mFolderSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mHotseatSpecsId = INVALID_RESOURCE_HANDLE;
                mHotseatSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mWorkspaceCellSpecsId = INVALID_RESOURCE_HANDLE;
                mWorkspaceCellSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mAllAppsCellSpecsId = INVALID_RESOURCE_HANDLE;
                mAllAppsCellSpecsTwoPanelId = INVALID_RESOURCE_HANDLE;
                mNumAllAppsRowsForCellHeightCalculation = numRows;
            }

            mIsFixedLandscape = a.getBoolean(R.styleable.GridDisplayOption_isFixedLandscape, false);
            gridType = a.getInt(R.styleable.GridDisplayOption_gridType, GRID_TYPE_ANY);

            int inlineForRotation = a.getInt(R.styleable.GridDisplayOption_inlineQsb,
                    DONT_INLINE_QSB);
            inlineQsb[INDEX_DEFAULT] =
                    (inlineForRotation & INLINE_QSB_FOR_PORTRAIT) == INLINE_QSB_FOR_PORTRAIT;
            inlineQsb[INDEX_LANDSCAPE] =
                    (inlineForRotation & INLINE_QSB_FOR_LANDSCAPE) == INLINE_QSB_FOR_LANDSCAPE;
            inlineQsb[INDEX_TWO_PANEL_PORTRAIT] =
                    (inlineForRotation & INLINE_QSB_FOR_TWO_PANEL_PORTRAIT)
                            == INLINE_QSB_FOR_TWO_PANEL_PORTRAIT;
            inlineQsb[INDEX_TWO_PANEL_LANDSCAPE] =
                    (inlineForRotation & INLINE_QSB_FOR_TWO_PANEL_LANDSCAPE)
                            == INLINE_QSB_FOR_TWO_PANEL_LANDSCAPE;

            a.recycle();
        }

        public boolean isEnabled(@DeviceType int deviceType) {
            switch (deviceType) {
                case TYPE_PHONE:
                    return (deviceCategory & DEVICE_CATEGORY_PHONE) == DEVICE_CATEGORY_PHONE;
                case TYPE_TABLET:
                    return (deviceCategory & DEVICE_CATEGORY_TABLET) == DEVICE_CATEGORY_TABLET;
                case TYPE_MULTI_DISPLAY:
                    return (deviceCategory & DEVICE_CATEGORY_MULTI_DISPLAY)
                            == DEVICE_CATEGORY_MULTI_DISPLAY;
                default:
                    return false;
            }
        }

        /**
         * Returns true if the grid option should be used given the flags that are toggled on/off.
         */
        public boolean filterByFlag(int deviceType, boolean isFixedLandscape) {
            if (deviceType == TYPE_TABLET) {
                return Flags.oneGridRotationHandling() == mIsDualGrid;
            }

            // Here we return true if fixed landscape mode should be on.
            if (mIsFixedLandscape || isFixedLandscape) {
                return mIsFixedLandscape && isFixedLandscape && Flags.oneGridSpecs();
            }

            // If the grid type is one grid we return true when the flag is on, if the grid type
            // is non-one grid we return true when the flag is off. Otherwise, we return true.
            if (gridType == GRID_TYPE_ONE_GRID) {
                return Flags.oneGridSpecs();
            } else if (gridType == GRID_TYPE_NON_ONE_GRID) {
                return !Flags.oneGridSpecs();
            }

            return true;
        }
    }

    public static final class GridSize {
        final int mNumRows;
        final int mNumColumns;
        final float mMinDeviceWidthPx;
        final float mMinDeviceHeightPx;
        final String mDbFile;
        final int mDefaultLayoutId;
        final int mDemoModeLayoutId;


        GridSize(Context context, AttributeSet attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridSize);

            mNumRows = (int) a.getFloat(R.styleable.GridSize_numGridRows, 0);
            mNumColumns = (int) a.getFloat(R.styleable.GridSize_numGridColumns, 0);
            mMinDeviceWidthPx = a.getFloat(R.styleable.GridSize_minDeviceWidthPx, 0);
            mMinDeviceHeightPx = a.getFloat(R.styleable.GridSize_minDeviceHeightPx, 0);
            mDbFile = a.getString(R.styleable.GridSize_dbFile);
            mDefaultLayoutId = a.getResourceId(
                    R.styleable.GridSize_defaultLayoutId, 0);
            mDemoModeLayoutId = a.getResourceId(
                    R.styleable.GridSize_demoModeLayoutId, mDefaultLayoutId);

            a.recycle();
        }
    }

    @VisibleForTesting
    static final class DisplayOption {
        public final GridOption grid;

        private final float minWidthDps;
        private final float minHeightDps;
        private final boolean canBeDefault;

        private final PointF[] minCellSize = new PointF[COUNT_SIZES];

        private final PointF[] borderSpaces = new PointF[COUNT_SIZES];
        private final float[] horizontalMargin = new float[COUNT_SIZES];
        private final float[] hotseatBarBottomSpace = new float[COUNT_SIZES];
        private final float[] hotseatQsbSpace = new float[COUNT_SIZES];

        private final float[] iconSizes = new float[COUNT_SIZES];
        private final float[] textSizes = new float[COUNT_SIZES];

        private final PointF[] allAppsCellSize = new PointF[COUNT_SIZES];
        private final float[] allAppsIconSizes = new float[COUNT_SIZES];
        private final float[] allAppsIconTextSizes = new float[COUNT_SIZES];
        private final PointF[] allAppsBorderSpaces = new PointF[COUNT_SIZES];

        private final float[] transientTaskbarIconSize = new float[COUNT_SIZES];

        private final boolean[] startAlignTaskbar = new boolean[COUNT_SIZES];

        DisplayOption(GridOption grid, Context context, AttributeSet attrs) {
            this.grid = grid;

            Resources res = context.getResources();

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileDisplayOption);

            minWidthDps = a.getFloat(R.styleable.ProfileDisplayOption_minWidthDps, 0);
            minHeightDps = a.getFloat(R.styleable.ProfileDisplayOption_minHeightDps, 0);

            canBeDefault = a.getBoolean(R.styleable.ProfileDisplayOption_canBeDefault, false);

            float x;
            float y;

            x = a.getFloat(R.styleable.ProfileDisplayOption_minCellWidth, 0);
            y = a.getFloat(R.styleable.ProfileDisplayOption_minCellHeight, 0);
            minCellSize[INDEX_DEFAULT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_minCellWidthLandscape,
                    minCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_minCellHeightLandscape,
                    minCellSize[INDEX_DEFAULT].y);
            minCellSize[INDEX_LANDSCAPE] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_minCellWidthTwoPanelPortrait,
                    minCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_minCellHeightTwoPanelPortrait,
                    minCellSize[INDEX_DEFAULT].y);
            minCellSize[INDEX_TWO_PANEL_PORTRAIT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_minCellWidthTwoPanelLandscape,
                    minCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_minCellHeightTwoPanelLandscape,
                    minCellSize[INDEX_DEFAULT].y);
            minCellSize[INDEX_TWO_PANEL_LANDSCAPE] = new PointF(x, y);

            float borderSpace = a.getFloat(R.styleable.ProfileDisplayOption_borderSpace, 0);
            float borderSpaceLandscape = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceLandscape, borderSpace);
            float borderSpaceTwoPanelPortrait = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelPortrait, borderSpace);
            float borderSpaceTwoPanelLandscape = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelLandscape, borderSpace);

            x = a.getFloat(R.styleable.ProfileDisplayOption_borderSpaceHorizontal, borderSpace);
            y = a.getFloat(R.styleable.ProfileDisplayOption_borderSpaceVertical, borderSpace);
            borderSpaces[INDEX_DEFAULT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_borderSpaceLandscapeHorizontal,
                    borderSpaceLandscape);
            y = a.getFloat(R.styleable.ProfileDisplayOption_borderSpaceLandscapeVertical,
                    borderSpaceLandscape);
            borderSpaces[INDEX_LANDSCAPE] = new PointF(x, y);

            x = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelPortraitHorizontal,
                    borderSpaceTwoPanelPortrait);
            y = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelPortraitVertical,
                    borderSpaceTwoPanelPortrait);
            borderSpaces[INDEX_TWO_PANEL_PORTRAIT] = new PointF(x, y);

            x = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelLandscapeHorizontal,
                    borderSpaceTwoPanelLandscape);
            y = a.getFloat(
                    R.styleable.ProfileDisplayOption_borderSpaceTwoPanelLandscapeVertical,
                    borderSpaceTwoPanelLandscape);
            borderSpaces[INDEX_TWO_PANEL_LANDSCAPE] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellWidth,
                    minCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellHeight,
                    minCellSize[INDEX_DEFAULT].y);
            allAppsCellSize[INDEX_DEFAULT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellWidthLandscape,
                    allAppsCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellHeightLandscape,
                    allAppsCellSize[INDEX_DEFAULT].y);
            allAppsCellSize[INDEX_LANDSCAPE] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellWidthTwoPanelPortrait,
                    allAppsCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellHeightTwoPanelPortrait,
                    allAppsCellSize[INDEX_DEFAULT].y);
            allAppsCellSize[INDEX_TWO_PANEL_PORTRAIT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellWidthTwoPanelLandscape,
                    allAppsCellSize[INDEX_DEFAULT].x);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsCellHeightTwoPanelLandscape,
                    allAppsCellSize[INDEX_DEFAULT].y);
            allAppsCellSize[INDEX_TWO_PANEL_LANDSCAPE] = new PointF(x, y);

            float allAppsBorderSpace = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpace, borderSpace);
            float allAppsBorderSpaceLandscape = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceLandscape,
                    allAppsBorderSpace);
            float allAppsBorderSpaceTwoPanelPortrait = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelPortrait,
                    allAppsBorderSpace);
            float allAppsBorderSpaceTwoPanelLandscape = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelLandscape,
                    allAppsBorderSpace);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsBorderSpaceHorizontal,
                    allAppsBorderSpace);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsBorderSpaceVertical,
                    allAppsBorderSpace);
            allAppsBorderSpaces[INDEX_DEFAULT] = new PointF(x, y);

            x = a.getFloat(R.styleable.ProfileDisplayOption_allAppsBorderSpaceLandscapeHorizontal,
                    allAppsBorderSpaceLandscape);
            y = a.getFloat(R.styleable.ProfileDisplayOption_allAppsBorderSpaceLandscapeVertical,
                    allAppsBorderSpaceLandscape);
            allAppsBorderSpaces[INDEX_LANDSCAPE] = new PointF(x, y);

            x = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelPortraitHorizontal,
                    allAppsBorderSpaceTwoPanelPortrait);
            y = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelPortraitVertical,
                    allAppsBorderSpaceTwoPanelPortrait);
            allAppsBorderSpaces[INDEX_TWO_PANEL_PORTRAIT] = new PointF(x, y);

            x = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelLandscapeHorizontal,
                    allAppsBorderSpaceTwoPanelLandscape);
            y = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsBorderSpaceTwoPanelLandscapeVertical,
                    allAppsBorderSpaceTwoPanelLandscape);
            allAppsBorderSpaces[INDEX_TWO_PANEL_LANDSCAPE] = new PointF(x, y);

            iconSizes[INDEX_DEFAULT] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconImageSize, 0);
            iconSizes[INDEX_LANDSCAPE] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconSizeLandscape,
                            iconSizes[INDEX_DEFAULT]);
            iconSizes[INDEX_TWO_PANEL_PORTRAIT] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconSizeTwoPanelPortrait,
                            iconSizes[INDEX_DEFAULT]);
            iconSizes[INDEX_TWO_PANEL_LANDSCAPE] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconSizeTwoPanelLandscape,
                            iconSizes[INDEX_DEFAULT]);

            allAppsIconSizes[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconSize, iconSizes[INDEX_DEFAULT]);
            allAppsIconSizes[INDEX_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconSizeLandscape,
                    allAppsIconSizes[INDEX_DEFAULT]);
            allAppsIconSizes[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconSizeTwoPanelPortrait,
                    allAppsIconSizes[INDEX_DEFAULT]);
            allAppsIconSizes[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconSizeTwoPanelLandscape,
                    allAppsIconSizes[INDEX_DEFAULT]);

            textSizes[INDEX_DEFAULT] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconTextSize, 0);
            textSizes[INDEX_LANDSCAPE] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconTextSizeLandscape,
                            textSizes[INDEX_DEFAULT]);
            textSizes[INDEX_TWO_PANEL_PORTRAIT] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconTextSizeTwoPanelPortrait,
                            textSizes[INDEX_DEFAULT]);
            textSizes[INDEX_TWO_PANEL_LANDSCAPE] =
                    a.getFloat(R.styleable.ProfileDisplayOption_iconTextSizeTwoPanelLandscape,
                            textSizes[INDEX_DEFAULT]);

            allAppsIconTextSizes[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconTextSize, textSizes[INDEX_DEFAULT]);
            allAppsIconTextSizes[INDEX_LANDSCAPE] = allAppsIconTextSizes[INDEX_DEFAULT];
            allAppsIconTextSizes[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconTextSizeTwoPanelPortrait,
                    allAppsIconTextSizes[INDEX_DEFAULT]);
            allAppsIconTextSizes[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_allAppsIconTextSizeTwoPanelLandscape,
                    allAppsIconTextSizes[INDEX_DEFAULT]);

            horizontalMargin[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_horizontalMargin, 0);
            horizontalMargin[INDEX_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_horizontalMarginLandscape,
                    horizontalMargin[INDEX_DEFAULT]);
            horizontalMargin[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_horizontalMarginTwoPanelLandscape,
                    horizontalMargin[INDEX_DEFAULT]);
            horizontalMargin[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_horizontalMarginTwoPanelPortrait,
                    horizontalMargin[INDEX_DEFAULT]);

            hotseatBarBottomSpace[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatBarBottomSpace,
                    ResourcesCompat.getFloat(res, R.dimen.hotseat_bar_bottom_space_default));
            hotseatBarBottomSpace[INDEX_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatBarBottomSpaceLandscape,
                    hotseatBarBottomSpace[INDEX_DEFAULT]);
            hotseatBarBottomSpace[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatBarBottomSpaceTwoPanelLandscape,
                    hotseatBarBottomSpace[INDEX_DEFAULT]);
            hotseatBarBottomSpace[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatBarBottomSpaceTwoPanelPortrait,
                    hotseatBarBottomSpace[INDEX_DEFAULT]);

            hotseatQsbSpace[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatQsbSpace,
                    ResourcesCompat.getFloat(res, R.dimen.hotseat_qsb_space_default));
            hotseatQsbSpace[INDEX_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatQsbSpaceLandscape,
                    hotseatQsbSpace[INDEX_DEFAULT]);
            hotseatQsbSpace[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatQsbSpaceTwoPanelLandscape,
                    hotseatQsbSpace[INDEX_DEFAULT]);
            hotseatQsbSpace[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_hotseatQsbSpaceTwoPanelPortrait,
                    hotseatQsbSpace[INDEX_DEFAULT]);

            transientTaskbarIconSize[INDEX_DEFAULT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_transientTaskbarIconSize,
                    ResourcesCompat.getFloat(res, R.dimen.taskbar_icon_size));
            transientTaskbarIconSize[INDEX_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_transientTaskbarIconSizeLandscape,
                    transientTaskbarIconSize[INDEX_DEFAULT]);
            transientTaskbarIconSize[INDEX_TWO_PANEL_LANDSCAPE] = a.getFloat(
                    R.styleable.ProfileDisplayOption_transientTaskbarIconSizeTwoPanelLandscape,
                    transientTaskbarIconSize[INDEX_DEFAULT]);
            transientTaskbarIconSize[INDEX_TWO_PANEL_PORTRAIT] = a.getFloat(
                    R.styleable.ProfileDisplayOption_transientTaskbarIconSizeTwoPanelPortrait,
                    transientTaskbarIconSize[INDEX_DEFAULT]);

            startAlignTaskbar[INDEX_DEFAULT] = a.getBoolean(
                    R.styleable.ProfileDisplayOption_startAlignTaskbar, false);
            startAlignTaskbar[INDEX_LANDSCAPE] = a.getBoolean(
                    R.styleable.ProfileDisplayOption_startAlignTaskbarLandscape,
                    startAlignTaskbar[INDEX_DEFAULT]);
            startAlignTaskbar[INDEX_TWO_PANEL_LANDSCAPE] = a.getBoolean(
                    R.styleable.ProfileDisplayOption_startAlignTaskbarTwoPanelLandscape,
                    startAlignTaskbar[INDEX_LANDSCAPE]);
            startAlignTaskbar[INDEX_TWO_PANEL_PORTRAIT] = a.getBoolean(
                    R.styleable.ProfileDisplayOption_startAlignTaskbarTwoPanelPortrait,
                    startAlignTaskbar[INDEX_DEFAULT]);

            a.recycle();
        }

        DisplayOption() {
            this(null);
        }

        DisplayOption(GridOption grid) {
            this.grid = grid;
            minWidthDps = 0;
            minHeightDps = 0;
            canBeDefault = false;
            for (int i = 0; i < COUNT_SIZES; i++) {
                iconSizes[i] = 0;
                textSizes[i] = 0;
                borderSpaces[i] = new PointF();
                minCellSize[i] = new PointF();
                allAppsCellSize[i] = new PointF();
                allAppsIconSizes[i] = 0;
                allAppsIconTextSizes[i] = 0;
                allAppsBorderSpaces[i] = new PointF();
                transientTaskbarIconSize[i] = 0;
                startAlignTaskbar[i] = false;
            }
        }

        private DisplayOption multiply(float w) {
            for (int i = 0; i < COUNT_SIZES; i++) {
                iconSizes[i] *= w;
                textSizes[i] *= w;
                borderSpaces[i].x *= w;
                borderSpaces[i].y *= w;
                minCellSize[i].x *= w;
                minCellSize[i].y *= w;
                horizontalMargin[i] *= w;
                hotseatBarBottomSpace[i] *= w;
                hotseatQsbSpace[i] *= w;
                allAppsCellSize[i].x *= w;
                allAppsCellSize[i].y *= w;
                allAppsIconSizes[i] *= w;
                allAppsIconTextSizes[i] *= w;
                allAppsBorderSpaces[i].x *= w;
                allAppsBorderSpaces[i].y *= w;
                transientTaskbarIconSize[i] *= w;
            }

            return this;
        }

        private DisplayOption add(DisplayOption p) {
            for (int i = 0; i < COUNT_SIZES; i++) {
                iconSizes[i] += p.iconSizes[i];
                textSizes[i] += p.textSizes[i];
                borderSpaces[i].x += p.borderSpaces[i].x;
                borderSpaces[i].y += p.borderSpaces[i].y;
                minCellSize[i].x += p.minCellSize[i].x;
                minCellSize[i].y += p.minCellSize[i].y;
                horizontalMargin[i] += p.horizontalMargin[i];
                hotseatBarBottomSpace[i] += p.hotseatBarBottomSpace[i];
                hotseatQsbSpace[i] += p.hotseatQsbSpace[i];
                allAppsCellSize[i].x += p.allAppsCellSize[i].x;
                allAppsCellSize[i].y += p.allAppsCellSize[i].y;
                allAppsIconSizes[i] += p.allAppsIconSizes[i];
                allAppsIconTextSizes[i] += p.allAppsIconTextSizes[i];
                allAppsBorderSpaces[i].x += p.allAppsBorderSpaces[i].x;
                allAppsBorderSpaces[i].y += p.allAppsBorderSpaces[i].y;
                transientTaskbarIconSize[i] += p.transientTaskbarIconSize[i];
                startAlignTaskbar[i] |= p.startAlignTaskbar[i];
            }

            return this;
        }
    }
}

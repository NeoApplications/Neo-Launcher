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
package com.android.launcher3;

import static com.android.launcher3.InvariantDeviceProfile.INDEX_DEFAULT;
import static com.android.launcher3.InvariantDeviceProfile.INDEX_LANDSCAPE;
import static com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_LANDSCAPE;
import static com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_PORTRAIT;
import static com.android.launcher3.ResourceUtils.INVALID_RESOURCE_HANDLE;
import static com.android.launcher3.ResourceUtils.pxFromDp;
import static com.android.launcher3.Utilities.dpiFromPx;
import static com.android.launcher3.Utilities.pxFromSp;
import static com.android.launcher3.folder.ClippedFolderIconLayoutRule.ICON_OVERLAP_FACTOR;
import static com.android.launcher3.icons.GraphicsUtils.getShapePath;
import static com.android.launcher3.testing.shared.ResourceUtils.roundPxValueFromFloat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.CellLayout.ContainerType;
import com.android.launcher3.DevicePaddings.DevicePadding;
import com.android.launcher3.icons.DotRenderer;
import com.android.launcher3.icons.IconNormalizer;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.uioverrides.ApiWrapper;
import com.android.launcher3.util.DisplayController;
import com.android.launcher3.util.DisplayController.Info;
import com.android.launcher3.util.WindowBounds;
import com.saggitt.omega.DeviceProfileOverrides;
import com.saggitt.omega.preferences.NeoPrefs;

import java.io.PrintWriter;
import java.util.function.Consumer;

@SuppressLint("NewApi")
public class DeviceProfile {
    private static final int DEFAULT_DOT_SIZE = 100;
    private static final float MIN_FOLDER_TEXT_SIZE_SP = 16f;
    public static final PointF DEFAULT_SCALE = new PointF(1.0f, 1.0f);
    public static final ViewScaleProvider DEFAULT_PROVIDER = itemInfo -> DEFAULT_SCALE;
    public static final Consumer<DeviceProfile> DEFAULT_DIMENSION_PROVIDER = dp -> {
    };
    // Ratio of empty space, qsb should take up to appear visually centered.
    private final float mQsbCenterFactor;
    public final InvariantDeviceProfile inv;
    private final Info mInfo;
    private final DisplayMetrics mMetrics;
    // Device properties
    public final boolean isTablet;
    public final boolean isPhone;
    public final boolean transposeLayoutWithOrientation;
    public final boolean isMultiDisplay;
    public final boolean isTwoPanels;
    public final boolean isQsbInline;
    // Device properties in current orientation
    public final boolean isLandscape;
    public final boolean isMultiWindowMode;
    public final boolean isGestureMode;
    public final int windowX;
    public final int windowY;
    public final int widthPx;
    public final int heightPx;
    public final int availableWidthPx;
    public final int availableHeightPx;
    public final int rotationHint;
    public final float aspectRatio;
    public final boolean isScalableGrid;
    public final int mTypeIndex;
    /**
     * The maximum amount of left/right workspace padding as a percentage of the screen width.
     * To be clear, this means that up to 7% of the screen width can be used as left padding, and
     * 7% of the screen width can be used as right padding.
     */
    private static final float MAX_HORIZONTAL_PADDING_PERCENT = 0.14f;
    private static final float TALL_DEVICE_ASPECT_RATIO_THRESHOLD = 2.0f;
    private static final float TALLER_DEVICE_ASPECT_RATIO_THRESHOLD = 2.15f;
    private static final float TALL_DEVICE_EXTRA_SPACE_THRESHOLD_DP = 252;
    private static final float TALL_DEVICE_MORE_EXTRA_SPACE_THRESHOLD_DP = 268;
    // Workspace
    public final int desiredWorkspaceHorizontalMarginOriginalPx;
    public int desiredWorkspaceHorizontalMarginPx;
    public int gridVisualizationPaddingX;
    public int gridVisualizationPaddingY;
    public Point cellLayoutBorderSpaceOriginalPx;
    public Point cellLayoutBorderSpacePx;
    public Rect cellLayoutPaddingPx = new Rect();
    public final int edgeMarginPx;
    public float workspaceSpringLoadShrunkTop;
    public float workspaceSpringLoadShrunkBottom;
    public final int workspaceSpringLoadedBottomSpace;
    public final int workspaceSpringLoadedMinNextPageVisiblePx;
    private final int extraSpace;
    public int workspaceTopPadding;
    public int workspaceBottomPadding;
    public int extraHotseatBottomPadding;
    // Workspace page indicator
    public final int workspacePageIndicatorHeight;
    private final int mWorkspacePageIndicatorOverlapWorkspace;
    // Workspace icons
    public float iconScale;
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;
    public int iconDrawablePaddingOriginalPx;
    public float cellScaleToFit;
    public int cellWidthPx;
    public int cellHeightPx;
    public int workspaceCellPaddingXPx;
    public int cellYPaddingPx;
    // Folder
    public float folderLabelTextScale;
    public int folderLabelTextSizePx;
    public int folderFooterHeightPx;
    public int folderIconSizePx;
    public int folderIconOffsetYPx;
    // Folder content
    public int folderCellLayoutBorderSpacePx;
    public int folderContentPaddingLeftRight;
    public int folderContentPaddingTop;

    public int folderCellLayoutBorderSpaceOriginalPx;
    // Folder cell
    public int folderCellWidthPx;
    public int folderCellHeightPx;
    // Folder child
    public int folderChildIconSizePx;
    public int folderChildTextSizePx;
    public int folderChildDrawablePaddingPx;
    // Hotseat
    public int hotseatBarSizeExtraSpacePx;
    public final int numShownHotseatIcons;
    public int hotseatCellHeightPx;
    private final int hotseatExtraVerticalSize;
    private final boolean areNavButtonsInline;
    // In portrait: size = height, in landscape: size = width
    public int hotseatBarSizePx;
    public int hotseatBarTopPaddingPx;
    public int hotseatBarBottomPaddingPx;
    public int springLoadedHotseatBarTopMarginPx;
    // Start is the side next to the nav bar, end is the side next to the workspace
    public final int hotseatBarSidePaddingStartPx;
    public final int hotseatBarSidePaddingEndPx;
    public final int hotseatQsbHeight;
    public int hotseatBorderSpace;
    public final float qsbBottomMarginOriginalPx;
    public int qsbBottomMarginPx;
    public int qsbWidth; // only used when isQsbInline

    // Bottom sheets
    public int bottomSheetTopPadding;
    public int bottomSheetOpenDuration;
    public int bottomSheetCloseDuration;
    public float bottomSheetWorkspaceScale;
    public float bottomSheetDepth;

    // All apps
    public Point allAppsBorderSpacePx;
    public int allAppsShiftRange;
    public int allAppsTopPadding;
    public int allAppsOpenDuration;
    public int allAppsCloseDuration;
    public int allAppsCellHeightPx;
    public int allAppsCellWidthPx;
    public int allAppsIconSizePx;
    public int allAppsIconDrawablePaddingPx;
    public int allAppsLeftRightPadding;
    public int allAppsLeftRightMargin;
    public final int numShownAllAppsColumns;
    public float allAppsIconTextSizePx;
    // Overview
    public int overviewTaskMarginPx;
    public int overviewTaskMarginGridPx;
    public int overviewTaskIconSizePx;
    public int overviewTaskIconDrawableSizePx;
    public int overviewTaskIconDrawableSizeGridPx;
    public int overviewTaskThumbnailTopMarginPx;
    public final int overviewActionsHeight;
    public final int overviewActionsTopMarginPx;
    public final int overviewActionsButtonSpacing;
    public int overviewPageSpacing;
    public int overviewRowSpacing;
    public int overviewGridSideMargin;
    // Split staging
    public int splitPlaceholderInset;

    // Widgets
    private final ViewScaleProvider mViewScaleProvider;
    public final PointF appWidgetScale = new PointF(1.0f, 1.0f);
    // Drop Target
    public int dropTargetBarSizePx;
    public int dropTargetBarTopMarginPx;
    public int dropTargetBarBottomMarginPx;
    public int dropTargetDragPaddingPx;
    public int dropTargetTextSizePx;
    public int dropTargetHorizontalPaddingPx;
    public int dropTargetVerticalPaddingPx;
    public int dropTargetGapPx;
    public int dropTargetButtonWorkspaceEdgeGapPx;
    // Insets
    private final Rect mInsets = new Rect();
    public final Rect workspacePadding = new Rect();
    private final Rect mHotseatPadding = new Rect();
    // When true, nav bar is on the left side of the screen.
    private boolean mIsSeascape;
    // Notification dots
    public DotRenderer mDotRendererWorkSpace;
    public DotRenderer mDotRendererAllApps;
    // Taskbar
    public boolean isTaskbarPresent;
    // Whether Taskbar will inset the bottom of apps by taskbarSize.
    public boolean isTaskbarPresentInApps;
    public int taskbarSize;
    public int stashedTaskbarSize;
    // DragController
    public int flingToDeleteThresholdVelocity;

    private final NeoPrefs prefs;

    private final DeviceProfileOverrides.TextFactors mTextFactors;

    /**
     * TODO: Once we fully migrate to staged split, remove "isMultiWindowMode"
     */
    DeviceProfile(Context context, InvariantDeviceProfile inv, Info info, WindowBounds windowBounds,
                  SparseArray<DotRenderer> dotRendererCache,
                  boolean isMultiWindowMode, boolean transposeLayoutWithOrientation,
                  boolean isMultiDisplay, boolean isGestureMode,
                  @NonNull final ViewScaleProvider viewScaleProvider,
                  @NonNull final Consumer<DeviceProfile> dimensionOverrideProvider) {
        prefs = Utilities.getOmegaPrefs(context);
        boolean fullWidthWidgets = prefs.getDesktopAllowFullWidthWidgets().getValue();

        mTextFactors = DeviceProfileOverrides.INSTANCE.get(context).getTextFactors();
        this.inv = inv;
        this.isLandscape = windowBounds.isLandscape();
        this.isMultiWindowMode = isMultiWindowMode;
        this.transposeLayoutWithOrientation = transposeLayoutWithOrientation;
        this.isMultiDisplay = isMultiDisplay;
        this.isGestureMode = isGestureMode;
        windowX = windowBounds.bounds.left;
        windowY = windowBounds.bounds.top;
        this.rotationHint = windowBounds.rotationHint;
        mInsets.set(windowBounds.insets);
        isScalableGrid = inv.isScalable && !isVerticalBarLayout() && !isMultiWindowMode;
        // Determine device posture.
        mInfo = info;
        isTablet = info.isTablet(windowBounds);
        isPhone = !isTablet;
        isTwoPanels = isTablet && isMultiDisplay;
        isTaskbarPresent = isTablet && ApiWrapper.TASKBAR_DRAWN_IN_PROCESS;
        // Some more constants.
        context = getContext(context, info, isVerticalBarLayout() || (isTablet && isLandscape)
                        ? Configuration.ORIENTATION_LANDSCAPE
                        : Configuration.ORIENTATION_PORTRAIT,
                windowBounds);
        final Resources res = context.getResources();
        mMetrics = res.getDisplayMetrics();
        // Determine sizes.
        widthPx = windowBounds.bounds.width();
        heightPx = windowBounds.bounds.height();
        availableWidthPx = windowBounds.availableSize.x;
        availableHeightPx = windowBounds.availableSize.y;
        aspectRatio = ((float) Math.max(widthPx, heightPx)) / Math.min(widthPx, heightPx);
        boolean isTallDevice = Float.compare(aspectRatio, TALL_DEVICE_ASPECT_RATIO_THRESHOLD) >= 0;
        mQsbCenterFactor = res.getFloat(R.dimen.qsb_center_factor);
        if (isTwoPanels) {
            if (isLandscape) {
                mTypeIndex = INDEX_TWO_PANEL_LANDSCAPE;
            } else {
                mTypeIndex = INDEX_TWO_PANEL_PORTRAIT;
            }
        } else {
            if (isLandscape) {
                mTypeIndex = INDEX_LANDSCAPE;
            } else {
                mTypeIndex = INDEX_DEFAULT;
            }
        }
        if (isTaskbarPresent) {
            taskbarSize = res.getDimensionPixelSize(R.dimen.taskbar_size);
            stashedTaskbarSize = res.getDimensionPixelSize(R.dimen.taskbar_stashed_size);
        }
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        desiredWorkspaceHorizontalMarginPx = getHorizontalMarginPx(inv, res);
        desiredWorkspaceHorizontalMarginOriginalPx = desiredWorkspaceHorizontalMarginPx;
        gridVisualizationPaddingX = res.getDimensionPixelSize(
                R.dimen.grid_visualization_horizontal_cell_spacing);
        gridVisualizationPaddingY = res.getDimensionPixelSize(
                R.dimen.grid_visualization_vertical_cell_spacing);
        bottomSheetTopPadding = mInsets.top // statusbar height
                + res.getDimensionPixelSize(R.dimen.bottom_sheet_extra_top_padding)
                + (isTablet ? 0 : edgeMarginPx); // phones need edgeMarginPx additional padding
        bottomSheetOpenDuration = 500;//res.getInteger(R.integer.config_bottomSheetOpenDuration);
        bottomSheetCloseDuration = 500;// res.getInteger(R.integer.config_bottomSheetCloseDuration);
        allAppsTopPadding = isTablet ? bottomSheetTopPadding : 0;
        allAppsShiftRange = isTablet
                ? heightPx - allAppsTopPadding
                : res.getDimensionPixelSize(R.dimen.all_apps_starting_vertical_translate);
        folderLabelTextScale = res.getFloat(R.dimen.folder_label_text_scale);

        if (isScalableGrid && inv.folderStyle != INVALID_RESOURCE_HANDLE) {
            TypedArray folderStyle = context.obtainStyledAttributes(inv.folderStyle,
                    R.styleable.FolderStyle);
            // These are re-set in #updateFolderCellSize if the grid is not scalable
            folderCellHeightPx = folderStyle.getDimensionPixelSize(
                    R.styleable.FolderStyle_folderCellHeight, 0);
            folderCellWidthPx = folderStyle.getDimensionPixelSize(
                    R.styleable.FolderStyle_folderCellWidth, 0);

            folderContentPaddingTop = folderStyle.getDimensionPixelSize(
                    R.styleable.FolderStyle_folderTopPadding, 0);
            folderCellLayoutBorderSpacePx = folderStyle.getDimensionPixelSize(
                    R.styleable.FolderStyle_folderBorderSpace, 0);
            folderFooterHeightPx = folderStyle.getDimensionPixelSize(
                    R.styleable.FolderStyle_folderFooterHeight, 0);
            folderStyle.recycle();
        } else {
            folderCellLayoutBorderSpacePx = 0;
            folderFooterHeightPx = res.getDimensionPixelSize(R.dimen.folder_footer_height_default);
            folderContentPaddingTop = res.getDimensionPixelSize(R.dimen.folder_top_padding_default);
        }

        cellLayoutBorderSpacePx = getCellLayoutBorderSpace(inv);
        cellLayoutBorderSpaceOriginalPx = new Point(cellLayoutBorderSpacePx);

        allAppsBorderSpacePx = new Point(
                pxFromDp(inv.allAppsBorderSpaces[mTypeIndex].x, mMetrics),
                pxFromDp(inv.allAppsBorderSpaces[mTypeIndex].y, mMetrics));
        setupAllAppsStyle(context);

        workspacePageIndicatorHeight = res.getDimensionPixelSize(
                R.dimen.workspace_page_indicator_height);
        mWorkspacePageIndicatorOverlapWorkspace =
                res.getDimensionPixelSize(R.dimen.workspace_page_indicator_overlap_workspace);
        iconDrawablePaddingOriginalPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        dropTargetBarSizePx = res.getDimensionPixelSize(R.dimen.dynamic_grid_drop_target_size);
        dropTargetBarTopMarginPx = res.getDimensionPixelSize(R.dimen.drop_target_top_margin);
        dropTargetBarBottomMarginPx = res.getDimensionPixelSize(R.dimen.drop_target_bottom_margin);
        dropTargetDragPaddingPx = res.getDimensionPixelSize(R.dimen.drop_target_drag_padding);
        dropTargetTextSizePx = res.getDimensionPixelSize(R.dimen.drop_target_text_size);
        dropTargetHorizontalPaddingPx = res.getDimensionPixelSize(
                R.dimen.drop_target_button_drawable_horizontal_padding);
        dropTargetVerticalPaddingPx = res.getDimensionPixelSize(
                R.dimen.drop_target_button_drawable_vertical_padding);
        dropTargetGapPx = res.getDimensionPixelSize(R.dimen.drop_target_button_gap);
        dropTargetButtonWorkspaceEdgeGapPx = res.getDimensionPixelSize(
                R.dimen.drop_target_button_workspace_edge_gap);
        workspaceSpringLoadedBottomSpace =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_min_spring_loaded_space);
        workspaceSpringLoadedMinNextPageVisiblePx = res.getDimensionPixelSize(
                R.dimen.dynamic_grid_spring_loaded_min_next_space_visible);
        workspaceCellPaddingXPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_cell_padding_x);
        hotseatQsbHeight = res.getDimensionPixelSize(R.dimen.qsb_widget_height);
        // Whether QSB might be inline in appropriate orientation (e.g. landscape).
        boolean canQsbInline = (isTwoPanels ? inv.inlineQsb[INDEX_TWO_PANEL_PORTRAIT]
                || inv.inlineQsb[INDEX_TWO_PANEL_LANDSCAPE]
                : inv.inlineQsb[INDEX_DEFAULT] || inv.inlineQsb[INDEX_LANDSCAPE])
                && hotseatQsbHeight > 0;
        isQsbInline = inv.inlineQsb[mTypeIndex] && canQsbInline;
        // We shrink hotseat sizes regardless of orientation, if nav buttons are inline and QSB
        // might be inline in either orientations, to keep hotseat size consistent across rotation.
        areNavButtonsInline = isTaskbarPresent && !isGestureMode;
        if (areNavButtonsInline && canQsbInline) {
            numShownHotseatIcons = inv.numShrunkenHotseatIcons;
        } else {
            numShownHotseatIcons =
                    isTwoPanels ? inv.numDatabaseHotseatIcons : inv.numShownHotseatIcons;
        }
        numShownAllAppsColumns =
                isTwoPanels ? inv.numDatabaseAllAppsColumns : inv.numAllAppsColumns;
        hotseatBarSizeExtraSpacePx = 0;
        hotseatBarTopPaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_top_padding);
        if (isQsbInline) {
            hotseatBarBottomPaddingPx = res.getDimensionPixelSize(R.dimen.inline_qsb_bottom_margin);
        } else {
            hotseatBarBottomPaddingPx = (isTallDevice ? res.getDimensionPixelSize(
                    R.dimen.dynamic_grid_hotseat_bottom_tall_padding)
                    : res.getDimensionPixelSize(
                    R.dimen.dynamic_grid_hotseat_bottom_non_tall_padding))
                    + res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_bottom_padding);
        }
        springLoadedHotseatBarTopMarginPx = res.getDimensionPixelSize(
                R.dimen.spring_loaded_hotseat_top_margin);
        hotseatBarSidePaddingEndPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_side_padding);
        // Add a bit of space between nav bar and hotseat in vertical bar layout.
        hotseatBarSidePaddingStartPx = isVerticalBarLayout() ? workspacePageIndicatorHeight : 0;
        hotseatExtraVerticalSize =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_extra_vertical_size);
        updateHotseatIconSize(pxFromDp(inv.iconSize[INDEX_DEFAULT], mMetrics));
        qsbBottomMarginOriginalPx = isScalableGrid
                ? res.getDimensionPixelSize(R.dimen.scalable_grid_qsb_bottom_margin)
                : 0;
        overviewTaskMarginPx = res.getDimensionPixelSize(R.dimen.overview_task_margin);
        overviewTaskMarginGridPx = res.getDimensionPixelSize(R.dimen.overview_task_margin_grid);
        overviewTaskIconSizePx = res.getDimensionPixelSize(R.dimen.task_thumbnail_icon_size);
        overviewTaskIconDrawableSizePx =
                res.getDimensionPixelSize(R.dimen.task_thumbnail_icon_drawable_size);
        overviewTaskIconDrawableSizeGridPx =
                res.getDimensionPixelSize(R.dimen.task_thumbnail_icon_drawable_size_grid);
        overviewTaskThumbnailTopMarginPx = overviewTaskIconSizePx + overviewTaskMarginPx * 2;
        // In vertical bar, use the smaller task margin for the top regardless of mode.
        overviewActionsTopMarginPx = isVerticalBarLayout()
                ? overviewTaskMarginPx
                : res.getDimensionPixelSize(R.dimen.overview_actions_top_margin);
        overviewPageSpacing = res.getDimensionPixelSize(R.dimen.overview_page_spacing);
        overviewActionsButtonSpacing = res.getDimensionPixelSize(
                R.dimen.overview_actions_button_spacing);
        overviewActionsHeight = res.getDimensionPixelSize(R.dimen.overview_actions_height);
        // Grid task's top margin is only overviewTaskIconSizePx + overviewTaskMarginGridPx, but
        // overviewTaskThumbnailTopMarginPx is applied to all TaskThumbnailView, so exclude the
        // extra  margin when calculating row spacing.
        int extraTopMargin = overviewTaskThumbnailTopMarginPx - overviewTaskIconSizePx
                - overviewTaskMarginGridPx;
        overviewRowSpacing = res.getDimensionPixelSize(R.dimen.overview_grid_row_spacing)
                - extraTopMargin;
        overviewGridSideMargin = res.getDimensionPixelSize(R.dimen.overview_grid_side_margin);

        splitPlaceholderInset = res.getDimensionPixelSize(R.dimen.split_placeholder_inset);
        // Calculate all of the remaining variables.
        extraSpace = updateAvailableDimensions(res);
        // Now that we have all of the variables calculated, we can tune certain sizes.
        if (isScalableGrid && inv.devicePaddings != null) {
            // Paddings were created assuming no scaling, so we first unscale the extra space.
            int unscaledExtraSpace = (int) (extraSpace / cellScaleToFit);
            DevicePadding padding = inv.devicePaddings.getDevicePadding(unscaledExtraSpace);
            int paddingWorkspaceTop = padding.getWorkspaceTopPadding(unscaledExtraSpace);
            int paddingWorkspaceBottom = padding.getWorkspaceBottomPadding(unscaledExtraSpace);
            int paddingHotseatBottom = padding.getHotseatBottomPadding(unscaledExtraSpace);
            workspaceTopPadding = Math.round(paddingWorkspaceTop * cellScaleToFit);
            workspaceBottomPadding = Math.round(paddingWorkspaceBottom * cellScaleToFit);
            extraHotseatBottomPadding = Math.round(paddingHotseatBottom * cellScaleToFit);
            hotseatBarSizePx += extraHotseatBottomPadding;
            qsbBottomMarginPx = Math.round(qsbBottomMarginOriginalPx * cellScaleToFit);
        }/* else if (!isVerticalBarLayout() && isPhone && isTallDevice) {
            // We increase the hotseat size when there is extra space.
            if (Float.compare(aspectRatio, TALLER_DEVICE_ASPECT_RATIO_THRESHOLD) >= 0
                    && extraSpace >= Utilities.dpToPx(TALL_DEVICE_EXTRA_SPACE_THRESHOLD_DP)) {
                // For taller devices, we will take a piece of the extra space from each row,
                // and add it to the space above and below the hotseat.
                // For devices with more extra space, we take a larger piece from each cell.
                int piece = extraSpace < Utilities.dpToPx(TALL_DEVICE_MORE_EXTRA_SPACE_THRESHOLD_DP)
                        ? 7 : 5;
                int extraSpace = ((getCellSize().y - iconSizePx - iconDrawablePaddingPx * 2)
                        * inv.numRows) / piece;
                workspaceTopPadding = extraSpace / 8;
                int halfLeftOver = (extraSpace - workspaceTopPadding) / 2;
                hotseatBarTopPaddingPx += halfLeftOver;
                hotseatBarSizeExtraSpacePx = halfLeftOver;
            } else {
                // ie. For a display with a large aspect ratio, we can keep the icons on the
                // workspace in portrait mode closer together by adding more height to the hotseat.
                // Note: This calculation was created after noticing a pattern in the design spec.
                hotseatBarSizeExtraSpacePx = getCellSize().y - iconSizePx
                        - iconDrawablePaddingPx * 2 - workspacePageIndicatorHeight;
            }
            updateHotseatIconSize(iconSizePx);
            // Recalculate the available dimensions using the new hotseat size.
            updateAvailableDimensions(res);
        }*/

        updateHotseatIconSize(iconSizePx);
        // Recalculate the available dimensions using the new hotseat size.
        updateAvailableDimensions(res);
        int cellLayoutPadding =
                isTwoPanels ? cellLayoutBorderSpacePx.x / 2 : res.getDimensionPixelSize(
                        R.dimen.cell_layout_padding);

        int cellLayoutPaddingX = fullWidthWidgets ? 0 : cellLayoutPadding;
        cellLayoutPaddingPx = new Rect(cellLayoutPaddingX, cellLayoutPadding, cellLayoutPaddingX, cellLayoutPadding);
        updateHotseatScale(res);
        updateWorkspacePadding();
        // Hotseat and QSB width depends on updated cellSize and workspace padding
        hotseatBorderSpace = calculateHotseatBorderSpace();
        qsbWidth = calculateQsbWidth();

        allAppsOpenDuration = res.getInteger(R.integer.config_allAppsOpenDuration);
        allAppsCloseDuration = res.getInteger(R.integer.config_allAppsCloseDuration);
        flingToDeleteThresholdVelocity = res.getDimensionPixelSize(
                R.dimen.drag_flingToDeleteMinVelocity);

        mViewScaleProvider = viewScaleProvider;

        dimensionOverrideProvider.accept(this);

        // This is done last, after iconSizePx is calculated above.
        mDotRendererWorkSpace = createDotRenderer(context, iconSizePx, dotRendererCache);
        mDotRendererAllApps = createDotRenderer(context, allAppsIconSizePx, dotRendererCache);
    }

    private DotRenderer createDotRenderer(
            @NonNull Context context, int size, @NonNull SparseArray<DotRenderer> cache) {
        DotRenderer renderer = cache.get(size);
        // This is done last, after iconSizePx is calculated above.
        float dotSize = 0.228f;
        if (prefs.getNotificationCount().getValue()) {
            dotSize = 0.38f;
        }

        if (renderer == null) {
            renderer = new DotRenderer(size, getShapePath(context,DEFAULT_DOT_SIZE), DEFAULT_DOT_SIZE);
            cache.put(size, renderer);
        }
        return renderer;
    }

    /**
     * QSB width is always calculated because when in 3 button nav the width doesn't follow the
     * width of the hotseat.
     */
    private int calculateQsbWidth() {
        if (isQsbInline) {
            int columns = getPanelCount() * inv.numColumns;
            return getIconToIconWidthForColumns(columns)
                    - iconSizePx * numShownHotseatIcons
                    - hotseatBorderSpace * numShownHotseatIcons;
        } else {
            int columns = inv.hotseatColumnSpan[mTypeIndex];
            return getIconToIconWidthForColumns(columns);
        }
    }
    private int getIconToIconWidthForColumns(int columns) {
        return columns * getCellSize().x
                + (columns - 1) * cellLayoutBorderSpacePx.x
                - (getCellSize().x - iconSizePx);  // left and right cell space
    }

    private int getHorizontalMarginPx(InvariantDeviceProfile idp, Resources res) {
        if (isVerticalBarLayout()) {
            return 0;
        }
        return isScalableGrid
                ? pxFromDp(idp.horizontalMargin[mTypeIndex], mMetrics)
                : res.getDimensionPixelSize(R.dimen.dynamic_grid_left_right_margin);
    }

    private void updateHotseatIconSize(int hotseatIconSizePx) {
        // Ensure there is enough space for folder icons, which have a slightly larger radius.
        hotseatCellHeightPx = (int) Math.ceil(hotseatIconSizePx * ICON_OVERLAP_FACTOR);
        if (prefs.getDockHide().getValue()) {
            hotseatBarSizePx = 0;
        } else if (isVerticalBarLayout()) {
            hotseatBarSizePx = hotseatIconSizePx + hotseatBarSidePaddingStartPx
                    + hotseatBarSidePaddingEndPx;
        } else {
            hotseatBarSizePx = hotseatIconSizePx + hotseatBarTopPaddingPx
                    + hotseatBarBottomPaddingPx + (isScalableGrid ? 0 : hotseatExtraVerticalSize)
                    + hotseatBarSizeExtraSpacePx;
        }
    }
    private Point getCellLayoutBorderSpace(InvariantDeviceProfile idp) {
        return getCellLayoutBorderSpace(idp, 1f);
    }
    private Point getCellLayoutBorderSpace(InvariantDeviceProfile idp, float scale) {
        if (!isScalableGrid) {
            return new Point(0, 0);
        }
        int horizontalSpacePx = pxFromDp(idp.borderSpaces[mTypeIndex].x, mMetrics, scale);
        int verticalSpacePx = pxFromDp(idp.borderSpaces[mTypeIndex].y, mMetrics, scale);
        return new Point(horizontalSpacePx, verticalSpacePx);
    }
    public Info getDisplayInfo() {
        return mInfo;
    }
    /**
     * We inset the widget padding added by the system and instead rely on the border spacing
     * between cells to create reliable consistency between widgets
     */
    public boolean shouldInsetWidgets() {
        Rect widgetPadding = inv.defaultWidgetPadding;
        // Check all sides to ensure that the widget won't overlap into another cell, or into
        // status bar.
        return workspaceTopPadding > widgetPadding.top
                && cellLayoutBorderSpacePx.x > widgetPadding.left
                && cellLayoutBorderSpacePx.y > widgetPadding.top
                && cellLayoutBorderSpacePx.x > widgetPadding.right
                && cellLayoutBorderSpacePx.y > widgetPadding.bottom;
    }
    public Builder toBuilder(Context context) {
        WindowBounds bounds = new WindowBounds(
                widthPx, heightPx, availableWidthPx, availableHeightPx, rotationHint);
        bounds.bounds.offsetTo(windowX, windowY);
        bounds.insets.set(mInsets);
        return new Builder(context, inv, mInfo)
                .setWindowBounds(bounds)
                .setIsMultiDisplay(isMultiDisplay)
                .setMultiWindowMode(isMultiWindowMode)
                .setGestureMode(isGestureMode);
    }
    public DeviceProfile copy(Context context) {
        return toBuilder(context).build();
    }
    /**
     * TODO: Move this to the builder as part of setMultiWindowMode
     */
    public DeviceProfile getMultiWindowProfile(Context context, WindowBounds windowBounds) {
        DeviceProfile profile = toBuilder(context)
                .setWindowBounds(windowBounds)
                .setMultiWindowMode(true)
                .build();
        profile.hideWorkspaceLabelsIfNotEnoughSpace();
        // We use these scales to measure and layout the widgets using their full invariant profile
        // sizes and then draw them scaled and centered to fit in their multi-window mode cellspans.
        float appWidgetScaleX = (float) profile.getCellSize().x / getCellSize().x;
        float appWidgetScaleY = (float) profile.getCellSize().y / getCellSize().y;
        profile.appWidgetScale.set(appWidgetScaleX, appWidgetScaleY);
        return profile;
    }
    /**
     * Checks if there is enough space for labels on the workspace.
     * If there is not, labels on the Workspace are hidden.
     * It is important to call this method after the All Apps variables have been set.
     */
    private void hideWorkspaceLabelsIfNotEnoughSpace() {
        float iconTextHeight = Utilities.calculateTextHeight(iconTextSizePx);
        float workspaceCellPaddingY = getCellSize().y - iconSizePx - iconDrawablePaddingPx
                - iconTextHeight;
        // We want enough space so that the text is closer to its corresponding icon.
        if (workspaceCellPaddingY < iconTextHeight) {
            iconTextSizePx = 0;
            iconDrawablePaddingPx = 0;
            cellHeightPx = (int) Math.ceil(iconSizePx * ICON_OVERLAP_FACTOR);
            autoResizeAllAppsCells();
        }
    }
    /**
     * Re-computes the all-apps cell size to be independent of workspace
     */
    public void autoResizeAllAppsCells() {
        int textHeight = Utilities.calculateTextHeight(allAppsIconTextSizePx);
        int topBottomPadding = textHeight;
        allAppsCellHeightPx = allAppsIconSizePx + allAppsIconDrawablePaddingPx
                + textHeight + (topBottomPadding * 2);
    }
    private void updateAllAppsContainerWidth(Resources res) {
        int cellLayoutHorizontalPadding =
                (cellLayoutPaddingPx.left + cellLayoutPaddingPx.right) / 2;
        if (isTablet) {
            allAppsLeftRightPadding =
                    res.getDimensionPixelSize(R.dimen.all_apps_bottom_sheet_horizontal_padding);
            int usedWidth = (allAppsCellWidthPx * numShownAllAppsColumns)
                    + (allAppsBorderSpacePx.x * (numShownAllAppsColumns - 1))
                    + allAppsLeftRightPadding * 2;
            allAppsLeftRightMargin = Math.max(1, (availableWidthPx - usedWidth) / 2);
        } else {
            allAppsLeftRightPadding = desiredWorkspaceHorizontalMarginPx + cellLayoutHorizontalPadding;
        }
    }

    private void setupAllAppsStyle(Context context) {
        TypedArray allAppsStyle;
        if (inv.allAppsStyle != INVALID_RESOURCE_HANDLE) {
            allAppsStyle = context.obtainStyledAttributes(inv.allAppsStyle,
                    R.styleable.AllAppsStyle);
        } else {
            allAppsStyle = context.obtainStyledAttributes(R.style.AllAppsStyleDefault,
                    R.styleable.AllAppsStyle);
        }
        allAppsLeftRightPadding = allAppsStyle.getDimensionPixelSize(
                R.styleable.AllAppsStyle_horizontalPadding, 0);
        allAppsStyle.recycle();
    }

    /**
     * Returns the amount of extra (or unused) vertical space.
     */
    private int updateAvailableDimensions(Resources res) {
        updateIconSize(1f, res);
        updateWorkspacePadding();
        // Check to see if the icons fit within the available height.
        float usedHeight = getCellLayoutHeightSpecification();
        final int maxHeight = getCellLayoutHeight();
        float extraHeight = Math.max(0, maxHeight - usedHeight);
        float scaleY = maxHeight / usedHeight;
        boolean shouldScale = scaleY < 1f;
        float scaleX = 1f;
        if (isScalableGrid) {
            // We scale to fit the cellWidth and cellHeight in the available space.
            // The benefit of scalable grids is that we can get consistent aspect ratios between
            // devices.
            float usedWidth =
                    getCellLayoutWidthSpecification() + (desiredWorkspaceHorizontalMarginPx * 2);
            // We do not subtract padding here, as we also scale the workspace padding if needed.
            scaleX = availableWidthPx / usedWidth;
            shouldScale = true;
        }
        if (shouldScale) {
            float scale = Math.min(scaleX, scaleY);
            updateIconSize(scale, res);
            extraHeight = Math.max(0, maxHeight - getCellLayoutHeightSpecification());
        }
        updateAvailableFolderCellDimensions(res);
        return Math.round(extraHeight);
    }
    private int getCellLayoutHeightSpecification() {
        return (cellHeightPx * inv.numRows) + (cellLayoutBorderSpacePx.y * (inv.numRows - 1))
                + cellLayoutPaddingPx.top + cellLayoutPaddingPx.bottom;
    }
    private int getCellLayoutWidthSpecification() {
        int numColumns = getPanelCount() * inv.numColumns;
        return (cellWidthPx * numColumns) + (cellLayoutBorderSpacePx.x * (numColumns - 1))
                + cellLayoutPaddingPx.left + cellLayoutPaddingPx.right;
    }
    /**
     * Updating the iconSize affects many aspects of the launcher layout, such as: iconSizePx,
     * iconTextSizePx, iconDrawablePaddingPx, cellWidth/Height, allApps* variants,
     * hotseat sizes, workspaceSpringLoadedShrinkFactor, folderIconSizePx, and folderIconOffsetYPx.
     */
    public void updateIconSize(float scale, Resources res) {
        // Icon scale should never exceed 1, otherwise pixellation may occur.
        iconScale = Math.min(1f, scale);
        cellScaleToFit = scale;
        // Workspace
        final boolean isVerticalLayout = isVerticalBarLayout();
        float invIconSizeDp = inv.iconSize[mTypeIndex];
        float invIconTextSizeSp = inv.iconTextSize[mTypeIndex];
        iconSizePx = Math.max(1, pxFromDp(invIconSizeDp, mMetrics, iconScale));
        iconTextSizePx = (int) (pxFromSp(invIconTextSizeSp, mMetrics) * iconScale);
        iconTextSizePx *= mTextFactors.getIconTextSizeFactor();
        iconDrawablePaddingPx = (int) (iconDrawablePaddingOriginalPx * iconScale);
        cellLayoutBorderSpacePx = getCellLayoutBorderSpace(inv, scale);
        if (isScalableGrid) {
            cellWidthPx = pxFromDp(inv.minCellSize[mTypeIndex].x, mMetrics, scale);
            cellHeightPx = pxFromDp(inv.minCellSize[mTypeIndex].y, mMetrics, scale);
            int cellContentHeight = iconSizePx + iconDrawablePaddingPx
                    + Utilities.calculateTextHeight(iconTextSizePx);
            cellYPaddingPx = Math.max(0, cellHeightPx - cellContentHeight) / 2;
            desiredWorkspaceHorizontalMarginPx =
                    (int) (desiredWorkspaceHorizontalMarginOriginalPx * scale);
        } else {
            cellWidthPx = iconSizePx + iconDrawablePaddingPx;
            cellHeightPx = (int) Math.ceil(iconSizePx * ICON_OVERLAP_FACTOR)
                    + iconDrawablePaddingPx
                    + Utilities.calculateTextHeight(iconTextSizePx);
            int cellPaddingY = (getCellSize().y - cellHeightPx) / 2;
            if (iconDrawablePaddingPx > cellPaddingY && !isVerticalLayout
                    && !isMultiWindowMode) {
                // Ensures that the label is closer to its corresponding icon. This is not an issue
                // with vertical bar layout or multi-window mode since the issue is handled
                // separately with their calls to {@link #adjustToHideWorkspaceLabels}.
                cellHeightPx -= (iconDrawablePaddingPx - cellPaddingY);
                iconDrawablePaddingPx = cellPaddingY;
            }
        }
        // All apps
        updateAllAppsIconSize(scale, res);

        updateHotseatIconSize(iconSizePx);

        // Folder icon
        folderIconSizePx = IconNormalizer.getNormalizedCircleSize(iconSizePx);
        folderIconOffsetYPx = (iconSizePx - folderIconSizePx) / 2;
    }
    /**
     * Hotseat width spans a certain number of columns on scalable grids.
     * This method calculates the space between the icons to achieve that width.
     */
    private int calculateHotseatBorderSpace() {
        if (!isScalableGrid) return 0;
        //TODO(http://b/228998082) remove this when 3 button spaces are fixed
        if (areNavButtonsInline) {
            return pxFromDp(inv.hotseatBorderSpaces[mTypeIndex], mMetrics);
        } else {
            int columns = inv.hotseatColumnSpan[mTypeIndex];
            float hotseatWidthPx = getIconToIconWidthForColumns(columns);
            float hotseatIconsTotalPx = iconSizePx * numShownHotseatIcons;
            return (int) (hotseatWidthPx - hotseatIconsTotalPx) / (numShownHotseatIcons - 1);
        }
    }
    /**
     * Updates the iconSize for allApps* variants.
     */
    private void updateAllAppsIconSize(float scale, Resources res) {
        allAppsBorderSpacePx = new Point(
                pxFromDp(inv.allAppsBorderSpaces[mTypeIndex].x, mMetrics, scale),
                pxFromDp(inv.allAppsBorderSpaces[mTypeIndex].y, mMetrics, scale));
        // AllApps cells don't have real space between cells,
        // so we add the border space to the cell height
        allAppsCellHeightPx = pxFromDp(inv.allAppsCellSize[mTypeIndex].y, mMetrics, scale)
                + allAppsBorderSpacePx.y;
        // but width is just the cell,
        // the border is added in #updateAllAppsContainerWidth
        allAppsCellWidthPx = pxFromDp(inv.allAppsCellSize[mTypeIndex].x, mMetrics, scale);
        if (isScalableGrid) {
            allAppsIconSizePx =
                    pxFromDp(inv.allAppsIconSize[mTypeIndex], mMetrics, scale);
            allAppsIconTextSizePx = pxFromSp(inv.allAppsIconTextSize[mTypeIndex], mMetrics, scale);
            allAppsIconTextSizePx *= mTextFactors.getAllAppsIconTextSizeFactor();
            allAppsIconDrawablePaddingPx = iconDrawablePaddingOriginalPx;
        } else {
            float invIconSizeDp = inv.allAppsIconSize[mTypeIndex];
            float invIconTextSizeSp = inv.allAppsIconTextSize[mTypeIndex];
            allAppsIconSizePx = Math.max(1, pxFromDp(invIconSizeDp, mMetrics, scale));
            allAppsIconTextSizePx = (int) (pxFromSp(invIconTextSizeSp, mMetrics) * scale);
            allAppsIconTextSizePx *= mTextFactors.getAllAppsIconTextSizeFactor();
            allAppsIconDrawablePaddingPx =
                    res.getDimensionPixelSize(R.dimen.all_apps_icon_drawable_padding);
        }
        updateAllAppsContainerWidth(res);
        if (isVerticalBarLayout()) {
            hideWorkspaceLabelsIfNotEnoughSpace();
        }
    }

    private void updateAvailableFolderCellDimensions(Resources res) {
        updateFolderCellSize(1f, res);

        // For usability we can't have the folder use the whole width of the screen
        Point totalWorkspacePadding = getTotalWorkspacePadding();

        // Check if the folder fit within the available height.
        float contentUsedHeight = folderCellHeightPx * inv.numFolderRows
                + ((inv.numFolderRows - 1) * folderCellLayoutBorderSpacePx)
                + folderFooterHeightPx
                + folderContentPaddingTop;
        int contentMaxHeight = availableHeightPx - totalWorkspacePadding.y;
        float scaleY = contentMaxHeight / contentUsedHeight;

        // Check if the folder fit within the available width.
        float contentUsedWidth = folderCellWidthPx * inv.numFolderColumns
                + ((inv.numFolderColumns - 1) * folderCellLayoutBorderSpacePx)
                + folderContentPaddingLeftRight * 2;
        int contentMaxWidth = availableWidthPx - totalWorkspacePadding.x;
        float scaleX = contentMaxWidth / contentUsedWidth;

        float scale = Math.min(scaleX, scaleY);
        if (scale < 1f) {
            updateFolderCellSize(scale, res);
        }
    }

    private void updateFolderCellSize(float scale, Resources res) {
        float invIconSizeDp = inv.iconSize[mTypeIndex];
        folderChildIconSizePx = Math.max(1, pxFromDp(invIconSizeDp, mMetrics, scale));
        folderChildTextSizePx = pxFromSp(inv.iconTextSize[mTypeIndex], mMetrics, scale);
        folderLabelTextSizePx = Math.max(pxFromSp(MIN_FOLDER_TEXT_SIZE_SP, mMetrics, scale),
                (int) (folderChildTextSizePx * folderLabelTextScale));

        int textHeight = Utilities.calculateTextHeight(folderChildTextSizePx);

        if (isScalableGrid) {
            if (inv.folderStyle == INVALID_RESOURCE_HANDLE) {
                folderCellWidthPx = roundPxValueFromFloat(getCellSize().x * scale);
                folderCellHeightPx = roundPxValueFromFloat(getCellSize().y * scale);
            } else {
                folderCellWidthPx = roundPxValueFromFloat(folderCellWidthPx * scale);
                folderCellHeightPx = roundPxValueFromFloat(folderCellHeightPx * scale);
            }

            folderContentPaddingTop = roundPxValueFromFloat(folderContentPaddingTop * scale);
            folderCellLayoutBorderSpacePx = roundPxValueFromFloat(
                    folderCellLayoutBorderSpacePx * scale);
            folderFooterHeightPx = roundPxValueFromFloat(folderFooterHeightPx * scale);

            folderContentPaddingLeftRight = folderCellLayoutBorderSpacePx;
        } else {
            int cellPaddingX = (int) (res.getDimensionPixelSize(R.dimen.folder_cell_x_padding)
                    * scale);
            int cellPaddingY = (int) (res.getDimensionPixelSize(R.dimen.folder_cell_y_padding)
                    * scale);

            folderCellWidthPx = folderChildIconSizePx + 2 * cellPaddingX;
            folderCellHeightPx = folderChildIconSizePx + 2 * cellPaddingY + textHeight;
            folderContentPaddingTop = roundPxValueFromFloat(folderContentPaddingTop * scale);
            folderContentPaddingLeftRight =
                    res.getDimensionPixelSize(R.dimen.folder_content_padding_left_right);
            folderFooterHeightPx =
                    roundPxValueFromFloat(
                            res.getDimensionPixelSize(R.dimen.folder_footer_height_default)
                                    * scale);

        }

        folderChildDrawablePaddingPx = Math.max(0,
                (folderCellHeightPx - folderChildIconSizePx - textHeight) / 3);
    }

    public void updateInsets(Rect insets) {
        mInsets.set(insets);
    }
    /**
     * The current device insets. This is generally same as the insets being dispatched to
     * {@link Insettable} elements, but can differ if the element is using a different profile.
     */
    public Rect getInsets() {
        return mInsets;
    }
    public Point getCellSize() {
        return getCellSize(null);
    }
    public Point getCellSize(Point result) {
        if (result == null) {
            result = new Point();
        }
        int shortcutAndWidgetContainerWidth =
                getCellLayoutWidth() - (cellLayoutPaddingPx.left + cellLayoutPaddingPx.right);
        result.x = calculateCellWidth(shortcutAndWidgetContainerWidth, cellLayoutBorderSpacePx.x,
                inv.numColumns);
        int shortcutAndWidgetContainerHeight =
                getCellLayoutHeight() - (cellLayoutPaddingPx.top + cellLayoutPaddingPx.bottom);
        result.y = calculateCellHeight(shortcutAndWidgetContainerHeight, cellLayoutBorderSpacePx.y,
                inv.numRows);
        return result;
    }

    /**
     * Gets the number of panels within the workspace.
     */
    public int getPanelCount() {
        return isTwoPanels ? 2 : 1;
    }

    /**
     * Gets the space in px from the bottom of last item in the vertical-bar hotseat to the
     * bottom of the screen.
     */
    public int getVerticalHotseatLastItemBottomOffset() {
        int cellHeight = calculateCellHeight(
                heightPx - mHotseatPadding.top - mHotseatPadding.bottom, hotseatBorderSpace,
                numShownHotseatIcons);
        int hotseatSize = (cellHeight * numShownHotseatIcons)
                + (hotseatBorderSpace * (numShownHotseatIcons - 1));
        int extraHotseatEndSpacing = (heightPx - hotseatSize) / 2;
        int extraIconEndSpacing = (cellHeight - iconSizePx) / 2;
        return extraHotseatEndSpacing + extraIconEndSpacing + mHotseatPadding.bottom;
    }

    /**
     * Gets the scaled top of the workspace in px for the spring-loaded edit state.
     */
    public float getCellLayoutSpringLoadShrunkTop() {
        workspaceSpringLoadShrunkTop = mInsets.top + dropTargetBarTopMarginPx + dropTargetBarSizePx
                + dropTargetBarBottomMarginPx;
        return workspaceSpringLoadShrunkTop;
    }

    /**
     * Gets the scaled bottom of the workspace in px for the spring-loaded edit state.
     */
    private float getCellLayoutSpringLoadShrunkBottom() {
        int topOfHotseat = hotseatBarSizePx + springLoadedHotseatBarTopMarginPx;
        workspaceSpringLoadShrunkBottom =
                heightPx - (isVerticalBarLayout() ? getVerticalHotseatLastItemBottomOffset()
                        : topOfHotseat);
        return workspaceSpringLoadShrunkBottom;
    }

    /**
     * Gets the scale of the workspace for the spring-loaded edit state.
     */
    public float getWorkspaceSpringLoadScale() {
        float scale = (getCellLayoutSpringLoadShrunkBottom() - getCellLayoutSpringLoadShrunkTop())
                / getCellLayoutHeight();
        scale = Math.min(scale, 1f);
        // Reduce scale if next pages would not be visible after scaling the workspace
        int workspaceWidth = availableWidthPx;
        float scaledWorkspaceWidth = workspaceWidth * scale;
        float maxAvailableWidth = workspaceWidth - (2 * workspaceSpringLoadedMinNextPageVisiblePx);
        if (scaledWorkspaceWidth > maxAvailableWidth) {
            scale *= maxAvailableWidth / scaledWorkspaceWidth;
        }
        return scale;
    }
    /**
     * Gets the width of a single Cell Layout, aka a single panel within a Workspace.
     *
     * <p>This is the width of a Workspace, less its horizontal padding. Note that two-panel
     * layouts have two Cell Layouts per workspace.
     */
    public int getCellLayoutWidth() {
        return (availableWidthPx - getTotalWorkspacePadding().x) / getPanelCount();
    }
    /**
     * Gets the height of a single Cell Layout, aka a single panel within a Workspace.
     *
     * <p>This is the height of a Workspace, less its vertical padding.
     */
    public int getCellLayoutHeight() {
        return availableHeightPx - getTotalWorkspacePadding().y;
    }
    public Point getTotalWorkspacePadding() {
        return new Point(workspacePadding.left + workspacePadding.right,
                workspacePadding.top + workspacePadding.bottom);
    }

    private void updateHotseatScale(Resources res) {
        float targetDockScale = prefs.getDockScale().getValue();

        int previousDockSize = hotseatBarSizePx;
        int previousDockBottomPadding = hotseatBarBottomPaddingPx;
        if (prefs.getDockHide().getValue()) {
            hotseatBarSizePx = 0;
            updateAvailableDimensions(res);
        } else if (targetDockScale > 0f && !isVerticalBarLayout()) {
            int extraSpace = (int) (targetDockScale * previousDockSize - hotseatBarSizePx);
            if (extraSpace != 0) {
                hotseatBarSizePx += extraSpace;

                int dockTopSpace = workspacePageIndicatorHeight - mWorkspacePageIndicatorOverlapWorkspace;
                int dockBottomSpace =
                        Math.max(hotseatBarBottomPaddingPx - previousDockBottomPadding, dockTopSpace);
                int dockVerticalSpace = dockTopSpace + dockBottomSpace;

                hotseatBarBottomPaddingPx += extraSpace * ((float) dockBottomSpace / dockVerticalSpace);

                updateAvailableDimensions(res);
            }
        }
    }

    /**
     * Updates {@link #workspacePadding} as a result of any internal value change to reflect the
     * new workspace padding
     */
    private void updateWorkspacePadding() {
        Rect padding = workspacePadding;
        if (isVerticalBarLayout()) {
            padding.top = 0;
            padding.bottom = edgeMarginPx;
            if (isSeascape()) {
                padding.left = hotseatBarSizePx;
                padding.right = hotseatBarSidePaddingStartPx;
            } else {
                padding.left = hotseatBarSidePaddingStartPx;
                padding.right = hotseatBarSizePx;
            }
        } else {
            // Pad the bottom of the workspace with search/hotseat bar sizes
            int hotseatTop = hotseatBarSizePx;
            int paddingBottom = hotseatTop + workspacePageIndicatorHeight
                    + workspaceBottomPadding - mWorkspacePageIndicatorOverlapWorkspace;
            int paddingTop = workspaceTopPadding + (isScalableGrid ? 0 : edgeMarginPx);

            // Pad the top and bottom of the workspace with search/hotseat bar sizes
            int horizontalPadding = prefs.getDesktopAllowFullWidthWidgets().getValue() ? 0 : desiredWorkspaceHorizontalMarginPx;

            // Pad the top and bottom of the workspace with search/hotseat bar sizes
            padding.set(horizontalPadding, paddingTop, horizontalPadding, paddingBottom);
        }
        insetPadding(workspacePadding, cellLayoutPaddingPx);
    }
    private void insetPadding(Rect paddings, Rect insets) {
        insets.left = Math.min(insets.left, paddings.left);
        paddings.left -= insets.left;
        insets.top = Math.min(insets.top, paddings.top);
        paddings.top -= insets.top;
        insets.right = Math.min(insets.right, paddings.right);
        paddings.right -= insets.right;
        insets.bottom = Math.min(insets.bottom, paddings.bottom);
        paddings.bottom -= insets.bottom;
    }
    /**
     * Returns the padding for hotseat view
     */
    public Rect getHotseatLayoutPadding(Context context) {
        if (isVerticalBarLayout()) {
            // The hotseat icons will be placed in the middle of the hotseat cells.
            // Changing the hotseatCellHeightPx is not affecting hotseat icon positions
            // in vertical bar layout.
            // Workspace icons are moved up by a small factor. The variable diffOverlapFactor
            // is set to account for that difference.
            float diffOverlapFactor = iconSizePx * (ICON_OVERLAP_FACTOR - 1) / 2;
            int paddingTop = Math.max((int) (mInsets.top + cellLayoutPaddingPx.top
                    - diffOverlapFactor), 0);
            int paddingBottom = Math.max((int) (mInsets.bottom + cellLayoutPaddingPx.bottom
                    + diffOverlapFactor), 0);
            if (isSeascape()) {
                mHotseatPadding.set(mInsets.left + hotseatBarSidePaddingStartPx, paddingTop,
                        hotseatBarSidePaddingEndPx, paddingBottom);
            } else {
                mHotseatPadding.set(hotseatBarSidePaddingEndPx, paddingTop,
                        mInsets.right + hotseatBarSidePaddingStartPx, paddingBottom);
            }
        } else if (isTaskbarPresent) {
            // Center the QSB vertically with hotseat
            int hotseatBottomPadding = getHotseatBottomPadding();
            int hotseatTopPadding =
                    workspacePadding.bottom - hotseatBottomPadding - hotseatCellHeightPx;
            // Push icons to the side
            int additionalQsbSpace = isQsbInline ? qsbWidth + hotseatBorderSpace : 0;
            int requiredWidth = iconSizePx * numShownHotseatIcons
                    + hotseatBorderSpace * (numShownHotseatIcons - 1)
                    + additionalQsbSpace;
            int endOffset = ApiWrapper.getHotseatEndOffset(context);
            int hotseatWidth = Math.min(requiredWidth, availableWidthPx - endOffset);
            int sideSpacing = (availableWidthPx - hotseatWidth) / 2;
            mHotseatPadding.set(sideSpacing, hotseatTopPadding, sideSpacing, hotseatBottomPadding);
            boolean isRtl = Utilities.isRtl(context.getResources());
            if (isRtl) {
                mHotseatPadding.right += additionalQsbSpace;
            } else {
                mHotseatPadding.left += additionalQsbSpace;
            }
            if (endOffset > sideSpacing) {
                int diff = isRtl
                        ? sideSpacing - endOffset
                        : endOffset - sideSpacing;
                mHotseatPadding.left -= diff;
                mHotseatPadding.right += diff;
            }
        } else if (isScalableGrid) {
            int sideSpacing = (availableWidthPx - qsbWidth) / 2;
            mHotseatPadding.set(sideSpacing,
                    hotseatBarTopPaddingPx,
                    sideSpacing,
                    hotseatBarSizePx - hotseatCellHeightPx - hotseatBarTopPaddingPx
                            + mInsets.bottom);
        } else {
            // We want the edges of the hotseat to line up with the edges of the workspace, but the
            // icons in the hotseat are a different size, and so don't line up perfectly. To account
            // for this, we pad the left and right of the hotseat with half of the difference of a
            // workspace cell vs a hotseat cell.
            float workspaceCellWidth = (float) widthPx / inv.numColumns;
            float hotseatCellWidth = (float) widthPx / numShownHotseatIcons;
            int hotseatAdjustment = Math.round((workspaceCellWidth - hotseatCellWidth) / 2);
            mHotseatPadding.set(hotseatAdjustment + workspacePadding.left + cellLayoutPaddingPx.left
                            + mInsets.left, hotseatBarTopPaddingPx,
                    hotseatAdjustment + workspacePadding.right + cellLayoutPaddingPx.right
                            + mInsets.right,
                    hotseatBarSizePx - hotseatCellHeightPx - hotseatBarTopPaddingPx
                            + mInsets.bottom);
        }
        return mHotseatPadding;
    }
    /**
     * Returns the number of pixels the QSB is translated from the bottom of the screen.
     */
    public int getQsbOffsetY() {
        if (isQsbInline) {
            return hotseatBarBottomPaddingPx;
        }
        int freeSpace = isTaskbarPresent
                ? workspacePadding.bottom
                : hotseatBarSizePx - hotseatCellHeightPx - hotseatQsbHeight;
        if (isScalableGrid && qsbBottomMarginPx > mInsets.bottom) {
            // Note that taskbarSize = 0 unless isTaskbarPresent.
            return Math.min(qsbBottomMarginPx + taskbarSize, freeSpace);
        } else {
            return (int) (freeSpace * mQsbCenterFactor)
                    + (isTaskbarPresent ? taskbarSize : mInsets.bottom);
        }
    }

    private int getHotseatBottomPadding() {
        if (isQsbInline) {
            return getQsbOffsetY() - (Math.abs(hotseatQsbHeight - hotseatCellHeightPx) / 2);
        } else {
            return (getQsbOffsetY() - taskbarSize) / 2;
        }
    }

    /**
     * Returns the number of pixels the taskbar is translated from the bottom of the screen.
     */
    public int getTaskbarOffsetY() {
        int taskbarIconBottomSpace = (taskbarSize - iconSizePx) / 2;
        int launcherIconBottomSpace =
                Math.min((hotseatCellHeightPx - iconSizePx) / 2, gridVisualizationPaddingY);
        return getHotseatBottomPadding() + launcherIconBottomSpace - taskbarIconBottomSpace;
    }
    /**
     * Returns the number of pixels required below OverviewActions excluding insets.
     */
    public int getOverviewActionsClaimedSpaceBelow() {
        if (isTaskbarPresent && !isGestureMode) {
            // Align vertically to where nav buttons are.
            return ((taskbarSize - overviewActionsHeight) / 2) + getTaskbarOffsetY();
        }
        return isTaskbarPresent ? stashedTaskbarSize : mInsets.bottom;
    }

    /**
     * Gets the space that the overview actions will take, including bottom margin.
     */
    public int getOverviewActionsClaimedSpace() {
        return overviewActionsTopMarginPx + overviewActionsHeight
                + getOverviewActionsClaimedSpaceBelow();
    }

    /**
     * Takes the View and return the scales of width and height depending on the DeviceProfile
     * specifications
     *
     * @param itemInfo The tag of the widget view
     * @return A PointF instance with the x set to be the scale of width, and y being the scale of
     * height
     */
    @NonNull
    public PointF getAppWidgetScale(@Nullable final ItemInfo itemInfo) {
        return mViewScaleProvider.getScaleFromItemInfo(itemInfo);
    }

    /**
     * @return the bounds for which the open folders should be contained within
     */
    public Rect getAbsoluteOpenFolderBounds() {
        if (isVerticalBarLayout()) {
            // Folders should only appear right of the drop target bar and left of the hotseat
            return new Rect(mInsets.left + dropTargetBarSizePx + edgeMarginPx,
                    mInsets.top,
                    mInsets.left + availableWidthPx - hotseatBarSizePx - edgeMarginPx,
                    mInsets.top + availableHeightPx);
        } else {
            // Folders should only appear below the drop target bar and above the hotseat
            int hotseatTop = isTaskbarPresent ? taskbarSize : hotseatBarSizePx;
            return new Rect(mInsets.left + edgeMarginPx,
                    mInsets.top + dropTargetBarSizePx + edgeMarginPx,
                    mInsets.left + availableWidthPx - edgeMarginPx,
                    mInsets.top + availableHeightPx - hotseatTop
                            - workspacePageIndicatorHeight - edgeMarginPx);
        }
    }
    public static int calculateCellWidth(int width, int borderSpacing, int countX) {
        return (width - ((countX - 1) * borderSpacing)) / countX;
    }
    public static int calculateCellHeight(int height, int borderSpacing, int countY) {
        return (height - ((countY - 1) * borderSpacing)) / countY;
    }
    /**
     * When {@code true}, the device is in landscape mode and the hotseat is on the right column.
     * When {@code false}, either device is in portrait mode or the device is in landscape mode and
     * the hotseat is on the bottom row.
     */
    public boolean isVerticalBarLayout() {
        return isLandscape && transposeLayoutWithOrientation;
    }
    /**
     * Updates orientation information and returns true if it has changed from the previous value.
     */
    public boolean updateIsSeascape(Context context) {
        if (isVerticalBarLayout()) {
            boolean isSeascape = DisplayController.INSTANCE.get(context)
                    .getInfo().rotation == Surface.ROTATION_270;
            if (mIsSeascape != isSeascape) {
                mIsSeascape = isSeascape;
                // Hotseat changing sides requires updating workspace left/right paddings
                updateWorkspacePadding();
                return true;
            }
        }
        return false;
    }
    public boolean isSeascape() {
        return isVerticalBarLayout() && mIsSeascape;
    }
    public boolean shouldFadeAdjacentWorkspaceScreens() {
        return isVerticalBarLayout();
    }
    public int getCellContentHeight(@ContainerType int containerType) {
        switch (containerType) {
            case CellLayout.WORKSPACE:
                return cellHeightPx;
            case CellLayout.FOLDER:
                return folderCellHeightPx;
            case CellLayout.HOTSEAT:
                // The hotseat is the only container where the cell height is going to be
                // different from the content within that cell.
                return iconSizePx;
            default:
                // ??
                return 0;
        }
    }

    private String pxToDpStr(String name, float value) {
        return "\t" + name + ": " + value + "px (" + dpiFromPx(value, mMetrics.densityDpi) + "dp)";
    }

    public void dump(String prefix, PrintWriter writer) {
        writer.println(prefix + "DeviceProfile:");
        writer.println(prefix + "\t1 dp = " + mMetrics.density + " px");
        writer.println(prefix + "\tisTablet:" + isTablet);
        writer.println(prefix + "\tisPhone:" + isPhone);
        writer.println(prefix + "\ttransposeLayoutWithOrientation:"
                + transposeLayoutWithOrientation);
        writer.println(prefix + "\tisGestureMode:" + isGestureMode);
        writer.println(prefix + "\tisLandscape:" + isLandscape);
        writer.println(prefix + "\tisMultiWindowMode:" + isMultiWindowMode);
        writer.println(prefix + "\tisTwoPanels:" + isTwoPanels);
        writer.println(prefix + pxToDpStr("windowX", windowX));
        writer.println(prefix + pxToDpStr("windowY", windowY));
        writer.println(prefix + pxToDpStr("widthPx", widthPx));
        writer.println(prefix + pxToDpStr("heightPx", heightPx));
        writer.println(prefix + pxToDpStr("availableWidthPx", availableWidthPx));
        writer.println(prefix + pxToDpStr("availableHeightPx", availableHeightPx));
        writer.println(prefix + pxToDpStr("mInsets.left", mInsets.left));
        writer.println(prefix + pxToDpStr("mInsets.top", mInsets.top));
        writer.println(prefix + pxToDpStr("mInsets.right", mInsets.right));
        writer.println(prefix + pxToDpStr("mInsets.bottom", mInsets.bottom));
        writer.println(prefix + "\taspectRatio:" + aspectRatio);
        writer.println(prefix + "\tisScalableGrid:" + isScalableGrid);
        writer.println(prefix + "\tinv.numRows: " + inv.numRows);
        writer.println(prefix + "\tinv.numColumns: " + inv.numColumns);
        writer.println(prefix + "\tinv.numSearchContainerColumns: "
                + inv.numSearchContainerColumns);
        writer.println(prefix + "\tminCellSize: " + inv.minCellSize[mTypeIndex] + "dp");
        writer.println(prefix + pxToDpStr("cellWidthPx", cellWidthPx));
        writer.println(prefix + pxToDpStr("cellHeightPx", cellHeightPx));
        writer.println(prefix + pxToDpStr("getCellSize().x", getCellSize().x));
        writer.println(prefix + pxToDpStr("getCellSize().y", getCellSize().y));
        writer.println(prefix + pxToDpStr("cellLayoutBorderSpacePx Horizontal",
                cellLayoutBorderSpacePx.x));
        writer.println(prefix + pxToDpStr("cellLayoutBorderSpacePx Vertical",
                cellLayoutBorderSpacePx.y));
        writer.println(prefix + pxToDpStr("cellLayoutPaddingPx.left", cellLayoutPaddingPx.left));
        writer.println(prefix + pxToDpStr("cellLayoutPaddingPx.top", cellLayoutPaddingPx.top));
        writer.println(prefix + pxToDpStr("cellLayoutPaddingPx.right", cellLayoutPaddingPx.right));
        writer.println(
                prefix + pxToDpStr("cellLayoutPaddingPx.bottom", cellLayoutPaddingPx.bottom));
        writer.println(prefix + pxToDpStr("iconSizePx", iconSizePx));
        writer.println(prefix + pxToDpStr("iconTextSizePx", iconTextSizePx));
        writer.println(prefix + pxToDpStr("iconDrawablePaddingPx", iconDrawablePaddingPx));
        writer.println(prefix + pxToDpStr("folderCellWidthPx", folderCellWidthPx));
        writer.println(prefix + pxToDpStr("folderCellHeightPx", folderCellHeightPx));
        writer.println(prefix + pxToDpStr("folderChildIconSizePx", folderChildIconSizePx));
        writer.println(prefix + pxToDpStr("folderChildTextSizePx", folderChildTextSizePx));
        writer.println(prefix + pxToDpStr("folderChildDrawablePaddingPx",
                folderChildDrawablePaddingPx));
        writer.println(prefix + pxToDpStr("folderCellLayoutBorderSpaceOriginalPx",
                folderCellLayoutBorderSpaceOriginalPx));
        writer.println(prefix + pxToDpStr("folderCellLayoutBorderSpacePx",
                folderCellLayoutBorderSpacePx));
        writer.println(prefix + pxToDpStr("folderContentPaddingLeftRight",
                folderContentPaddingLeftRight));
        writer.println(prefix + pxToDpStr("folderTopPadding", folderContentPaddingTop));
        writer.println(prefix + pxToDpStr("folderFooterHeight", folderFooterHeightPx));
        writer.println(prefix + pxToDpStr("bottomSheetTopPadding", bottomSheetTopPadding));
        writer.println(prefix + "\tbottomSheetOpenDuration: " + bottomSheetOpenDuration);
        writer.println(prefix + "\tbottomSheetCloseDuration: " + bottomSheetCloseDuration);
        writer.println(prefix + pxToDpStr("allAppsShiftRange", allAppsShiftRange));
        writer.println(prefix + pxToDpStr("allAppsTopPadding", allAppsTopPadding));
        writer.println(prefix + "\tallAppsOpenDuration: " + allAppsOpenDuration);
        writer.println(prefix + "\tallAppsCloseDuration: " + allAppsCloseDuration);
        writer.println(prefix + pxToDpStr("allAppsIconSizePx", allAppsIconSizePx));
        writer.println(prefix + pxToDpStr("allAppsIconTextSizePx", allAppsIconTextSizePx));
        writer.println(prefix + pxToDpStr("allAppsIconDrawablePaddingPx",
                allAppsIconDrawablePaddingPx));
        writer.println(prefix + pxToDpStr("allAppsCellHeightPx", allAppsCellHeightPx));
        writer.println(prefix + pxToDpStr("allAppsCellWidthPx", allAppsCellWidthPx));
        writer.println(prefix + pxToDpStr("allAppsBorderSpacePx", allAppsBorderSpacePx.x));
        writer.println(prefix + "\tnumShownAllAppsColumns: " + numShownAllAppsColumns);
        writer.println(prefix + pxToDpStr("allAppsLeftRightPadding", allAppsLeftRightPadding));
        writer.println(prefix + pxToDpStr("allAppsLeftRightMargin", allAppsLeftRightMargin));
        writer.println(prefix + pxToDpStr("hotseatBarSizePx", hotseatBarSizePx));
        writer.println(prefix + "\tinv.hotseatColumnSpan: " + inv.hotseatColumnSpan[mTypeIndex]);
        writer.println(prefix + pxToDpStr("hotseatCellHeightPx", hotseatCellHeightPx));
        writer.println(prefix + pxToDpStr("hotseatBarTopPaddingPx", hotseatBarTopPaddingPx));
        writer.println(prefix + pxToDpStr("hotseatBarBottomPaddingPx", hotseatBarBottomPaddingPx));
        writer.println(prefix + pxToDpStr("hotseatBarSidePaddingStartPx",
                hotseatBarSidePaddingStartPx));
        writer.println(prefix + pxToDpStr("hotseatBarSidePaddingEndPx",
                hotseatBarSidePaddingEndPx));
        writer.println(prefix + pxToDpStr("springLoadedHotseatBarTopMarginPx",
                springLoadedHotseatBarTopMarginPx));
        writer.println(prefix + pxToDpStr("mHotseatPadding.top", mHotseatPadding.top));
        writer.println(prefix + pxToDpStr("mHotseatPadding.bottom", mHotseatPadding.bottom));
        writer.println(prefix + pxToDpStr("mHotseatPadding.left", mHotseatPadding.left));
        writer.println(prefix + pxToDpStr("mHotseatPadding.right", mHotseatPadding.right));
        writer.println(prefix + "\tnumShownHotseatIcons: " + numShownHotseatIcons);
        writer.println(prefix + pxToDpStr("hotseatBorderSpace", hotseatBorderSpace));
        writer.println(prefix + "\tisQsbInline: " + isQsbInline);
        writer.println(prefix + pxToDpStr("qsbWidth", qsbWidth));
        writer.println(prefix + "\tisTaskbarPresent:" + isTaskbarPresent);
        writer.println(prefix + "\tisTaskbarPresentInApps:" + isTaskbarPresentInApps);
        writer.println(prefix + pxToDpStr("taskbarSize", taskbarSize));
        writer.println(prefix + pxToDpStr("desiredWorkspaceHorizontalMarginPx",
                desiredWorkspaceHorizontalMarginPx));
        writer.println(prefix + pxToDpStr("workspacePadding.left", workspacePadding.left));
        writer.println(prefix + pxToDpStr("workspacePadding.top", workspacePadding.top));
        writer.println(prefix + pxToDpStr("workspacePadding.right", workspacePadding.right));
        writer.println(prefix + pxToDpStr("workspacePadding.bottom", workspacePadding.bottom));
        writer.println(prefix + pxToDpStr("iconScale", iconScale));
        writer.println(prefix + pxToDpStr("cellScaleToFit ", cellScaleToFit));
        writer.println(prefix + pxToDpStr("extraSpace", extraSpace));
        writer.println(prefix + pxToDpStr("unscaled extraSpace", extraSpace / iconScale));
        if (inv.devicePaddings != null) {
            int unscaledExtraSpace = (int) (extraSpace / iconScale);
            writer.println(prefix + pxToDpStr("maxEmptySpace",
                    inv.devicePaddings.getDevicePadding(unscaledExtraSpace).getMaxEmptySpacePx()));
        }
        writer.println(prefix + pxToDpStr("workspaceTopPadding", workspaceTopPadding));
        writer.println(prefix + pxToDpStr("workspaceBottomPadding", workspaceBottomPadding));
        writer.println(prefix + pxToDpStr("extraHotseatBottomPadding", extraHotseatBottomPadding));
        writer.println(prefix + pxToDpStr("overviewTaskMarginPx", overviewTaskMarginPx));
        writer.println(prefix + pxToDpStr("overviewTaskMarginGridPx", overviewTaskMarginGridPx));
        writer.println(prefix + pxToDpStr("overviewTaskIconSizePx", overviewTaskIconSizePx));
        writer.println(prefix + pxToDpStr("overviewTaskIconDrawableSizePx",
                overviewTaskIconDrawableSizePx));
        writer.println(prefix + pxToDpStr("overviewTaskIconDrawableSizeGridPx",
                overviewTaskIconDrawableSizeGridPx));
        writer.println(prefix + pxToDpStr("overviewTaskThumbnailTopMarginPx",
                overviewTaskThumbnailTopMarginPx));
        writer.println(prefix + pxToDpStr("overviewActionsTopMarginPx",
                overviewActionsTopMarginPx));
        writer.println(prefix + pxToDpStr("overviewActionsHeight",
                overviewActionsHeight));
        writer.println(prefix + pxToDpStr("overviewActionsButtonSpacing",
                overviewActionsButtonSpacing));
        writer.println(prefix + pxToDpStr("overviewPageSpacing", overviewPageSpacing));
        writer.println(prefix + pxToDpStr("overviewRowSpacing", overviewRowSpacing));
        writer.println(prefix + pxToDpStr("overviewGridSideMargin", overviewGridSideMargin));
        writer.println(prefix + pxToDpStr("dropTargetBarTopMarginPx", dropTargetBarTopMarginPx));
        writer.println(prefix + pxToDpStr("dropTargetBarSizePx", dropTargetBarSizePx));
        writer.println(
                prefix + pxToDpStr("dropTargetBarBottomMarginPx", dropTargetBarBottomMarginPx));
        writer.println(
                prefix + pxToDpStr("workspaceSpringLoadShrunkTop", workspaceSpringLoadShrunkTop));
        writer.println(prefix + pxToDpStr("workspaceSpringLoadShrunkBottom",
                workspaceSpringLoadShrunkBottom));
        writer.println(prefix + pxToDpStr("workspaceSpringLoadedBottomSpace",
                workspaceSpringLoadedBottomSpace));
        writer.println(prefix + pxToDpStr("workspaceSpringLoadedMinNextPageVisiblePx",
                workspaceSpringLoadedMinNextPageVisiblePx));
        writer.println(
                prefix + pxToDpStr("getWorkspaceSpringLoadScale()", getWorkspaceSpringLoadScale()));
    }
    private static Context getContext(Context c, Info info, int orientation, WindowBounds bounds) {
        Configuration config = new Configuration(c.getResources().getConfiguration());
        config.orientation = orientation;
        config.densityDpi = info.getDensityDpi();
        config.smallestScreenWidthDp = (int) info.smallestSizeDp(bounds);
        return c.createConfigurationContext(config);
    }
    /**
     * Callback when a component changes the DeviceProfile associated with it, as a result of
     * configuration change
     */
    public interface OnDeviceProfileChangeListener {
        /**
         * Called when the device profile is reassigned. Note that for layout and measurements, it
         * is sufficient to listen for inset changes. Use this callback when you need to perform
         * a one time operation.
         */
        void onDeviceProfileChanged(DeviceProfile dp);
    }

    /**
     * Handler that deals with ItemInfo of the views for the DeviceProfile
     */
    @FunctionalInterface
    public interface ViewScaleProvider {
        /**
         * Get the scales from the view
         *
         * @param itemInfo The tag of the widget view
         * @return PointF instance containing the scale information, or null if using the default
         * app widget scale of this device profile.
         */
        @NonNull
        PointF getScaleFromItemInfo(@Nullable ItemInfo itemInfo);
    }

    public static class Builder {
        private final Context mContext;
        private final InvariantDeviceProfile mInv;
        private final Info mInfo;
        private WindowBounds mWindowBounds;
        private boolean mIsMultiDisplay;
        private boolean mIsMultiWindowMode = false;
        private Boolean mTransposeLayoutWithOrientation;
        private Boolean mIsGestureMode;
        private ViewScaleProvider mViewScaleProvider = null;

        private SparseArray<DotRenderer> mDotRendererCache;

        private Consumer<DeviceProfile> mOverrideProvider;

        public Builder(Context context, InvariantDeviceProfile inv, Info info) {
            mContext = context;
            mInv = inv;
            mInfo = info;
        }

        public Builder setMultiWindowMode(boolean isMultiWindowMode) {
            mIsMultiWindowMode = isMultiWindowMode;
            return this;
        }

        public Builder setIsMultiDisplay(boolean isMultiDisplay) {
            mIsMultiDisplay = isMultiDisplay;
            return this;
        }

        public Builder setWindowBounds(WindowBounds bounds) {
            mWindowBounds = bounds;
            return this;
        }

        public Builder setTransposeLayoutWithOrientation(boolean transposeLayoutWithOrientation) {
            mTransposeLayoutWithOrientation = transposeLayoutWithOrientation;
            return this;
        }

        public Builder setGestureMode(boolean isGestureMode) {
            mIsGestureMode = isGestureMode;
            return this;
        }

        public Builder withDimensionsOverride(Consumer<DeviceProfile> overrideProvider) {
            mOverrideProvider = overrideProvider;
            return this;
        }

        /**
         * Set the viewScaleProvider for the builder
         *
         * @param viewScaleProvider The viewScaleProvider to be set for the
         *                          DeviceProfile
         * @return This builder
         */
        @NonNull
        public Builder setViewScaleProvider(@Nullable ViewScaleProvider viewScaleProvider) {
            mViewScaleProvider = viewScaleProvider;
            return this;
        }

        public DeviceProfile build() {
            if (mWindowBounds == null) {
                throw new IllegalArgumentException("Window bounds not set");
            }
            if (mTransposeLayoutWithOrientation == null) {
                mTransposeLayoutWithOrientation = !mInfo.isTablet(mWindowBounds);
            }
            if (mIsGestureMode == null) {
                mIsGestureMode = DisplayController.getNavigationMode(mContext).hasGestures;
            }
            if (mDotRendererCache == null) {
                mDotRendererCache = new SparseArray<>();
            }
            if (mViewScaleProvider == null) {
                mViewScaleProvider = DEFAULT_PROVIDER;
            }
            if (mOverrideProvider == null) {
                mOverrideProvider = DEFAULT_DIMENSION_PROVIDER;
            }
            return new DeviceProfile(mContext, mInv, mInfo, mWindowBounds, mDotRendererCache,
                    mIsMultiWindowMode, mTransposeLayoutWithOrientation, mIsMultiDisplay,
                    mIsGestureMode, mViewScaleProvider, mOverrideProvider);
        }
    }
}

/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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

package com.neoapps.neolauncher.folder

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.launcher3.CellLayout
import com.android.launcher3.DeviceProfile.calculateCellHeight
import com.android.launcher3.DeviceProfile.calculateCellWidth
import com.android.launcher3.R
import com.android.launcher3.folder.FolderPagedView
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.pageindicators.PageIndicatorDots
import com.neoapps.neolauncher.util.dpToPx
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class NeoFolderPagedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FolderPagedView(context, attrs) {
    private var mAnimator: ValueAnimator? = null
    private var mIsScaled: Boolean = false
    private var mPagePadding: Rect? = null

    init {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.cornerRadius = resources.getDimension(R.dimen.folder_content_corner_radius)
        gradientDrawable.setColor(getBgColorByTheme(false))
        background = gradientDrawable
        clipChildren = true
        clipToPadding = true

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            updateContentHeight(insets)
            insets
        }
    }

    override fun setFixedSize(width: Int, height: Int) {
        super.setFixedSize(width, height)

        val folderProfile = deviceProfile.folderProfile
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val rows = if (isLandscape) folderProfile.numColumns else folderProfile.numRows
        val columns = if (isLandscape) folderProfile.numRows else folderProfile.numColumns
        val borderSpaceX = folderProfile.cellLayoutBorderSpacePx.x
        val borderSpaceY = folderProfile.cellLayoutBorderSpacePx.y

        val contentWidth = max(0, width - paddingLeft - paddingRight)
        val contentHeight = max(0, height - paddingTop - paddingBottom)

        for (index in 0 until childCount) {
            val page = getChildAt(index) as? CellLayout ?: continue
            page.setGridSize(columns, rows)

            val pageContentWidth = max(0, contentWidth - page.paddingLeft - page.paddingRight)
            val pageContentHeight = max(0, contentHeight - page.paddingTop - page.paddingBottom)

            val computedCellWidth = calculateCellWidth(pageContentWidth, borderSpaceX, columns)
            val computedCellHeight = calculateCellHeight(pageContentHeight, borderSpaceY, rows)

            val cellWidth = when {
                computedCellWidth <= 0 -> folderProfile.cellWidthPx
                else -> min(folderProfile.cellWidthPx, computedCellWidth)
            }

            val cellHeight = when {
                computedCellHeight <= 0 -> folderProfile.cellHeightPx
                else -> min(folderProfile.cellHeightPx, computedCellHeight)
            }

            page.setCellDimensions(cellWidth, cellHeight)
            page.clipChildren = false
            page.clipToPadding = false
            page.shortcutsAndWidgets.clipChildren = false
            page.shortcutsAndWidgets.clipToPadding = false
        }
    }

    override fun getChildGap(fromIndex: Int, toIndex: Int): Int {
        return 0
    }

    override fun getDesiredHeight(): Int {
        if (childCount == 0) return 0

        val folderProfile = deviceProfile.folderProfile
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val rows = if (isLandscape) folderProfile.numRows else getRequiredRows()
        return paddingTop + paddingBottom + rows * folderProfile.cellHeightPx

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            updateContentHeight(ViewCompat.getRootWindowInsets(this))
            requestApplyInsets()
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val folderProfile = deviceProfile.folderProfile
        val maxPortraitRows = folderProfile.numRows
        val maxPortraitHeight =
            paddingTop + paddingBottom + (maxPortraitRows * folderProfile.cellHeightPx)

        if (!isLandscape) {
            val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
            val resolvedHeight =
                if (parentHeight > 0) min(parentHeight, maxPortraitHeight) else maxPortraitHeight
            val cappedHeightSpec = MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, cappedHeightSpec)
            return
        }

        val maxLandscapeColumns = folderProfile.numColumns
        val borderSpaceX = folderProfile.cellLayoutBorderSpacePx.x
        val maxLandscapeWidth = paddingLeft + paddingRight +
                (maxLandscapeColumns * folderProfile.cellWidthPx) +
                (maxLandscapeColumns - 1).coerceAtLeast(0) * borderSpaceX
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val resolvedWidth =
            if (parentWidth > 0) min(parentWidth, maxLandscapeWidth) else maxLandscapeWidth
        val cappedWidthSpec = MeasureSpec.makeMeasureSpec(resolvedWidth, MeasureSpec.EXACTLY)

        val maxLandscapeRows = folderProfile.numRows
        val maxLandscapeHeight =
            paddingTop + paddingBottom + (maxLandscapeRows * folderProfile.cellHeightPx)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val resolvedHeight =
            if (parentHeight > 0) min(parentHeight, maxLandscapeHeight) else maxLandscapeHeight
        val cappedHeightSpec = MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY)

        super.onMeasure(cappedWidthSpec, cappedHeightSpec)
    }

    override fun addViewForRank(view: View, info: ItemInfo, rank: Int) {
        if (mViewsBound) {
            super.addViewForRank(view, info, rank)
        }
    }

    override fun arrangeChildren(children: MutableList<View>) {
        super.arrangeChildren(children)
        if (pageCount <= 1) {
            (mPageIndicator as PageIndicatorDots).setActiveMarker(0)
        }
    }

    fun changePagePadding(view: View, isScaled: Boolean, animate: Boolean) {
        if (view !is CellLayout) return

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val horizontalPadding = if (isLandscape) {
            resources.getDimension(R.dimen.folder_container_landscape_padding).toInt()
        } else {
            0
        }
        val bottomPadding = resources.getDimension(R.dimen.folder_container_bottom_padding).toInt()

        var extraHorizontal = 0
        var extraVertical = 0
        if (isScaled) {
            if (isLandscape) {
                extraHorizontal += resources.getDimension(R.dimen.folder_container_landscape_horizontal_padding_scale)
                    .toInt()
            } else {
                extraHorizontal += resources.getDimension(R.dimen.folder_container_vertical_padding_scale)
                    .toInt()
                extraVertical += resources.getDimension(R.dimen.folder_container_horizontal_padding_scale)
                    .toInt()
            }
        }
        val newPadding = Rect(
            horizontalPadding + extraHorizontal,
            extraVertical,
            horizontalPadding + extraHorizontal,
            bottomPadding + extraVertical
        )

        if (!animate) {
            view.setPadding(newPadding.left, newPadding.top, newPadding.right, newPadding.bottom)
            mIsScaled = isScaled
            mPagePadding = newPadding
            return
        }

        mAnimator?.let {
            if (it.isRunning) {
                it.end()
                mPagePadding?.let { oldPadding ->
                    view.setPadding(
                        oldPadding.left,
                        oldPadding.top,
                        oldPadding.right,
                        oldPadding.bottom
                    )
                }
            }
        }

        val oldLeft = view.paddingLeft
        val oldRight = view.paddingRight
        val oldTop = view.paddingTop
        val oldBottom = view.paddingBottom

        val deltaLeft = newPadding.left - oldLeft
        val deltaRight = newPadding.right - oldRight
        val deltaTop = newPadding.top - oldTop
        val deltaBottom = newPadding.bottom - oldBottom

        if (deltaLeft == 0 && deltaRight == 0 && deltaTop == 0 && deltaBottom == 0) {
            return
        }

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            start()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                view.setPadding(
                    oldLeft + (deltaLeft * fraction).toInt(),
                    oldTop + (deltaTop * fraction).toInt(),
                    oldRight + (deltaRight * fraction).toInt(),
                    oldBottom + (deltaBottom * fraction).toInt()
                )
            }
            mAnimator = this
        }
        mPagePadding = newPadding
        mIsScaled = isScaled
    }

    override fun createAndAddNewPage(): CellLayout {
        val page = super.createAndAddNewPage()
        changePagePadding(page, mIsScaled, true)
        return page
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        getChildAt(currentPage)?.let { child ->
            changePagePadding(child, mIsScaled, true)
        }
    }

    override fun setFocusOnFirstChild() {
        if (mViewsBound) {
            super.setFocusOnFirstChild()
        }
    }

    fun getBgColorByTheme(isDark: Boolean): Int {
        if (isDark) {
            return resources.getColor(R.color.folder_background_dark)
        }
        return resources.getColor(R.color.folder_background_light)
    }

    private fun updateContentHeight(insets: WindowInsetsCompat?) {
        val params = layoutParams ?: return

        if (params.height == 0) return

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val systemBars = insets?.getInsets(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )

        val statusBarHeight = systemBars?.top ?: getSystemBarHeight("status_bar_height")
        val navigationBarHeight = systemBars?.bottom ?: getSystemBarHeight("navigation_bar_height")
        val titleTopMargin =
            if (isLandscape) FOLDER_TITLE_TOP_MARGIN_LANDSCAPE_DP else FOLDER_TITLE_TOP_MARGIN_DP
        val occupiedHeight = statusBarHeight +
                navigationBarHeight +
                dpToPx(FOLDER_TITLE_HEIGHT_DP) +
                dpToPx(titleTopMargin) +
                dpToPx(FOLDER_CONTAINER_MARGIN_DP) +
                dpToPx(FOLDER_FOOTER_HEIGHT_DP)

        val targetHeight =
            max(0, resources.displayMetrics.heightPixels - occupiedHeight.roundToInt())

        if (params.height != targetHeight) {
            params.height = targetHeight
            layoutParams = params
        }
    }

    private fun getSystemBarHeight(resourceName: String): Int {
        val resourceId = resources.getIdentifier(resourceName, "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun getRequiredRows(): Int {
        if (childCount == 0) return 1

        val maxItemsOnPage = (0 until childCount).maxOf { index ->
            (getChildAt(index) as? CellLayout)?.shortcutsAndWidgets?.childCount ?: 0
        }
        val columns = deviceProfile.folderProfile.numColumns
        return ((maxItemsOnPage + columns - 1) / columns)
            .coerceIn(1, deviceProfile.folderProfile.numRows)
    }

    private companion object {
        private const val FOLDER_TITLE_HEIGHT_DP = 56f
        private const val FOLDER_TITLE_TOP_MARGIN_DP = 130f
        private const val FOLDER_TITLE_TOP_MARGIN_LANDSCAPE_DP = 40f
        private const val FOLDER_CONTAINER_MARGIN_DP = 24f
        private const val FOLDER_FOOTER_HEIGHT_DP = 56f
    }
}

/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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

package com.neoapps.neolauncher.allapps

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.launcher3.DeviceProfile
import com.android.launcher3.Insettable
import com.android.launcher3.PagedView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsRecyclerView
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.allapps.search.SearchAdapterProvider
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.Themes
import com.android.launcher3.views.ActivityContext
import com.neoapps.neolauncher.NeoLauncher
import com.neoapps.neolauncher.preferences.NeoPrefs

class HorizontalAppsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs),
    Insettable {

    private var pagedView: AllAppsPagedView? = null
    private var pageIndicator: LinearLayout? = null
    private var pagerAdapter: AllAppViewPagerAdapter? = null
    private var mainAppsList: AlphabeticalAppsList? = null
    private var allAppsStore: AllAppsStore? = null
    private var deviceProfile: DeviceProfile? = null
    private var storeListener: AllAppsStore.OnUpdateListener? = null
    private var isSetUp = false
    private var bottomInset = 0

    private val dotSize = Utilities.dpToPx(8f)
    private val dotMargin = Utilities.dpToPx(4f)
    private val paginationHeight = Utilities.dpToPx(40f)
    private val contentTopPadding = Utilities.dpToPx(8f)
    private val dotActiveAlpha = 255
    private val dotInactiveAlpha = 100

    override fun onFinishInflate() {
        super.onFinishInflate()
        clipChildren = false
        clipToPadding = false
        pagedView = findViewById(R.id.apps_paged_view)
        pageIndicator = findViewById(R.id.page_indicator_dots)
        pageIndicator?.elevation = Utilities.dpToPx(4f).toFloat()
        updatePageIndicatorPosition()
        updatePagedViewPadding()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bottomInset = getEffectiveBottomInset()
        updatePageIndicatorPosition()
        updatePagedViewPadding()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Utilities.ATLEAST_R) {
            val navBar = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            val tappable = insets.getInsets(WindowInsets.Type.tappableElement()).bottom
            val gestures = insets.getInsets(WindowInsets.Type.systemGestures()).bottom
            bottomInset = maxOf(navBar, maxOf(tappable, gestures))
        }
        val effective = getEffectiveBottomInset()
        if (effective != bottomInset) {
            bottomInset = effective
        }
        updatePageIndicatorPosition()
        updatePagedViewPadding()
        return super.onApplyWindowInsets(insets)
    }

    override fun setInsets(insets: Rect) {
        bottomInset = insets.bottom
        val effective = getEffectiveBottomInset()
        if (effective > 0 || insets.bottom == 0) {
            bottomInset = effective
        }
        updatePageIndicatorPosition()
        updatePagedViewPadding()

        if (isSetUp) {
            deviceProfile?.let { dp ->
                val newRows = calculateRows(dp)
                val columns = dp.numShownAllAppsColumns
                pagerAdapter?.let { adapter ->
                    if (adapter.columnCount != columns || adapter.rowCount != newRows) {
                        adapter.setGridDimensions(columns, newRows)
                        post {
                            adapter.recalculateAndUpdate()
                        }
                    }
                }
            }
        }
    }

    private fun getNavBarHeightIfVisible(dp: DeviceProfile): Int {
        if (Utilities.ATLEAST_R) {
            rootWindowInsets?.let { wi ->
                val isVisible = wi.isVisible(WindowInsets.Type.navigationBars())
                if (!isVisible) {
                    return 0
                }
                val navBar = wi.getInsets(WindowInsets.Type.navigationBars()).bottom
                val tappable = wi.getInsets(WindowInsets.Type.tappableElement()).bottom
                val gestures = wi.getInsets(WindowInsets.Type.systemGestures()).bottom
                return maxOf(navBar, maxOf(tappable, gestures))
            }
        }
        if (bottomInset > 0) return bottomInset
        if (dp.insets.bottom > 0) return dp.insets.bottom
        return 0
    }

    private fun getEffectiveBottomInset(): Int {
        val dp = deviceProfile
        return if (dp != null) getNavBarHeightIfVisible(dp) else bottomInset
    }

    private fun updatePageIndicatorPosition() {
        val indicator = pageIndicator ?: return
        val lp = indicator.layoutParams as? LayoutParams ?: return
        lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        val effective = getEffectiveBottomInset()
        lp.bottomMargin = effective + Utilities.dpToPx(12f)
        indicator.layoutParams = lp
    }

    private fun updatePagedViewPadding() {
        val effective = getEffectiveBottomInset()
        val bottomReserved = effective + paginationHeight
        val topPadding = contentTopPadding
        pagedView?.setPadding(0, topPadding, 0, bottomReserved)
    }

    fun setup(
        appsList: AlphabeticalAppsList,
        store: AllAppsStore,
        dp: DeviceProfile,
        searchAdapterProvider: SearchAdapterProvider<*>
    ) {
        mainAppsList = appsList
        allAppsStore = store
        deviceProfile = dp

        bottomInset = getEffectiveBottomInset()
        updatePageIndicatorPosition()
        updatePagedViewPadding()

        pagerAdapter = AllAppViewPagerAdapter(context, store, searchAdapterProvider)

        val columns = dp.numShownAllAppsColumns
        val rows = calculateRows(dp)
        pagerAdapter?.setGridDimensions(columns, rows)
        pagerAdapter?.setMainAppsList(appsList)
        pagerAdapter?.setPagedView(pagedView)
        pagerAdapter?.setOnPageCountChanged { updatePageIndicator() }

        val launcher = ActivityContext.lookupContext(context) as? NeoLauncher
        launcher?.allAppsPredictions?.getContents()?.let { predictions ->
            if (predictions.isNotEmpty()) {
                pagerAdapter?.setPredictedApps(predictions)
            }
        }

        pagedView?.addPageSwitchListener {
            val page = pagedView?.let {
                if (it.nextPage != PagedView.INVALID_PAGE) it.nextPage else it.currentPage
            } ?: 0
            updateActivePageDot(page)
        }

        storeListener = AllAppsStore.OnUpdateListener {
            post {
                pagerAdapter?.recalculateAndUpdate()
            }
        }
        store.addUpdateListener(storeListener)

        isSetUp = true
        pagerAdapter?.recalculateAndUpdate()
    }

    fun setPredictedApps(apps: List<ItemInfo>) {
        pagerAdapter?.setPredictedApps(apps)
    }

    fun setPredictionUiUpdatePaused(paused: Boolean) {
        pagerAdapter?.setPredictionUiUpdatePaused(paused)
    }

    fun getRecyclerViewForCurrentPage(): AllAppsRecyclerView? {
        val currentPage = pagedView?.let {
            if (it.nextPage != PagedView.INVALID_PAGE) it.nextPage else it.currentPage
        } ?: 0
        return pagerAdapter?.getRecyclerView(currentPage)
    }

    private fun calculateRows(dp: DeviceProfile): Int {
        val cellHeight = dp.allAppsProfile.cellHeightPx
        if (cellHeight <= 0) return 4

        val screenHeight = if (dp.deviceProperties.heightPx > 0) {
            dp.deviceProperties.heightPx
        } else {
            context.resources.displayMetrics.heightPixels
        }

        val launcher = ActivityContext.lookupContext(context) as? NeoLauncher
        val searchContainer =
            (parent as? ViewGroup)?.findViewById<View>(R.id.search_container_all_apps)
                ?: launcher?.appsView?.findViewById<View>(R.id.search_container_all_apps)
        val isSearchVisible = if (searchContainer != null) {
            searchContainer.visibility == VISIBLE
        } else {
            NeoPrefs.getInstance().searchDrawerEnabled.getValue()
        }

        val searchBarHeight = if (isSearchVisible) {
            if (searchContainer != null && searchContainer.bottom > 0) {
                searchContainer.bottom
            } else {
                dp.insets.top + Utilities.dpToPx(8f) + (if ((searchContainer?.measuredHeight
                        ?: 0) > 0
                ) searchContainer!!.measuredHeight else Utilities.dpToPx(56f))
            }
        } else {
            dp.insets.top
        }

        val navBarHeight = getNavBarHeightIfVisible(dp)

        val availableHeight =
            screenHeight - searchBarHeight - paginationHeight - navBarHeight - contentTopPadding

        val calculated = availableHeight / cellHeight

        return maxOf(1, calculated)
    }

    private fun updatePageIndicator() {
        val indicator = pageIndicator ?: return
        indicator.removeAllViews()

        val pageCount = pagerAdapter?.getPageCount() ?: 1
        if (pageCount <= 1) {
            indicator.visibility = GONE
            return
        }

        indicator.visibility = VISIBLE
        updatePageIndicatorPosition()

        val currentPage = pagedView?.currentPage ?: 0

        for (i in 0 until pageCount) {
            val dot = createDot(i == currentPage)
            val params = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                setMargins(dotMargin, 0, dotMargin, 0)
            }
            indicator.addView(dot, params)
        }
    }

    private fun updateActivePageDot(activePage: Int) {
        val indicator = pageIndicator ?: return
        for (i in 0 until indicator.childCount) {
            val dot = indicator.getChildAt(i)
            dot.alpha = if (i == activePage) 1.0f else dotInactiveAlpha / 255f
        }
    }

    private fun createDot(active: Boolean): View {
        val dot = View(context)
        val dotColor = Themes.getAttrColor(context, R.attr.pageIndicatorDotColor).let {
            if (it == 0) Themes.getAttrColor(context, android.R.attr.textColorPrimary) else it
        }
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(dotColor)
        }
        dot.background = drawable
        dot.alpha = if (active) 1.0f else dotInactiveAlpha / 255f
        return dot
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val effective = getEffectiveBottomInset()
        if (effective != bottomInset) {
            bottomInset = effective
            updatePageIndicatorPosition()
            updatePagedViewPadding()
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isSetUp && measuredWidth > 0 && measuredHeight > 0) {
            deviceProfile?.let { dp ->
                val newRows = calculateRows(dp)
                val columns = dp.numShownAllAppsColumns
                pagerAdapter?.let { adapter ->
                    if (adapter.columnCount != columns || adapter.rowCount != newRows) {
                        adapter.setGridDimensions(columns, newRows)
                        post {
                            adapter.recalculateAndUpdate()
                        }
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        storeListener?.let { allAppsStore?.removeUpdateListener(it) }
        storeListener = null
    }
}
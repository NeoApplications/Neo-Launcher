/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.launcher3.R
import com.android.launcher3.allapps.AllAppsGridAdapter
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsRecyclerView
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.allapps.BaseAllAppsAdapter
import com.android.launcher3.allapps.search.SearchAdapterProvider
import com.android.launcher3.appprediction.PredictionRowView
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.views.ActivityContext
import com.neoapps.neolauncher.NeoLauncher
import com.neoapps.neolauncher.preferences.NeoPrefs
import java.util.function.Predicate

class AllAppViewPagerAdapter(
    private val context: Context,
    private val allAppsStore: AllAppsStore,
    private val searchAdapterProvider: SearchAdapterProvider<*>
) {
    private var mainAppsList: AlphabeticalAppsList? = null
    private var pagedView: AllAppsPagedView? = null
    var columnCount: Int = 5
        private set
    var rowCount: Int = 4
        private set
    private var pageCount = 1
    private var appsPerPage = 20
    private val pageRecyclerViews = mutableListOf<AllAppsRecyclerView>()
    private val pageAppsLists = mutableListOf<AlphabeticalAppsList>()
    private val pageFilters = mutableListOf<PageFilter>()
    private var onPageCountChanged: ((Int) -> Unit)? = null

    private var predictedApps: List<ItemInfo> = emptyList()
    private var currentPredictionRow: PredictionRowView<*>? = null

    fun setMainAppsList(list: AlphabeticalAppsList) {
        mainAppsList = list
    }

    fun setPagedView(view: AllAppsPagedView?) {
        pagedView = view
    }

    fun setGridDimensions(columns: Int, rows: Int) {
        columnCount = columns
        rowCount = rows
        appsPerPage = columns * rows
    }

    fun setOnPageCountChanged(listener: (Int) -> Unit) {
        onPageCountChanged = listener
    }

    fun setPredictedApps(apps: List<ItemInfo>) {
        val hadPredictions = shouldShowPredictions()
        predictedApps = apps
        val hasPredictions = shouldShowPredictions()
        if (hadPredictions != hasPredictions) {
            recalculateAndUpdate()
        } else {
            currentPredictionRow?.setPredictedApps(apps)
        }
    }

    fun shouldShowPredictions(): Boolean {
        val prefs = NeoPrefs.getInstance()
        return prefs.drawerAppSuggestions.getValue() && predictedApps.isNotEmpty() && rowCount > 1
    }

    fun getAppsPerPage(): Int = appsPerPage

    fun getPageCount(): Int = pageCount

    fun getRecyclerView(page: Int): AllAppsRecyclerView? {
        return pageRecyclerViews.getOrNull(page)
    }

    fun recalculateAndUpdate() {
        if (predictedApps.isEmpty()) {
            val launcher = ActivityContext.lookupContext(context) as? NeoLauncher
            val initial = launcher?.allAppsPredictions?.getContents()
            if (!initial.isNullOrEmpty()) {
                predictedApps = initial
            }
        }

        val appItems = getSortedAppInfos()
        val totalApps = appItems.size
        val showPredictions = shouldShowPredictions()
        val page0Capacity =
            if (showPredictions) (rowCount - 1) * columnCount else rowCount * columnCount
        val otherPageCapacity = rowCount * columnCount

        val newPageCount = if (totalApps <= page0Capacity) {
            1
        } else {
            1 + (totalApps - page0Capacity + otherPageCapacity - 1) / otherPageCapacity
        }

        updatePageAssignments(appItems, newPageCount)

        val pageCountChanged = pageCount != newPageCount
        pageCount = newPageCount

        rebuildPages()

        if (pageCountChanged) {
            onPageCountChanged?.invoke(pageCount)
        }
    }

    private fun getSortedAppInfos(): List<AppInfo> {
        val adapterItems = mainAppsList?.adapterItems ?: return emptyList()
        return adapterItems
            .filter { it.viewType == BaseAllAppsAdapter.VIEW_TYPE_ICON && it.itemInfo is AppInfo }
            .mapNotNull { it.itemInfo as? AppInfo }
    }

    /**
     * Computes which apps belong on which page and updates the page filters.
     */
    private fun updatePageAssignments(sortedApps: List<AppInfo>, newPageCount: Int) {
        val showPredictions = shouldShowPredictions()
        val page0Capacity =
            if (showPredictions) maxOf(0, (rowCount - 1) * columnCount) else rowCount * columnCount
        val otherPageCapacity = rowCount * columnCount

        val pageAssignments = HashMap<ComponentKey, Int>(sortedApps.size)
        for ((index, appInfo) in sortedApps.withIndex()) {
            val page = if (index < page0Capacity) {
                0
            } else {
                1 + (index - page0Capacity) / otherPageCapacity
            }
            pageAssignments[ComponentKey(appInfo.componentName, appInfo.user)] = page
        }

        pageFilters.clear()
        for (pageIndex in 0 until newPageCount) {
            pageFilters.add(PageFilter(pageAssignments, pageIndex))
        }
    }

    private fun rebuildPages() {
        val pv = pagedView ?: return
        val currentPage = pv.currentPage.coerceIn(0, maxOf(0, pageCount - 1))

        for (list in pageAppsLists) {
            allAppsStore.removeUpdateListener(list)
        }
        unregisterIconContainers(allAppsStore)
        currentPredictionRow?.let {
            allAppsStore.unregisterIconContainer(it)
        }
        currentPredictionRow = null

        pv.removeAllViews()
        pageRecyclerViews.clear()
        pageAppsLists.clear()

        val activityContext = ActivityContext.lookupContext(context) as ActivityContext
        val inflater = LayoutInflater.from(context)
        val showPredictions = shouldShowPredictions()

        for (pageIndex in 0 until pageCount) {
            val pageAppsList = AlphabeticalAppsList(
                activityContext, allAppsStore, null, null
            )
            pageAppsList.setNumAppsPerRowAllApps(columnCount)

            val filter = pageFilters.getOrNull(pageIndex)
            if (filter != null) {
                pageAppsList.updateItemFilter(filter)
            }

            val adapter = AllAppsGridAdapter(
                activityContext, inflater, pageAppsList, searchAdapterProvider
            )
            pageAppsList.setAdapter(adapter)

            val rv = inflater.inflate(
                R.layout.all_apps_rv_layout, pv, false
            ) as AllAppsRecyclerView

            rv.apps = pageAppsList
            rv.layoutManager = adapter.layoutManager
            rv.adapter = adapter
            rv.setHasFixedSize(true)
            rv.itemAnimator = null
            rv.isVerticalScrollBarEnabled = false
            rv.clipToPadding = false

            pageRecyclerViews.add(rv)
            pageAppsLists.add(pageAppsList)

            if (pageIndex == 0 && showPredictions) {
                val pageContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    clipChildren = false
                    clipToPadding = false
                }

                val predictionRow = PredictionRowView<NeoLauncher>(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    clipChildren = false
                    clipToPadding = false
                }
                predictionRow.setPredictedApps(predictedApps)
                currentPredictionRow = predictionRow
                allAppsStore.registerIconContainer(predictionRow)

                pageContainer.addView(predictionRow)

                val rvParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                pageContainer.addView(rv, rvParams)
                pv.addView(pageContainer)
            } else {
                pv.addView(rv)
            }
        }

        registerIconContainers(allAppsStore)

        if (currentPage < pageCount) {
            pv.currentPage = currentPage
        } else {
            pv.currentPage = 0
        }
    }

    fun registerIconContainers(store: AllAppsStore) {
        for (rv in pageRecyclerViews) {
            store.registerIconContainer(rv)
        }
    }

    fun unregisterIconContainers(store: AllAppsStore) {
        for (rv in pageRecyclerViews) {
            store.unregisterIconContainer(rv)
        }
    }

    private class PageFilter(
        private val pageAssignments: Map<ComponentKey, Int>,
        private val pageIndex: Int
    ) : Predicate<com.android.launcher3.model.data.ItemInfo> {

        override fun test(info: com.android.launcher3.model.data.ItemInfo): Boolean {
            val key = ComponentKey(info.targetComponent ?: return false, info.user)
            return pageAssignments[key] == pageIndex
        }
    }
}
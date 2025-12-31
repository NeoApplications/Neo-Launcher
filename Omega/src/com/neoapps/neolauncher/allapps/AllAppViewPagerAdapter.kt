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
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.android.launcher3.allapps.AllAppsGridAdapter
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsRecyclerView
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.views.ActivityContext

class AllAppViewPagerAdapter(private val context: Context) {
    private var appsList: AlphabeticalAppsList? = null
    private var pagedView: AllAppsPagedView? = null
    private var columNumber = 1
    private var rowNumber = 1
    private var pageCount = 1
    private var gridPaddingBottom = 15
    private var showAnimation = true
    private var appsPerPage = 1
    private val pageAdapters = mutableListOf<AllAppsGridAdapter>()

    fun setAppsList(list: AlphabeticalAppsList) {
        appsList = list
        calculatePages()
    }

    fun setPagedView(view: AllAppsPagedView?) {
        pagedView = view
    }

    fun setGridDimensions(columns: Int, rows: Int) {
        columNumber = columns
        rowNumber = rows
        appsPerPage = columns * rows
        calculatePages()
    }

    fun calculatePages() {
        val totalApps = appsList?.adapterItems?.size ?: 0
        pageCount = if (totalApps > 0 && appsPerPage > 0) {
            (totalApps + appsPerPage - 1) / appsPerPage
        } else {
            1
        }
    }

    fun getPageCount(): Int = pageCount

    fun getAppsForPage(pageIndex: Int): List<Any> {
        val apps = appsList?.adapterItems ?: return emptyList()
        val startIndex = pageIndex * appsPerPage
        val endIndex = minOf(startIndex + appsPerPage, apps.size)
        return if (startIndex < apps.size) {
            apps.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    fun notifyDataChanged() {
        pagedView?.removeAllViews()
        pageAdapters.clear()

        if (appsList == null || pagedView == null) return

        for (pageIndex in 0 until pageCount) {
            val recyclerView = createRecyclerViewForPage(pageIndex)
            pagedView?.addView(recyclerView)
        }

        pagedView?.currentPage = 0
    }

    private fun createRecyclerViewForPage(pageIndex: Int): AllAppsRecyclerView {
        val recyclerView = AllAppsRecyclerView(context)
        recyclerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val layoutManager = GridLayoutManager(context, columNumber)
        recyclerView.layoutManager = layoutManager

        val pageApps = getAppsForPage(pageIndex)
        val pageAdapter = createAdapterForPage(pageApps)
        pageAdapters.add(pageAdapter)

        recyclerView.adapter = pageAdapter
        recyclerView.setPadding(0, 0, 0, gridPaddingBottom)
        recyclerView.clipToPadding = false

        return recyclerView
    }

    private fun createAdapterForPage(apps: List<Any>): AllAppsGridAdapter {
        val activityContext = context as? ActivityContext

        val pageAppsList = AlphabeticalAppsList(activityContext, null, null, null)

        appsList?.let { originalList ->
            pageAppsList.setNumAppsPerRowAllApps(columNumber)
        }

        val adapter = AllAppsGridAdapter(
            activityContext,
            android.view.LayoutInflater.from(context),
            pageAppsList,
            null
        )

        return adapter
    }
}
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
 */

package com.neoapps.neolauncher.allapps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.android.launcher3.R
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AlphabeticalAppsList

class HorizontalAppsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var pagerAdapter: AllAppViewPagerAdapter? = null
    private var allAppsPagedView: AllAppsPagedView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.all_apps_horizontal, this)
        allAppsPagedView = findViewById(R.id.apps_paged_view)
    }

    fun setData(
        alphabeticalAppsList: AlphabeticalAppsList,
        allAppsViewPagerAdapter: AllAppViewPagerAdapter
    ) {
        pagerAdapter = allAppsViewPagerAdapter
        pagerAdapter?.setAppsList(alphabeticalAppsList)
        pagerAdapter?.setPagedView(allAppsPagedView)

        pagerAdapter?.setGridDimensions(5, 4)

        updatePages()
    }

    private fun updatePages() {
        pagerAdapter?.notifyDataChanged()
    }

    fun onConfigurationChanged() {
        pagerAdapter?.calculatePages()
        updatePages()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (measuredWidth > 0 && measuredHeight > 0) {
            pagerAdapter?.let {
                val oldPageCount = it.getPageCount()
                it.calculatePages()
                if (oldPageCount != it.getPageCount()) {
                    updatePages()
                }
            }
        }
    }
}
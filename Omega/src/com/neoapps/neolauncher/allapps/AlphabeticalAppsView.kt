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
import androidx.compose.ui.platform.ComposeView
import com.android.launcher3.FastScrollRecyclerView
import com.android.launcher3.R
import com.neoapps.neolauncher.theme.OmegaAppTheme

class AlphabeticalAppsView(context: Context, attrs: AttributeSet) :
    FastScrollRecyclerView(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.all_apps_alphabetical, this, true)
        findViewById<ComposeView>(R.id.categories_bar)!!.setContent {
            OmegaAppTheme {
                //AllAppsCategories()
            }
        }

    }

    override fun scrollToPositionAtProgress(touchFraction: Float): String {
        TODO("Not yet implemented")
    }

    override fun onUpdateScrollbar(dy: Int) {
        TODO("Not yet implemented")
    }
}
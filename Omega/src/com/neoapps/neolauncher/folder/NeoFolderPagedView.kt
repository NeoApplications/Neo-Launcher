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

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.launcher3.R
import com.android.launcher3.folder.FolderPagedView
import kotlin.math.max

class NeoFolderPagedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FolderPagedView(context, attrs) {

    private companion object {
        private const val FOLDER_TITLE_HEIGHT_DP = 56
        private const val FOLDER_TITLE_TOP_MARGIN_DP = 130
        private const val FOLDER_CONTAINER_MARGIN_DP = 24
        private const val FOLDER_FOOTER_HEIGHT_DP = 56
    }

    init {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.cornerRadius = resources.getDimension(R.dimen.folder_content_corner_radius)
        gradientDrawable.setColor(getBgColorByTheme(false))
        background = gradientDrawable

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            updateContentHeight(insets)
            insets
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            updateContentHeight(ViewCompat.getRootWindowInsets(this))
            requestApplyInsets()
        }
    }

    fun getBgColorByTheme(isDark: Boolean): Int {
        if (isDark) {
            return resources.getColor(R.color.folder_background_dark)
        }
        return resources.getColor(R.color.folder_background_light)
    }

    private fun updateContentHeight(insets: WindowInsetsCompat?) {
        val systemBars = insets?.getInsets(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )

        val statusBarHeight = systemBars?.top ?: getSystemBarHeight("status_bar_height")
        val navigationBarHeight = systemBars?.bottom ?: getSystemBarHeight("navigation_bar_height")
        val occupiedHeight = statusBarHeight +
                navigationBarHeight +
                dpToPx(FOLDER_TITLE_HEIGHT_DP) +
                dpToPx(FOLDER_TITLE_TOP_MARGIN_DP) +
                dpToPx(FOLDER_CONTAINER_MARGIN_DP) +
                dpToPx(FOLDER_FOOTER_HEIGHT_DP)

        val targetHeight = max(0, resources.displayMetrics.heightPixels - occupiedHeight)
        val params = layoutParams ?: return

        if (params.height != targetHeight) {
            params.height = targetHeight
            layoutParams = params
        }
    }

    private fun getSystemBarHeight(resourceName: String): Int {
        val resourceId = resources.getIdentifier(resourceName, "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}
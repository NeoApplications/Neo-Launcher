/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

package com.saggitt.omega.widget

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RemoteViews
import com.android.launcher3.R
import com.android.launcher3.util.Themes
import com.android.launcher3.widget.LauncherAppWidgetHostView
import com.saggitt.omega.smartspace.SmartspaceAppWidgetProvider

class CustomAppWidgetHostView @JvmOverloads constructor(
    context: Context,
    private var previewMode: Boolean = false
) : LauncherAppWidgetHostView(context) {

    private var customView: ViewGroup? = null

    override fun setAppWidget(appWidgetId: Int, info: AppWidgetProviderInfo) {
        inflateCustomView(info)
        super.setAppWidget(appWidgetId, info)
    }

    fun disablePreviewMode() {
        previewMode = false
        inflateCustomView(appWidgetInfo)
    }

    private fun inflateCustomView(info: AppWidgetProviderInfo) {
        customView = inflateCustomView(context, info, previewMode)
        if (customView == null) {
            return
        }
        customView!!.setOnLongClickListener(this)
        removeAllViews()
        addView(customView, MATCH_PARENT, MATCH_PARENT)
    }

    override fun updateAppWidget(remoteViews: RemoteViews?) {
        if (customView != null) return
        super.updateAppWidget(remoteViews)
    }

    override fun getDefaultView(): View {
        if (customView != null) return getEmptyView()
        return super.getDefaultView()
    }

    override fun getErrorView(): View {
        if (customView != null) return getEmptyView()
        return super.getErrorView()
    }

    private fun getEmptyView(): View {
        return View(context)
    }

    companion object {

        private val customLayouts = mapOf(
            SmartspaceAppWidgetProvider.componentName to R.layout.smartspace_widget
        )

        @JvmStatic
        fun inflateCustomView(
            context: Context,
            info: AppWidgetProviderInfo,
            previewMode: Boolean
        ): ViewGroup? {
            val layoutId = customLayouts[info.provider] ?: return null

            val inflationContext =
                if (previewMode) Themes.createWidgetPreviewContext(context) else context
            return LayoutInflater.from(inflationContext)
                .inflate(layoutId, null, false) as ViewGroup
        }
    }
}

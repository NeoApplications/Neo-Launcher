/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.saggitt.omega.views

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.android.launcher3.Hotseat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.theme.ThemeOverride

class DockPreview(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        OmegaPreferences.OnPreferenceChangeListener {
    private var currentView: Hotseat? = null

    private val prefs = Utilities.getOmegaPrefs(context)
    private val themedContext = ContextThemeWrapper(context, ThemeOverride.Launcher().getTheme(context))

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.addOnPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.removeOnPreferenceChangeListener(this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (currentView == null) {
            removeAllViews()
            inflateCurrentView()
        }
    }

    private fun inflateCurrentView() {
        val layout = R.layout.hotseat
        addView(inflateView(layout))
    }

    private fun inflateView(layout: Int): View {
        val view = LayoutInflater.from(themedContext).inflate(layout, this, false)
        view.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dock_preview_height)
        currentView = view as? Hotseat
        return view
    }
}
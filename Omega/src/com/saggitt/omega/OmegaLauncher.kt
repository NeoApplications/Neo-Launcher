/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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
package com.saggitt.omega

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.BaseActivity
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.preferences.NLPrefs
import com.saggitt.omega.util.Config

class OmegaLauncher : Launcher() {

    val prefs: NLPrefs by lazy { Utilities.getOmegaPrefs(this) }
    val gestureController by lazy { GestureController(this) }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }
    val optionsView by lazy { findViewById<OptionsPopupView>(R.id.options_view)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = Config(this)
        config.setAppLanguage(prefs.profileLanguage.getValue())
        mOverlayManager = defaultOverlay
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        if (mOverlayManager == null) {
            mOverlayManager = OverlayCallbackImpl(this)
        }
        return mOverlayManager
    }

    inline fun prepareDummyView(view: View, crossinline callback: (View) -> Unit) {
        val rect = Rect()
        dragLayer.getViewRectRelativeToSelf(view, rect)
        prepareDummyView(rect.left, rect.top, rect.right, rect.bottom, callback)
    }

    inline fun prepareDummyView(left: Int, top: Int, crossinline callback: (View) -> Unit) {
        val size = resources.getDimensionPixelSize(R.dimen.options_menu_thumb_size)
        val halfSize = size / 2
        prepareDummyView(left - halfSize, top - halfSize, left + halfSize, top + halfSize, callback)
    }

    inline fun prepareDummyView(
        left: Int, top: Int, right: Int, bottom: Int,
        crossinline callback: (View) -> Unit
    ) {
        (dummyView.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.width = right - left
            it.height = bottom - top
            it.leftMargin = left
            it.topMargin = top
        }
        dummyView.requestLayout()
        dummyView.post { callback(dummyView) }
    }

    companion object {
        var showFolderNotificationCount = false

        @JvmStatic
        fun getLauncher(context: Context): OmegaLauncher {
            return context as? OmegaLauncher
                ?: (context as ContextWrapper).baseContext as? OmegaLauncher
                ?: LauncherAppState.getInstance(context).launcher as OmegaLauncher
        }
    }
}

val Context.launcher: OmegaLauncher
    get() = BaseActivity.fromContext(this)
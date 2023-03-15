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

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.LauncherApps
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.AppFilter
import com.android.launcher3.BaseActivity
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.touch.AllAppsSwipeController
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.util.TouchController
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.VerticalSwipeGestureController
import com.saggitt.omega.preferences.NLPrefs
import com.saggitt.omega.util.Config
import com.saggitt.omega.views.OmegaBackgroundView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OmegaLauncher : Launcher() {

    val prefs: NLPrefs by lazy { Utilities.getOmegaPrefs(this) }
    val gestureController by lazy { GestureController(this) }
    val background by lazy { findViewById<OmegaBackgroundView>(R.id.omega_background)!! }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }
    val optionsView by lazy { findViewById<OptionsPopupView>(R.id.options_view)!! }

    val hiddenApps = ArrayList<AppInfo>()
    val allApps = ArrayList<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = Config(this)
        config.setAppLanguage(prefs.profileLanguage.getValue())
        mOverlayManager = defaultOverlay

        //Load hidden apps to use with hidden apps preference
        MODEL_EXECUTOR.handler.postAtFrontOfQueue { loadHiddenApps(prefs.drawerHiddenAppSet.getValue()) }

    }

    private fun loadHiddenApps(hiddenAppsSet: Set<String>) {
        val mContext = this
        CoroutineScope(Dispatchers.IO).launch {
            val appFilter = AppFilter()
            for (user in UserCache.INSTANCE[mContext].userProfiles) {
                val duplicatePreventionCache: MutableList<ComponentName> = ArrayList()
                for (info in getSystemService(
                    LauncherApps::class.java
                ).getActivityList(null, user)) {
                    val key = ComponentKey(info.componentName, info.user)
                    if (hiddenAppsSet.contains(key.toString())) {
                        val appInfo = AppInfo(info, info.user, false)
                        hiddenApps.add(appInfo)
                    }
                    if (prefs.searchHiddenApps.get().first()) {
                        if (!appFilter.shouldShowApp(info.componentName, user)) {
                            continue
                        }
                        if (!duplicatePreventionCache.contains(info.componentName)) {
                            duplicatePreventionCache.add(info.componentName)
                            val appInfo = AppInfo(mContext, info, user)
                            allApps.add(appInfo)
                        }
                    }
                }
            }
        }
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        if (mOverlayManager == null) {
            mOverlayManager = OverlayCallbackImpl(this)
        }
        return mOverlayManager
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

    override fun createTouchControllers(): Array<TouchController> {
        val list = ArrayList<TouchController>()
        list.add(dragController)
        list.add(AllAppsSwipeController(this))
        list.add(VerticalSwipeGestureController(this))


        return list.toTypedArray()
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
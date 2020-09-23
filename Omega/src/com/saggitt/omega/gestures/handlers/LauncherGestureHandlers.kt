/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.gestures.handlers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.views.OptionsPopupView
import com.saggitt.omega.dash.DashBottomSheet
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.util.omegaPrefs
import org.json.JSONObject


@Keep
open class OpenDrawerGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config),
        VerticalSwipeGestureHandler, StateChangeGestureHandler {

    override val displayName: String = context.getString(R.string.action_open_drawer)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_allapps_adaptive) }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.stateManager.goToState(LauncherState.ALL_APPS, true, getOnCompleteRunnable(controller))
    }

    open fun getOnCompleteRunnable(controller: GestureController): Runnable? = null

    override fun getTargetState(): LauncherState {
        return LauncherState.ALL_APPS
    }
}

@Keep
class StartGlobalSearchGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    private val searchProvider get() = SearchProviderController.getInstance(context).searchProvider
    override val displayName: String = context.getString(R.string.action_global_search)
    override val icon: Drawable by lazy { searchProvider.getIcon() }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        searchProvider.startSearch {
            try {
                if (context !is Activity) {
                    it.flags = it.flags or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(it)
            } catch (e: Exception) {
                Log.e(this::class.java.name, "Failed to start global search", e)
            }
        }
    }
}


@Keep
class StartAppSearchGestureHandler(context: Context, config: JSONObject?) : OpenDrawerGestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_app_search)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_search_shadow) }

    override fun getOnCompleteRunnable(controller: GestureController): Runnable? {
        return Runnable { controller.launcher.appsView.searchUiManager.startSearch() }
    }
}


@Keep
class OpenSettingsGestureHandler(context: Context, config: JSONObject?) :
        GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_settings)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_setting)
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
            `package` = controller.launcher.packageName
        })
    }
}

@Keep
class OpenDashGestureHandler(context: Context, config: JSONObject?) :
        GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_dash)
    override fun onGestureTrigger(controller: GestureController, view: View?) {
        //TODO: INFLAR DASH VIEW
        DashBottomSheet.show(controller.launcher, true)
    }
}

@Keep
class OpenOverviewGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_overview)
    override val iconResource: Intent.ShortcutIconResource by lazy { Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_setting) }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (context.omegaPrefs.usePopupMenuView) {
            OptionsPopupView.showDefaultOptions(controller.launcher,
                    controller.touchDownPoint.x, controller.touchDownPoint.y)
        } else {
            controller.launcher.stateManager.goToState(LauncherState.OPTIONS)
        }
    }
}


interface VerticalSwipeGestureHandler {

    fun onDragStart(start: Boolean) {}

    fun onDrag(displacement: Float, velocity: Float) {}

    fun onDragEnd(velocity: Float, fling: Boolean) {}
}

interface StateChangeGestureHandler {

    fun getTargetState(): LauncherState
}

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

package com.saggitt.omega.gestures.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.anim.AnimatorListeners
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.widget.picker.WidgetsFullSheet
import com.saggitt.omega.dash.DashSheet
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.neoapps.neolauncher.nLauncher
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.getIcon
import com.neoapps.neolauncher.search.SearchProviderController
import org.json.JSONObject

@Keep
open class OpenDrawerGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config),
    VerticalSwipeGestureHandler, StateChangeGestureHandler {

    override val displayName: String = context.getString(R.string.action_open_drawer)
    override val displayNameRes: Int = R.string.action_open_drawer
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_apps)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_apps
        )
    }
    override val requiresForeground = false

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.stateManager.goToState(LauncherState.ALL_APPS)
    }

    open fun getOnCompleteRunnable(controller: GestureController): Runnable? {
        return Runnable { }
    }

    override fun getTargetState(): LauncherState {
        return LauncherState.ALL_APPS
    }
}

@Keep
class OpenSettingsGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_settings)
    override val displayNameRes: Int = R.string.action_open_settings
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_omega_settings)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_omega_settings)
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
            `package` = controller.launcher.packageName
        })
    }
}

@Keep
class OpenOverviewGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_overview)
    override val displayNameRes: Int = R.string.action_open_overview
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_empty_recents)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_plus
        )
    }
    override val requiresForeground = false

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        Log.d("OpenOverviewGestureHandler", "onGestureTrigger from $view")
        if (true) { // TODO add pref for showing popup menu
            controller.launcher.showDefaultOptions(
                controller.touchDownPoint.x,
                controller.touchDownPoint.y
            )
        } else {
            //controller.launcher.stateManager.goToState(LauncherState.OPTIONS)
        }
    }
}

@Keep
class StartAppGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val hasConfig = true
    override val displayName
        get() = if (target != null)
            String.format(displayNameWithTarget, appName) else displayNameWithoutTarget

    override val displayNameRes
        get() = if (target != null)
            R.string.action_open_app else {
            R.string.action_open_app_with_target
        }

    override val icon: Drawable
        get() = when {
            intent != null -> try {
                context.packageManager.getActivityIcon(intent!!)
            } catch (e: Exception) {
                context.getIcon()
            }

            target != null -> try {
                context.packageManager.getApplicationIcon(target?.componentName!!.packageName)
            } catch (e: Exception) {
                context.getIcon()
            }

            else -> context.getIcon()
        }

    private val displayNameWithoutTarget: String = context.getString(R.string.action_open_app)
    private val displayNameWithTarget: String =
        context.getString(R.string.action_open_app_with_target)

    var type: String? = null
    var appName: String? = null
    var target: ComponentKey? = null
    var intent: Intent? = null
    var user: UserHandle? = null
    var packageName: String? = null
    var id: String? = null

    init {
        if (config?.has("appName") == true) {
            appName = config.getString("appName")
            type = if (config.has("type")) config.getString("type") else "app"
            if (type == "app") {
                Log.d("GestureController", "Class $target")
                target = Utilities.makeComponentKey(context, config.getString("target"))
            } else {
                intent = Intent.parseUri(config.getString("intent"), 0)
                user = context.getSystemService(UserManager::class.java)
                    .getUserForSerialNumber(config.getLong("user"))
                packageName = config.getString("packageName")
                id = config.getString("id")
            }
        }
    }

    override fun saveConfig(config: JSONObject) {
        super.saveConfig(config)
        config.put("appName", appName)
        config.put("type", type)
        when (type) {
            "app" -> {
                config.put("target", target.toString())
            }

            "shortcut" -> {
                config.put("intent", intent!!.toUri(0))
                config.put(
                    "user",
                    context.getSystemService(UserManager::class.java).getSerialNumberForUser(user)
                )
                config.put("packageName", packageName)
                config.put("id", id)
            }
        }
    }

    override fun onConfigResult(data: Intent?) {
        super.onConfigResult(data)
        if (data != null) {
            appName = data.getStringExtra("appName")
            type = data.getStringExtra("type")
            when (type) {
                "app" ->
                    target = Utilities
                        .makeComponentKey(context, data.getStringExtra("target") ?: "")

                "shortcut" -> {
                    intent = Intent.parseUri(data.getStringExtra("intent"), 0)
                    user = data.getParcelableExtra("user")
                    packageName = data.getStringExtra("packageName")
                    id = data.getStringExtra("id")
                }
            }
        }
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (view == null) {
            val down = controller.touchDownPoint
            controller.launcher.prepareDummyView(down.x.toInt(), down.y.toInt()) {
                onGestureTrigger(controller, controller.launcher.dummyView)
            }
            return
        }

        val opts = view.let {
            controller.launcher.getActivityLaunchOptions(it, null).toBundle()
        }

        fun showErrorToast() {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_LONG).show()
        }

        when (type) {
            "app" -> {
                target?.let {
                    try {
                        context.getSystemService(LauncherApps::class.java)
                            .startMainActivity(it.componentName, it.user, null, opts)
                    } catch (e: SecurityException) {
                        showErrorToast()
                    }
                } ?: run {
                    showErrorToast()
                }
            }

            "shortcut" -> {
                target?.let {
                    try {
                        context.getSystemService(LauncherApps::class.java)
                            .startShortcut(packageName!!, id!!, null, opts, user!!)
                    } catch (e: SecurityException) {
                        showErrorToast()
                    }
                } ?: run {
                    showErrorToast()
                }
            }
        }
    }
}

@Keep
class OpenWidgetsGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_widgets)
    override val displayNameRes: Int = R.string.action_open_widgets
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_widget)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_widget
        )
    }
    override val requiresForeground = false

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        WidgetsFullSheet.show(controller.launcher, true)
    }
}

@Keep
class OpenDashGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_dash)
    override val displayNameRes: Int = R.string.action_open_dash

    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_edit_dash)
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        DashSheet.show(controller.launcher)
    }
}

@Keep
class StartGlobalSearchGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    private val searchProvider get() = SearchProviderController.getInstance(context).activeSearchProvider
    override val displayName: String = context.getString(R.string.action_global_search)
    override val displayNameRes: Int = R.string.action_global_search
    override val icon: Drawable? by lazy {
        AppCompatResources.getDrawable(
            context,
            searchProvider.iconId
        )
    }
    override val requiresForeground = false

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        context.nLauncher.stateManager.goToState(
            LauncherState.ALL_APPS,
            true,
            AnimatorListeners.forEndCallback(Runnable { context.nLauncher.appsView.searchUiManager.startSearch() })
        )
    }
}

@Keep
class OpenOverlayGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_overlay)
    override val displayNameRes: Int = R.string.action_overlay
    override val icon = ContextCompat.getDrawable(context, R.drawable.ic_super_g_color)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_super_g_color
        )
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(
            Intent(Intent.ACTION_MAIN).setClassName(
                Config.GOOGLE_QSB,
                "${Config.GOOGLE_QSB}.SearchActivity"
            )
        )
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

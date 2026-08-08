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

package com.neoapps.neolauncher

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.android.launcher3.AppFilter
import com.android.launcher3.BaseActivity
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_ALL_APPS_PREDICTION
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_HOTSEAT_PREDICTION
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_WIDGETS_PREDICTION
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
import com.android.launcher3.LauncherState
import com.android.launcher3.LauncherState.ALL_APPS
import com.android.launcher3.LauncherState.EDIT_MODE
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.WorkspaceLayoutManager.ADD_PAGE_SCREEN_ID
import com.android.launcher3.appprediction.PredictionRowView
import com.android.launcher3.hybridhotseat.HotseatPredictionController
import com.android.launcher3.logging.InstanceId
import com.android.launcher3.logging.StatsLogManager
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.PredictedContainerInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.util.RunnableList
import com.android.launcher3.util.TouchController
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.neoapps.neolauncher.blur.WallpaperPermissionHelper
import com.neoapps.neolauncher.gestures.GestureController
import com.neoapps.neolauncher.gestures.VerticalSwipeGestureController
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PreferencesChangeCallback
import com.neoapps.neolauncher.shortcuts.OmegaShortcuts
import com.neoapps.neolauncher.theme.ThemeManager
import com.neoapps.neolauncher.theme.ThemeOverride
import com.neoapps.neolauncher.util.Config
import com.neoapps.neolauncher.util.hasWallpaperAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Objects
import java.util.function.Predicate
import java.util.stream.Stream

class NeoLauncher : Launcher(), SavedStateRegistryOwner,
    ActivityResultRegistryOwner, ThemeManager.ThemeableActivity {
    val prefs: NeoPrefs by inject()
    private val prefCallback = PreferencesChangeCallback(this)
    private var paused = false
    override var currentTheme = 0
    override var currentAccent = 0
    val allApps = ArrayList<AppInfo>()
    private val hiddenApps = ArrayList<AppInfo>()
    val gestureController by lazy { GestureController(this) }
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    val optionsView by lazy { findViewById<OptionsPopupView<Launcher>>(R.id.options_view)!! }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    var allAppsPredictions: PredictedContainerInfo =
        PredictedContainerInfo(CONTAINER_ALL_APPS_PREDICTION, emptyList())

    private lateinit var mHotseatPredictionController: HotseatPredictionController

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!this.hasWallpaperAccess) {
            WallpaperPermissionHelper.requestIfNeeded(this)
        }

        prefs.registerCallback(prefCallback)
        super.onCreate(savedInstanceState)
        savedStateRegistryController.performRestore(savedInstanceState)

        MODEL_EXECUTOR.handler.postAtFrontOfQueue { loadHiddenApps(prefs.drawerHiddenAppSet.getValue()) }

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val config = Config(this)
        config.setAppLanguage(prefs.profileLanguage.getValue())
        mOverlayManager = defaultOverlay
        val camManager = getSystemService(CAMERA_SERVICE) as CameraManager?
        camManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeUnavailable(cameraId: String) {
            }

            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                coroutineScope.launch {
                    if (cameraId == camManager.cameraIdList[0]) {
                        prefs.dashTorchState.setValue(enabled)
                    }
                }
            }
        }, Handler(Looper.getMainLooper()))

        NeoApp.instance?.onLauncherAppStateCreated()
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentAccent = prefs.profileAccentColor.getColor()
        currentTheme = themeOverride.getTheme(this)
        theme.applyStyle(
            resources.getIdentifier(
                Integer.toHexString(currentAccent),
                "style",
                packageName
            ), true
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == com.neoapps.neolauncher.util.Permissions.REQUEST_PERMISSION_WALLPAPER_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                com.neoapps.neolauncher.blur.BlurWallpaperProvider.getInstance(this).updateAsync()
            }
        }
    }

    override fun setupViews() {
        super.setupViews()
        mHotseatPredictionController = HotseatPredictionController(this)

        val overview: ViewGroup? = getOverviewPanel()
        val setHomeBtn = overview?.findViewById<View>(R.id.set_home_button)
        setHomeBtn?.setOnClickListener {
            val currentPage = workspace.currentPage
            prefs.desktopDefaultPage.setValue(currentPage)
            updateSetHomeButtonState()
            android.widget.Toast.makeText(
                this,
                R.string.home_screen_set_toast,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        val deletePageBtn = overview?.findViewById<View>(R.id.delete_page_button)
        deletePageBtn?.setOnClickListener {
            workspace?.removeCurrentPage()
            updateSetHomeButtonState()
        }

        val moveLeftBtn = overview?.findViewById<View>(R.id.move_page_left_button)
        moveLeftBtn?.setOnClickListener {
            workspace?.moveCurrentPage(-1)
            updateSetHomeButtonState()
        }

        val moveRightBtn = overview?.findViewById<View>(R.id.move_page_right_button)
        moveRightBtn?.setOnClickListener {
            workspace?.moveCurrentPage(1)
            updateSetHomeButtonState()
        }

        workspace?.addPageSwitchListener {
            updateSetHomeButtonState()
        }
    }

    override fun onStateSetStart(state: LauncherState) {
        super.onStateSetStart(state)
        val overview: ViewGroup? = getOverviewPanel()
        if (state == EDIT_MODE) {
            overview?.visibility = View.VISIBLE
            updateSetHomeButtonState()
        } else {
            overview?.visibility = View.GONE
        }
    }

    fun updateSetHomeButtonState() {
        val overview: ViewGroup = getOverviewPanel() ?: return
        val setHomeBtn = overview.findViewById<View>(R.id.set_home_button) ?: return
        val deletePageBtn = overview.findViewById<View>(R.id.delete_page_button)
        val moveLeftBtn = overview.findViewById<View>(R.id.move_page_left_button)
        val moveRightBtn = overview.findViewById<View>(R.id.move_page_right_button)

        val currentPage = workspace?.currentPage ?: 0
        val currentScreenId = workspace?.getScreenIdForPageIndex(currentPage) ?: -1

        val isAddPageScreen = currentScreenId == ADD_PAGE_SCREEN_ID

        if (isAddPageScreen) {
            setHomeBtn.visibility = View.GONE
            deletePageBtn?.visibility = View.GONE
            moveLeftBtn?.visibility = View.GONE
            moveRightBtn?.visibility = View.GONE
            return
        } else {
            setHomeBtn.visibility = View.VISIBLE
        }

        val screenOrder = workspace?.screenOrder
        var realScreenCount = 0
        var currentRealIndex = -1
        var realIdx = 0
        if (screenOrder != null) {
            for (i in 0 until screenOrder.size()) {
                val id = screenOrder.get(i)
                if (id != ADD_PAGE_SCREEN_ID) {
                    if (i == currentPage) currentRealIndex = realIdx
                    realIdx++
                    realScreenCount++
                }
            }
        }

        deletePageBtn?.visibility = if (realScreenCount > 1) View.VISIBLE else View.GONE

        moveLeftBtn?.visibility =
            if (realScreenCount > 1 && currentRealIndex > 0) View.VISIBLE else View.GONE
        moveRightBtn?.visibility =
            if (realScreenCount > 1 && currentRealIndex < realScreenCount - 1) View.VISIBLE else View.GONE

        val iconView = setHomeBtn.findViewById<android.widget.ImageView>(R.id.set_home_icon)
        val textView = setHomeBtn.findViewById<android.widget.TextView>(R.id.set_home_text)
        val defaultPage = prefs.desktopDefaultPage.getValue()

        if (currentPage == defaultPage) {
            iconView?.setImageResource(R.drawable.ic_home_page_filled)
            textView?.setText(R.string.default_home_screen)
            setHomeBtn.isSelected = true
        } else {
            iconView?.setImageResource(R.drawable.ic_home_page)
            textView?.setText(R.string.set_as_home_screen)
            setHomeBtn.isSelected = false
        }
    }

    override fun bindPredictedContainerInfo(info: PredictedContainerInfo?) {
        super.bindPredictedContainerInfo(info)
        if (info == null) return
        when (info.id) {
            CONTAINER_ALL_APPS_PREDICTION -> {
                allAppsPredictions = info
                appsView.floatingHeaderView.findFixedRowByType(PredictionRowView::class.java)
                    .setPredictedApps(info.getContents())
            }

            CONTAINER_HOTSEAT_PREDICTION -> {
                mHotseatPredictionController.setPredictedItems(info)
            }

            CONTAINER_WIDGETS_PREDICTION -> {
                widgetPickerDataProvider.setWidgetRecommendations(info.getContents())
            }
        }
    }

    override fun bindWorkspaceComponentsRemoved(matcher: Predicate<ItemInfo?>) {
        super.bindWorkspaceComponentsRemoved(matcher)
        mHotseatPredictionController.onModelItemsRemoved(matcher)
    }

    override fun logAppLaunch(
        statsLogManager: StatsLogManager,
        info: ItemInfo,
        instanceId: InstanceId
    ) {
        // If the app launch is from any of the surfaces in AllApps then add the InstanceId from
        // LiveSearchManager to recreate the AllApps session on the server side.

        var instanceId = instanceId
        if (mAllAppsSessionLogId != null && ALL_APPS.equals(
                stateManager.getCurrentStableState()
            )
        ) {
            instanceId = mAllAppsSessionLogId;
        }

        val logger = statsLogManager.logger().withItemInfo(info).withInstanceId(instanceId);

        if (info.itemType == ITEM_TYPE_APPLICATION
            || info.itemType == ITEM_TYPE_DEEP_SHORTCUT
        ) {
            val items = allAppsPredictions.getContents()
            val count = items.size
            for (i in 0 until count) {
                val targetInfo = items[i]
                if (targetInfo.itemType == info.itemType
                    && targetInfo.user == info.user
                    && Objects.equals(targetInfo.intent, info.intent)
                ) {
                    logger.withRank(i)
                    break
                }
            }
        }
        logger.log(StatsLogManager.LauncherEvent.LAUNCHER_APP_LAUNCH_TAP);

    }

    override fun onThemeChanged(forceUpdate: Boolean) = recreate()
    override val activityResultRegistry: ActivityResultRegistry
        get() = object : ActivityResultRegistry() {
            override fun <I : Any?, O : Any?> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?,
            ) {
                val activity = this@NeoLauncher

                // Immediate result path
                val synchronousResult = contract.getSynchronousResult(activity, input)
                if (synchronousResult != null) {
                    Handler(Looper.getMainLooper()).post {
                        dispatchResult(
                            requestCode,
                            synchronousResult.value
                        )
                    }
                    return
                }

                // Start activity path
                val intent = contract.createIntent(activity, input)
                var optionsBundle: Bundle? = null
                // If there are any extras, we should defensively set the classLoader
                if (intent.extras != null && intent.extras!!.classLoader == null) {
                    intent.setExtrasClassLoader(activity.classLoader)
                }
                if (intent.hasExtra(StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)) {
                    optionsBundle =
                        intent.getBundleExtra(StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)
                    intent.removeExtra(StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE)
                } else if (options != null) {
                    optionsBundle = options.toBundle()
                }
                if (ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS == intent.action) {
                    // requestPermissions path
                    var permissions =
                        intent.getStringArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS)
                    if (permissions == null) {
                        permissions = arrayOfNulls(0)
                    }
                    ActivityCompat.requestPermissions(activity, permissions, requestCode)
                } else if (StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST == intent.action) {
                    val request: IntentSenderRequest =
                        intent.getParcelableExtra(StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST)!!
                    try {
                        // startIntentSenderForResult path
                        ActivityCompat.startIntentSenderForResult(
                            activity, request.intentSender,
                            requestCode, request.fillInIntent, request.flagsMask,
                            request.flagsValues, 0, optionsBundle
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Handler(Looper.getMainLooper()).post {
                            dispatchResult(
                                requestCode, RESULT_CANCELED,
                                Intent()
                                    .setAction(StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST)
                                    .putExtra(
                                        StartIntentSenderForResult.EXTRA_SEND_INTENT_EXCEPTION,
                                        e
                                    )
                            )
                        }
                    }
                } else {
                    // startActivityForResult path
                    ActivityCompat.startActivityForResult(
                        activity,
                        intent,
                        requestCode,
                        optionsBundle
                    )
                }
            }

        }


    override fun startActivitySafely(v: View?, intent: Intent?, item: ItemInfo?): RunnableList? {
        val predictionRowView =
            appsView.floatingHeaderView.findFixedRowByType(PredictionRowView::class.java)
        predictionRowView.setPredictionUiUpdatePaused(true)
        val result = super.startActivitySafely(v, intent, item)
        if (result == null) {
            predictionRowView.setPredictionUiUpdatePaused(false)
        } else {
            result.add {
                predictionRowView.setPredictionUiUpdatePaused(false)
            }
        }
        return result
    }

    fun startActivitySafelyAuth(v: View?, intent: Intent, itemInfo: ItemInfo) {
        Config.showLockScreen(this, getString(R.string.trust_apps_manager_name)) {
            startActivitySafely(v, intent, itemInfo)
        }
    }

    private fun loadHiddenApps(hiddenAppsSet: Set<String>) {
        val mContext = this
        val appFilter = AppFilter(mContext)
        CoroutineScope(Dispatchers.IO).launch {
            for (user in UserCache.INSTANCE[mContext].userProfiles) {
                val duplicatePreventionCache: MutableList<ComponentName> = ArrayList()
                for (info in getSystemService(LauncherApps::class.java)!!
                    .getActivityList(null, user)) {
                    val key = ComponentKey(info.componentName, info.user)
                    if (hiddenAppsSet.contains(key.toString())) {
                        val appInfo = AppInfo(mContext, info, info.user)
                        appInfo.title = info.label
                        hiddenApps.add(appInfo)
                    }
                    if (prefs.searchHiddenApps.getValue()) {
                        if (!appFilter.shouldShowApp(info.componentName)) {
                            continue
                        }
                        if (!duplicatePreventionCache.contains(info.componentName)) {
                            duplicatePreventionCache.add(info.componentName)
                            val appInfo = AppInfo(mContext, info, user)
                            appInfo.title = info.label
                            allApps.add(appInfo)
                        }
                    }
                }
            }
        }
    }

    override fun getSupportedShortcuts(itemInfo: ItemInfo): Stream<SystemShortcut.Factory<*>> {
        return Stream.concat(
            super.getSupportedShortcuts(itemInfo),
            Stream.of(
                mHotseatPredictionController,
                OmegaShortcuts.CUSTOMIZE,
                OmegaShortcuts.APP_UNINSTALL
            )
        )
    }

    override fun onUiChangedWhileSleeping() {
        if (Utilities.ATLEAST_S) {
            super.onUiChangedWhileSleeping()
        }
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        if (mOverlayManager == null) {
            mOverlayManager = OverlayCallbackImpl(this)
        }
        return mOverlayManager
    }

    override fun onResume() {
        super.onResume()
        // lifecycle handled by the Activity/Launcher base class
        restartIfPending()
        dragLayer.viewTreeObserver.addOnDrawListener(object : ViewTreeObserver.OnDrawListener {
            private var handled = false

            override fun onDraw() {
                if (handled) {
                    return
                }
                handled = true

                dragLayer.post {
                    dragLayer.viewTreeObserver.removeOnDrawListener(this)
                }
            }
        })
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterCallback()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        savedStateRegistryController.performSave(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activityResultRegistry.dispatchResult(requestCode, resultCode, data)) {
            mPendingActivityRequestCode = -1
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    override fun createTouchControllers(): Array<TouchController> {
        val list = ArrayList<TouchController>()
        list.add(dragController)
        list.add(VerticalSwipeGestureController(this))

        return list.toTypedArray() + super.createTouchControllers()
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
        crossinline callback: (View) -> Unit,
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

    fun getViewBounds(v: View): Rect {
        val pos = IntArray(2)
        v.getLocationOnScreen(pos)
        return Rect(pos[0], pos[1], pos[0] + v.width, pos[1] + v.height)
    }

    fun recreateIfNotScheduled() {
        if (sRestartFlags == 0) {
            recreate()
        }
    }

    fun scheduleRestart() {
        if (paused) {
            sRestartFlags = FLAG_RESTART
        } else {
            Utilities.restartLauncher(this)
        }
    }

    private fun restartIfPending() {
        when {
            sRestartFlags and FLAG_RESTART != 0 -> neoApp.restart(false)
            sRestartFlags and FLAG_RECREATE != 0 -> {
                sRestartFlags = 0
                recreate()
            }
        }
    }

    companion object {
        @JvmStatic
        fun getLauncher(context: Context): NeoLauncher {
            return context as? NeoLauncher
                ?: (context as ContextWrapper).baseContext as? NeoLauncher
                ?: Launcher.getLauncher(context) as NeoLauncher
        }

        private const val FLAG_RECREATE = 1 shl 0
        private const val FLAG_RESTART = 1 shl 1

        var sRestartFlags = 0
    }
}

val Context.launcher: NeoLauncher
    get() = BaseActivity.fromContext(this)

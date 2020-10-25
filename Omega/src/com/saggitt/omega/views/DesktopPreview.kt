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
import android.graphics.Canvas
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.android.launcher3.*
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.views.BaseDragLayer
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.IconPreviewUtils
import com.saggitt.omega.util.runOnMainThread
import com.saggitt.omega.util.runOnThread
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider
import kotlinx.android.synthetic.omega.desktop_preview.view.*

class DesktopPreview(context: Context, attrs: AttributeSet?) :
        FrameLayout(PreviewContext(context), attrs), WorkspaceLayoutManager,
        OmegaPreferences.OnPreferenceChangeListener {

    private val previewContext = this.context as PreviewContext
    private val previewApps = IconPreviewUtils.getPreviewAppInfos(context)
    private var iconsLoaded = false
    private val viewLocation = IntArray(2)
    private val wallpaper = WallpaperPreviewProvider.getInstance(context).wallpaper

    private val idp = previewContext.idp
    private val homeElementInflater = LayoutInflater.from(ContextThemeWrapper(previewContext, R.style.HomeScreenElementTheme))

    private val prefsToWatch = arrayOf("pref_iconShape", "pref_colorizeGeneratedBackgrounds",
            "pref_enableWhiteOnlyTreatment", "pref_enableLegacyTreatment",
            "pref_generateAdaptiveForIconPack", "pref_forceShapeless")
    private var firstLoad = true

    init {
        runOnThread(Handler(MODEL_EXECUTOR.looper)) {
            val mIconCache = LauncherAppState.getInstance(context).iconCache
            previewApps.forEach { mIconCache.getTitleAndIcon(it, false) }
            iconsLoaded = true
            runOnMainThread { populatePreview() }
        }
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (!firstLoad) {
            populatePreview()
            requestLayout()
        } else
            firstLoad = false;
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = wallpaper.intrinsicWidth
        val height = wallpaper.intrinsicHeight
        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas)
            return
        }

        getLocationInWindow(viewLocation)
        val dm = resources.displayMetrics
        val scaleX = dm.widthPixels.toFloat() / width
        val scaleY = dm.heightPixels.toFloat() / height
        val scale = kotlin.math.max(scaleX, scaleY)

        canvas.save()
        canvas.translate(0f, -viewLocation[1].toFloat())
        canvas.scale(scale, scale)
        wallpaper.setBounds(0, 0, width, height)
        wallpaper.draw(canvas)
        canvas.restore()

        super.dispatchDraw(canvas)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        //populatePreview()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        firstLoad = true
        Utilities.getOmegaPrefs(previewContext).addOnPreferenceChangeListener(this, *prefsToWatch)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Utilities.getOmegaPrefs(previewContext).removeOnPreferenceChangeListener(this, *prefsToWatch)
    }

    fun populatePreview() {
        val dp = idp.getDeviceProfile(previewContext)
        val leftPadding = dp.workspacePadding.left + dp.workspaceCellPaddingXPx
        val rightPadding = dp.workspacePadding.right + dp.workspaceCellPaddingXPx
        val verticalPadding = (leftPadding + rightPadding) / 2 + dp.iconDrawablePaddingPx
        layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.dock_preview_height)
        if (!iconsLoaded || !isAttachedToWindow) return
        Log.d("DesktopPreview", "Ejecuntando vista")
        workspace.removeAllViews()


        workspace.setGridSize(idp.numColumns, 1)
        workspace.setPadding(leftPadding,
                verticalPadding,
                rightPadding,
                verticalPadding)

        previewApps.take(idp.numColumns).forEachIndexed { index, info ->
            info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP
            info.screenId = 0
            info.cellX = index
            info.cellY = 0
            inflateAndAddIcon(info)
        }
    }

    private fun inflateAndAddIcon(info: AppInfo) {
        val icon = homeElementInflater.inflate(
                R.layout.app_icon, workspace, false) as BubbleTextView
        icon.applyFromApplicationInfo(info)
        addInScreenFromBind(icon, info)
    }

    override fun getScreenWithId(screenId: Int) = workspace!!

    override fun getHotseat() = null

    private class PreviewContext(base: Context) : ContextThemeWrapper(
            base, ThemeOverride.Launcher().getTheme(base)), ActivityContext {

        val idp = LauncherAppState.getIDP(this)!!
        val dp get() = idp.getDeviceProfile(this)!!

        override fun getDeviceProfile(): DeviceProfile {
            return dp
        }

        override fun getDragLayer(): BaseDragLayer<*> {
            throw UnsupportedOperationException()
        }
    }
}
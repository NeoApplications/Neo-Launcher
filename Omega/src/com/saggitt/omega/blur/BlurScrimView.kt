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

package com.saggitt.omega.blur

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.Interpolator
import androidx.core.graphics.ColorUtils
import com.android.app.animation.Interpolators.ACCELERATE
import com.android.app.animation.Interpolators.ACCELERATE_2
import com.android.app.animation.Interpolators.LINEAR
import com.android.launcher3.LauncherState.BACKGROUND_APP
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.SystemUiController
import com.android.launcher3.util.Themes
import com.android.launcher3.views.ScrimView
import com.neoapps.neolauncher.nLauncher
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.runOnMainThread
import kotlin.math.roundToInt

// TODO remove
class BlurScrimView(context: Context, attrs: AttributeSet?) : ScrimView(context, attrs),
                                                              BlurWallpaperProvider.Listener {
    private val prefs = NeoPrefs.getInstance()
    private var drawerOpacity = prefs.drawerBackgroundOpacity.getValue()
    private var radius = prefs.profileBlurRadius.getValue()
    private var mLauncher = context.nLauncher

    private val blurDrawableCallback by lazy {
        object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {}

            override fun invalidateDrawable(who: Drawable) {
                runOnMainThread { invalidate() }
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
        }
    }
    private val provider by lazy { BlurWallpaperProvider.getInstance(context) }
    private val useFlatColor get() = mLauncher.deviceProfile.isVerticalBarLayout
    private var blurDrawable: BlurDrawable? = null

    private val insets = Rect()
    private val colorRanges = ArrayList<ColorRange>()

    private var allAppsBackground = 0

    private val reInitUiRunnable = this::reInitUi

    private var mEndScrim = Themes.getAttrColor(context, R.attr.allAppsScrimColor)

    private var mEndAlpha = Color.alpha(mEndScrim)
    private var mProgress = 1f
    private var mShelfColor = 0
    private var fullBlurProgress = 0f
    private var mBeforeMidProgressColorInterpolator: Interpolator = ACCELERATE
    private var mAfterMidProgressColorInterpolator: Interpolator = ACCELERATE

    // Mid point where the alpha changes
    private var mMidAlpha = 0
    private var mMidProgress = 0f

    fun updateSysUiColors() {
        val threshold = STATUS_BAR_COLOR_FORCE_UPDATE_THRESHOLD
        val forceChange = visibility == VISIBLE &&
                alpha > threshold && Color.alpha(backgroundColor) / (255f * drawerOpacity) > threshold
        with(systemUiController) {
            if (forceChange) {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, !isScrimDark)
            } else {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, 0)
            }
        }
    }

    private fun createBlurDrawable(): BlurDrawable? {
        blurDrawable?.let { if (isAttachedToWindow) it.stopListening() }
        return if (BlurWallpaperProvider.isEnabled) {
            provider.createDrawable(radius, 0f).apply {
                callback = blurDrawableCallback
                setBounds(left, top, right, bottom)
                if (isAttachedToWindow) startListening()
            }
        } else {
            null
        }
    }

    private fun reInitUi() {
        blurDrawable = createBlurDrawable()
        blurDrawable?.alpha = 0
        rebuildColors()
        updateColors()
    }

    private fun updateColors() {
        val alpha = when {
            useFlatColor                  -> ((1 - mProgress) * 255).toInt()
            mProgress >= fullBlurProgress -> (255 * ACCELERATE_2.getInterpolation(
                0f.coerceAtLeast(1 - mProgress) / (1 - fullBlurProgress)
            )).roundToInt()

            else                          -> 255
        }
        blurDrawable?.alpha = alpha

        if (!useFlatColor) {
            mShelfColor = if (mProgress >= 1
                && mLauncher.stateManager.state == BACKGROUND_APP
            ) {
                ColorUtils.setAlphaComponent(allAppsBackground, mMidAlpha)
            } else {
                getColorForProgress(mProgress)
            }
        }
    }

    private fun getColorForProgress(progress: Float): Int {
        val interpolatedProgress: Float = when {
            progress >= 1            -> progress
            progress >= mMidProgress -> Utilities.mapToRange(
                progress, mMidProgress, 1f, mMidProgress, 1f,
                mBeforeMidProgressColorInterpolator
            )

            else                     -> Utilities.mapToRange(
                progress, 0f, mMidProgress, 0f, mMidProgress, mAfterMidProgressColorInterpolator
            )
        }
        colorRanges.forEach {
            if (interpolatedProgress in it) {
                return it.getColor(interpolatedProgress)
            }
        }
        return 0
    }

    override fun isScrimDark() = if (drawerOpacity <= 0.3f) {
        !Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText)
    } else {
        super.isScrimDark()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        BlurWallpaperProvider.getInstance(context).addListener(this)
        blurDrawable?.startListening()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        BlurWallpaperProvider.getInstance(context).removeListener(this)
        blurDrawable?.stopListening()
    }

    private fun rebuildColors() {

        val fullShelfColor = ColorUtils.setAlphaComponent(allAppsBackground, mEndAlpha)
        val nullShelfColor = ColorUtils.setAlphaComponent(allAppsBackground, 0)

        val colors = ArrayList<Pair<Float, Int>>()
        colors.add(Pair(Float.NEGATIVE_INFINITY, fullShelfColor))
        colors.add(Pair(0.5f, fullShelfColor))

        colors.add(Pair(1f, nullShelfColor))
        colors.add(Pair(Float.POSITIVE_INFINITY, nullShelfColor))

        colorRanges.clear()

        for (i in (1 until colors.size)) {
            val color1 = colors[i - 1]
            val color2 = colors[i]
            colorRanges.add(ColorRange(color1.first, color2.first, color1.second, color2.second))
        }
    }

    private fun postReInitUi() {
        handler?.removeCallbacks(reInitUiRunnable)
        handler?.post(reInitUiRunnable)
    }

    override fun setInsets(insets: Rect) {
        super.setInsets(insets)
        this.insets.set(insets)
        postReInitUi()
    }

    fun onDrawRoundRect(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        rx: Float,
        ry: Float,
        paint: Paint,
    ) {
        blurDrawable?.run {
            setBlurBounds(left, top, right, bottom)
            draw(canvas)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (useFlatColor) {
            blurDrawable?.setBounds(left, top, right, bottom)
        }
    }

    class ColorRange(
        private val start: Float, private val end: Float,
        private val startColor: Int, private val endColor: Int,
    ) {

        private val range = start..end

        fun getColor(progress: Float): Int {
            if (start == Float.NEGATIVE_INFINITY) return endColor
            if (end == Float.POSITIVE_INFINITY) return startColor
            val amount = Utilities.mapToRange(progress, start, end, 0f, 1f, LINEAR)
            return ColorUtils.blendARGB(startColor, endColor, amount)
        }

        operator fun contains(value: Float) = value in range
    }
}
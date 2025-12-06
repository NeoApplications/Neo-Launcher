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

package com.neoapps.neolauncher.hotseat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.android.launcher3.Hotseat
import com.android.launcher3.R
import com.android.launcher3.icons.ShadowGenerator
import com.neoapps.neolauncher.NeoLauncher
import com.saggitt.omega.blur.BlurDrawable
import com.saggitt.omega.blur.BlurWallpaperProvider
import com.saggitt.omega.graphics.NinePatchDrawHelper
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.runOnMainThread
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlin.math.roundToInt

open class CustomHotseat @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) :
    Hotseat(context, attrs, defStyleAttr),
    BlurWallpaperProvider.Listener {

    val launcher = NeoLauncher.getLauncher(context)
    val prefs by lazy { NeoPrefs.getInstance() }

    private var backgroundEnable = false
    private var hotseatDisabled = false
    private var radius = context.resources.getDimension(R.dimen.enforced_rounded_corner_max_radius)
    private var defaultRadius = radius

    val scope = launcher.lifecycleScope.coroutineContext

    private var backgroundColor = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowBlur = resources.getDimension(R.dimen.all_apps_scrim_blur)
    private val shadowHelper = NinePatchDrawHelper()
    private var shadowBitmap = generateShadowBitmap()
    private val blurProvider by lazy { BlurWallpaperProvider.getInstance(context) }
    private var blurDrawable: BlurDrawable? = null
        set(value) {
            if (isAttachedToWindow) {
                field?.stopListening()
            }
            field = value
            if (isAttachedToWindow) {
                field?.startListening()
            }
        }

    private val blurDrawableCallback by lazy {
        object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {}
            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
            override fun invalidateDrawable(who: Drawable) {
                runOnMainThread { invalidate() }
            }
        }
    }

    init {
        if (hotseatDisabled) {
            super.setVisibility(GONE)
        }
        setWillNotDraw(!backgroundEnable || launcher.deviceProfile.isVerticalBarLayout)
        createBlurDrawable()
        combine(
            prefs.dockCustomBackground.get(),
            prefs.dockBackgroundColor.get(),
            prefs.dockHide.get(),
            prefs.profileWindowCornerRadius.get()
        ) { customBackground, color, hide, dockRadius ->
            backgroundEnable = customBackground
            backgroundColor = AccentColorOption.fromString(color).accentColor
            hotseatDisabled = hide
            radius = dpToPx(if (dockRadius > -1) dockRadius else defaultRadius)
            if (hotseatDisabled) {
                super.setVisibility(GONE)
            } else {
                super.setVisibility(VISIBLE)
            }
            reload()
        }.launchIn(launcher.lifecycleScope)
    }

    private fun reload() {
        shadowBitmap = generateShadowBitmap()
        setWillNotDraw(!backgroundEnable || launcher.deviceProfile.isVerticalBarLayout)
        createBlurDrawable()
        paint.color = backgroundColor
        invalidate()
    }

    override fun setVisibility(visibility: Int) {
        if (!hotseatDisabled) {
            super.setVisibility(visibility)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        blurProvider.addListener(this)
        blurDrawable?.startListening()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        blurProvider.removeListener(this)
        blurDrawable?.stopListening()
    }

    override fun draw(canvas: Canvas) {
        if (backgroundEnable) {
            drawBackground(canvas)
        }
        super.draw(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        val adjustmentX = left.toFloat() + translationX
        val adjustmentY = top.toFloat() + translationY
        val left = 0f + adjustmentX
        val top = -radius + adjustmentY
        val right = width.toFloat() + adjustmentX
        val bottom = height * 2f + adjustmentY
        canvas.save()
        canvas.translate(-adjustmentX, -adjustmentY)
        blurDrawable?.run {
            blurScaleX = 1 / scaleX
            blurScaleY = 1 / scaleY
            blurPivotX = pivotX
            blurPivotY = pivotY
            setBlurBounds(left, top, right, bottom)
            draw(canvas)
        }
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        shadowHelper.drawVerticallyStretched(
            shadowBitmap, canvas,
            left - shadowBlur,
            top - shadowBlur,
            right + shadowBlur,
            bottom
        )
        canvas.restore()
    }

    override fun setAlpha(alpha: Float) {
        shortcutsAndWidgets.alpha = alpha
    }

    override fun getAlpha(): Float {
        if (shortcutsAndWidgets == null) return 1f
        return shortcutsAndWidgets.alpha
    }

    override fun setTranslationX(translationX: Float) {
        super.setTranslationX(translationX)
        invalidateBlur()
    }

    private fun generateShadowBitmap(): Bitmap {
        val tmp = radius + shadowBlur
        val builder = ShadowGenerator.Builder(0)
        builder.radius = radius
        builder.shadowBlur = shadowBlur
        val round = 2 * tmp.roundToInt() + 20
        val bitmap = Bitmap.createBitmap(round, round / 2, Bitmap.Config.ARGB_8888)
        val f = 2f * tmp + 20f - shadowBlur
        builder.bounds.set(shadowBlur, shadowBlur, f, f)
        builder.drawShadow(Canvas(bitmap))
        return bitmap
    }

    private fun createBlurDrawable() {
        blurDrawable = if (isVisible && BlurWallpaperProvider.isEnabled) {
            val drawable = blurDrawable ?: blurProvider.createDrawable(radius, radius)
            drawable.apply {
                blurRadii = BlurDrawable.Radii(radius)
                callback = blurDrawableCallback
                setBounds(left, top, right, bottom)
                if (isAttachedToWindow) startListening()
            }
        } else {
            null
        }
    }

    private fun invalidateBlur() {
        if (blurDrawable != null) {
            invalidate()
        }
    }

    override fun onEnabledChanged() {
        super.onEnabledChanged()
        createBlurDrawable()
        invalidate()
    }
}
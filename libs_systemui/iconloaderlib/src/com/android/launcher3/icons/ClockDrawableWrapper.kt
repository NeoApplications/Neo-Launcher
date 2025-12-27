/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.icons

import android.content.Context
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.SystemClock
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.util.Supplier
import com.android.launcher3.icons.BitmapInfo.Extender
import com.android.launcher3.icons.FastBitmapDrawableDelegate.Companion.drawShaderInBounds
import com.android.launcher3.icons.FastBitmapDrawableDelegate.DelegateFactory
import com.android.launcher3.icons.GraphicsUtils.getColorMultipliedFilter
import com.android.launcher3.icons.GraphicsUtils.resizeToContentSize
import com.neoapps.neolauncher.icons.ClockMetadata
import java.util.Calendar
import java.util.concurrent.TimeUnit.MINUTES


/**
 * Wrapper over [AdaptiveIconDrawable] to intercept icon flattening logic for dynamic clock icons
 */
class ClockDrawableWrapper
private constructor(base: AdaptiveIconDrawable, private val animationInfo: ClockAnimationInfo) :
    AdaptiveIconDrawable(base.background, base.foreground), Extender {

    override fun getMonochrome(): Drawable? {
        val monoLayer =
            (animationInfo.baseDrawableState.newDrawable().mutate() as? AdaptiveIconDrawable)
                ?.monochrome
        if (monoLayer is LayerDrawable) animationInfo.applyTime(Calendar.getInstance(), monoLayer)
        return monoLayer
    }

    override fun getUpdatedBitmapInfo(info: BitmapInfo, factory: BaseIconFactory): BitmapInfo {
        val bitmapSize = factory.iconBitmapSize
        val flattenBG =
            BitmapRenderer.createHardwareBitmap(bitmapSize, bitmapSize) {
                val drawable = AdaptiveIconDrawable(background.constantState!!.newDrawable(), null)
                drawable.setBounds(0, 0, bitmapSize, bitmapSize)
                it.drawColor(Color.BLACK)
                drawable.background?.draw(it)
            }
        return info.copy(
            delegateFactory =
                animationInfo.copy(
                    themeFgColor = NO_COLOR,
                    shader = BitmapShader(flattenBG, CLAMP, CLAMP),
                )
        )
    }

    override fun drawForPersistence() {
        val foreground = foreground as LayerDrawable
        resetLevel(foreground, animationInfo.hourLayerIndex)
        resetLevel(foreground, animationInfo.minuteLayerIndex)
        resetLevel(foreground, animationInfo.secondLayerIndex)
    }

    private fun resetLevel(drawable: LayerDrawable, index: Int) {
        if (index != INVALID_VALUE) drawable.getDrawable(index).setLevel(0)
    }

    data class ClockAnimationInfo(
        val hourLayerIndex: Int,
        val minuteLayerIndex: Int,
        val secondLayerIndex: Int,
        val defaultHour: Int,
        val defaultMinute: Int,
        val defaultSecond: Int,
        val baseDrawableState: ConstantState,
        val themeFgColor: Int = NO_COLOR,
        val shader: Shader? = null,
    ) : DelegateFactory {

        fun applyTime(time: Calendar, foregroundDrawable: LayerDrawable): Boolean {
            time.timeInMillis = System.currentTimeMillis()

            // We need to rotate by the difference from the default time if one is specified.
            val invalidateHour =
                foregroundDrawable.applyLevel(hourLayerIndex) {
                    val convertedHour = (time[Calendar.HOUR] + (12 - defaultHour)) % 12
                    convertedHour * 60 + time[Calendar.MINUTE]
                }
            val invalidateMinute =
                foregroundDrawable.applyLevel(minuteLayerIndex) {
                    val convertedMinute = (time[Calendar.MINUTE] + (60 - defaultMinute)) % 60
                    time[Calendar.HOUR] * 60 + convertedMinute
                }
            val invalidateSecond =
                foregroundDrawable.applyLevel(secondLayerIndex) {
                    val convertedSecond = (time[Calendar.SECOND] + (60 - defaultSecond)) % 60
                    convertedSecond * LEVELS_PER_SECOND
                }
            return invalidateHour || invalidateMinute || invalidateSecond
        }

        override fun newDelegate(
            bitmapInfo: BitmapInfo,
            iconShape: IconShape,
            paint: Paint,
            host: FastBitmapDrawable,
        ): FastBitmapDrawableDelegate {
            return ClockDrawableDelegate(this, host, paint, iconShape)
        }
    }

    private class ClockDrawableDelegate(
        private val animInfo: ClockAnimationInfo,
        private val host: FastBitmapDrawable,
        private val paint: Paint,
        private val iconShape: IconShape,
    ) : FastBitmapDrawableDelegate, Runnable {

        private val time = Calendar.getInstance()
        private val themedFgColor = animInfo.themeFgColor

        private val foreground =
            ((animInfo.baseDrawableState.newDrawable().mutate() as AdaptiveIconDrawable).foreground
                    as LayerDrawable)
                .apply {
                    val extraMargin = (getExtraInsetFraction() * iconShape.pathSize).toInt()
                    setBounds(
                        -extraMargin,
                        -extraMargin,
                        iconShape.pathSize + extraMargin,
                        iconShape.pathSize + extraMargin,
                    )
                    colorFilter = getColorMultipliedFilter(themedFgColor, paint.colorFilter)
                }

        override fun setAlpha(alpha: Int) {
            foreground.alpha = alpha
        }

        override fun drawContent(
            info: BitmapInfo,
            iconShape: IconShape,
            canvas: Canvas,
            bounds: Rect,
            paint: Paint,
        ) {
            canvas.drawShaderInBounds(bounds, iconShape, paint, animInfo.shader)

            // prepare and draw the foreground
            animInfo.applyTime(time, foreground)
            canvas.resizeToContentSize(bounds, iconShape.pathSize.toFloat()) {
                clipPath(iconShape.path)
                foreground.draw(this)
            }
            reschedule()
        }

        override fun isThemed(): Boolean {
            return themedFgColor != NO_COLOR
        }

        override fun updateFilter(filter: ColorFilter?) {
            foreground.colorFilter = getColorMultipliedFilter(themedFgColor, filter)
        }

        override fun getIconColor(info: BitmapInfo): Int {
            return if (isThemed()) themedFgColor else super.getIconColor(info)
        }

        override fun run() {
            if (animInfo.applyTime(time, foreground)) {
                host.invalidateSelf()
            } else {
                reschedule()
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            if (isVisible) {
                reschedule()
            } else {
                host.unscheduleSelf(this)
            }
        }

        fun reschedule() {
            if (!host.isVisible) {
                return
            }
            host.unscheduleSelf(this)
            val upTime = SystemClock.uptimeMillis()
            val step = TICK_MS /* tick every 200 ms */
            host.scheduleSelf(this, upTime - ((upTime % step)) + step)
        }
    }

    companion object {
        @JvmField
        var sRunningInTest: Boolean = false

        private const val TAG = "ClockDrawableWrapper"

        private const val DISABLE_SECONDS = true
        private const val NO_COLOR = Color.TRANSPARENT

        // Time after which the clock icon should check for an update. The actual invalidate
        // will only happen in case of any change.
        val TICK_MS: Long = if (DISABLE_SECONDS) MINUTES.toMillis(1) else 200L

        private const val LAUNCHER_PACKAGE = "com.android.launcher3"
        private const val ROUND_ICON_METADATA_KEY = "$LAUNCHER_PACKAGE.LEVEL_PER_TICK_ICON_ROUND"
        private const val HOUR_INDEX_METADATA_KEY = "$LAUNCHER_PACKAGE.HOUR_LAYER_INDEX"
        private const val MINUTE_INDEX_METADATA_KEY = "$LAUNCHER_PACKAGE.MINUTE_LAYER_INDEX"
        private const val SECOND_INDEX_METADATA_KEY = "$LAUNCHER_PACKAGE.SECOND_LAYER_INDEX"
        private const val DEFAULT_HOUR_METADATA_KEY = "$LAUNCHER_PACKAGE.DEFAULT_HOUR"
        private const val DEFAULT_MINUTE_METADATA_KEY = "$LAUNCHER_PACKAGE.DEFAULT_MINUTE"
        private const val DEFAULT_SECOND_METADATA_KEY = "$LAUNCHER_PACKAGE.DEFAULT_SECOND"

        /* Number of levels to jump per second for the second hand */
        private const val LEVELS_PER_SECOND = 10

        const val INVALID_VALUE: Int = -1

        /**
         * Loads and returns the wrapper from the provided package, or returns null if it is unable
         * to load.
         */
        @JvmStatic
        fun forPackage(context: Context, pkg: String, iconDpi: Int): ClockDrawableWrapper? {
            try {
                return loadClockDrawableUnsafe(context, pkg, iconDpi)
            } catch (e: Exception) {
                Log.d(TAG, "Unable to load clock drawable info", e)
            }
            return null
        }

        private inline fun LayerDrawable.applyLevel(index: Int, level: () -> Int) =
            (index != INVALID_VALUE && getDrawable(index).setLevel(level.invoke()))

        /** Tries to load clock drawable by reading packageManager information */
        @Throws(Exception::class)
        private fun loadClockDrawableUnsafe(
            context: Context,
            pkg: String,
            iconDpi: Int,
        ): ClockDrawableWrapper? {
            val pm = context.packageManager
            val appInfo =
                pm.getApplicationInfo(pkg, MATCH_UNINSTALLED_PACKAGES or GET_META_DATA)
                    ?: return null
            val res = pm.getResourcesForApplication(appInfo)
            val metadata = appInfo.metaData ?: return null
            val drawableId = metadata.getInt(ROUND_ICON_METADATA_KEY, 0)
            val drawable =
                res.getDrawableForDensity(drawableId, iconDpi)?.mutate() as? AdaptiveIconDrawable
                    ?: return null

            val foreground = drawable.foreground as? LayerDrawable ?: return null
            val layerCount = foreground.numberOfLayers

            fun getLayerIndex(key: String) =
                metadata.getInt(key, INVALID_VALUE).let {
                    if (it < 0 || it >= layerCount) INVALID_VALUE else it
                }

            var animInfo =
                ClockAnimationInfo(
                    hourLayerIndex = getLayerIndex(HOUR_INDEX_METADATA_KEY),
                    minuteLayerIndex = getLayerIndex(MINUTE_INDEX_METADATA_KEY),
                    secondLayerIndex = getLayerIndex(SECOND_INDEX_METADATA_KEY),
                    defaultHour = metadata.getInt(DEFAULT_HOUR_METADATA_KEY, 0),
                    defaultMinute = metadata.getInt(DEFAULT_MINUTE_METADATA_KEY, 0),
                    defaultSecond = metadata.getInt(DEFAULT_SECOND_METADATA_KEY, 0),
                    baseDrawableState = drawable.constantState!!,
                )

            if (DISABLE_SECONDS && animInfo.secondLayerIndex != INVALID_VALUE) {
                foreground.setDrawable(animInfo.secondLayerIndex, null)
                animInfo = animInfo.copy(secondLayerIndex = INVALID_VALUE)
            }
            animInfo.applyTime(Calendar.getInstance(), foreground)
            return ClockDrawableWrapper(drawable, animInfo)
        }
    }
}

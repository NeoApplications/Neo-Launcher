/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.R
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.Callback
import android.util.FloatProperty
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.ColorUtils
import com.android.launcher3.icons.BitmapInfo.DrawableCreationFlags
import kotlin.math.min
open class FastBitmapDrawable(info: BitmapInfo?) : Drawable(), Callback {
    @JvmOverloads constructor(b: Bitmap, iconColor: Int = 0) : this(BitmapInfo.of(b, iconColor))
    @JvmField val bitmapInfo: BitmapInfo = info ?: BitmapInfo.LOW_RES_INFO
    var isAnimationEnabled: Boolean = true
    @JvmField protected val paint: Paint = Paint(FILTER_BITMAP_FLAG or ANTI_ALIAS_FLAG)
    @JvmField @VisibleForTesting var isPressed: Boolean = false
    @JvmField @VisibleForTesting var isHovered: Boolean = false
    @JvmField var disabledAlpha: Float = 1f
    var isDisabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                badge.let { if (it is FastBitmapDrawable) it.isDisabled = value }
                updateFilter()
            }
        }
    @JvmField @DrawableCreationFlags var creationFlags: Int = 0
    @JvmField @VisibleForTesting var scaleAnimation: ObjectAnimator? = null
    var hoverScaleEnabledForDisplay = true
    private var scale = 1f
    private var paintAlpha = 255
    private var paintFilter: ColorFilter? = null
    init {
        isFilterBitmap = true
    }
    var badge: Drawable? = null
        set(value) {
            field?.callback = null
            field = value
            field?.let {
                it.callback = this
                it.setBadgeBounds(bounds)
            }
            updateFilter()
        }
    /** Returns true if the drawable points to the same bitmap icon object */
    fun isSameInfo(info: BitmapInfo): Boolean = bitmapInfo === info
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        badge?.setBadgeBounds(bounds)
    }
    override fun draw(canvas: Canvas) {
        if (scale != 1f) {
            val count = canvas.save()
            val bounds = bounds
            canvas.scale(scale, scale, bounds.exactCenterX(), bounds.exactCenterY())
            drawInternal(canvas, bounds)
            badge?.draw(canvas)
            canvas.restoreToCount(count)
        } else {
            drawInternal(canvas, bounds)
            badge?.draw(canvas)
        }
    }
    protected open fun drawInternal(canvas: Canvas, bounds: Rect) {
        canvas.drawBitmap(bitmapInfo.icon, null, bounds, paint)
    }
    /** Returns the primary icon color, slightly tinted white */
    open fun getIconColor(): Int =
        ColorUtils.compositeColors(
            GraphicsUtils.setColorAlphaBound(Color.WHITE, WHITE_SCRIM_ALPHA),
            bitmapInfo.color,
        )
    /** Returns if this represents a themed icon */
    open fun isThemed(): Boolean = false
    /**
     * Returns true if the drawable was created with theme, even if it doesn't support theming
     * itself.
     */
    fun isCreatedForTheme(): Boolean = isThemed() || (creationFlags and BitmapInfo.FLAG_THEMED) != 0
    override fun setColorFilter(cf: ColorFilter?) {
        paintFilter = cf
        updateFilter()
    }
    override fun getColorFilter(): ColorFilter? = paint.colorFilter
    @Deprecated("This method is no longer used in graphics optimizations")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) {
        if (paintAlpha != alpha) {
            paintAlpha = alpha
            paint.alpha = alpha
            invalidateSelf()
            badge?.alpha = alpha
        }
    }
    override fun getAlpha(): Int = paintAlpha
    override fun setFilterBitmap(filterBitmap: Boolean) {
        paint.isFilterBitmap = filterBitmap
        paint.isAntiAlias = filterBitmap
    }
    fun resetScale() {
        scaleAnimation?.cancel()
        scaleAnimation = null
        scale = 1f
        invalidateSelf()
    }
    fun getAnimatedScale(): Float = if (scaleAnimation == null) 1f else scale
    override fun getIntrinsicWidth(): Int = bitmapInfo.icon.width
    override fun getIntrinsicHeight(): Int = bitmapInfo.icon.height
    override fun getMinimumWidth(): Int = bounds.width()
    override fun getMinimumHeight(): Int = bounds.height()
    override fun isStateful(): Boolean = true
    public override fun onStateChange(state: IntArray): Boolean {
        if (!isAnimationEnabled) {
            return false
        }
        var isPressed = false
        var isHovered = false
        for (s in state) {
            if (s == R.attr.state_pressed) {
                isPressed = true
                break
            } else if (s == R.attr.state_hovered && hoverScaleEnabledForDisplay) {
                isHovered = true
                // Do not break on hovered state, as pressed state should take precedence.
            }
        }
        if (this.isPressed != isPressed || this.isHovered != isHovered) {
            scaleAnimation?.cancel()
            val endScale =
                when {
                    isPressed -> PRESSED_SCALE
                    isHovered -> HOVERED_SCALE
                    else -> 1f
                }
            if (scale != endScale) {
                if (isVisible) {
                    scaleAnimation =
                        ObjectAnimator.ofFloat(this, SCALE, endScale).apply {
                            duration =
                                if (isPressed != this@FastBitmapDrawable.isPressed)
                                    CLICK_FEEDBACK_DURATION.toLong()
                                else HOVER_FEEDBACK_DURATION.toLong()
                            interpolator =
                                if (isPressed != this@FastBitmapDrawable.isPressed)
                                    (if (isPressed) ACCEL else DEACCEL)
                                else HOVER_EMPHASIZED_DECELERATE_INTERPOLATOR
                        }
                    scaleAnimation?.start()
                } else {
                    scale = endScale
                    invalidateSelf()
                }
            }
            this.isPressed = isPressed
            this.isHovered = isHovered
            return true
        }
        return false
    }
    /** Updates the paint to reflect the current brightness and saturation. */
    protected open fun updateFilter() {
        paint.setColorFilter(if (isDisabled) getDisabledColorFilter(disabledAlpha) else paintFilter)
        badge?.colorFilter = colorFilter
        invalidateSelf()
    }
    protected open fun newConstantState(): FastBitmapConstantState {
        return FastBitmapConstantState(bitmapInfo)
    }
    override fun getConstantState(): ConstantState {
        val cs = newConstantState()
        cs.mIsDisabled = isDisabled
        cs.mBadgeConstantState = badge?.constantState
        cs.mCreationFlags = creationFlags
        return cs
    }
    // Returns if the FastBitmapDrawable contains a badge.
    fun hasBadge(): Boolean = (creationFlags and BitmapInfo.FLAG_NO_BADGE) == 0
    override fun invalidateDrawable(who: Drawable) {
        if (who === badge) {
            invalidateSelf()
        }
    }
    override fun scheduleDrawable(who: Drawable, what: Runnable, time: Long) {
        if (who === badge) {
            scheduleSelf(what, time)
        }
    }
    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }
    open class FastBitmapConstantState(val bitmapInfo: BitmapInfo) : ConstantState() {
        // These are initialized later so that subclasses don't need to
        // pass everything in constructor
        var mIsDisabled: Boolean = false
        var mBadgeConstantState: ConstantState? = null
        @DrawableCreationFlags var mCreationFlags: Int = 0
        constructor(bitmap: Bitmap, color: Int) : this(BitmapInfo.of(bitmap, color))
        protected open fun createDrawable(): FastBitmapDrawable {
            return FastBitmapDrawable(bitmapInfo)
        }
        override fun newDrawable(): FastBitmapDrawable {
            val drawable = createDrawable()
            drawable.isDisabled = mIsDisabled
            if (mBadgeConstantState != null) {
                drawable.badge = mBadgeConstantState!!.newDrawable()
            }
            drawable.creationFlags = mCreationFlags
            return drawable
        }
        override fun getChangingConfigurations(): Int = 0
    }
    companion object {
        private val ACCEL: Interpolator = AccelerateInterpolator()
        private val DEACCEL: Interpolator = DecelerateInterpolator()
        private val HOVER_EMPHASIZED_DECELERATE_INTERPOLATOR: Interpolator =
            PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f)
        @VisibleForTesting const val PRESSED_SCALE: Float = 1.1f
        @VisibleForTesting const val HOVERED_SCALE: Float = 1.1f
        const val WHITE_SCRIM_ALPHA: Int = 138
        private const val DISABLED_DESATURATION = 1f
        private const val DISABLED_BRIGHTNESS = 0.5f
        const val FULLY_OPAQUE: Int = 255
        const val CLICK_FEEDBACK_DURATION: Int = 200
        const val HOVER_FEEDBACK_DURATION: Int = 300
        // Animator and properties for the fast bitmap drawable's scale
        @VisibleForTesting
        @JvmField
        val SCALE: FloatProperty<FastBitmapDrawable> =
            object : FloatProperty<FastBitmapDrawable>("scale") {
                override fun get(fastBitmapDrawable: FastBitmapDrawable): Float {
                    return fastBitmapDrawable.scale
                }
                override fun setValue(fastBitmapDrawable: FastBitmapDrawable, value: Float) {
                    fastBitmapDrawable.scale = value
                    fastBitmapDrawable.invalidateSelf()
                }
            }
        @JvmStatic
        @JvmOverloads
        fun getDisabledColorFilter(disabledAlpha: Float = 1f): ColorFilter {
            val tempBrightnessMatrix = ColorMatrix()
            val tempFilterMatrix = ColorMatrix()
            tempFilterMatrix.setSaturation(1f - DISABLED_DESATURATION)
            val scale = 1 - DISABLED_BRIGHTNESS
            val brightnessI = (255 * DISABLED_BRIGHTNESS).toInt()
            val mat = tempBrightnessMatrix.array
            mat[0] = scale
            mat[6] = scale
            mat[12] = scale
            mat[4] = brightnessI.toFloat()
            mat[9] = brightnessI.toFloat()
            mat[14] = brightnessI.toFloat()
            mat[18] = disabledAlpha
            tempFilterMatrix.preConcat(tempBrightnessMatrix)
            return ColorMatrixColorFilter(tempFilterMatrix)
        }
        @JvmStatic
        fun getDisabledColor(color: Int): Int {
            val avgComponent = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
            val scale = 1 - DISABLED_BRIGHTNESS
            val brightnessI = (255 * DISABLED_BRIGHTNESS).toInt()
            val component = min(Math.round(scale * avgComponent + brightnessI), FULLY_OPAQUE)
            return Color.rgb(component, component, component)
        }
        /** Sets the bounds for the badge drawable based on the main icon bounds */
        @JvmStatic
        fun Drawable.setBadgeBounds(iconBounds: Rect) {
            val size = BaseIconFactory.getBadgeSizeForIconSize(iconBounds.width())
            setBounds(
                iconBounds.right - size,
                iconBounds.bottom - size,
                iconBounds.right,
                iconBounds.bottom,
            )
        }
    }
}
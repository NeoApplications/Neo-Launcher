/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import com.android.launcher3.icons.FastBitmapDrawableDelegate.DelegateFactory
import com.android.launcher3.icons.GraphicsUtils.getAttrColor
import com.android.launcher3.icons.GraphicsUtils.resizeToContentSize

/** Subclass which draws a placeholder icon when the actual icon is not yet loaded */
class PlaceHolderDrawableDelegate(info: BitmapInfo, paint: Paint, loadingColor: Int) :
    FastBitmapDrawableDelegate {

    private val fillColor = ColorUtils.compositeColors(loadingColor, info.color)

    init {
        paint.color = fillColor
    }

    override fun drawContent(
        info: BitmapInfo,
        iconShape: IconShape,
        canvas: Canvas,
        bounds: Rect,
        paint: Paint,
    ) {
        canvas.resizeToContentSize(bounds, iconShape.pathSize.toFloat()) {
            iconShape.shapeRenderer.render(this, paint)
        }
    }

    /** Updates this placeholder to `newIcon` with animation. */
    fun animateIconUpdate(newIcon: Drawable) {
        val placeholderColor = fillColor
        val originalAlpha = Color.alpha(placeholderColor)

        ValueAnimator.ofInt(originalAlpha, 0)
            .apply {
                duration = 375L
                addUpdateListener {
                    newIcon.colorFilter =
                        PorterDuffColorFilter(
                            ColorUtils.setAlphaComponent(placeholderColor, it.animatedValue as Int),
                            SRC_ATOP,
                        )
                }
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            newIcon.colorFilter = null
                        }
                    }
                )
            }
            .start()
    }

    class PlaceHolderDelegateFactory(context: Context) : DelegateFactory {
        private val loadingColor = getAttrColor(context, R.attr.loadingIconColor)

        override fun newDelegate(
            bitmapInfo: BitmapInfo,
            iconShape: IconShape,
            paint: Paint,
            host: FastBitmapDrawable,
        ): FastBitmapDrawableDelegate {
            return PlaceHolderDrawableDelegate(bitmapInfo, paint, loadingColor)
        }
    }
}

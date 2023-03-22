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

package com.saggitt.omega.smartspace.superg

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R

class QsbConnector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mForegroundAlpha = 0
    private var mRevealAnimator: ObjectAnimator? = null
    private val mForegroundColor: Int

    init {
        mForegroundColor = resources.getColor(R.color.qsb_background, context.theme) and 0xFFFFFF
        background =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bg_pixel_qsb_connector,
                getContext().theme
            )
    }

    private fun stopRevealAnimation() {
        if (mRevealAnimator != null) {
            mRevealAnimator!!.end()
            mRevealAnimator = null
        }
    }

    private fun updateAlpha(alpha: Int) {
        if (mForegroundAlpha != alpha) {
            mForegroundAlpha = alpha
            invalidate()
        }
    }

    fun changeVisibility(makeVisible: Boolean) {
        if (makeVisible) {
            stopRevealAnimation()
            updateAlpha(255)
            mRevealAnimator = ObjectAnimator.ofInt(this, sAlphaProperty, 0)
            mRevealAnimator!!.setInterpolator(AccelerateDecelerateInterpolator())
            mRevealAnimator!!.start()
        } else {
            updateAlpha(0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (mForegroundAlpha > 0) {
            canvas.drawColor(ColorUtils.setAlphaComponent(mForegroundColor, mForegroundAlpha))
        }
    }

    override fun onSetAlpha(alpha: Int): Boolean {
        if (alpha == 0) {
            stopRevealAnimation()
        }
        return super.onSetAlpha(alpha)
    }

    companion object {
        private val sAlphaProperty: Property<QsbConnector, Int> =
            object : Property<QsbConnector, Int>(
                Int::class.java, "overlayAlpha"
            ) {
                override fun get(qsbConnector: QsbConnector): Int {
                    return qsbConnector.mForegroundAlpha
                }

                override fun set(qsbConnector: QsbConnector, newAlpha: Int) {
                    qsbConnector.updateAlpha(newAlpha)
                }
            }
    }
}
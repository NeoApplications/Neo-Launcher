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

package com.google.android.systemui.smartspace

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import com.android.launcher3.views.ShadowInfo
import kotlin.math.max

open class DoubleShadowTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    TextView(context, attrs, defStyleAttr) {
    private val mShadowInfo: ShadowInfo

    init {
        mShadowInfo = ShadowInfo.fromContext(context, attrs, defStyleAttr)
        setShadowLayer(
            max(
                mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffsetX,
                mShadowInfo.ambientShadowBlur
            ), 0f, 0f, mShadowInfo.keyShadowColor
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas)
            return
        }
        paint.setShadowLayer(
            mShadowInfo.ambientShadowBlur,
            0.0f,
            0.0f,
            mShadowInfo.ambientShadowColor
        )
        super.onDraw(canvas)
        canvas.save()
        canvas.clipRect(
            scrollX, scrollY + extendedPaddingTop,
            scrollX + width,
            scrollY + height
        )
        paint.setShadowLayer(
            mShadowInfo.keyShadowBlur,
            mShadowInfo.keyShadowOffsetX,
            mShadowInfo.keyShadowOffsetY,
            mShadowInfo.keyShadowColor
        )
        super.onDraw(canvas)
        canvas.restore()
    }
}

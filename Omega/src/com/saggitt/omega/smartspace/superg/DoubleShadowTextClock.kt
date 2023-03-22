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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.provider.CalendarContract
import android.util.AttributeSet
import android.widget.TextClock
import com.android.launcher3.R

class DoubleShadowTextClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    i: Int = 0
) :
    TextClock(context, attrs, i) {
    private val ambientShadowBlur: Float
    private val ambientShadowColor: Int
    private val keyShadowBlur: Float
    private val keyShadowColor: Int
    private val keyShadowOffset: Float

    init {
        val ta = context.obtainStyledAttributes(
            attrs, intArrayOf(
                R.attr.ambientShadowColor,
                R.attr.keyShadowColor,
                R.attr.ambientShadowBlur,
                R.attr.keyShadowBlur,
                R.attr.keyShadowOffsetX
            ), i, 0
        )
        ambientShadowColor = ta.getColor(0, 0)
        keyShadowColor = ta.getColor(1, 0)
        ambientShadowBlur = ta.getDimension(2, 0f)
        keyShadowBlur = ta.getDimension(3, 0f)
        keyShadowOffset = ta.getDimension(4, 0f)
        ta.recycle()
        setShadowLayer(
            Math.max(keyShadowBlur + keyShadowOffset, ambientShadowBlur),
            0f,
            0f,
            keyShadowColor
        )
        setOnClickListener { view ->
            try {
                view.context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                    )
                )
            } catch (ignored: ActivityNotFoundException) {
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        paint.setShadowLayer(keyShadowBlur, 0f, keyShadowOffset, keyShadowColor)
        super.onDraw(canvas)
        paint.setShadowLayer(ambientShadowBlur, 0f, 0f, ambientShadowColor)
        super.onDraw(canvas)
    }

    fun setFormat(charSequence: CharSequence?) {
        format24Hour = charSequence
        format12Hour = charSequence
    }
}
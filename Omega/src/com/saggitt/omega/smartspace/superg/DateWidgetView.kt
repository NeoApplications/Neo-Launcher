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

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.saggitt.omega.util.getLauncherOrNull
import java.util.Locale

class DateWidgetView(context: Context?, attributeSet: AttributeSet?) :
    LinearLayout(context, attributeSet), TextWatcher {
    private var mText = ""
    private var mDateText1TextSize = 0f
    private var mDateText1: DoubleShadowTextClock? = null
    private var mDateText2: DoubleShadowTextClock? = null
    private var mWidth = 0
    override fun onFinishInflate() {
        super.onFinishInflate()
        mDateText1 = findViewById(R.id.date_text1)
        mDateText1TextSize = mDateText1!!.getTextSize()
        mDateText1!!.addTextChangedListener(this)
        mDateText1!!.setFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMMd"))
        mDateText2 = findViewById(R.id.date_text2)
        mDateText2!!.setFormat(context.getString(R.string.week_day_format, "EEEE", "yyyy"))
        init()
    }

    private fun init() {
        val locale = Locale.getDefault()
        if (Locale.ENGLISH.language == locale.language) {
            val paint: Paint = mDateText1!!.paint
            val rect = Rect()
            paint.getTextBounds("x", 0, 1, rect)
            val height = rect.height()
            mDateText2!!.setPadding(
                0,
                0,
                0,
                (Math.abs(paint.fontMetrics.ascent) - height.toFloat()).toInt() / 2
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val launcher = context.getLauncherOrNull()
        val marginEnd: Int
        if (launcher != null) {
            val deviceProfile = Launcher.getLauncher(context).deviceProfile
            val size = MeasureSpec.getSize(widthMeasureSpec) / deviceProfile.inv.numColumns
            marginEnd = (size - deviceProfile.iconSizePx) / 2
            mWidth = (deviceProfile.inv.numColumns - Math.max(
                1,
                Math.ceil((resources.getDimension(R.dimen.qsb_min_width_with_mic) / size.toFloat()).toDouble())
                    .toInt()
            )) * size
            mText = ""
            update()
        } else {
            marginEnd = resources.getDimensionPixelSize(R.dimen.smartspace_preview_widget_margin)
        }
        setMarginEnd(mDateText1, marginEnd)
        setMarginEnd(mDateText2, marginEnd)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun setMarginEnd(view: View?, marginEnd: Int) {
        val layoutParams = view!!.layoutParams as LayoutParams
        layoutParams.marginEnd = marginEnd
        layoutParams.resolveLayoutDirection(layoutParams.layoutDirection)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: Editable) {
        update()
    }

    private fun update() {
        if (mWidth > 0) {
            val dateText1Text = mDateText1!!.text.toString()
            if (mText != dateText1Text) {
                mText = dateText1Text
                if (!dateText1Text.isEmpty()) {
                    val paint = mDateText1!!.paint
                    val textSize = paint.textSize
                    var size = mDateText1TextSize
                    for (i in 0..9) {
                        paint.textSize = size
                        val measureText = paint.measureText(dateText1Text)
                        if (measureText <= mWidth.toFloat()) {
                            break
                        }
                        size = size * mWidth.toFloat() / measureText
                    }
                    if (java.lang.Float.compare(size, textSize) == 0) {
                        paint.textSize = textSize
                    } else {
                        mDateText1!!.setTextSize(0, size)
                        init()
                    }
                }
            }
        }
    }
}
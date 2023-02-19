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

package com.saggitt.omega.smartspace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import kotlin.math.abs


class ColorManipulation {
    private val mMatrix: Matrix = Matrix()
    private var mPixels: IntArray? = IntArray(2)
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mPaint: Paint? = null


    fun dB(icon: Bitmap): Boolean {
        var height: Int = icon.height
        var width: Int = icon.width
        val bitmap: Bitmap
        if (height > 64 || width > 64) {
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(mBitmap!!)
                mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                mPaint?.isFilterBitmap = true
            }
            mMatrix.reset()
            mMatrix.setScale(64f / width, 64f / height, 0f, 0f)
            mCanvas?.drawColor(0, PorterDuff.Mode.SRC)
            mCanvas?.drawBitmap(icon, mMatrix, mPaint)
            width = 64
            height = 64
            bitmap = mBitmap!!
        } else {
            bitmap = icon
        }
        val pixelCount = height * width
        resizeIfNecessary(pixelCount)
        bitmap.getPixels(mPixels, 0, width, 0, 0, width, height)
        for (i in 0 until pixelCount) {
            if (!dC(mPixels!![i])) {
                return false
            }
        }
        return true
    }

    private fun resizeIfNecessary(pixelCount: Int) {
        if (mPixels == null || mPixels!!.size < pixelCount) {
            mPixels = IntArray(pixelCount)
        }
    }

    companion object {
        fun dC(RGBA: Int): Boolean {
            val maxDiff = 20
            if (RGBA shr 24 and 0xFF < 50) {
                return true
            }
            val red = RGBA shr 16 and 0xFF
            val green = RGBA shr 8 and 0xFF
            val blue = RGBA and 0xFF
            var returnValue = true
            if (abs(red - green) < maxDiff && abs(red - blue) < maxDiff) {
                if (abs(green - blue) >= maxDiff) {
                    returnValue = false
                }
            } else {
                returnValue = false
            }
            return returnValue
        }
    }
}
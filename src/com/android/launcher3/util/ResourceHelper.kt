/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.launcher3.util

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Half.EPSILON
import android.util.TypedValue
import kotlin.IntArray
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * This class is a helper that can be subclassed in tests to provide a way to parse attributes
 * correctly.
 */
open class ResourceHelper(private val context: Context, private val specsFileId: Int) {
    open fun getXml(): XmlResourceParser {
        return context.resources.getXml(specsFileId)
    }

    open fun obtainStyledAttributes(attrs: AttributeSet, styleId: IntArray): TypedArray {
        return context.obtainStyledAttributes(attrs, styleId)
    }

    companion object {
        const val DEFAULT_NAVBAR_VALUE = 48
        const val INVALID_RESOURCE_HANDLE = -1
        const val NAVBAR_LANDSCAPE_LEFT_RIGHT_SIZE = "navigation_bar_width"
        const val NAVBAR_BOTTOM_GESTURE_SIZE = "navigation_bar_gesture_height"
        const val NAVBAR_BOTTOM_GESTURE_LARGER_SIZE = "navigation_bar_gesture_larger_height"
        const val NAVBAR_HEIGHT = "navigation_bar_height"
        const val NAVBAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape"
        const val STATUS_BAR_HEIGHT = "status_bar_height"
        const val STATUS_BAR_HEIGHT_LANDSCAPE = "status_bar_height_landscape"
        const val STATUS_BAR_HEIGHT_PORTRAIT = "status_bar_height_portrait"
        const val NAV_BAR_INTERACTION_MODE_RES_NAME = "config_navBarInteractionMode"

        fun getNavbarSize(resName: String?, res: Resources): Int {
            return getDimenByName(resName, res, DEFAULT_NAVBAR_VALUE)
        }

        fun getDimenByName(resName: String?, res: Resources, defaultValue: Int): Int {
            val frameSize: Int
            val frameSizeResID = res.getIdentifier(resName, "dimen", "android")
            frameSize = if (frameSizeResID != 0) {
                res.getDimensionPixelSize(frameSizeResID)
            } else {
                pxFromDp(defaultValue.toFloat(), res.displayMetrics)
            }
            return frameSize
        }

        fun getBoolByName(resName: String?, res: Resources, defaultValue: Boolean): Boolean {
            val `val`: Boolean
            val resId = res.getIdentifier(resName, "bool", "android")
            `val` = if (resId != 0) {
                res.getBoolean(resId)
            } else {
                defaultValue
            }
            return `val`
        }

        fun getIntegerByName(resName: String?, res: Resources, defaultValue: Int): Int {
            val resId = res.getIdentifier(resName, "integer", "android")
            return if (resId != 0) res.getInteger(resId) else defaultValue
        }

        fun pxFromDp(size: Float, metrics: DisplayMetrics?): Int {
            return pxFromDp(size, metrics, 1f)
        }

        fun pxFromDp(size: Float, metrics: DisplayMetrics?, scale: Float): Int {
            val value =
                scale * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics)
            return if (size < 0) INVALID_RESOURCE_HANDLE else roundPxValueFromFloat(value)
        }

        /**
         * Rounds a pixel value, taking into account floating point errors.
         *
         *
         * If a dp (or sp) value typically returns a half pixel, such as 20dp at a 2.625 density
         * returning 52.5px, there is a small chance that due to floating-point errors, the value will
         * be stored as 52.499999. As we round to the nearest pixel, this could cause a 1px difference
         * in final values, which we correct for in this method.
         */
        fun roundPxValueFromFloat(value: Float): Int {
            var value = value
            val fraction = (value - floor(value.toDouble())).toFloat()
            if (Math.abs(0.5f - fraction) < EPSILON) {
                // Note: we add for negative values as well, as Math.round brings -.5 to the next
                // "highest" value, e.g. Math.round(-2.5) == -2 [i.e. (int)Math.floor(a + 0.5d)]
                value += EPSILON
            }
            return value.roundToInt()
        }
    }
}

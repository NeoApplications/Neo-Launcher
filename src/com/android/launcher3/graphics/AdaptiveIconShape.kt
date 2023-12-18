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
package com.android.launcher3.graphics

import android.animation.FloatArrayEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import com.saggitt.omega.icons.IconShape.Companion.fromString
import com.saggitt.omega.preferences.NeoPrefs.Companion.getInstance

internal class AdaptiveIconShape(context: Context?) : IconShape.PathShape() {
    private val mIconShape: com.saggitt.omega.icons.IconShape

    init {
        val iconShape = getInstance(context!!).profileIconShape.getValue()
        mIconShape = fromString(context, iconShape)
    }

    override fun addToPath(path: Path, offsetX: Float, offsetY: Float, radius: Float) {
        mIconShape.addShape(path, offsetX, offsetY, radius)
    }

    override fun newUpdateListener(
        startRect: Rect,
        endRect: Rect,
        endRadius: Float,
        outPath: Path
    ): ValueAnimator.AnimatorUpdateListener {
        val startRadius = startRect.width() / 2f
        val start = floatArrayOf(
            startRect.left.toFloat(),
            startRect.top.toFloat(),
            startRect.right.toFloat(),
            startRect.bottom.toFloat()
        )
        val end = floatArrayOf(
            endRect.left.toFloat(),
            endRect.top.toFloat(),
            endRect.right.toFloat(),
            endRect.bottom.toFloat()
        )
        val evaluator = FloatArrayEvaluator()
        return ValueAnimator.AnimatorUpdateListener { animation: ValueAnimator ->
            val progress = animation.animatedValue as Float
            val values = evaluator.evaluate(progress, start, end)
            mIconShape.addToPath(
                outPath,
                values[0], values[1], values[2], values[3],
                startRadius, endRadius, progress
            )
        }
    }
}

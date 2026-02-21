/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import com.android.launcher3.icons.BitmapRenderer.createSoftwareBitmap

sealed interface ShapeRenderer {
    /**
     * Draws shape to the canvas using the provided parameters. This is used in draw methods, so
     * operations should be fast, with no new objects initialized.
     *
     * @param canvas Canvas to draw shape on.
     * @param paint Paint to draw on the Canvas with.
     */
    fun render(canvas: Canvas, paint: Paint)

    /** A renderer which draws a circle of radius [r] */
    class CircleRenderer(private val r: Float) : ShapeRenderer {

        override fun render(canvas: Canvas, paint: Paint) {
            canvas.drawCircle(r, r, r, paint)
        }
    }

    /** A renderer which draws a rounded rect in [0, 0, [size], [size]] of corner radius [r] */
    class RoundedRectRenderer(private val size: Float, private val r: Float) : ShapeRenderer {
        override fun render(canvas: Canvas, paint: Paint) {
            canvas.drawRoundRect(0f, 0f, size, size, r, r, paint)
        }
    }

    /** A renderer which draws the [path] */
    class PathRenderer(private val path: Path) : ShapeRenderer {
        override fun render(canvas: Canvas, paint: Paint) {
            canvas.drawPath(path, paint)
        }
    }

    /**
     * A renderer which draws the a alpha bitmap mask. This is preferred over [PathRenderer] if the
     * max rendering size is known
     */
    class AlphaMaskRenderer(path: Path, size: Int) : ShapeRenderer {

        private val mask =
            createSoftwareBitmap(size, size) { it.drawPath(path, Paint(ANTI_ALIAS_FLAG)) }
                .extractAlpha()

        override fun render(canvas: Canvas, paint: Paint) {
            canvas.drawBitmap(mask, 0f, 0f, paint)
        }
    }
}

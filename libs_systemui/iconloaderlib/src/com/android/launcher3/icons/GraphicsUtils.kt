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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.RegionIterator
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils.compositeColors
import com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR
import com.android.launcher3.icons.ShadowGenerator.BLUR_FACTOR
import com.android.launcher3.icons.ShapeRenderer.AlphaMaskRenderer
import com.android.launcher3.icons.ShapeRenderer.CircleRenderer
import com.android.launcher3.icons.ShapeRenderer.RoundedRectRenderer
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.max

object GraphicsUtils {
    private const val TAG = "GraphicsUtils"

    @JvmField
    var sOnNewBitmapRunnable: Runnable = Runnable {}

    /**
     * Set the alpha component of `color` to be `alpha`. Unlike the support lib version, it bounds
     * the alpha in valid range instead of throwing an exception to allow for safer interpolation of
     * color animations
     */
    @JvmStatic
    @ColorInt
    fun setColorAlphaBound(color: Int, alpha: Int): Int =
        (color and 0x00ffffff) or (alpha.coerceIn(0, 255) shl 24)

    /** Compresses the bitmap to a byte array for serialization. */
    @JvmStatic
    fun flattenBitmap(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream(getExpectedBitmapSize(bitmap))
        try {
            bitmap.compress(PNG, 100, out)
            out.flush()
            out.close()
            return out.toByteArray()
        } catch (e: IOException) {
            Log.w(TAG, "Could not write bitmap")
            return ByteArray(0)
        }
    }

    /** Compresses BitmapInfo default shape bitmap to a byte array **/
    @JvmStatic
    fun createDefaultFlatBitmap(bitmapInfo: BitmapInfo): ByteArray {
        // BitmapInfo uses immutable hardware bitmaps, so we need to make a software copy to apply
        // the default shape mask.
        val bitmap = bitmapInfo.icon.copy(Bitmap.Config.ARGB_8888, /* isMutable **/ true)
        val cropBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropBitmap)

        var paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        canvas.drawPath(bitmapInfo.defaultIconShape.path, paint)

        paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val flatBitmap = flattenBitmap(cropBitmap)
        cropBitmap.recycle()
        bitmap.recycle()
        return flatBitmap
    }

    /** Tries to decode the [ByteArray] into a [Bitmap] consuming any parsing errors */
    fun ByteArray.parseBitmapSafe(config: Bitmap.Config): Bitmap? =
        try {
            BitmapFactory.decodeByteArray(
                /* data= */ this,
                /* offset= */ 0,
                /* length= */ size,
                Options().apply { inPreferredConfig = config },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing persisted bitmap", e)
            null
        }

    /**
     * Try go guesstimate how much space the icon will take when serialized to avoid unnecessary
     * allocations/copies during the write (4 bytes per pixel).
     */
    @JvmStatic
    fun getExpectedBitmapSize(bitmap: Bitmap): Int = bitmap.width * bitmap.height * 4

    @JvmStatic
    fun getArea(region: Region): Int {
        val regionIterator = RegionIterator(region)
        var area = 0
        val tempRect = Rect()
        while (regionIterator.next(tempRect)) {
            area += tempRect.width() * tempRect.height()
        }
        return area
    }

    /** Utility method to track new bitmap creation */
    @JvmStatic
    fun noteNewBitmapCreated() = sOnNewBitmapRunnable.run()

    /** Returns the color associated with the attribute */
    @JvmStatic
    fun getAttrColor(context: Context, attr: Int): Int =
        context.obtainStyledAttributes(intArrayOf(attr)).use { it.getColor(0, 0) }

    /** Returns the alpha corresponding to the theme attribute {@param attr} */
    @JvmStatic
    fun getFloat(context: Context, attr: Int, defValue: Float): Float =
        context.obtainStyledAttributes(intArrayOf(attr)).use { it.getFloat(0, defValue) }

    /**
     * Canvas extension function which runs the [block] after preserving the canvas transform using
     * same/restore pair.
     */
    inline fun Canvas.transformed(block: Canvas.() -> Unit) {
        val saveCount = save()
        block.invoke(this)
        restoreToCount(saveCount)
    }

    /** Resizes this path from [oldSize] to [newSize] as a new instance of Path. */
    @JvmStatic
    fun Path.resize(oldSize: Int, newSize: Int): Path =
        Path(this).apply {
            transform(
                Matrix().apply {
                    setRectToRect(
                        RectF(0f, 0f, oldSize.toFloat(), oldSize.toFloat()),
                        RectF(0f, 0f, newSize.toFloat(), newSize.toFloat()),
                        Matrix.ScaleToFit.CENTER,
                    )
                }
            )
        }

    /**
     * Resizes the canvas to that [bounds] align with [0, 0, [sizeX], [sizeY]] space and executes
     * the [block]. It also scales down the drawing by [ICON_VISIBLE_AREA_FACTOR] to account for
     * icon normalization.
     */
    inline fun Canvas.resizeToContentSize(
        bounds: Rect,
        sizeX: Float,
        sizeY: Float = sizeX,
        block: Canvas.() -> Unit,
    ) = transformed {
        translate(bounds.left.toFloat(), bounds.top.toFloat())
        scale(bounds.width() / sizeX, bounds.height() / sizeY)
        scale(ICON_VISIBLE_AREA_FACTOR, ICON_VISIBLE_AREA_FACTOR, sizeX / 2, sizeY / 2)
        block.invoke(this)
    }

    /**
     * Generates a new [IconShape] for the [size] and the [shapePath] (in bounds [0, 0, [size],
     * [size]]
     */
    @JvmStatic
    fun generateIconShape(size: Int, shapePath: Path): IconShape {
        // Generate shadow layer:
        // Based on adaptive icon drawing in BaseIconFactory
        val offset =
            max(
                ceil((BLUR_FACTOR * size)).toInt(),
                Math.round(size * (1 - ICON_VISIBLE_AREA_FACTOR) / 2),
            )
        val shadowLayer =
            BitmapRenderer.createHardwareBitmap(size, size) { canvas: Canvas ->
                canvas.transformed {
                    canvas.translate(offset.toFloat(), offset.toFloat())
                    val drawnPathSize = size - offset * 2
                    val drawnPath = shapePath.resize(size, drawnPathSize)
                    ShadowGenerator(size).addPathShadow(drawnPath, canvas)
                }
            }

        val roundRectEstimation = RoundRectEstimator.estimateRadius(shapePath, size.toFloat())
        return IconShape(
            pathSize = size,
            path = shapePath,
            shadowLayer = shadowLayer,
            shapeRenderer =
                when {
                    roundRectEstimation >= 1f -> CircleRenderer(size.toFloat() / 2)
                    roundRectEstimation >= 0f ->
                        RoundedRectRenderer(size.toFloat(), roundRectEstimation * size / 2)

                    else -> AlphaMaskRenderer(shapePath, size)
                },
        )
    }

    /** Returns a color filter which is equivalent to [filter] x BlendModeFilter with [color] */
    fun getColorMultipliedFilter(color: Int, filter: ColorFilter?): ColorFilter? {
        if (Color.alpha(color) == 0) return filter
        if (filter == null) return BlendModeColorFilter(color, BlendMode.SRC_IN)

        return when {
            filter is BlendModeColorFilter && filter.mode == BlendMode.SRC_IN ->
                BlendModeColorFilter(compositeColors(filter.color, color), BlendMode.SRC_IN)

            filter is ColorMatrixColorFilter -> {
                val matrix = ColorMatrix().apply { filter.getColorMatrix(this) }.array
                val components = IntArray(4)
                for (i in 0..3) {
                    val s = 5 * i
                    components[i] =
                        (Color.red(color) * matrix[s] +
                                Color.green(color) * matrix[s + 1] +
                                Color.blue(color) * matrix[s + 2] +
                                Color.alpha(color) * matrix[s + 3] +
                                matrix[s + 4])
                            .toInt()
                            .coerceIn(0, 255)
                }
                BlendModeColorFilter(
                    Color.argb(components[3], components[0], components[1], components[2]),
                    BlendMode.SRC_IN,
                )
            }
            // Don't know what this is, draw and find out
            else -> {
                val bitmap =
                    BitmapRenderer.createSoftwareBitmap(1, 1) { c ->
                        c.drawPaint(
                            Paint().also {
                                it.color = color
                                it.colorFilter = filter
                            }
                        )
                    }
                BlendModeColorFilter(bitmap.getPixel(0, 0), BlendMode.SRC_IN)
            }
        }
    }
}

/**
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.launcher3.icons

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import kotlin.math.abs

/** The type of computation to use when computing the luminance of a drawable or a bitmap. */
enum class ComputationType {
    /** Compute the median luminance of a drawable or a bitmap. */
    MEDIAN,

    /** Compute the average luminance of a drawable or a bitmap. */
    AVERAGE,

    /** Compute the difference between the min and max luminance of a drawable or a bitmap. */
    SPREAD,
}

/** Wrapper for the color space to use when computing the luminance. */
interface ColorWrapper {
    /** The luminance of the color, in the range [0, 1]. */
    var luminance: Double

    /** The color as an integer in the format of the color space. */
    fun toColorInt(): Int
}

@JvmInline
value class LabColor(val data: DoubleArray) : ColorWrapper {
    override var luminance: Double
        get() = data[0] / 100
        set(value) {
            data[0] = value * 100
        }

    override fun toColorInt(): Int = ColorUtils.LABToColor(data[0], data[1], data[2])
}

@JvmInline
value class HslColor(val data: FloatArray) : ColorWrapper {
    override var luminance: Double
        get() = data[2].toDouble()
        set(value) {
            data[2] = value.toFloat()
        }

    override fun toColorInt(): Int = ColorUtils.HSLToColor(data)
}

/** The color space to use when computing the luminance of a drawable or a bitmap. */
enum class LuminanceColorSpace {
    /** Use the HSL color space. */
    HSL,

    /** Use the LAB color space. */
    LAB,
}

/** Class to compute the luminance of a drawable or a bitmap using the chosen color space. */
class LuminanceComputer(
    val colorSpace: LuminanceColorSpace,
    val computationType: ComputationType,
    private val options: Options = Options(),
) {

    /**
     * Options for the luminance computer.
     *
     * @param ensureMinContrast If true, the resulting luminance ratio will always be the minimum
     *   contrast ratio passed into [adaptColorLuminance].
     * @param absoluteLuminanceDelta If true, the luminance delta will always be the absolute value
     *   of the luminance delta passed into [adaptColorLuminance], meaning that the luminance delta
     *   will always be positive and the foreground color will always be considered to be brighter
     *   than the background color.
     */
    data class Options(
        val ensureMinContrast: Boolean = ENABLED_CONTRAST_ADJUSTMENT,
        val absoluteLuminanceDelta: Boolean = ENABLED_ABSOLUTE_LUMINANCE_DELTA,
    )

    /**
     * Adapt a color to a different luminance level using the selected color space, and optionally
     * adjust the contrast and absolute luminance delta.
     *
     * @param targetColor The color to adapt.
     * @param basisColor The color to use as a basis for the luminance.
     * @param luminanceDelta The luminance delta to use, which is the difference between the target
     *   and the basis luminance.
     * @param minimumContrast The minimum contrast to use between the target and the basis color.
     * @return The adapted color.
     */
    fun adaptColorLuminance(
        targetColor: Int,
        basisColor: Int,
        @FloatRange(from = -1.0, to = 1.0, toInclusive = true, fromInclusive = true)
        luminanceDelta: Double,
        minimumContrast: Double,
        useAbsoluteLuminanceDelta: Boolean = options.absoluteLuminanceDelta,
    ): Int {
        if (luminanceDelta.isNaN()) {
            return targetColor
        }

        var localLuminanceDelta =
            if (useAbsoluteLuminanceDelta) {
                // get the absolute value of the luminance delta
                abs(luminanceDelta).coerceAtLeast(DEFAULT_ABSOLUTE_LUMINANCE_DELTA)
            } else {
                luminanceDelta
            }

        val mutatedColorWrapper =
            mutateColorLuminance(targetColor, basisColor, localLuminanceDelta, minimumContrast)
        return mutatedColorWrapper.toColorInt()
    }

    private fun mutateColorLuminance(
        targetColor: Int,
        basisColor: Int,
        luminanceDelta: Double,
        minimumContrast: Double = 0.0,
    ): ColorWrapper {
        if (luminanceDelta.isNaN()) {
            return colorToColorWrapper(targetColor)
        }

        val targetColorWrapper = colorToColorWrapper(targetColor)
        val basisColorWrapper = colorToColorWrapper(basisColor)

        val basisLuminance = basisColorWrapper.luminance

        // The target luminance should be between 0 and 1, so we need to clamp
        // it to that range
        var targetLuminance = (basisLuminance + luminanceDelta).coerceIn(0.0, 1.0)

        targetLuminance =
            adjustLuminanceForContrast(
                targetLuminance,
                basisLuminance,
                luminanceDelta,
                minimumContrast,
            )

        targetColorWrapper.luminance = targetLuminance

        return targetColorWrapper
    }

    /**
     * Compute the luminance of a bitmap using the selected color space.
     *
     * @param bitmap The bitmap to compute the luminance of.
     * @param scale if true, the bitmap is resized to [BITMAP_SAMPLE_SIZE] for color calculation
     */
    @JvmOverloads
    fun computeLuminance(bitmap: Bitmap, scale: Boolean = true): Double {
        val bitmapHeight = bitmap.height
        val bitmapWidth = bitmap.width
        if (bitmapHeight == 0 || bitmapWidth == 0) {
            Log.e(TAG, "Bitmap is null")
            return Double.NaN
        }

        val bitmapToProcess =
            if (scale) {
                Bitmap.createScaledBitmap(bitmap, BITMAP_SAMPLE_SIZE, BITMAP_SAMPLE_SIZE, true)
            } else {
                bitmap
            }

        val processedWidth = bitmapToProcess.width
        val processedHeight = bitmapToProcess.height

        val pixels = IntArray(processedWidth * processedHeight)
        bitmapToProcess.getPixels(
            /** pixels = */
            pixels,
            /** offset = */
            0,
            /** stride = */
            processedWidth,
            /** x = */
            0,
            /** y = */
            0,
            /** width = */
            processedWidth,
            /** height = */
            processedHeight,
        )
        val luminances = pixels.map { colorToColorWrapper(it).luminance }

        when (computationType) {
            ComputationType.MEDIAN -> return luminances.sorted().median()
            ComputationType.AVERAGE -> return luminances.average()
            ComputationType.SPREAD -> return luminances.max() - luminances.min()
        }
    }

    // The minimum contrast is the ratio minimum ratio that should exist
    // between the target and the basis luminance
    private fun adjustLuminanceForContrast(
        targetLuminance: Double,
        basisLuminance: Double,
        luminanceDelta: Double,
        minimumContrast: Double,
    ): Double {
        if (!options.ensureMinContrast) return targetLuminance

        val currentContrast = targetLuminance - basisLuminance
        if (currentContrast >= minimumContrast) return targetLuminance

        val contrastedTargetLuminance = basisLuminance + (luminanceDelta * minimumContrast)
        return contrastedTargetLuminance.coerceIn(0.0, 1.0)
    }

    private fun List<Double>.median(): Double {
        if (isEmpty()) {
            return Double.NaN
        }
        val size = this.size
        return if (size % 2 == 0) {
            (this[size / 2 - 1] + this[size / 2]) / 2
        } else {
            this[size / 2]
        }
    }

    private fun List<Double>.average(): Double {
        if (isEmpty()) {
            return Double.NaN
        }
        return sum() / size
    }

    // Update to return ColorWrapper
    private fun colorToColorWrapper(color: Int): ColorWrapper {
        return when (colorSpace) {
            LuminanceColorSpace.HSL -> {
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(color, hsl)
                HslColor(hsl)
            }

            LuminanceColorSpace.LAB -> {
                val lab = DoubleArray(3)
                ColorUtils.colorToLAB(color, lab)
                LabColor(lab)
            }
        }
    }

    companion object Factory {
        const val TAG: String = "LuminanceComputer"

        // If true, the resulting luminance ratio will always be the
        // minimum contrast ratio passed into adaptColor
        const val ENABLED_CONTRAST_ADJUSTMENT = true

        // If true, the luminance delta will always be the absolute value
        // of the luminance delta passed into adaptColor, meaning that
        // the luminance delta will always be positive and the foreground
        // color will always be considered to be brighter than the background
        // color.
        const val ENABLED_ABSOLUTE_LUMINANCE_DELTA = true

        // The size of bitmap to derive the luminance from
        // eg: 64x64
        const val BITMAP_SAMPLE_SIZE = 64

        // The default absolute luminance delta to use if the user does not
        // specify one. Only valid when ENABLED_ABSOLUTE_LUMINANCE_DELTA is
        // true.
        const val DEFAULT_ABSOLUTE_LUMINANCE_DELTA = 0.1

        @JvmStatic
        @JvmOverloads
        fun createDefaultLuminanceComputer(
            computationType: ComputationType = ComputationType.AVERAGE
        ): LuminanceComputer {
            return LuminanceComputer(
                LuminanceColorSpace.LAB, // Keep this as the default color space
                computationType,
                Options(
                    ensureMinContrast = ENABLED_CONTRAST_ADJUSTMENT,
                    absoluteLuminanceDelta = ENABLED_ABSOLUTE_LUMINANCE_DELTA,
                ),
            )
        }
    }
}

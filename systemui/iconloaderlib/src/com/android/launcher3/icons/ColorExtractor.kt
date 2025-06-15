/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.graphics.Bitmap
import android.graphics.Color
import android.util.SparseArray
import kotlin.math.sqrt

/** Utility class for extracting colors from a bitmap. */
object ColorExtractor {
    private const val NUM_SAMPLES = 20

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     *
     * @param bitmap The bitmap to scan
     */
    @JvmStatic
    fun findDominantColorByHue(bitmap: Bitmap): Int {
        val height = bitmap.height
        val width = bitmap.width
        val sampleStride = sqrt((height * width) / NUM_SAMPLES.toDouble()).toInt().coerceAtLeast(1)

        // This is an out-param, for getting the hsv values for an rgb
        val hsv = FloatArray(3)

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        val hueScoreHistogram = FloatArray(360)
        var highScore = -1f
        var bestHue = -1

        val pixels = IntArray(NUM_SAMPLES)
        var pixelCount = 0

        for (y in 0..<height step sampleStride) {
            for (x in 0..<width step sampleStride) {
                val argb = bitmap.getPixel(x, y)
                val alpha = 0xFF and (argb shr 24)
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue
                }
                // Remove the alpha channel.
                val rgb = argb or -0x1000000
                Color.colorToHSV(rgb, hsv)
                // Bucket colors by the 360 integer hues.
                val hue = hsv[0].toInt()
                if (hue < 0 || hue >= hueScoreHistogram.size) {
                    // Defensively avoid array bounds violations.
                    continue
                }
                if (pixelCount < NUM_SAMPLES) {
                    pixels[pixelCount++] = rgb
                }
                val score = hsv[1] * hsv[2]
                hueScoreHistogram[hue] += score
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue]
                    bestHue = hue
                }
            }
        }

        val rgbScores = SparseArray<Float>()
        var bestColor = -0x1000000
        highScore = -1f
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (i in 0..<pixelCount) {
            val rgb = pixels[i]
            Color.colorToHSV(rgb, hsv)
            val hue = hsv[0].toInt()
            if (hue == bestHue) {
                val s = hsv[1]
                val v = hsv[2]
                val bucket = (s * 100).toInt() + (v * 10000).toInt()
                // Score by cumulative saturation * value.
                val score = s * v
                val oldTotal = rgbScores[bucket]
                val newTotal = if (oldTotal == null) score else oldTotal + score
                rgbScores.put(bucket, newTotal)
                if (newTotal > highScore) {
                    highScore = newTotal
                    // All the colors in the winning bucket are very similar. Last in wins.
                    bestColor = rgb
                }
            }
        }
        return bestColor
    }
}

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

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Region
import android.graphics.RegionIterator

/** Utility class to estimate round rect parameters from a [Path] */
object RoundRectEstimator {

    internal const val AREA_CALC_SIZE = 1000

    // .1% error margin
    internal const val AREA_DIFF_THRESHOLD = AREA_CALC_SIZE * AREA_CALC_SIZE / 1000

    internal const val ITERATION_COUNT = 20

    fun getArea(r: Region): Int {
        val itr = RegionIterator(r)
        var area = 0
        val tempRect = Rect()
        while (itr.next(tempRect)) {
            area += tempRect.width() * tempRect.height()
        }
        return area
    }

    /**
     * For the provided [path] in bounds [0, 0, [size], [size]], tries to estimate the radius of the
     * rounded rectangle which closely resembles this path. Returns the radius as a factor of
     * half-[size] or -1 if the provided path can't be estimated as a rounded rectangle.
     */
    fun estimateRadius(path: Path, size: Float): Float {
        val fullRegion = Region(0, 0, AREA_CALC_SIZE, AREA_CALC_SIZE)

        val tmpPath = Path()
        path.transform(
            Matrix().apply { setScale(AREA_CALC_SIZE / size, AREA_CALC_SIZE / size) },
            tmpPath,
        )
        val iconRegion = Region().apply { setPath(tmpPath, fullRegion) }

        val shapePath = Path()
        val shapeRegion = Region()

        var minAreaDiff = Int.MAX_VALUE
        var radiusFactor = -1f
        // iterate over radius factor
        for (f in 0..ITERATION_COUNT) {
            shapePath.reset()
            val currentRadiusFactor = f.toFloat() / ITERATION_COUNT
            val radius = currentRadiusFactor * AREA_CALC_SIZE / 2
            shapePath.addRoundRect(
                0f,
                0f,
                AREA_CALC_SIZE.toFloat(),
                AREA_CALC_SIZE.toFloat(),
                radius,
                radius,
                Path.Direction.CW,
            )
            shapeRegion.setPath(shapePath, fullRegion)
            shapeRegion.op(iconRegion, Region.Op.XOR)

            val rectArea = getArea(shapeRegion)
            if (rectArea < minAreaDiff) {
                minAreaDiff = rectArea
                radiusFactor = currentRadiusFactor
            }
        }

        return if (minAreaDiff < AREA_DIFF_THRESHOLD) radiusFactor else -1f
    }
}

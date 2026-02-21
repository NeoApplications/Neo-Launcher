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

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import com.android.launcher3.icons.ShapeRenderer.PathRenderer

data class IconShape(
    /** Size that [path] should be scaled to. */
    @JvmField val pathSize: Int,
    /** Path for icon shape to be used as mask. Ensure this is scaled to [pathSize] */
    @JvmField val path: Path,
    /** Shadow layer to draw behind icon. Should use the same shape and scale as [path] */
    @JvmField val shadowLayer: Bitmap,
    /** Renderer for customizing how shapes are drawn to canvas */
    @JvmField val shapeRenderer: ShapeRenderer = PathRenderer(path),
) {
    companion object {
        private const val DEFAULT_PATH_SIZE = 100

        // Placeholder that can be used if icon shape is not needed.
        @JvmField
        val EMPTY =
            IconShape(
                DEFAULT_PATH_SIZE,
                AdaptiveIconDrawable(ColorDrawable(Color.WHITE), null)
                    .apply { setBounds(0, 0, DEFAULT_PATH_SIZE, DEFAULT_PATH_SIZE) }
                    .iconMask,
                createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) },
            )
    }
}

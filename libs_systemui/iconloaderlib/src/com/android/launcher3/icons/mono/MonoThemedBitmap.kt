/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.launcher3.icons.mono

import android.content.Context
import android.graphics.Bitmap
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.FastBitmapDrawable
import com.android.launcher3.icons.ThemedBitmap
import com.android.launcher3.icons.mono.ThemedIconDrawable.ThemedConstantState
import java.nio.ByteBuffer

class MonoThemedBitmap(val mono: Bitmap, private val whiteShadowLayer: Bitmap) : ThemedBitmap {

    override fun newDrawable(info: BitmapInfo, context: Context): FastBitmapDrawable {
        val colors = ThemedIconDrawable.getColors(context)
        return ThemedConstantState(info, mono, whiteShadowLayer, colors[0], colors[1]).newDrawable()
    }

    override fun serialize() =
        ByteArray(mono.width * mono.height).apply { mono.copyPixelsToBuffer(ByteBuffer.wrap(this)) }
}

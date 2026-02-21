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
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode.CLAMP
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.android.launcher3.Flags
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.ClockDrawableWrapper.ClockAnimationInfo
import com.android.launcher3.icons.FastBitmapDrawableDelegate.DelegateFactory
import com.android.launcher3.icons.LuminanceComputer
import com.android.launcher3.icons.ThemedBitmap
import java.nio.ByteBuffer

class MonoThemedBitmap(
    val mono: Bitmap,
    private val colorProvider: (Context) -> IntArray = ThemedIconDelegate.Companion::getColors,
    @get:VisibleForTesting val luminanceDelta: Double? = null,
) : ThemedBitmap {

    override fun newDelegateFactory(info: BitmapInfo, context: Context): DelegateFactory =
        getUpdatedColors(context).let { ThemedIconInfo(mono, it[0], it[1]) }

    override fun serialize(): ByteArray {
        val expectedSize = mono.width * mono.height
        return if (luminanceDelta == null)
            ByteArray(expectedSize).apply { mono.copyPixelsToBuffer(ByteBuffer.wrap(this)) }
        else
            ByteArray(expectedSize + DOUBLE_BYTE_SIZE).apply {
                val buffer = ByteBuffer.wrap(this)
                mono.copyPixelsToBuffer(buffer)
                buffer.asDoubleBuffer().put(luminanceDelta)
            }
    }

    fun getUpdatedColors(ctx: Context): IntArray =
        if (luminanceDelta != null)
            ColorAdapter(luminanceDelta).adaptedColorProvider(colorProvider)(ctx)
        else colorProvider(ctx)

    companion object {
        const val DOUBLE_BYTE_SIZE = 8
    }
}

class ClockThemedBitmap(
    private val animInfo: ClockAnimationInfo,
    private val colorProvider: (Context) -> IntArray = ThemedIconDelegate.Companion::getColors,
) : ThemedBitmap {

    override fun newDelegateFactory(info: BitmapInfo, context: Context): DelegateFactory =
        colorProvider(context).let { colors ->
            animInfo.copy(
                themeFgColor = colors[1],
                shader = LinearGradient(0f, 0f, 1f, 1f, colors[0], colors[0], CLAMP),
            )
        }

    override fun serialize() = byteArrayOf()
}

class ColorAdapter(private val luminanceDelta: Double) {

    private val luminanceComputer = LuminanceComputer.createDefaultLuminanceComputer()

    fun adaptedColorProvider(colorProvider: (Context) -> IntArray): (Context) -> IntArray {
        // if the feature flag is off, then we don't need to adapt the colors at all.
        if (!Flags.forceMonochromeAppIconsAdaptColors()) {
            return colorProvider
        }

        // we need to adapt the color provider here, by adapting the foregrund color at
        // index 0, and the background color at index 1.

        // order is important here, we want to adapt the background color first, then the foreground
        // color.
        return { context ->
            val colors = colorProvider(context)
            intArrayOf(
                adaptBackgroundColor(colors[0], colors[2]),
                adaptForegroundColor(colors[1], colors[0]),
                colors[2],
            )
        }
    }

    private fun adaptForegroundColor(localFgColor: Int, localBgColor: Int): Int {
        if (luminanceDelta.isNaN()) {
            return localFgColor
        }

        try {
            val adaptedColor =
                luminanceComputer.adaptColorLuminance(
                    localFgColor,
                    localBgColor,
                    luminanceDelta,
                    MINIMUM_CONTRAST_RATIO,
                )
            return adaptedColor
        } catch (e: Exception) {
            Log.e(TAG, "Failed to adjust luminance color", e)
        }
        return localFgColor
    }

    private fun adaptBackgroundColor(colorBg: Int, colorBgNonMonochrome: Int): Int {
        if (luminanceDelta.isNaN()) {
            return colorBg
        }
        return colorBgNonMonochrome
    }

    private companion object {
        const val TAG = "ColorAdapter"
        const val MINIMUM_CONTRAST_RATIO = 8.0
    }
}

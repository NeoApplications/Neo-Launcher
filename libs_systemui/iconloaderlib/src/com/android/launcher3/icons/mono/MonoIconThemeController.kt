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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ALPHA_8
import android.graphics.Bitmap.Config.HARDWARE
import android.graphics.BlendMode.SRC_IN
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import com.android.launcher3.Flags
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.ClockDrawableWrapper.ClockAnimationInfo
import com.android.launcher3.icons.IconThemeController
import com.android.launcher3.icons.MonochromeIconFactory
import com.android.launcher3.icons.SourceHint
import com.android.launcher3.icons.ThemedBitmap
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.TIRAMISU)
class MonoIconThemeController(
    private val shouldForceThemeIcon: Boolean = false,
    private val colorProvider: (Context) -> IntArray = ThemedIconDelegate.Companion::getColors,
) : IconThemeController {

    override val themeID = "with-theme"

    override fun createThemedBitmap(
        icon: AdaptiveIconDrawable,
        info: BitmapInfo,
        factory: BaseIconFactory,
        sourceHint: SourceHint?,
    ): ThemedBitmap {
        val currentDelegateFactory = info.delegateFactory
        if (currentDelegateFactory is ClockAnimationInfo) {
            val fullDrawable = currentDelegateFactory.baseDrawableState.newDrawable()
            val monoDrawable = (fullDrawable as? AdaptiveIconDrawable)?.monochrome?.mutate()

            if (monoDrawable is LayerDrawable) {
                return ClockThemedBitmap(
                    currentDelegateFactory.copy(
                        baseDrawableState = AdaptiveIconDrawable(null, monoDrawable).constantState!!
                    ),
                    colorProvider,
                )
            } else {
                return ThemedBitmap.NOT_SUPPORTED
            }
        }

        val mono = icon.monochrome
        if (mono != null) {
            return MonoThemedBitmap(
                InsetDrawable(mono, -getExtraInsetFraction()).toAlphaBitmap(factory.iconBitmapSize),
                colorProvider,
            )
        }

        if (Flags.forceMonochromeAppIcons() && shouldForceThemeIcon) {
            val monoFactory = MonochromeIconFactory(info.icon.width)
            val wrappedIcon = monoFactory.wrap(icon)
            return MonoThemedBitmap(
                wrappedIcon.toAlphaBitmap(factory.iconBitmapSize),
                colorProvider,
                monoFactory.luminanceDiff,
            )
        }

        return ThemedBitmap.NOT_SUPPORTED
    }

    private fun Drawable.toAlphaBitmap(size: Int): Bitmap {
        val result = Bitmap.createBitmap(size, size, ALPHA_8)
        setBounds(0, 0, size, size)
        draw(Canvas(result))
        return result
    }

    override fun decode(
        bytes: ByteArray,
        info: BitmapInfo,
        factory: BaseIconFactory,
        sourceHint: SourceHint,
    ): ThemedBitmap {
        val icon = info.icon
        val expectedSize = icon.height * icon.width

        return when (bytes.size) {
            expectedSize -> {
                MonoThemedBitmap(
                    ByteBuffer.wrap(bytes).readMonoBitmap(icon.width, icon.height),
                    colorProvider,
                )
            }

            (expectedSize + MonoThemedBitmap.DOUBLE_BYTE_SIZE) -> {
                val buffer = ByteBuffer.wrap(bytes)
                val monoBitmap = buffer.readMonoBitmap(icon.width, icon.height)
                val luminanceDelta = buffer.asDoubleBuffer().get()
                MonoThemedBitmap(monoBitmap, colorProvider, luminanceDelta)
            }

            else -> ThemedBitmap.NOT_SUPPORTED
        }
    }

    private fun ByteBuffer.readMonoBitmap(width: Int, height: Int): Bitmap {
        val monoBitmap = Bitmap.createBitmap(width, height, ALPHA_8)
        monoBitmap.copyPixelsFromBuffer(this)

        val hwMonoBitmap = monoBitmap.copy(HARDWARE, false /*isMutable*/)
        return hwMonoBitmap?.also { monoBitmap.recycle() } ?: monoBitmap
    }

    override fun createThemedAdaptiveIcon(
        context: Context,
        originalIcon: AdaptiveIconDrawable,
        info: BitmapInfo?,
    ): AdaptiveIconDrawable {

        originalIcon.mutate()
        originalIcon.monochrome?.let {
            val colors = colorProvider(context)
            it.setTint(colors[1])
            return@createThemedAdaptiveIcon AdaptiveIconDrawable(ColorDrawable(colors[0]), it)
        }

        val themedBitmap = info?.themedBitmap as? MonoThemedBitmap ?: return originalIcon
        val colors = themedBitmap.getUpdatedColors(context)

        // Inject a previously generated monochrome icon
        // Use BitmapDrawable instead of FastBitmapDrawable so that the colorState is
        // preserved in constantState
        // Inset the drawable according to the AdaptiveIconDrawable layers
        val monoDrawable =
            BitmapDrawable(themedBitmap.mono).apply {
                colorFilter = BlendModeColorFilter(colors[1], SRC_IN)
            }
        return AdaptiveIconDrawable(ColorDrawable(colors[0]), monoDrawable)
    }
}

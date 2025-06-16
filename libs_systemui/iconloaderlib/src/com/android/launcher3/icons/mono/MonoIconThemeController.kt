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
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import com.android.launcher3.Flags
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.IconThemeController
import com.android.launcher3.icons.MonochromeIconFactory
import com.android.launcher3.icons.ThemedBitmap
import com.android.launcher3.icons.mono.ThemedIconDrawable.Companion.getColors
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.TIRAMISU)
class MonoIconThemeController : IconThemeController {

    override fun createThemedBitmap(
        icon: AdaptiveIconDrawable,
        info: BitmapInfo,
        factory: BaseIconFactory,
    ): ThemedBitmap? {
        val mono = getMonochromeDrawable(icon, info)
        if (mono != null) {
            val scale =
                factory.normalizer.getScale(
                    AdaptiveIconDrawable(ColorDrawable(Color.BLACK), null),
                    null,
                    null,
                    null,
                )
            return MonoThemedBitmap(
                factory.createIconBitmap(mono, scale, BaseIconFactory.MODE_ALPHA),
                factory.whiteShadowLayer,
            )
        }
        return null
    }

    /**
     * Returns a monochromatic version of the given drawable or null, if it is not supported
     *
     * @param base the original icon
     */
    private fun getMonochromeDrawable(base: AdaptiveIconDrawable, info: BitmapInfo): Drawable? {
        val mono = base.monochrome
        if (mono != null) {
            return ClippedMonoDrawable(mono)
        }
        if (Flags.forceMonochromeAppIcons()) {
            return MonochromeIconFactory(info.icon.width).wrap(base)
        }
        return null
    }

    override fun decode(
        data: ByteArray,
        info: BitmapInfo,
        factory: BaseIconFactory,
    ): ThemedBitmap? {
        val icon = info.icon
        if (data.size != icon.height * icon.width) return null

        var monoBitmap = Bitmap.createBitmap(icon.width, icon.height, ALPHA_8)
        monoBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data))

        val hwMonoBitmap = monoBitmap.copy(HARDWARE, false /*isMutable*/)
        if (hwMonoBitmap != null) {
            monoBitmap.recycle()
            monoBitmap = hwMonoBitmap
        }
        return MonoThemedBitmap(monoBitmap, factory.whiteShadowLayer)
    }

    override fun createThemedAdaptiveIcon(
        context: Context,
        originalIcon: AdaptiveIconDrawable,
        info: BitmapInfo?,
    ): AdaptiveIconDrawable? {
        val colors = getColors(context)
        originalIcon.mutate()
        var monoDrawable = originalIcon.monochrome?.apply { setTint(colors[1]) }

        if (monoDrawable == null) {
            info?.themedBitmap?.let { themedBitmap ->
                if (themedBitmap is MonoThemedBitmap) {
                    // Inject a previously generated monochrome icon
                    // Use BitmapDrawable instead of FastBitmapDrawable so that the colorState is
                    // preserved in constantState
                    // Inset the drawable according to the AdaptiveIconDrawable layers
                    monoDrawable =
                        InsetDrawable(
                            BitmapDrawable(themedBitmap.mono).apply {
                                colorFilter = BlendModeColorFilter(colors[1], SRC_IN)
                            },
                            AdaptiveIconDrawable.getExtraInsetFraction() / 2,
                        )
                }
            }
        }

        return monoDrawable?.let { AdaptiveIconDrawable(ColorDrawable(colors[0]), it) }
    }

    class ClippedMonoDrawable(base: Drawable?) :
        InsetDrawable(base, -AdaptiveIconDrawable.getExtraInsetFraction()) {
        private val mCrop = AdaptiveIconDrawable(ColorDrawable(Color.BLACK), null)

        override fun draw(canvas: Canvas) {
            mCrop.bounds = bounds
            val saveCount = canvas.save()
            canvas.clipPath(mCrop.iconMask)
            super.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }
}

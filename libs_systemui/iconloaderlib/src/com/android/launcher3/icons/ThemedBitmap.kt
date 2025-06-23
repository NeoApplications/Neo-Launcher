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

package com.android.launcher3.icons

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import com.android.launcher3.icons.cache.CachingLogic
import com.android.launcher3.util.ComponentKey

/** Represents a themed version of a BitmapInfo */
interface ThemedBitmap {

    /** Creates a new Drawable */
    fun newDrawable(info: BitmapInfo, context: Context): FastBitmapDrawable

    fun serialize(): ByteArray
}

interface IconThemeController {

    val themeID: String

    fun createThemedBitmap(
        icon: AdaptiveIconDrawable,
        info: BitmapInfo,
        factory: BaseIconFactory,
        sourceHint: SourceHint? = null,
    ): ThemedBitmap?

    fun decode(
        data: ByteArray,
        info: BitmapInfo,
        factory: BaseIconFactory,
        sourceHint: SourceHint,
    ): ThemedBitmap?

    fun createThemedAdaptiveIcon(
        context: Context,
        originalIcon: AdaptiveIconDrawable,
        info: BitmapInfo?,
    ): AdaptiveIconDrawable?
}

data class SourceHint(
    val key: ComponentKey,
    val logic: CachingLogic<*>,
    val freshnessId: String? = null,
    val isFileDrawable: Boolean = false,
)

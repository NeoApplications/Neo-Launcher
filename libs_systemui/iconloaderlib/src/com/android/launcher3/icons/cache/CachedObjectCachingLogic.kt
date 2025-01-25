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

package com.android.launcher3.icons.cache

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.icons.BaseIconFactory.IconOptions
import com.android.launcher3.icons.BitmapInfo

/** Caching logic for ComponentWithLabelAndIcon */
class CachedObjectCachingLogic<T : BaseIconCache>
@JvmOverloads
constructor(
    context: Context,
    private val loadIcons: Boolean = true,
    private val addToMemCache: Boolean = true,
) : CachingLogic<CachedObject<T>> {

    private val pm = context.packageManager

    override fun getComponent(info: CachedObject<T>): ComponentName = info.component

    override fun getUser(info: CachedObject<T>): UserHandle = info.user

    override fun getLabel(info: CachedObject<T>): CharSequence? = info.getLabel(pm)

    override fun loadIcon(
        context: Context,
        cache: BaseIconCache,
        info: CachedObject<T>,
    ): BitmapInfo {
        if (!loadIcons) return BitmapInfo.LOW_RES_INFO
        val d = info.getFullResIcon(cache as T) ?: return BitmapInfo.LOW_RES_INFO
        cache.iconFactory.use { li ->
            return li.createBadgedIconBitmap(d, IconOptions().setUser(info.user))
        }
    }

    override fun addToMemCache() = addToMemCache
}

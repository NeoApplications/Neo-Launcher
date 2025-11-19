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
import com.android.launcher3.icons.IconProvider

/** Caching logic for ComponentWithLabelAndIcon */
object CachedObjectCachingLogic : CachingLogic<CachedObject> {

    override fun getComponent(info: CachedObject): ComponentName = info.component

    override fun getUser(info: CachedObject): UserHandle = info.user

    override fun getLabel(info: CachedObject): CharSequence? = info.label

    override fun loadIcon(context: Context, cache: BaseIconCache, info: CachedObject): BitmapInfo {
        val d = info.getFullResIcon(cache) ?: return BitmapInfo.LOW_RES_INFO
        cache.iconFactory.use { li ->
            return li.createBadgedIconBitmap(d, IconOptions().setUser(info.user))
        }
    }

    override fun getApplicationInfo(info: CachedObject) = info.applicationInfo

    override fun getFreshnessIdentifier(item: CachedObject, provider: IconProvider): String? =
        item.getFreshnessIdentifier(provider)
}

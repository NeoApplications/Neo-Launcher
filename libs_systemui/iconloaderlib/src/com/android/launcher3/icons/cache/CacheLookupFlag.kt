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

import androidx.annotation.IntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/** Flags to control cache lookup behavior */
data class CacheLookupFlag private constructor(@LookupFlag private val flag: Int) {

    /**
     * Cache will try to load the low res version of the entry unless a high-res is already in
     * memory
     */
    fun useLowRes() = hasFlag(USE_LOW_RES)

    @JvmOverloads fun withUseLowRes(useLowRes: Boolean = true) = updateMask(USE_LOW_RES, useLowRes)

    /** Cache will try to lookup the package entry for the item, if the object entry fails */
    fun usePackageIcon() = hasFlag(USE_PACKAGE_ICON)

    @JvmOverloads
    fun withUsePackageIcon(usePackageIcon: Boolean = true) =
        updateMask(USE_PACKAGE_ICON, usePackageIcon)

    /**
     * Entry will not be added to the memory cache if it was not already added by a previous lookup
     */
    fun skipAddToMemCache() = hasFlag(SKIP_ADD_TO_MEM_CACHE)

    @JvmOverloads
    fun withSkipAddToMemCache(skipAddToMemCache: Boolean = true) =
        updateMask(SKIP_ADD_TO_MEM_CACHE, skipAddToMemCache)

    private fun hasFlag(@LookupFlag mask: Int) = flag.and(mask) != 0

    private fun updateMask(@LookupFlag mask: Int, addMask: Boolean) =
        if (addMask) flagCache[flag.or(mask)] else flagCache[flag.and(mask.inv())]

    /** Returns `true` if this flag has less UI information then [other] */
    fun isVisuallyLessThan(other: CacheLookupFlag): Boolean {
        return useLowRes() && !other.useLowRes()
    }

    @Retention(SOURCE)
    @IntDef(value = [USE_LOW_RES, USE_PACKAGE_ICON, SKIP_ADD_TO_MEM_CACHE], flag = true)
    /** Various options to control cache lookup */
    private annotation class LookupFlag

    companion object {
        private const val USE_LOW_RES: Int = 1 shl 0
        private const val USE_PACKAGE_ICON: Int = 1 shl 1
        private const val SKIP_ADD_TO_MEM_CACHE: Int = 1 shl 2

        private val flagCache = Array(8) { CacheLookupFlag(it) }

        @JvmField val DEFAULT_LOOKUP_FLAG = CacheLookupFlag(0)
    }
}

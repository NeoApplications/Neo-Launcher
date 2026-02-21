/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.launcher3.util

import android.os.UserHandle
import androidx.annotation.IntDef
import com.android.launcher3.icons.BitmapInfo

/**
 * Data class which stores various properties of a [android.os.UserHandle] which affects rendering
 */
data class UserIconInfo
@JvmOverloads
constructor(
    @JvmField val user: UserHandle,
    @JvmField @UserType val type: Int,
    @JvmField val userSerial: Long = user.hashCode().toLong(),
) {
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
    @IntDef(TYPE_MAIN, TYPE_WORK, TYPE_CLONED, TYPE_PRIVATE)
    annotation class UserType

    val isMain: Boolean
        get() = type == TYPE_MAIN

    val isWork: Boolean
        get() = type == TYPE_WORK

    val isCloned: Boolean
        get() = type == TYPE_CLONED

    val isPrivate: Boolean
        get() = type == TYPE_PRIVATE

    fun applyBitmapInfoFlags(op: FlagOp): FlagOp =
        op.setFlag(BitmapInfo.FLAG_WORK, isWork)
            .setFlag(BitmapInfo.FLAG_CLONE, isCloned)
            .setFlag(BitmapInfo.FLAG_PRIVATE, isPrivate)

    companion object {
        const val TYPE_MAIN: Int = 0
        const val TYPE_WORK: Int = 1
        const val TYPE_CLONED: Int = 2
        const val TYPE_PRIVATE: Int = 3
    }
}

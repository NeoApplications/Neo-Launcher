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

package com.google.android.msdl.data.model

import android.os.VibrationEffect

/**
 * A haptic composition as a list of [HapticCompositionPrimitive] and a [android.os.VibrationEffect]
 * to use as a fallback.
 */
data class HapticComposition(
    val primitives: List<HapticCompositionPrimitive>,
    val fallbackEffect: VibrationEffect,
)

/**
 * An abstraction of a haptic primitive in a composition that includes:
 *
 * @param[primitiveId] The id of the primitive.
 * @param[scale] The scale of the primitive.
 * @param[delayMillis] The delay of the primitive relative to the end of a previous primitive. Given
 *   in milliseconds.
 */
data class HapticCompositionPrimitive(
    val primitiveId: Int,
    var scale: Float = 1f,
    var delayMillis: Int = 0,
)

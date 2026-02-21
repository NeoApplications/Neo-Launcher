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

package com.google.android.msdl.domain

import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator

class FakeVibrator : Vibrator() {
    var hasAmplitudeControl = true
    var hasVibrator = true
    var latestVibration: VibrationEffect? = null
        private set

    val supportedPrimitives =
        mutableMapOf(
            VibrationEffect.Composition.PRIMITIVE_CLICK to true,
            VibrationEffect.Composition.PRIMITIVE_SPIN to true,
            VibrationEffect.Composition.PRIMITIVE_THUD to true,
            VibrationEffect.Composition.PRIMITIVE_TICK to true,
            VibrationEffect.Composition.PRIMITIVE_LOW_TICK to true,
            VibrationEffect.Composition.PRIMITIVE_QUICK_FALL to true,
            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE to true,
            VibrationEffect.Composition.PRIMITIVE_SLOW_RISE to true,
        )

    var latestAttributes: VibrationAttributes? = null
        private set

    fun setSupportForAllPrimitives(supported: Boolean) =
        supportedPrimitives.replaceAll { _, _ -> supported }

    override fun cancel() {}

    override fun cancel(usageFilter: Int) {}

    override fun hasAmplitudeControl(): Boolean = this.hasAmplitudeControl

    override fun hasVibrator(): Boolean = this.hasVibrator

    override fun arePrimitivesSupported(vararg primitiveIds: Int): BooleanArray =
        primitiveIds.map { id -> supportedPrimitives[id] ?: false }.toBooleanArray()

    override fun vibrate(
        uid: Int,
        opPkg: String,
        vibe: VibrationEffect,
        reason: String,
        attributes: VibrationAttributes,
    ) {
        latestVibration = vibe
        latestAttributes = attributes
    }

    override fun vibrate(
        vibe: VibrationEffect,
        attributes: VibrationAttributes,
    ) {
        latestVibration = vibe
        latestAttributes = attributes
    }
}

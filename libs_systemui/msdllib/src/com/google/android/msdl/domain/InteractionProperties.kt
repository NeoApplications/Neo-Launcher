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
import androidx.annotation.FloatRange

/**
 * Properties associated to an interaction that is currently occurring.
 *
 * The properties define parameters required to play the data associated with a
 * [com.google.android.msdl.data.model.MSDLToken]. These can be dynamic, in the sense that they can
 * be created as an interaction progresses throughout time.
 *
 * Each set of properties needs to define [VibrationAttributes] for a haptic effect to play with. If
 * no properties are provided when playing a token, the effect will play with a default set of
 * attributes with [VibrationAttributes.USAGE_TOUCH] usage.
 */
interface InteractionProperties {

    /** [android.os.VibrationAttributes] for haptics in the interaction */
    val vibrationAttributes: VibrationAttributes

    /**
     * Properties for a vibration that changes scale dynamically.
     *
     * The scale must be calculated at the time of calling the
     * [com.google.android.msdl.domain.MSDLPlayer] API to play feedback. Use these properties for
     * effects where vibration scales depend on temporal variables, such as position and velocity
     * for slider haptics.
     *
     * @param[scale] The scale of the vibration at the time of calling. Must be between 0 and 1.
     * @param[vibrationUsageId] Id used to create [android.os.VibrationAttributes]
     */
    data class DynamicVibrationScale(
        @FloatRange(from = 0.0, to = 1.0) val scale: Float,
        override val vibrationAttributes: VibrationAttributes,
    ) : InteractionProperties
}

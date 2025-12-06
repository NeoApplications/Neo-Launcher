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

package com.google.android.msdl.data.repository

import android.os.VibrationEffect
import com.google.android.msdl.data.model.HapticComposition
import com.google.android.msdl.data.model.HapticCompositionPrimitive
import com.google.android.msdl.data.model.HapticToken
import com.google.android.msdl.data.model.SoundToken

/** A [MSDLRepository] that holds haptic compositions as haptic data. */
internal class MSDLRepositoryImpl : MSDLRepository {

    override fun getAudioData(soundToken: SoundToken): MSDLSoundData? {
        // TODO(b/345248875) Implement a caching strategy in accordance to the audio file strategy
        return null
    }

    override fun getHapticData(hapticToken: HapticToken): MSDLHapticData? = HAPTIC_DATA[hapticToken]

    companion object {
        // Timings and amplitudes that recreate a composition of three SPIN primitives as a waveform
        private val SPIN_TIMINGS = longArrayOf(20, 20, 3, 43, 20, 20, 3)
        private val SPIN_AMPLITUDES = intArrayOf(40, 80, 40, 0, 40, 80, 40)
        private const val SPIN_DELAY = 56L
        private const val SPIN_BREAK = 10
        private val SPIN_WAVEFORM_TIMINGS =
            SPIN_TIMINGS + SPIN_DELAY + SPIN_TIMINGS + SPIN_DELAY + SPIN_TIMINGS
        private val SPIN_WAVEFORM_AMPLITUDES =
            SPIN_AMPLITUDES + SPIN_BREAK + SPIN_AMPLITUDES + SPIN_BREAK + SPIN_AMPLITUDES

        private val HAPTIC_DATA: Map<HapticToken, MSDLHapticData> =
            mapOf(
                HapticToken.NEGATIVE_CONFIRMATION_HIGH_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_SPIN,
                                    scale = 1f,
                                    delayMillis = 0,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_SPIN,
                                    scale = 1f,
                                    delayMillis = SPIN_DELAY.toInt(),
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_SPIN,
                                    scale = 1f,
                                    delayMillis = SPIN_DELAY.toInt(),
                                ),
                            ),
                            VibrationEffect.createWaveform(
                                SPIN_WAVEFORM_TIMINGS,
                                SPIN_WAVEFORM_AMPLITUDES,
                                -1,
                            ),
                        )
                    },
                HapticToken.NEGATIVE_CONFIRMATION_MEDIUM_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 114,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 114,
                                ),
                            ),
                            VibrationEffect.createWaveform(
                                longArrayOf(10, 10, 10, 114, 10, 10, 10, 114, 10, 10, 10),
                                intArrayOf(10, 255, 20, 0, 10, 255, 20, 0, 10, 255, 20),
                                -1,
                            ),
                        )
                    },
                HapticToken.POSITIVE_CONFIRMATION_HIGH_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 114,
                                ),
                            ),
                            VibrationEffect.createWaveform(
                                longArrayOf(10, 10, 10, 114, 10, 10, 10),
                                intArrayOf(10, 255, 20, 0, 10, 255, 20),
                                -1,
                            ),
                        )
                    },
                HapticToken.POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 52,
                                ),
                            ),
                            VibrationEffect.createWaveform(
                                longArrayOf(10, 10, 10, 52, 10, 10, 10),
                                intArrayOf(10, 255, 20, 0, 10, 255, 20),
                                -1,
                            ),
                        )
                    },
                HapticToken.POSITIVE_CONFIRMATION_LOW_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_TICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                ),
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 52,
                                ),
                            ),
                            VibrationEffect.createWaveform(
                                longArrayOf(5, 52, 10, 10, 10),
                                intArrayOf(100, 0, 10, 255, 20),
                                -1,
                            ),
                        )
                    },
                HapticToken.NEUTRAL_CONFIRMATION_HIGH_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_THUD,
                                    scale = 1f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createWaveform(
                                longArrayOf(50, 100, 100, 50),
                                intArrayOf(5, 50, 20, 10),
                                -1,
                            ),
                        )
                    },
                HapticToken.NEUTRAL_CONFIRMATION_MEDIUM_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.LONG_PRESS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.SWIPE_THRESHOLD_INDICATOR to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.7f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.TAP_HIGH_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.7f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.TAP_MEDIUM_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.5f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.DRAG_THRESHOLD_INDICATOR to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK),
                        )
                    },
                HapticToken.DRAG_INDICATOR_CONTINUOUS to
                    MSDLHapticData {
                        HapticComposition(
                            List(size = 5) {
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                                    scale = 0.3f,
                                    delayMillis = 0,
                                )
                            },
                            VibrationEffect.createWaveform(
                                longArrayOf(10, 20, 20, 10),
                                intArrayOf(10, 30, 50, 10),
                                -1,
                            ),
                        )
                    },
                HapticToken.DRAG_INDICATOR_DISCRETE to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_TICK,
                                    scale = 0.5f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK),
                        )
                    },
                HapticToken.TAP_LOW_EMPHASIS to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.3f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.KEYPRESS_STANDARD to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.5f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.KEYPRESS_SPACEBAR to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.7f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.KEYPRESS_RETURN to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 0.7f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
                HapticToken.KEYPRESS_DELETE to
                    MSDLHapticData {
                        HapticComposition(
                            listOf(
                                HapticCompositionPrimitive(
                                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                                    scale = 1f,
                                    delayMillis = 0,
                                )
                            ),
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
                        )
                    },
            )
    }
}

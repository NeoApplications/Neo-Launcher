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

import android.os.Vibrator
import android.util.Log
import com.google.android.msdl.data.model.FeedbackLevel
import com.google.android.msdl.data.model.HapticComposition
import com.google.android.msdl.data.model.MSDLToken
import com.google.android.msdl.data.repository.MSDLRepository
import com.google.android.msdl.data.repository.MSDLRepositoryImpl
import com.google.android.msdl.domain.MSDLPlayerImpl.Companion.REQUIRED_PRIMITIVES
import com.google.android.msdl.logging.MSDLEvent
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Player of MSDL feedback.
 *
 * This player is central API to deliver audio and haptic feedback bundled and referenced by
 * instances of [MSDLToken].
 */
interface MSDLPlayer {

    // Current feedback level set in the system
    fun getSystemFeedbackLevel(): FeedbackLevel

    /**
     * Play a [MSDLToken].
     *
     * @param[token] The [MSDLToken] to play. This will be used to fetch its corresponding haptic
     *   and sound data.
     * @param[properties] [InteractionProperties] associated with the token requested to play. These
     *   properties can modify how a token plays (e.g.,
     *   [InteractionProperties.DynamicVibrationScale] for slider haptics in the
     *   [MSDLToken.DRAG_INDICATOR] token) and can be supplied if custom
     *   [android.os.VibrationAttributes] are required for haptic playback. If no properties are
     *   supplied, haptic feedback will play using USAGE_TOUCH [android.os.VibrationAttributes].
     */
    fun playToken(token: MSDLToken, properties: InteractionProperties? = null)

    /**
     * Get the history of recent [MSDLEvent]s. The list can be useful to include in loggers and
     * system dumps for debugging purposes.
     */
    fun getHistory(): List<MSDLEvent>

    companion object {

        // TODO(b/355230334): remove once we have a system setting for the level
        var SYSTEM_FEEDBACK_LEVEL = FeedbackLevel.DEFAULT

        /**
         * Create a new [MSDLPlayer].
         *
         * @param[vibrator] The [Vibrator] this player will use for haptic playback.
         * @param[executor] An [Executor] to schedule haptic playback.
         * @param[useHapticFeedbackForToken] A map that determines if a haptic fallback effect
         *   should be used to play haptics for a given [MSDLToken]. If null, the map will be
         *   created using the support information from the given vibrator.
         */
        fun createPlayer(
            vibrator: Vibrator?,
            executor: Executor = Executors.newSingleThreadExecutor(),
            useHapticFeedbackForToken: Map<MSDLToken, Boolean>? = null,
        ): MSDLPlayer {
            // Return an empty player if no vibrator is available
            if (vibrator == null) {
                Log.w(
                    "MSDLPlayer",
                    "A null vibrator was used to create a MSDLPlayer. An empty player was created",
                )
                return EmptyMSDLPlayer()
            }

            // Create repository
            val repository = MSDLRepositoryImpl()

            // Determine the support for haptic primitives to know if fallbacks will be used.
            // This can be provided by the client. If omitted, it will be determined from the
            // supported primitives of the given vibrator.
            val shouldUseFallbackForToken =
                useHapticFeedbackForToken ?: createHapticFallbackDecisionMap(vibrator, repository)

            return MSDLPlayerImpl(repository, vibrator, executor, shouldUseFallbackForToken)
        }

        private fun createHapticFallbackDecisionMap(
            vibrator: Vibrator,
            repository: MSDLRepository,
        ): Map<MSDLToken, Boolean> {
            val supportedPrimitives =
                REQUIRED_PRIMITIVES.associateWith { vibrator.arePrimitivesSupported(it).first() }
            return MSDLToken.entries.associateWith { token ->
                // For each token, determine if the haptic data from the repository
                // should use the fallback effect.
                val hapticComposition =
                    repository.getHapticData(token.hapticToken)?.get() as? HapticComposition
                hapticComposition?.shouldPlayFallback(supportedPrimitives) ?: false
            }
        }
    }
}

fun HapticComposition.shouldPlayFallback(supportedPrimitives: Map<Int, Boolean>): Boolean {
    primitives.forEach { primitive ->
        val isSupported = supportedPrimitives[primitive.primitiveId]
        if (isSupported == null || isSupported == false) {
            return true
        }
    }
    return false
}

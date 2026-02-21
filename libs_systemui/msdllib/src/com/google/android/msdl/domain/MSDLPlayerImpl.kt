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

import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.msdl.data.model.FeedbackLevel
import com.google.android.msdl.data.model.HapticComposition
import com.google.android.msdl.data.model.MSDLToken
import com.google.android.msdl.data.repository.MSDLRepository
import com.google.android.msdl.logging.MSDLEvent
import com.google.android.msdl.logging.MSDLHistoryLogger
import com.google.android.msdl.logging.MSDLHistoryLoggerImpl
import java.util.concurrent.Executor

/**
 * Implementation of the MSDLPlayer.
 *
 * At the core, the player is in charge of delivering haptic and audio feedback closely in time.
 *
 * @param[repository] Repository to retrieve audio and haptic data.
 * @param[executor] An [Executor] used to schedule haptic playback.
 * @param[vibrator] Instance of the default [Vibrator] on the device.
 * @param[useHapticFallbackForToken] A map that determines if the haptic fallback effect should be
 *   used for a given token.
 */
internal class MSDLPlayerImpl(
    private val repository: MSDLRepository,
    private val vibrator: Vibrator,
    private val executor: Executor,
    private val useHapticFallbackForToken: Map<MSDLToken, Boolean?>,
) : MSDLPlayer {

    /** A logger to keep a history of playback events */
    private val historyLogger = MSDLHistoryLoggerImpl(MSDLHistoryLogger.HISTORY_SIZE)

    // TODO(b/355230334): This should be retrieved from the system Settings
    override fun getSystemFeedbackLevel(): FeedbackLevel = MSDLPlayer.SYSTEM_FEEDBACK_LEVEL

    override fun playToken(token: MSDLToken, properties: InteractionProperties?) {
        // Don't play the data for the token if the current feedback level is below the minimal
        // level of the token
        if (getSystemFeedbackLevel() < token.minimumFeedbackLevel) return

        // Play the data for the token with the given properties
        playData(token, properties)
    }

    private fun playData(token: MSDLToken, properties: InteractionProperties?) {
        // Gather the data from the repositories
        val hapticData = repository.getHapticData(token.hapticToken)
        val soundData = repository.getAudioData(token.soundToken)

        // Nothing to play
        if (hapticData == null && soundData == null) return

        if (soundData == null) {
            // Play haptics only
            // 1. Create the effect
            val composition: HapticComposition? = hapticData?.get() as? HapticComposition
            val effect =
                if (useHapticFallbackForToken[token] == true) {
                    composition?.fallbackEffect
                } else {
                    when (properties) {
                        is InteractionProperties.DynamicVibrationScale -> {
                            composition?.composeIntoVibrationEffect(
                                scaleOverride = properties.scale
                            )
                        }
                        else -> composition?.composeIntoVibrationEffect() // compose as-is
                    }
                }

            // 2. Deliver the haptics with or without attributes
            if (effect == null || !vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attributes =
                    if (properties?.vibrationAttributes != null) {
                        properties.vibrationAttributes
                    } else {
                        VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_TOUCH)
                            .build()
                    }
                executor.execute { vibrator.vibrate(effect, attributes) }
            } else {
                executor.execute { vibrator.vibrate(effect) }
            }

            // 3. Log the event
            historyLogger.addEvent(MSDLEvent(token, properties))
        } else {
            // TODO(b/345248875): Play audio and haptics
        }
    }

    override fun getHistory(): List<MSDLEvent> = historyLogger.getHistory()

    override fun toString(): String =
        """
            Default MSDL player implementation.
            Vibrator: $vibrator
            Repository: $repository
        """
            .trimIndent()

    companion object {
        val REQUIRED_PRIMITIVES =
            listOf(
                VibrationEffect.Composition.PRIMITIVE_SPIN,
                VibrationEffect.Composition.PRIMITIVE_THUD,
                VibrationEffect.Composition.PRIMITIVE_TICK,
                VibrationEffect.Composition.PRIMITIVE_CLICK,
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
            )
    }
}

fun HapticComposition.composeIntoVibrationEffect(
    scaleOverride: Float? = null,
    delayOverride: Int? = null,
): VibrationEffect? {
    val effectComposition = VibrationEffect.startComposition()
    primitives.forEach { primitive ->
        effectComposition.addPrimitive(
            primitive.primitiveId,
            scaleOverride ?: primitive.scale,
            delayOverride ?: primitive.delayMillis,
        )
    }
    return effectComposition.compose()
}

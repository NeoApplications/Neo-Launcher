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
import com.google.android.msdl.data.model.FeedbackLevel
import com.google.android.msdl.data.model.HapticComposition
import com.google.android.msdl.data.model.MSDLToken
import com.google.android.msdl.data.repository.MSDLRepository
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Executor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MSDLPlayerImplTest {

    @Parameterized.Parameter lateinit var token: MSDLToken

    private val repository = MSDLRepository.createRepository()
    private val vibrator = FakeVibrator()
    private val executor = Executor { it.run() }
    private val useHapticFallbackForToken = MSDLToken.entries.associateWith { false }.toMutableMap()

    private var msdlPlayer = MSDLPlayer.createPlayer(vibrator, executor, useHapticFallbackForToken)

    @Before
    fun setup() {
        MSDLPlayer.SYSTEM_FEEDBACK_LEVEL = FeedbackLevel.EXPRESSIVE
        vibrator.setSupportForAllPrimitives(true)
    }

    @Test
    fun playToken_withExpressiveSetting_deliversAllFeedback() {
        // GIVEN that the feedback level is expressive (all tokens play)
        MSDLPlayer.SYSTEM_FEEDBACK_LEVEL = FeedbackLevel.EXPRESSIVE
        val composition = repository.getHapticData(token.hapticToken)?.get() as? HapticComposition
        val effect = composition?.composeIntoVibrationEffect()

        // WHEN the token plays
        msdlPlayer.playToken(token)

        // THEN the vibration is delivered
        assertThat(vibrator.latestVibration).isEqualTo(effect)
    }

    @Test
    fun playToken_withoutFeedbackSetting_doesNotDeliverAnyFeedback() {
        // GIVEN that the feedback level specifies no feedback
        MSDLPlayer.SYSTEM_FEEDBACK_LEVEL = FeedbackLevel.NO_FEEDBACK

        // WHEN the token plays
        msdlPlayer.playToken(token)

        // THEN no vibration is delivered
        assertThat(vibrator.latestVibration).isNull()
    }

    @Test
    fun playHapticComposition_withNullProperties_playsExpectedVibrationEffect() {
        // GIVEN the vibration effect of a composition
        val composition = repository.getHapticData(token.hapticToken)?.get() as? HapticComposition
        val effect = composition?.composeIntoVibrationEffect()

        // WHEN the composition is played for a token without interaction properties
        msdlPlayer.playToken(token)

        // THEN the vibration delivers the same vibration effect with USAGE_TOUCH vibration
        // attributes and the correct token reason.
        val touchAttributes =
            VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build()
        assertVibrationEffectDelivered(effect, touchAttributes)
    }

    @Test
    fun playHapticComposition_withoutSupportedPrimitives_playsFallbackEffects() {
        // GIVEN that no primitives are supported
        useHapticFallbackForToken.replaceAll { _, _ -> true }

        // GIVEN the fallback effect of a composition
        val composition = repository.getHapticData(token.hapticToken)?.get() as? HapticComposition
        val effect = composition?.fallbackEffect

        // WHEN the composition is played for a token without interaction properties
        msdlPlayer.playToken(token)

        // THEN the vibration delivers the same fallback effect with USAGE_TOUCH vibration
        val touchAttributes =
            VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build()
        assertVibrationEffectDelivered(effect, touchAttributes)
    }

    @Test
    fun playHapticComposition_withDynamicVibrationScaleProperties_playsExpectedVibrationEffect() {
        // GIVEN  DynamicVibrationScaleProperties and a vibration effect built with this scale
        val scaleOverride = 0.4f
        val composition = repository.getHapticData(token.hapticToken)?.get() as? HapticComposition
        val effect = composition?.composeIntoVibrationEffect(scaleOverride)
        val attributes =
            VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build()
        val properties = InteractionProperties.DynamicVibrationScale(scaleOverride, attributes)

        // WHEN the composition is played for the token with the properties
        msdlPlayer.playToken(token, properties)

        // THEN the vibration effect delivered is the same vibration effect with the properties
        assertVibrationEffectDelivered(effect, attributes)
    }

    @Test
    fun playHapticComposition_withoutHardwareVibrator_doesNotPlayVibrationEffect() {
        // GIVEN the vibration effect of a composition when there is no hardware vibrator
        vibrator.hasVibrator = false

        // WHEN the composition is played for a token
        msdlPlayer.playToken(token)

        // THEN the vibration does not deliver any effect
        assertThat(vibrator.latestVibration).isNull()
    }

    private fun assertVibrationEffectDelivered(
        effect: VibrationEffect?,
        attributes: VibrationAttributes,
    ) {
        assertThat(vibrator.latestVibration).isEqualTo(effect)
        if (effect != null) {
            assertThat(vibrator.latestAttributes).isEqualTo(attributes)
        } else {
            assertThat(vibrator.latestAttributes).isNull()
        }
    }

    companion object {
        @JvmStatic @Parameterized.Parameters fun tokens() = MSDLToken.entries
    }
}

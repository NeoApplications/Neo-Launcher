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

package com.google.android.msdl.logging

import android.os.VibrationAttributes
import com.google.android.msdl.data.model.MSDLToken
import com.google.android.msdl.domain.InteractionProperties
import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MSDLHistoryLoggerImplTest {

    private val properties =
        object : InteractionProperties {
            override val vibrationAttributes: VibrationAttributes =
                VibrationAttributes.createForUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
        }
    private val random = Random(0)

    private val logger = MSDLHistoryLoggerImpl(MSDLHistoryLogger.HISTORY_SIZE)

    @Test
    fun addEvent_eventIsAddedToHistory() {
        val token = getRandomToken()
        val event = MSDLEvent(token, properties)
        logger.addEvent(event)

        assertThat(logger.getHistory().containsEvent(event)).isTrue()
    }

    @Test
    fun addEvent_beyondLimit_keepsHistorySize() {
        val tokens = getRandomTokens(MSDLHistoryLogger.HISTORY_SIZE + 10)
        tokens.forEach { logger.addEvent(MSDLEvent(it, properties)) }

        assertThat(logger.getHistory().size).isEqualTo(MSDLHistoryLogger.HISTORY_SIZE)
    }

    @Test
    fun addEvent_beyondLimit_keepsLatestEvents() {
        val localHistory = arrayListOf<MSDLEvent>()
        val tokens = getRandomTokens(MSDLHistoryLogger.HISTORY_SIZE + 10)
        var event: MSDLEvent
        tokens.forEach {
            event = MSDLEvent(it, properties)
            logger.addEvent(event)
            localHistory.add(event)
        }

        val latestLocalHistory = localHistory.takeLast(MSDLHistoryLogger.HISTORY_SIZE)
        val loggerHistory = logger.getHistory()
        assertThat(latestLocalHistory.isEqualTo(loggerHistory)).isTrue()
    }

    private fun getRandomToken(): MSDLToken =
        MSDLToken.entries[random.nextInt(0, MSDLToken.entries.size)]

    private fun getRandomTokens(n: Int): List<MSDLToken> = List(n) { getRandomToken() }

    /**
     * Check if the list is equal to another by making sure it has the same elements in the same
     * order. Events are compared by their token name and interaction properties.
     *
     * @param[other] The other list to compare to.
     */
    private fun List<MSDLEvent>.isEqualTo(other: List<MSDLEvent>): Boolean {
        assert(other.size == this.size) { "Both lists must be of the same size" }
        this.forEachIndexed { i, event ->
            if (event.tokenName != other[i].tokenName || event.properties != other[i].properties) {
                return false
            }
        }
        return true
    }

    /**
     * Check if the list contains an event. Events are compared by their token name and interaction
     * properties.
     *
     * @param[other] The event to find.
     */
    private fun List<MSDLEvent>.containsEvent(other: MSDLEvent): Boolean {
        this.forEach { event ->
            if (event.tokenName == other.tokenName && event.properties == other.properties) {
                return true
            }
        }
        return false
    }
}

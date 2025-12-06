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

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PACKAGE_PRIVATE
import java.util.ArrayDeque
import java.util.Deque

@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
class MSDLHistoryLoggerImpl(private val maxHistorySize: Int) : MSDLHistoryLogger {

    // Use an [ArrayDequeue] with a fixed size as the history structure
    private val history: Deque<MSDLEvent> = ArrayDeque(maxHistorySize)

    override fun addEvent(event: MSDLEvent) {
        // Keep the history as a FIFO structure
        if (history.size == maxHistorySize) {
            history.removeFirst()
        }
        history.addLast(event)
    }

    override fun getHistory(): List<MSDLEvent> = history.toList()
}

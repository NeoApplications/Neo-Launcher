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

package com.android.launcher3.celllayout.integrationtest.events

import android.content.Context
import com.android.launcher3.debug.TestEvent
import com.android.launcher3.debug.TestEventEmitter
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Rule to create EventWaiters to wait for events that happens on the Launcher. For reference look
 * at [TestEvent] for existing events.
 *
 * Waiting for event should be used to prevent race conditions, it provides a more precise way of
 * waiting for events compared to [AbstractLauncherUiTest#waitForLauncherCondition].
 *
 * This class overrides the [TestEventEmitter] with [TestEventsEmitterImplementation] and makes sure
 * to return the [TestEventEmitter] to the previous value when finished.
 */
class EventsRule(val context: Context) : TestRule {

    private var prevEventEmitter: TestEventEmitter? = null

    private val eventEmitter = TestEventsEmitterImplementation()

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                beforeTest()
                base.evaluate()
                afterTest()
            }
        }
    }

    fun createEventWaiter(expectedEvent: TestEvent): EventWaiter {
        return eventEmitter.createEventWaiter(expectedEvent)
    }

    private fun beforeTest() {
        prevEventEmitter = TestEventEmitter.INSTANCE.get(context)
        TestEventEmitter.INSTANCE.initializeForTesting(eventEmitter)
    }

    private fun afterTest() {
        TestEventEmitter.INSTANCE.initializeForTesting(prevEventEmitter)
    }
}

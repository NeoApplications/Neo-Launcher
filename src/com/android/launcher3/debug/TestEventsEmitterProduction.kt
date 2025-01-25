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

package com.android.launcher3.debug

import android.content.Context
import android.util.Log
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.SafeCloseable

/** Events fired by the launcher. */
enum class TestEvent(val event: String) {
    LAUNCHER_ON_CREATE("LAUNCHER_ON_CREATE"),
    WORKSPACE_ON_DROP("WORKSPACE_ON_DROP"),
    RESIZE_FRAME_SHOWING("RESIZE_FRAME_SHOWING"),
    WORKSPACE_FINISH_LOADING("WORKSPACE_FINISH_LOADING"),
    SPRING_LOADED_STATE_STARTED("SPRING_LOADED_STATE_STARTED"),
    SPRING_LOADED_STATE_COMPLETED("SPRING_LOADED_STATE_COMPLETED"),
}

/** Interface to create TestEventEmitters. */
interface TestEventEmitter : SafeCloseable {

    companion object {
        @JvmField
        val INSTANCE =
            MainThreadInitializedObject<TestEventEmitter> { _: Context? ->
                TestEventsEmitterProduction()
            }
    }

    fun sendEvent(event: TestEvent)
}

/**
 * TestEventsEmitterProduction shouldn't do anything since it runs on the launcher code and not on
 * tests. This is just a placeholder and test should override this class.
 */
class TestEventsEmitterProduction : TestEventEmitter {

    override fun close() {}

    override fun sendEvent(event: TestEvent) {
        Log.d("TestEventsEmitterProduction", "Event sent ${event.event}")
    }
}

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

package com.android.app.animation

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class AnimationsTest {
    companion object {
        const val TEST_DURATION = 1000L
    }

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun ongoingAnimationsAreStoredAndCancelledCorrectly() {
        val view = View(context)

        val oldAnimation = FakeAnimator()
        Animations.setOngoingAnimation(view, oldAnimation)
        oldAnimation.start()
        assertEquals(oldAnimation, view.getTag(R.id.ongoing_animation))
        assertTrue(oldAnimation.started)

        val newAnimation = FakeAnimator()
        Animations.setOngoingAnimation(view, newAnimation)
        newAnimation.start()
        assertEquals(newAnimation, view.getTag(R.id.ongoing_animation))
        assertTrue(oldAnimation.cancelled)
        assertTrue(newAnimation.started)

        Animations.cancelOngoingAnimation(view)
        assertNull(view.getTag(R.id.ongoing_animation))
        assertTrue(newAnimation.cancelled)
    }
}

/** Test animator for tracking start and cancel signals. */
private class FakeAnimator : ValueAnimator() {
    var started = false
    var cancelled = false

    override fun start() {
        started = true
        cancelled = false
    }

    override fun cancel() {
        started = false
        cancelled = true
    }
}

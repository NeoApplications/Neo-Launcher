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

package com.android.launcher3.widget

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.android.launcher3.util.ActivityContextWrapper
import com.android.launcher3.util.Executors
import java.util.function.IntConsumer
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
@RunWith(AndroidJUnit4::class)
class LauncherAppWidgetHostTest {

    @Mock private lateinit var onAppWidgetRemovedCallback: IntConsumer

    private val context = ActivityContextWrapper(getInstrumentation().targetContext)
    private lateinit var underTest: LauncherAppWidgetHost

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        underTest = LauncherAppWidgetHost(context, onAppWidgetRemovedCallback, emptyList())
    }

    @Test
    fun `Host set view to recycle`() {
        val mockRecycleView = mock(ListenableHostView::class.java)

        assertNull(underTest.viewToRecycle)
        underTest.recycleViewForNextCreation(mockRecycleView)

        assertSame(mockRecycleView, underTest.viewToRecycle)
    }

    @Test
    fun `Host create view`() {
        val mockRecycleView = mock(ListenableHostView::class.java)

        var resultView = underTest.onCreateView(context, WIDGET_ID, null)

        assertNotSame(mockRecycleView, resultView)

        underTest.recycleViewForNextCreation(mockRecycleView)
        resultView = underTest.onCreateView(context, WIDGET_ID, null)

        assertSame(mockRecycleView, resultView)
    }

    @Test
    fun `Runnable called when app widget removed`() {
        underTest.onAppWidgetRemoved(WIDGET_ID)

        Executors.MODEL_EXECUTOR.submit {}.get()
        getInstrumentation().waitForIdleSync()

        verify(onAppWidgetRemovedCallback).accept(WIDGET_ID)
    }

    companion object {
        const val WIDGET_ID = 10001
    }
}

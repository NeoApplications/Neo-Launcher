/*
 * Copyright (C) 2023 The Android Open Source Project
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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import java.lang.reflect.Modifier
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private const val ANDROIDX_ANIM_PACKAGE_NAME = "androidx.core.animation."
private const val ANDROID_ANIM_PACKAGE_NAME = "android.view.animation."

@SmallTest
@RunWith(AndroidJUnit4::class)
class InterpolatorsAndroidXTest {

    @Test
    fun testInterpolatorsAndInterpolatorsAndroidXPublicMethodsAreEqual() {
        assertEquals(
                Interpolators::class.java.getPublicMethods(),
                InterpolatorsAndroidX::class.java.getPublicMethods()
        )
    }

    @Test
    fun testInterpolatorsAndInterpolatorsAndroidXPublicFieldsAreEqual() {
        assertEquals(
                Interpolators::class.java.getPublicFields(),
                InterpolatorsAndroidX::class.java.getPublicFields()
        )
    }

    private fun <T> Class<T>.getPublicMethods() =
            declaredMethods
                    .filter { Modifier.isPublic(it.modifiers) }
                    .map { it.toString().replace(name, "")
                        .replace(ANDROIDX_ANIM_PACKAGE_NAME, "")
                        .replace(ANDROID_ANIM_PACKAGE_NAME, "") }
                    .toSet()

    private fun <T> Class<T>.getPublicFields() =
            fields.filter { Modifier.isPublic(it.modifiers) }.map { it.name }.toSet()
}

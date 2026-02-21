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

package com.google.android.msdl.data.repository

import com.google.android.msdl.data.model.HapticComposition
import com.google.android.msdl.data.model.HapticToken
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MSDLRepositoryImplTest {

    private val repository = MSDLRepository.createRepository()

    @Test
    fun getHapticData_forAllHapticTokens_returnsCompositions() {
        var validCompositions = 0
        HapticToken.entries.forEach { token ->
            if (repository.getHapticData(token)?.get() is HapticComposition) {
                validCompositions++
            }
        }
        assertThat(validCompositions).isEqualTo(HapticToken.entries.size)
    }
}

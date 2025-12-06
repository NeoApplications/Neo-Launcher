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

import androidx.annotation.VisibleForTesting
import com.google.android.msdl.data.model.HapticToken
import com.google.android.msdl.data.model.SoundToken

/**
 * A repository of data for [HapticToken] and [SoundToken].
 *
 * The principle behind this repository is to hold the data for all tokens as a cache in memory.
 * This is only suitable if the number of tokens and the data stored is manageable. The purpose of
 * this design choice is to provide fast and easy access to the data when required to be played by
 * UI interactions.
 */
sealed interface MSDLRepository {

    /**
     * Get the [MSDLHapticData] that corresponds to the given haptic reference token. This function
     * needs to be fast since it will be called repeatedly to deliver feedback. If necessary, a
     * caching strategy should be applied.
     *
     * @param[hapticToken] The [HapticToken] that points to the data.
     * @return the data that corresponds to the token at the time this function is called.
     */
    fun getHapticData(hapticToken: HapticToken): MSDLHapticData?

    /**
     * Get the [MSDLSoundData] that corresponds to the given sound reference token. This function
     * needs to be fast since it will be called repeatedly to deliver feedback. If necessary, a
     * caching strategy should be applied.
     *
     * @param[soundToken] The [SoundToken] that points to the data.
     * @return the data that corresponds to the token at the time this function is called.
     */
    fun getAudioData(soundToken: SoundToken): MSDLSoundData?

    companion object {

        @VisibleForTesting fun createRepository(): MSDLRepository = MSDLRepositoryImpl()
    }
}

/** Representation of data contained in a [MSDLRepository] */
fun interface MSDLHapticData {

    /** Retrieve the haptic data */
    fun get(): Any?
}

/** Representation of data contained in a [MSDLRepository] */
fun interface MSDLSoundData {

    /** Retrieve the sound data */
    fun get(): Any?
}

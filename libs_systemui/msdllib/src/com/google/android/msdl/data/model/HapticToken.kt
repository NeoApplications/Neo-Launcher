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

package com.google.android.msdl.data.model

/** Haptic tokens from the Multi-sensory Design Language (MSDL) */
enum class HapticToken {
    NEGATIVE_CONFIRMATION_HIGH_EMPHASIS,
    NEGATIVE_CONFIRMATION_MEDIUM_EMPHASIS,
    POSITIVE_CONFIRMATION_HIGH_EMPHASIS,
    POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS,
    POSITIVE_CONFIRMATION_LOW_EMPHASIS,
    NEUTRAL_CONFIRMATION_HIGH_EMPHASIS,
    NEUTRAL_CONFIRMATION_MEDIUM_EMPHASIS,
    LONG_PRESS,
    SWIPE_THRESHOLD_INDICATOR,
    TAP_HIGH_EMPHASIS,
    TAP_MEDIUM_EMPHASIS,
    DRAG_THRESHOLD_INDICATOR,
    DRAG_INDICATOR_CONTINUOUS,
    DRAG_INDICATOR_DISCRETE,
    TAP_LOW_EMPHASIS,
    KEYPRESS_STANDARD,
    KEYPRESS_SPACEBAR,
    KEYPRESS_RETURN,
    KEYPRESS_DELETE,
}

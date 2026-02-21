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

/** System-level tokens defined in the Multi-sensory Design Language (MSDL) */
enum class MSDLToken(
    val hapticToken: HapticToken,
    val soundToken: SoundToken,
    val minimumFeedbackLevel: FeedbackLevel,
) {
    /* Inform the user with emphasis that their current action FAILED to complete */
    FAILURE_HIGH_EMPHASIS(
        HapticToken.NEGATIVE_CONFIRMATION_HIGH_EMPHASIS,
        SoundToken.FAILURE_HIGH_EMPHASIS,
        FeedbackLevel.MINIMAL,
    ),
    /* Inform the user that their current action FAILED to complete */
    FAILURE(
        HapticToken.NEGATIVE_CONFIRMATION_MEDIUM_EMPHASIS,
        SoundToken.FAILURE,
        FeedbackLevel.MINIMAL,
    ),
    /* Inform the user their current action was completed SUCCESSFULLY */
    SUCCESS(
        HapticToken.POSITIVE_CONFIRMATION_HIGH_EMPHASIS,
        SoundToken.SUCCESS,
        FeedbackLevel.MINIMAL,
    ),
    /* Inform the user that an ongoing activity has started */
    START(HapticToken.NEUTRAL_CONFIRMATION_HIGH_EMPHASIS, SoundToken.START, FeedbackLevel.DEFAULT),
    /* Inform the user that an ongoing activity has paused */
    PAUSE(
        HapticToken.NEUTRAL_CONFIRMATION_MEDIUM_EMPHASIS,
        SoundToken.PAUSE,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user that their previously started activity has stopped SUCCESSFULLY */
    STOP(HapticToken.POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS, SoundToken.STOP, FeedbackLevel.DEFAULT),
    /* Inform the user that their previously started activity has cancelled SUCCESSFULLY */
    CANCEL(
        HapticToken.POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS,
        SoundToken.CANCEL,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user that the state of an interactive component has been switched to on SUCCESSFULLY */
    SWITCH_ON(
        HapticToken.POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS,
        SoundToken.SWITCH_ON,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user that the state of an interactive component has been switched to off SUCCESSFULLY */
    SWITCH_OFF(
        HapticToken.POSITIVE_CONFIRMATION_MEDIUM_EMPHASIS,
        SoundToken.SWITCH_OFF,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user the state of their device changed to unlocked SUCCESSFULLY */
    UNLOCK(
        HapticToken.POSITIVE_CONFIRMATION_LOW_EMPHASIS,
        SoundToken.UNLOCK,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user the state of their device changed to locked SUCCESSFULLY */
    LOCK(HapticToken.POSITIVE_CONFIRMATION_LOW_EMPHASIS, SoundToken.LOCK, FeedbackLevel.DEFAULT),
    /* Inform the user that their long-press gesture has resulted in the revealing of more contextual information */
    LONG_PRESS(HapticToken.LONG_PRESS, SoundToken.LONG_PRESS, FeedbackLevel.MINIMAL),
    /* Inform the user that their swipe gesture has reached a threshold that confirms navigation or the reveal of additional information. */
    SWIPE_THRESHOLD_INDICATOR(
        HapticToken.SWIPE_THRESHOLD_INDICATOR,
        SoundToken.SWIPE_THRESHOLD_INDICATOR,
        FeedbackLevel.MINIMAL,
    ),
    /* Played when the user taps on a high-emphasis UI element */
    TAP_HIGH_EMPHASIS(
        HapticToken.TAP_HIGH_EMPHASIS,
        SoundToken.TAP_HIGH_EMPHASIS,
        FeedbackLevel.EXPRESSIVE,
    ),
    /* Inform the user that their tap has resulted in a selection */
    TAP_MEDIUM_EMPHASIS(
        HapticToken.TAP_MEDIUM_EMPHASIS,
        SoundToken.TAP_MEDIUM_EMPHASIS,
        FeedbackLevel.DEFAULT,
    ),
    /* Played when a users drag gesture reaches the maximum or minimum value */
    DRAG_THRESHOLD_INDICATOR_LIMIT(
        HapticToken.DRAG_THRESHOLD_INDICATOR,
        SoundToken.DRAG_THRESHOLD_INDICATOR_LIMIT,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user that their drag gesture has resulted in an incremental value change.
     * For usage in haptic sliders that change continuously, this token can be played along with
     * [InteractionProperties.DynamicVibrationScale] properties to control haptic scaling as a
     * function of position and velocity.
     */
    DRAG_INDICATOR_CONTINUOUS(
        HapticToken.DRAG_INDICATOR_CONTINUOUS,
        SoundToken.NO_SOUND,
        FeedbackLevel.DEFAULT,
    ),
    /* Inform the user that their drag gesture has resulted in a stepped value change.
     * For usage in haptic sliders that change in discrete steps, this token can be played with
     * [InteractionProperties.DynamicVibrationScale] properties to control haptic scaling as a
     * function of position and velocity.
     */
    DRAG_INDICATOR_DISCRETE(
        HapticToken.DRAG_INDICATOR_DISCRETE,
        SoundToken.DRAG_INDICATOR,
        FeedbackLevel.DEFAULT,
    ),
    /* Played when a user taps on any UI element that can be interacted with but is not otherwise defined */
    TAP_LOW_EMPHASIS(
        HapticToken.TAP_LOW_EMPHASIS,
        SoundToken.TAP_LOW_EMPHASIS,
        FeedbackLevel.EXPRESSIVE,
    ),
    /* Played when the user touches a key on the keyboard that is otherwise undefined */
    KEYPRESS_STANDARD(
        HapticToken.KEYPRESS_STANDARD,
        SoundToken.KEYPRESS_STANDARD,
        FeedbackLevel.DEFAULT,
    ),
    /* Played when the user touches the space key */
    KEYPRESS_SPACEBAR(
        HapticToken.KEYPRESS_SPACEBAR,
        SoundToken.KEYPRESS_SPACEBAR,
        FeedbackLevel.DEFAULT,
    ),
    /* Played when the user touches the return key */
    KEYPRESS_RETURN(HapticToken.KEYPRESS_RETURN, SoundToken.KEYPRESS_RETURN, FeedbackLevel.DEFAULT),
    /* Played when the user touches the delete key */
    KEYPRESS_DELETE(HapticToken.KEYPRESS_DELETE, SoundToken.KEYPRESS_DELETE, FeedbackLevel.DEFAULT),
}

/** Level of feedback that contains a token */
enum class FeedbackLevel {
    NO_FEEDBACK,
    MINIMAL,
    DEFAULT,
    EXPRESSIVE,
}

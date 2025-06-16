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

import android.animation.Animator
import android.view.View

/** A static class for general animation-related utilities. */
class Animations {
    companion object {
        /** Stores a [view]'s ongoing [animation] so it can be cancelled if needed. */
        @JvmStatic
        fun setOngoingAnimation(view: View, animation: Animator?) {
            cancelOngoingAnimation(view)
            view.setTag(R.id.ongoing_animation, animation)
        }

        /**
         * Cancels the ongoing animation affecting a [view], if any was previously stored using
         * [setOngoingAnimation].
         */
        @JvmStatic
        fun cancelOngoingAnimation(view: View) {
            (view.getTag(R.id.ongoing_animation) as? Animator)?.cancel()
            view.setTag(R.id.ongoing_animation, null)
        }
    }
}

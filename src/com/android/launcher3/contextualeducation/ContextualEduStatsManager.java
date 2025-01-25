/*
 * Copyright 2024 The Android Open Source Project
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

package com.android.launcher3.contextualeducation;

import static com.android.launcher3.util.MainThreadInitializedObject.forOverride;

import com.android.launcher3.R;
import com.android.launcher3.util.MainThreadInitializedObject;
import com.android.launcher3.util.ResourceBasedOverride;
import com.android.launcher3.util.SafeCloseable;
import com.android.systemui.contextualeducation.GestureType;

/**
 * A class to update contextual education data. It is a no-op implementation and could be
 * overridden by changing the resource value [R.string.contextual_edu_manager_class] to provide
 * a real implementation.
 */
public class ContextualEduStatsManager implements ResourceBasedOverride, SafeCloseable {
    public static final MainThreadInitializedObject<ContextualEduStatsManager> INSTANCE =
            forOverride(ContextualEduStatsManager.class, R.string.contextual_edu_manager_class);

    /**
     * Updates contextual education stats when a gesture is triggered
     * @param isTrackpadGesture indicates if the gesture is triggered by trackpad
     * @param gestureType type of gesture triggered
     */
    public void updateEduStats(boolean isTrackpadGesture, GestureType gestureType) {
    }

    @Override
    public void close() {
    }
}

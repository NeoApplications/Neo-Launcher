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

package com.android.launcher3.icons.cache;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.icons.IconProvider;

/**
 * A simple interface to represent an object which can be added to icon cache
 */
public interface CachedObject {

    /**
     * Returns the component name for the underlying object
     */
    @NonNull ComponentName getComponent();

    /**
     * Returns the user for the underlying object
     */
    @NonNull UserHandle getUser();

    /**
     * Loads the user visible label for the provided object
     */
    @Nullable CharSequence getLabel();

    /**
     * Loads the user visible icon for the provided object
     */
    @Nullable
    default Drawable getFullResIcon(@NonNull BaseIconCache cache) {
        return null;
    }

    /**
     * @see CachingLogic#getApplicationInfo
     */
    @Nullable
    ApplicationInfo getApplicationInfo();

    /**
     * Returns a persistable string that can be used to indicate indicate the correctness of the
     * cache for the provided item
     */
    @Nullable
    default String getFreshnessIdentifier(@NonNull IconProvider iconProvider) {
        return iconProvider.getStateForApp(getApplicationInfo());
    }
}

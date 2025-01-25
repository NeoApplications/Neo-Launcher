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
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A simple interface to represent an object which can be added to icon cache
 *
 * @param <T> Any subclass of the icon cache with which this object is associated
 */
public interface CachedObject<T extends BaseIconCache> {

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
    @Nullable CharSequence getLabel(PackageManager pm);

    /**
     * Loads the user visible icon for the provided object
     */
    @Nullable
    default Drawable getFullResIcon(@NonNull T cache) {
        return null;
    }
}

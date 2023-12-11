/**
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.launcher3.util;

/**
 * Utility interface for representing flag operations
 */
public interface FlagOp {

    FlagOp NO_OP = i -> i;

    int apply(int flags);

    /**
     * Returns a new OP which adds the provided flag after applying all previous operations
     */
    default FlagOp addFlag(int flag) {
        return i -> apply(i) | flag;
    }

    /**
     * Returns a new OP which removes the provided flag after applying all previous operations
     */
    default FlagOp removeFlag(int flag) {
        return i -> apply(i) & ~flag;
    }

    /**
     * Returns a new OP which adds or removed the provided flag based on {@code enable} after
     * applying all previous operations
     */
    default FlagOp setFlag(int flag, boolean enable) {
        return enable ? addFlag(flag) : removeFlag(flag);
    }
}

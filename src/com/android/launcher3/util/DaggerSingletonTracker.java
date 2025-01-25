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

package com.android.launcher3.util;

import com.android.launcher3.dagger.LauncherAppSingleton;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * A tracker class for keeping track of Dagger created singletons.
 * Dagger will take care of creating singletons. But we should take care of unregistering callbacks
 * if at all registered during singleton construction.
 * All singletons should be declared as SafeCloseable so that we can call close() method.
 */
@LauncherAppSingleton
public class DaggerSingletonTracker implements SafeCloseable {

    private final ArrayList<SafeCloseable> mLauncherAppSingletons = new ArrayList<>();

    @Inject
    DaggerSingletonTracker() {
    }

    /**
     * Adds the SafeCloseable Singletons to the mLauncherAppSingletons list.
     * This helps to track the singletons and close them appropriately.
     * See {@link DaggerSingletonTracker#close()} and
     * {@link MainThreadInitializedObject.SandboxContext#onDestroy()}
     */
    public void addCloseable(SafeCloseable closeable) {
        mLauncherAppSingletons.add(closeable);
    }

    @Override
    public void close() {
        // Destroy in reverse order
        for (int i = mLauncherAppSingletons.size() - 1; i >= 0; i--) {
            mLauncherAppSingletons.get(i).close();
        }
    }
}

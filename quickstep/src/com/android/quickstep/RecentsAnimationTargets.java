/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.quickstep;

import static android.app.WindowConfiguration.WINDOWING_MODE_FREEFORM;
import static android.view.RemoteAnimationTarget.MODE_CLOSING;

import android.app.WindowConfiguration;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.RemoteAnimationTarget;

import com.android.wm.shell.shared.desktopmode.DesktopModeStatus;

import java.io.PrintWriter;

/**
 * Extension of {@link RemoteAnimationTargets} with additional information about swipe
 * up animation
 */
public class RecentsAnimationTargets extends RemoteAnimationTargets {

    public final Rect homeContentInsets;
    public final Rect minimizedHomeBounds;

    public RecentsAnimationTargets(RemoteAnimationTarget[] apps,
            RemoteAnimationTarget[] wallpapers, RemoteAnimationTarget[] nonApps,
            Rect homeContentInsets, Rect minimizedHomeBounds, Bundle extras) {
        super(apps, wallpapers, nonApps, MODE_CLOSING, extras);
        this.homeContentInsets = homeContentInsets;
        this.minimizedHomeBounds = minimizedHomeBounds;
    }

    public boolean hasTargets() {
        return unfilteredApps.length != 0;
    }

    /**
     * Check if target apps contain desktop tasks which have windowing mode set to {@link
     * WindowConfiguration#WINDOWING_MODE_FREEFORM}
     *
     * @return {@code true} if at least one target app is a desktop task
     */
    public boolean hasDesktopTasks(Context context) {
        if (!DesktopModeStatus.canEnterDesktopMode(context)) {
            return false;
        }
        // TODO: b/400866688 - Check if we need to update this such that for an empty desk, we
        //  receive a list of apps that contain only the Launcher and the `DesktopWallpaperActivity`
        //  and both are fullscreen windowing mode. A desk can also have transparent modals and
        //  immersive apps which may not have a "freeform" windowing mode.
        for (RemoteAnimationTarget target : apps) {
            if (target.windowConfiguration.getWindowingMode() == WINDOWING_MODE_FREEFORM) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dump(String prefix, PrintWriter pw) {
        super.dump(prefix, pw);
        prefix += '\t';
        pw.println(prefix + "RecentsAnimationTargets:");

        pw.println(prefix + "\thomeContentInsets=" + homeContentInsets);
        pw.println(prefix + "\tminimizedHomeBounds=" + minimizedHomeBounds);
    }
}

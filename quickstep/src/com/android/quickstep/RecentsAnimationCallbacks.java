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

import static android.app.WindowConfiguration.ACTIVITY_TYPE_HOME;
import static android.view.RemoteAnimationTarget.MODE_CLOSING;
import static android.view.RemoteAnimationTarget.MODE_OPENING;
import static android.view.WindowManager.LayoutParams.TYPE_DOCK_DIVIDER;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.wm.shell.shared.TransitionUtil.TYPE_SPLIT_SCREEN_DIM_LAYER;

import android.annotation.Nullable;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.RemoteAnimationTarget;
import android.window.TransitionInfo;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.Preconditions;
import com.android.quickstep.fallback.window.RecentsWindowFlags;
import com.android.quickstep.util.ActiveGestureProtoLogProxy;
import com.android.systemui.shared.recents.model.ThumbnailData;
import com.android.systemui.shared.system.RecentsAnimationControllerCompat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Wrapper around {@link com.android.systemui.shared.system.RecentsAnimationListener} which
 * delegates callbacks to multiple listeners on the main thread
 */
public class RecentsAnimationCallbacks implements
        com.android.systemui.shared.system.RecentsAnimationListener {

    private final Set<RecentsAnimationListener> mListeners = new ArraySet<>();
    private final SystemUiProxy mSystemUiProxy;

    // TODO(141886704): Remove these references when they are no longer needed
    private RecentsAnimationController mController;

    private boolean mCancelled;

    public RecentsAnimationCallbacks(SystemUiProxy systemUiProxy) {
        mSystemUiProxy = systemUiProxy;
    }

    @UiThread
    public void addListener(RecentsAnimationListener listener) {
        Preconditions.assertUIThread();
        mListeners.add(listener);
    }

    @UiThread
    public void removeListener(RecentsAnimationListener listener) {
        Preconditions.assertUIThread();
        mListeners.remove(listener);
    }

    @UiThread
    public void removeAllListeners() {
        Preconditions.assertUIThread();
        mListeners.clear();
    }

    public void notifyAnimationCanceled() {
        mCancelled = true;
        onAnimationCanceled(new HashMap<>());
    }

    // Called only in Q platform
    @BinderThread
    @Deprecated
    public final void onAnimationStart(RecentsAnimationControllerCompat controller,
            RemoteAnimationTarget[] appTargets, Rect homeContentInsets,
            Rect minimizedHomeBounds, Bundle extras) {
        onAnimationStart(controller, appTargets, new RemoteAnimationTarget[0],
                homeContentInsets, minimizedHomeBounds, extras, /* transitionInfo= */ null);
    }

    // Called only in R+ platform
    @BinderThread
    public final void onAnimationStart(RecentsAnimationControllerCompat animationController,
            RemoteAnimationTarget[] appTargets,
            RemoteAnimationTarget[] wallpaperTargets,
            Rect homeContentInsets, Rect minimizedHomeBounds, Bundle extras,
            @Nullable TransitionInfo transitionInfo) {
        long appCount = Arrays.stream(appTargets)
                .filter(app -> app.mode == MODE_CLOSING)
                .count();

        boolean isOpeningHome = Arrays.stream(appTargets).filter(app -> app.mode == MODE_OPENING
                        && app.windowConfiguration.getActivityType() == ACTIVITY_TYPE_HOME)
                .count() > 0;
        if (appCount == 0 && (!RecentsWindowFlags.Companion.getEnableOverviewInWindow()
                || isOpeningHome)) {
            ActiveGestureProtoLogProxy.logOnRecentsAnimationStartCancelled();
            // Edge case, if there are no closing app targets, then Launcher has nothing to handle
            notifyAnimationCanceled();
            animationController.finish(false /* toHome */, false /* sendUserLeaveHint */,
                    null /* finishCb */);
            return;
        }

        mController = new RecentsAnimationController(animationController,
                this::onAnimationFinished);
        if (mCancelled) {
            Utilities.postAsyncCallback(MAIN_EXECUTOR.getHandler(),
                    mController::finishAnimationToApp);
        } else {
            RemoteAnimationTarget[] nonAppTargets;
            final ArrayList<RemoteAnimationTarget> apps = new ArrayList<>();
            final ArrayList<RemoteAnimationTarget> nonApps = new ArrayList<>();
            classifyTargets(appTargets, apps, nonApps);
            appTargets = apps.toArray(new RemoteAnimationTarget[apps.size()]);
            nonAppTargets = nonApps.toArray(new RemoteAnimationTarget[nonApps.size()]);
            if (nonAppTargets == null) {
                nonAppTargets = new RemoteAnimationTarget[0];
            }
            final RecentsAnimationTargets targets = new RecentsAnimationTargets(appTargets,
                    wallpaperTargets, nonAppTargets, homeContentInsets, minimizedHomeBounds,
                    extras);

            Utilities.postAsyncCallback(MAIN_EXECUTOR.getHandler(), () -> {
                ActiveGestureProtoLogProxy.logOnRecentsAnimationStart(targets.apps.length);
                for (RecentsAnimationListener listener : getListeners()) {
                    listener.onRecentsAnimationStart(mController, targets, transitionInfo);
                }
            });
        }
    }

    @BinderThread
    @Override
    public final void onAnimationCanceled(HashMap<Integer, ThumbnailData> thumbnailDatas) {
        Utilities.postAsyncCallback(MAIN_EXECUTOR.getHandler(), () -> {
            ActiveGestureProtoLogProxy.logRecentsAnimationCallbacksOnAnimationCancelled();
            for (RecentsAnimationListener listener : getListeners()) {
                listener.onRecentsAnimationCanceled(thumbnailDatas);
            }
        });
    }

    @BinderThread
    @Override
    public void onTasksAppeared(
            RemoteAnimationTarget[] apps, @Nullable TransitionInfo transitionInfo) {
        Utilities.postAsyncCallback(MAIN_EXECUTOR.getHandler(), () -> {
            ActiveGestureProtoLogProxy.logRecentsAnimationCallbacksOnTasksAppeared();
            for (RecentsAnimationListener listener : getListeners()) {
                listener.onTasksAppeared(apps, transitionInfo);
            }
        });
    }

    private void onAnimationFinished(RecentsAnimationController controller) {
        Utilities.postAsyncCallback(MAIN_EXECUTOR.getHandler(), () -> {
            ActiveGestureProtoLogProxy.logAbsSwipeUpHandlerOnRecentsAnimationFinished();
            for (RecentsAnimationListener listener : getListeners()) {
                listener.onRecentsAnimationFinished(controller);
            }
        });
    }

    private RecentsAnimationListener[] getListeners() {
        return mListeners.toArray(new RecentsAnimationListener[mListeners.size()]);
    }

    private void classifyTargets(RemoteAnimationTarget[] appTargets,
            ArrayList<RemoteAnimationTarget> apps, ArrayList<RemoteAnimationTarget> nonApps) {
        for (int i = 0; i < appTargets.length; i++) {
            RemoteAnimationTarget target = appTargets[i];
            if (target.windowType == TYPE_DOCK_DIVIDER
                    || target.windowType == TYPE_SPLIT_SCREEN_DIM_LAYER) {
                nonApps.add(target);
            } else {
                apps.add(target);
            }
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "RecentsAnimationCallbacks:");

        pw.println(prefix + "\tmCancelled=" + mCancelled);
    }

    /**
     * Listener for the recents animation callbacks.
     */
    public interface RecentsAnimationListener {
        default void onRecentsAnimationStart(RecentsAnimationController controller,
                RecentsAnimationTargets targets, @Nullable TransitionInfo transitionInfo) {}

        /**
         * Callback from the system when the recents animation is canceled. {@param thumbnailData}
         * is passed back for rendering screenshot to replace live tile.
         */
        default void onRecentsAnimationCanceled(
                @NonNull HashMap<Integer, ThumbnailData> thumbnailDatas) {}

        /**
         * Callback made whenever the recents animation is finished.
         */
        default void onRecentsAnimationFinished(@NonNull RecentsAnimationController controller) {}

        /**
         * Callback made when a task started from the recents is ready for an app transition.
         */
        default void onTasksAppeared(@NonNull RemoteAnimationTarget[] appearedTaskTarget,
                @Nullable TransitionInfo transitionInfo) {}
    }
}

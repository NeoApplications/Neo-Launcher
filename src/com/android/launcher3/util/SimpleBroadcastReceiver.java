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
package com.android.launcher3.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PatternMatcher;
import android.text.TextUtils;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

public class SimpleBroadcastReceiver extends BroadcastReceiver {

    private final Consumer<Intent> mIntentConsumer;

    // Handler to register/unregister broadcast receiver
    private final Handler mHandler;

    public SimpleBroadcastReceiver(LooperExecutor looperExecutor, Consumer<Intent> intentConsumer) {
        this(looperExecutor.getHandler(), intentConsumer);
    }

    public SimpleBroadcastReceiver(Handler handler, Consumer<Intent> intentConsumer) {
        mIntentConsumer = intentConsumer;
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntentConsumer.accept(intent);
    }

    /** Calls {@link #register(Context, Runnable, String...)} with null completionCallback. */
    @AnyThread
    public void register(Context context, String... actions) {
        register(context, null, actions);
    }

    /**
     * Calls {@link #register(Context, Runnable, int, String...)} with null completionCallback.
     */
    @AnyThread
    public void register(Context context, int flags, String... actions) {
        register(context, null, flags, actions);
    }

    /**
     * Register broadcast receiver. If this method is called on the same looper with mHandler's
     * looper, then register will be called synchronously. Otherwise asynchronously. This ensures
     * register happens on {@link #mHandler}'s looper.
     *
     * @param completionCallback callback that will be triggered after registration is completed,
     *                           caller usually pass this callback to check if states has changed
     *                           while registerReceiver() is executed on a binder call.
     */
    @AnyThread
    public void register(
            Context context, @Nullable Runnable completionCallback, String... actions) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            registerInternal(context, completionCallback, actions);
        } else {
            mHandler.post(() -> registerInternal(context, completionCallback, actions));
        }
    }

    /** Register broadcast receiver and run completion callback if passed. */
    @AnyThread
    private void registerInternal(
            Context context, @Nullable Runnable completionCallback, String... actions) {
        context.registerReceiver(this, getFilter(actions));
        if (completionCallback != null) {
            completionCallback.run();
        }
    }

    /**
     * Same as {@link #register(Context, Runnable, String...)} above but with additional flags
     * params.
     */
    @AnyThread
    public void register(
            Context context, @Nullable Runnable completionCallback, int flags, String... actions) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            registerInternal(context, completionCallback, flags, actions);
        } else {
            mHandler.post(() -> registerInternal(context, completionCallback, flags, actions));
        }
    }

    /** Register broadcast receiver and run completion callback if passed. */
    @AnyThread
    private void registerInternal(
            Context context, @Nullable Runnable completionCallback, int flags, String... actions) {
        context.registerReceiver(this, getFilter(actions), flags);
        if (completionCallback != null) {
            completionCallback.run();
        }
    }

    /** Same as {@link #register(Context, Runnable, String...)} above but with pkg name. */
    @AnyThread
    public void registerPkgActions(Context context, @Nullable String pkg, String... actions) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            context.registerReceiver(this, getPackageFilter(pkg, actions));
        } else {
            mHandler.post(() -> {
                context.registerReceiver(this, getPackageFilter(pkg, actions));
            });
        }
    }

    /**
     * Unregister broadcast receiver. If this method is called on the same looper with mHandler's
     * looper, then unregister will be called synchronously. Otherwise asynchronously. This ensures
     * unregister happens on {@link #mHandler}'s looper.
     */
    @AnyThread
    public void unregisterReceiverSafely(Context context) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            unregisterReceiverSafelyInternal(context);
        } else {
            mHandler.post(() -> {
                unregisterReceiverSafelyInternal(context);
            });
        }
    }

    /** Unregister broadcast receiver ignoring any errors. */
    @AnyThread
    private void unregisterReceiverSafelyInternal(Context context) {
        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            // It was probably never registered or already unregistered. Ignore.
        }
    }

    /**
     * Creates an intent filter to listen for actions with a specific package in the data field.
     */
    public static IntentFilter getPackageFilter(String pkg, String... actions) {
        IntentFilter filter = getFilter(actions);
        filter.addDataScheme("package");
        if (!TextUtils.isEmpty(pkg)) {
            filter.addDataSchemeSpecificPart(pkg, PatternMatcher.PATTERN_LITERAL);
        }
        return filter;
    }

    private static IntentFilter getFilter(String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String action : actions) {
            filter.addAction(action);
        }
        return filter;
    }
}

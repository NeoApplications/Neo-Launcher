/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static android.os.VibrationEffect.Composition.PRIMITIVE_LOW_TICK;
import static android.os.VibrationEffect.createPredefined;
import static android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED;

import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.annotation.VisibleForTesting;

/**
 * Wrapper around {@link Vibrator} to easily perform haptic feedback where necessary.
 */
public class VibratorWrapper implements SafeCloseable {

    public static final MainThreadInitializedObject<VibratorWrapper> INSTANCE =
            new MainThreadInitializedObject<>(VibratorWrapper::new);

    public static final AudioAttributes VIBRATION_ATTRS = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    public static final VibrationEffect EFFECT_CLICK =
            createPredefined(VibrationEffect.EFFECT_CLICK);
    @VisibleForTesting
    static final Uri HAPTIC_FEEDBACK_URI = Settings.System.getUriFor(HAPTIC_FEEDBACK_ENABLED);

    @VisibleForTesting static final float LOW_TICK_SCALE = 0.9f;

    /**
     * Haptic when entering overview.
     */
    public static final VibrationEffect OVERVIEW_HAPTIC = EFFECT_CLICK;

    private final Vibrator mVibrator;
    private final boolean mHasVibrator;

    private final SettingsCache mSettingsCache;

    @VisibleForTesting
    final SettingsCache.OnChangeListener mHapticChangeListener =
            isEnabled -> mIsHapticFeedbackEnabled = isEnabled;

    private boolean mIsHapticFeedbackEnabled;

    private VibratorWrapper(Context context) {
        this(context.getSystemService(Vibrator.class), SettingsCache.INSTANCE.get(context));
    }

    @VisibleForTesting
    VibratorWrapper(Vibrator vibrator, SettingsCache settingsCache) {
        mVibrator = vibrator;
        mHasVibrator = mVibrator.hasVibrator();
        mSettingsCache = settingsCache;
        if (mHasVibrator) {
            mSettingsCache.register(HAPTIC_FEEDBACK_URI, mHapticChangeListener);
            mIsHapticFeedbackEnabled = mSettingsCache.getValue(HAPTIC_FEEDBACK_URI, 0);
        } else {
            mIsHapticFeedbackEnabled = false;
        }
    }

    @Override
    public void close() {
        if (mHasVibrator) {
            mSettingsCache.unregister(HAPTIC_FEEDBACK_URI, mHapticChangeListener);
        }
    }

    /**
     * This should be used to cancel a haptic in case where the haptic shouldn't be vibrating. For
     * example, when no animation is happening but a vibrator happens to be vibrating still.
     */
    public void cancelVibrate() {
        UI_HELPER_EXECUTOR.execute(mVibrator::cancel);
    }

    /** Vibrates with the given effect if haptic feedback is available and enabled. */
    public void vibrate(VibrationEffect vibrationEffect) {
        if (mHasVibrator && mIsHapticFeedbackEnabled) {
            UI_HELPER_EXECUTOR.execute(() -> mVibrator.vibrate(vibrationEffect, VIBRATION_ATTRS));
        }
    }

    /**
     * Vibrates with a single primitive, if supported, or use a fallback effect instead. This only
     * vibrates if haptic feedback is available and enabled.
     */
    @SuppressLint("NewApi")
    public void vibrate(int primitiveId, float primitiveScale, VibrationEffect fallbackEffect) {
        if (mHasVibrator && mIsHapticFeedbackEnabled) {
            UI_HELPER_EXECUTOR.execute(() -> {
                if (primitiveId >= 0 && mVibrator.areAllPrimitivesSupported(primitiveId)) {
                    mVibrator.vibrate(VibrationEffect.startComposition()
                            .addPrimitive(primitiveId, primitiveScale)
                            .compose(), VIBRATION_ATTRS);
                } else {
                    mVibrator.vibrate(fallbackEffect, VIBRATION_ATTRS);
                }
            });
        }
    }

    /** Indicates that Taskbar has been invoked. */
    public void vibrateForTaskbarUnstash() {
        if (mVibrator.areAllPrimitivesSupported(PRIMITIVE_LOW_TICK)) {
            VibrationEffect primitiveLowTickEffect = VibrationEffect
                    .startComposition()
                    .addPrimitive(PRIMITIVE_LOW_TICK, LOW_TICK_SCALE)
                    .compose();

            vibrate(primitiveLowTickEffect);
        }
    }
}

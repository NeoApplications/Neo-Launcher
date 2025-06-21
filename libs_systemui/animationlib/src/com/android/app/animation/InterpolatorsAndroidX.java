/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.app.animation;

import android.graphics.Path;

import androidx.core.animation.AccelerateDecelerateInterpolator;
import androidx.core.animation.AccelerateInterpolator;
import androidx.core.animation.BounceInterpolator;
import androidx.core.animation.DecelerateInterpolator;
import androidx.core.animation.Interpolator;
import androidx.core.animation.LinearInterpolator;
import androidx.core.animation.OvershootInterpolator;
import androidx.core.animation.PathInterpolator;

/**
 * Utility class to receive interpolators from. (androidx compatible version)
 *
 * This is the androidx compatible version of {@link Interpolators}. Make sure that changes made to
 * this class are also reflected in {@link Interpolators}.
 *
 * Using the androidx versions of {@link androidx.core.animation.ValueAnimator} or
 * {@link androidx.core.animation.ObjectAnimator} improves animation testability. This file provides
 * the androidx compatible versions of the interpolators defined in {@link Interpolators}.
 * AnimatorTestRule can be used in Tests to manipulate the animation under test (e.g. artificially
 * advancing the time).
 */
public class InterpolatorsAndroidX {

    /*
     * ============================================================================================
     * Emphasized interpolators.
     * ============================================================================================
     */

    /**
     * The default emphasized interpolator. Used for hero / emphasized movement of content.
     */
    public static final Interpolator EMPHASIZED = createEmphasizedInterpolator();

    /**
     * Complement to {@link #EMPHASIZED}. Used when animating hero movement in two dimensions to
     * create a smooth, emphasized, curved movement.
     * <br>
     * Example usage: Animate y-movement with {@link #EMPHASIZED} and x-movement with this.
     */
    public static final Interpolator EMPHASIZED_COMPLEMENT = createEmphasizedComplement();

    /**
     * The accelerated emphasized interpolator. Used for hero / emphasized movement of content that
     * is disappearing e.g. when moving off screen.
     */
    public static final Interpolator EMPHASIZED_ACCELERATE = new PathInterpolator(
            0.3f, 0f, 0.8f, 0.15f);

    /**
     * The decelerating emphasized interpolator. Used for hero / emphasized movement of content that
     * is appearing e.g. when coming from off screen
     */
    public static final Interpolator EMPHASIZED_DECELERATE = new PathInterpolator(
            0.05f, 0.7f, 0.1f, 1f);

    public static final Interpolator EXAGGERATED_EASE;
    static {
        Path exaggeratedEase = new Path();
        exaggeratedEase.moveTo(0, 0);
        exaggeratedEase.cubicTo(0.05f, 0f, 0.133333f, 0.08f, 0.166666f, 0.4f);
        exaggeratedEase.cubicTo(0.225f, 0.94f, 0.5f, 1f, 1f, 1f);
        EXAGGERATED_EASE = new PathInterpolator(exaggeratedEase);
    }

    public static final Interpolator INSTANT = t -> 1;
    /**
     * All values of t map to 0 until t == 1. This is primarily useful for setting view visibility,
     * which should only happen at the very end of the animation (when it's already hidden).
     */
    public static final Interpolator FINAL_FRAME = t -> t < 1 ? 0 : 1;

    public static final Interpolator OVERSHOOT_0_75 = new OvershootInterpolator(0.75f);
    public static final Interpolator OVERSHOOT_1_2 = new OvershootInterpolator(1.2f);
    public static final Interpolator OVERSHOOT_1_7 = new OvershootInterpolator(1.7f);

    /*
     * ============================================================================================
     * Standard interpolators.
     * ============================================================================================
     */

    /**
     * The standard interpolator that should be used on every normal animation
     */
    public static final Interpolator STANDARD = new PathInterpolator(
            0.2f, 0f, 0f, 1f);

    /**
     * The standard accelerating interpolator that should be used on every regular movement of
     * content that is disappearing e.g. when moving off screen.
     */
    public static final Interpolator STANDARD_ACCELERATE = new PathInterpolator(
            0.3f, 0f, 1f, 1f);

    /**
     * The standard decelerating interpolator that should be used on every regular movement of
     * content that is appearing e.g. when coming from off screen.
     */
    public static final Interpolator STANDARD_DECELERATE = new PathInterpolator(
            0f, 0f, 0f, 1f);

    /*
     * ============================================================================================
     * Legacy
     * ============================================================================================
     */

    /**
     * The default legacy interpolator as defined in Material 1. Also known as FAST_OUT_SLOW_IN.
     */
    public static final Interpolator LEGACY = new PathInterpolator(0.4f, 0f, 0.2f, 1f);

    /**
     * The default legacy accelerating interpolator as defined in Material 1.
     * Also known as FAST_OUT_LINEAR_IN.
     */
    public static final Interpolator LEGACY_ACCELERATE = new PathInterpolator(0.4f, 0f, 1f, 1f);

    /**
     * The default legacy decelerating interpolator as defined in Material 1.
     * Also known as LINEAR_OUT_SLOW_IN.
     */
    public static final Interpolator LEGACY_DECELERATE = new PathInterpolator(0f, 0f, 0.2f, 1f);

    /**
     * Linear interpolator. Often used if the interpolator is for different properties who need
     * different interpolations.
     */
    public static final Interpolator LINEAR = new LinearInterpolator();

    /*
     * ============================================================================================
     * Custom interpolators
     * ============================================================================================
     */

    public static final Interpolator FAST_OUT_SLOW_IN = LEGACY;
    public static final Interpolator FAST_OUT_LINEAR_IN = LEGACY_ACCELERATE;
    public static final Interpolator LINEAR_OUT_SLOW_IN = LEGACY_DECELERATE;

    /**
     * Like {@link #FAST_OUT_SLOW_IN}, but used in case the animation is played in reverse (i.e. t
     * goes from 1 to 0 instead of 0 to 1).
     */
    public static final Interpolator FAST_OUT_SLOW_IN_REVERSE =
            new PathInterpolator(0.8f, 0f, 0.6f, 1f);
    public static final Interpolator SLOW_OUT_LINEAR_IN = new PathInterpolator(0.8f, 0f, 1f, 1f);
    public static final Interpolator AGGRESSIVE_EASE = new PathInterpolator(0.2f, 0f, 0f, 1f);
    public static final Interpolator AGGRESSIVE_EASE_IN_OUT = new PathInterpolator(0.6f,0, 0.4f, 1);

    public static final Interpolator DECELERATED_EASE = new PathInterpolator(0, 0, .2f, 1f);
    public static final Interpolator ACCELERATED_EASE = new PathInterpolator(0.4f, 0, 1f, 1f);
    public static final Interpolator ALPHA_IN = new PathInterpolator(0.4f, 0f, 1f, 1f);
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0f, 0f, 0.8f, 1f);
    public static final Interpolator ACCELERATE = new AccelerateInterpolator();
    public static final Interpolator ACCELERATE_0_5 = new AccelerateInterpolator(0.5f);
    public static final Interpolator ACCELERATE_0_75 = new AccelerateInterpolator(0.75f);
    public static final Interpolator ACCELERATE_1_5 = new AccelerateInterpolator(1.5f);
    public static final Interpolator ACCELERATE_2 = new AccelerateInterpolator(2);
    public static final Interpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    public static final Interpolator DECELERATE = new DecelerateInterpolator();
    public static final Interpolator DECELERATE_1_5 = new DecelerateInterpolator(1.5f);
    public static final Interpolator DECELERATE_1_7 = new DecelerateInterpolator(1.7f);
    public static final Interpolator DECELERATE_2 = new DecelerateInterpolator(2);
    public static final Interpolator DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
    public static final Interpolator DECELERATE_3 = new DecelerateInterpolator(3f);
    public static final Interpolator CUSTOM_40_40 = new PathInterpolator(0.4f, 0f, 0.6f, 1f);
    public static final Interpolator ICON_OVERSHOT = new PathInterpolator(0.4f, 0f, 0.2f, 1.4f);
    public static final Interpolator ICON_OVERSHOT_LESS = new PathInterpolator(0.4f, 0f, 0.2f,
            1.1f);
    public static final Interpolator PANEL_CLOSE_ACCELERATED = new PathInterpolator(0.3f, 0, 0.5f,
            1);
    public static final Interpolator BOUNCE = new BounceInterpolator();
    /**
     * For state transitions on the control panel that lives in GlobalActions.
     */
    public static final Interpolator CONTROL_STATE = new PathInterpolator(0.4f, 0f, 0.2f,
            1.0f);

    /**
     * Interpolator to be used when animating a move based on a click. Pair with enough duration.
     */
    public static final Interpolator TOUCH_RESPONSE =
            new PathInterpolator(0.3f, 0f, 0.1f, 1f);

    /**
     * Like {@link #TOUCH_RESPONSE}, but used in case the animation is played in reverse (i.e. t
     * goes from 1 to 0 instead of 0 to 1).
     */
    public static final Interpolator TOUCH_RESPONSE_REVERSE =
            new PathInterpolator(0.9f, 0f, 0.7f, 1f);

    public static final Interpolator TOUCH_RESPONSE_ACCEL_DEACCEL =
            v -> ACCELERATE_DECELERATE.getInterpolation(TOUCH_RESPONSE.getInterpolation(v));


    /**
     * Inversion of ZOOM_OUT, compounded with an ease-out.
     */
    public static final Interpolator ZOOM_IN = new Interpolator() {
        @Override
        public float getInterpolation(float v) {
            return DECELERATE_3.getInterpolation(1 - ZOOM_OUT.getInterpolation(1 - v));
        }
    };

    public static final Interpolator ZOOM_OUT = new Interpolator() {

        private static final float FOCAL_LENGTH = 0.35f;

        @Override
        public float getInterpolation(float v) {
            return zInterpolate(v);
        }

        /**
         * This interpolator emulates the rate at which the perceived scale of an object changes
         * as its distance from a camera increases. When this interpolator is applied to a scale
         * animation on a view, it evokes the sense that the object is shrinking due to moving away
         * from the camera.
         */
        private float zInterpolate(float input) {
            return (1.0f - FOCAL_LENGTH / (FOCAL_LENGTH + input)) /
                    (1.0f - FOCAL_LENGTH / (FOCAL_LENGTH + 1.0f));
        }
    };

    public static final Interpolator SCROLL = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1;
        }
    };

    public static final Interpolator SCROLL_CUBIC = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t + 1;
        }
    };

    /**
     * Use this interpolator for animating progress values coming from the back callback to get
     * the predictive-back-typical decelerate motion.
     *
     * This interpolator is similar to {@link Interpolators#STANDARD_DECELERATE} but has a slight
     * acceleration phase at the start.
     */
    public static final Interpolator BACK_GESTURE = new PathInterpolator(0.1f, 0.1f, 0f, 1f);

    private static final float FAST_FLING_PX_MS = 10;

    /*
     * ============================================================================================
     * Functions / Utilities
     * ============================================================================================
     */

    public static Interpolator scrollInterpolatorForVelocity(float velocity) {
        return Math.abs(velocity) > FAST_FLING_PX_MS ? SCROLL : SCROLL_CUBIC;
    }

    /**
     * Create an OvershootInterpolator with tension directly related to the velocity (in px/ms).
     * @param velocity The start velocity of the animation we want to overshoot.
     */
    public static Interpolator overshootInterpolatorForVelocity(float velocity) {
        return new OvershootInterpolator(Math.min(Math.abs(velocity), 3f));
    }

    /**
     * Calculate the amount of overshoot using an exponential falloff function with desired
     * properties, where the overshoot smoothly transitions at the 1.0f boundary into the
     * overshoot, retaining its acceleration.
     *
     * @param progress a progress value going from 0 to 1
     * @param overshootAmount the amount > 0 of overshoot desired. A value of 0.1 means the max
     *                        value of the overall progress will be at 1.1.
     * @param overshootStart the point in (0,1] where the result should reach 1
     * @return the interpolated overshoot
     */
    public static float getOvershootInterpolation(float progress, float overshootAmount,
            float overshootStart) {
        if (overshootAmount == 0.0f || overshootStart == 0.0f) {
            throw new IllegalArgumentException("Invalid values for overshoot");
        }
        float b = MathUtils.log((overshootAmount + 1) / (overshootAmount)) / overshootStart;
        return MathUtils.max(0.0f,
                (float) (1.0f - Math.exp(-b * progress)) * (overshootAmount + 1.0f));
    }

    /**
     * Similar to {@link #getOvershootInterpolation(float, float, float)} but the overshoot
     * starts immediately here, instead of first having a section of non-overshooting
     *
     * @param progress a progress value going from 0 to 1
     */
    public static float getOvershootInterpolation(float progress) {
        return MathUtils.max(0.0f, (float) (1.0f - Math.exp(-4 * progress)));
    }

    // Create the default emphasized interpolator
    private static PathInterpolator createEmphasizedInterpolator() {
        Path path = new Path();
        // Doing the same as fast_out_extra_slow_in
        path.moveTo(0f, 0f);
        path.cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f);
        path.cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f);
        return new PathInterpolator(path);
    }

    /**
     * Creates a complement to {@link #createEmphasizedInterpolator()} for use when animating in
     * two dimensions.
     */
    private static PathInterpolator createEmphasizedComplement() {
        Path path = new Path();
        path.moveTo(0f, 0f);
        path.cubicTo(0.1217f, 0.0462f, 0.15f, 0.4686f, 0.1667f, 0.66f);
        path.cubicTo(0.1834f, 0.8878f, 0.1667f, 1f, 1f, 1f);
        return new PathInterpolator(path);
    }

    /**
     * Returns a function that runs the given interpolator such that the entire progress is set
     * between the given bounds. That is, we set the interpolation to 0 until lowerBound and reach
     * 1 by upperBound.
     */
    public static Interpolator clampToProgress(Interpolator interpolator, float lowerBound,
            float upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException(
                    String.format("upperBound (%f) must be greater than lowerBound (%f)",
                            upperBound, lowerBound));
        }
        return t -> clampToProgress(interpolator, t, lowerBound, upperBound);
    }

    /**
     * Returns the progress value's progress between the lower and upper bounds. That is, the
     * progress will be 0f from 0f to lowerBound, and reach 1f by upperBound.
     *
     * Between lowerBound and upperBound, the progress value will be interpolated using the provided
     * interpolator.
     */
    public static float clampToProgress(
            Interpolator interpolator, float progress, float lowerBound, float upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException(
                    String.format("upperBound (%f) must be greater than lowerBound (%f)",
                            upperBound, lowerBound));
        }

        if (progress == lowerBound && progress == upperBound) {
            return progress == 0f ? 0 : 1;
        }
        if (progress < lowerBound) {
            return 0;
        }
        if (progress > upperBound) {
            return 1;
        }
        return interpolator.getInterpolation((progress - lowerBound) / (upperBound - lowerBound));
    }

    /**
     * Returns the progress value's progress between the lower and upper bounds. That is, the
     * progress will be 0f from 0f to lowerBound, and reach 1f by upperBound.
     */
    public static float clampToProgress(float progress, float lowerBound, float upperBound) {
        return clampToProgress(LINEAR, progress, lowerBound, upperBound);
    }

    private static float mapRange(float value, float min, float max) {
        return min + (value * (max - min));
    }

    /**
     * Runs the given interpolator such that the interpolated value is mapped to the given range.
     * This is useful, for example, if we only use this interpolator for part of the animation,
     * such as to take over a user-controlled animation when they let go.
     */
    public static Interpolator mapToProgress(Interpolator interpolator, float lowerBound,
            float upperBound) {
        return t -> mapRange(interpolator.getInterpolation(t), lowerBound, upperBound);
    }

    /**
     * Returns the reverse of the provided interpolator, following the formula: g(x) = 1 - f(1 - x).
     * In practice, this means that if f is an interpolator used to model a value animating between
     * m and n, g is the interpolator to use to obtain the specular behavior when animating from n
     * to m.
     */
    public static Interpolator reverse(Interpolator interpolator) {
        return t -> 1 - interpolator.getInterpolation(1 - t);
    }
}
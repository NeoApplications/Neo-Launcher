/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3.icons;

import static com.android.launcher3.icons.BaseIconFactory.getBadgeSizeForIconSize;
import static com.android.launcher3.icons.BitmapInfo.FLAG_NO_BADGE;
import static com.android.launcher3.icons.BitmapInfo.FLAG_THEMED;
import static com.android.launcher3.icons.GraphicsUtils.setColorAlphaBound;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.FloatProperty;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.icons.BitmapInfo.DrawableCreationFlags;

public class FastBitmapDrawable extends Drawable implements Drawable.Callback {

    private static final Interpolator ACCEL = new AccelerateInterpolator();
    private static final Interpolator DEACCEL = new DecelerateInterpolator();
    private static final Interpolator HOVER_EMPHASIZED_DECELERATE_INTERPOLATOR =
            new PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f);

    @VisibleForTesting protected static final float PRESSED_SCALE = 1.1f;
    @VisibleForTesting protected static final float HOVERED_SCALE = 1.1f;
    public static final int WHITE_SCRIM_ALPHA = 138;

    private static final float DISABLED_DESATURATION = 1f;
    private static final float DISABLED_BRIGHTNESS = 0.5f;
    protected static final int FULLY_OPAQUE = 255;

    public static final int CLICK_FEEDBACK_DURATION = 200;
    public static final int HOVER_FEEDBACK_DURATION = 300;

    private static boolean sFlagHoverEnabled = false;

    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    protected final Bitmap mBitmap;
    protected final int mIconColor;

    @Nullable private ColorFilter mColorFilter;

    @VisibleForTesting protected boolean mIsPressed;
    @VisibleForTesting protected boolean mIsHovered;
    protected boolean mIsDisabled;
    protected float mDisabledAlpha = 1f;

    @DrawableCreationFlags int mCreationFlags = 0;

    // Animator and properties for the fast bitmap drawable's scale
    @VisibleForTesting protected static final FloatProperty<FastBitmapDrawable> SCALE
            = new FloatProperty<FastBitmapDrawable>("scale") {
        @Override
        public Float get(FastBitmapDrawable fastBitmapDrawable) {
            return fastBitmapDrawable.mScale;
        }

        @Override
        public void setValue(FastBitmapDrawable fastBitmapDrawable, float value) {
            fastBitmapDrawable.mScale = value;
            fastBitmapDrawable.invalidateSelf();
        }
    };
    @VisibleForTesting protected ObjectAnimator mScaleAnimation;
    private float mScale = 1;
    private int mAlpha = 255;

    private Drawable mBadge;

    public FastBitmapDrawable(Bitmap b) {
        this(b, Color.TRANSPARENT);
    }

    public FastBitmapDrawable(BitmapInfo info) {
        this(info.icon, info.color);
    }

    protected FastBitmapDrawable(Bitmap b, int iconColor) {
        mBitmap = b;
        mIconColor = iconColor;
        setFilterBitmap(true);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateBadgeBounds(bounds);
    }

    private void updateBadgeBounds(Rect bounds) {
        if (mBadge != null) {
            setBadgeBounds(mBadge, bounds);
        }
    }

    @Override
    public final void draw(Canvas canvas) {
        if (mScale != 1f) {
            int count = canvas.save();
            Rect bounds = getBounds();
            canvas.scale(mScale, mScale, bounds.exactCenterX(), bounds.exactCenterY());
            drawInternal(canvas, bounds);
            if (mBadge != null) {
                mBadge.draw(canvas);
            }
            canvas.restoreToCount(count);
        } else {
            drawInternal(canvas, getBounds());
            if (mBadge != null) {
                mBadge.draw(canvas);
            }
        }
    }

    protected void drawInternal(Canvas canvas, Rect bounds) {
        canvas.drawBitmap(mBitmap, null, bounds, mPaint);
    }

    /**
     * Returns the primary icon color, slightly tinted white
     */
    public int getIconColor() {
        int whiteScrim = setColorAlphaBound(Color.WHITE, WHITE_SCRIM_ALPHA);
        return ColorUtils.compositeColors(whiteScrim, mIconColor);
    }

    /**
     * Returns if this represents a themed icon
     */
    public boolean isThemed() {
        return false;
    }

    /**
     * Returns true if the drawable was created with theme, even if it doesn't
     * support theming itself.
     */
    public boolean isCreatedForTheme() {
        return isThemed() || (mCreationFlags & FLAG_THEMED) != 0;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mColorFilter = cf;
        updateFilter();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            mPaint.setAlpha(alpha);
            invalidateSelf();
            if (mBadge != null) {
                mBadge.setAlpha(alpha);
            }
        }
    }

    @Override
    public void setFilterBitmap(boolean filterBitmap) {
        mPaint.setFilterBitmap(filterBitmap);
        mPaint.setAntiAlias(filterBitmap);
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    public void resetScale() {
        if (mScaleAnimation != null) {
            mScaleAnimation.cancel();
            mScaleAnimation = null;
        }
        mScale = 1;
        invalidateSelf();
    }

    public float getAnimatedScale() {
        return mScaleAnimation == null ? 1 : mScale;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmap.getHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getBounds().width();
    }

    @Override
    public int getMinimumHeight() {
        return getBounds().height();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public ColorFilter getColorFilter() {
        return mPaint.getColorFilter();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean isPressed = false;
        boolean isHovered = false;
        for (int s : state) {
            if (s == android.R.attr.state_pressed) {
                isPressed = true;
                break;
            } else if (sFlagHoverEnabled && s == android.R.attr.state_hovered) {
                isHovered = true;
                // Do not break on hovered state, as pressed state should take precedence.
            }
        }
        if (mIsPressed != isPressed || mIsHovered != isHovered) {
            if (mScaleAnimation != null) {
                mScaleAnimation.cancel();
            }

            float endScale = isPressed ? PRESSED_SCALE : (isHovered ? HOVERED_SCALE : 1f);
            if (mScale != endScale) {
                if (isVisible()) {
                    Interpolator interpolator =
                            isPressed != mIsPressed ? (isPressed ? ACCEL : DEACCEL)
                                    : HOVER_EMPHASIZED_DECELERATE_INTERPOLATOR;
                    int duration =
                            isPressed != mIsPressed ? CLICK_FEEDBACK_DURATION
                                    : HOVER_FEEDBACK_DURATION;
                    mScaleAnimation = ObjectAnimator.ofFloat(this, SCALE, endScale);
                    mScaleAnimation.setDuration(duration);
                    mScaleAnimation.setInterpolator(interpolator);
                    mScaleAnimation.start();
                } else {
                    mScale = endScale;
                    invalidateSelf();
                }
            }
            mIsPressed = isPressed;
            mIsHovered = isHovered;
            return true;
        }
        return false;
    }

    public void setIsDisabled(boolean isDisabled) {
        if (mIsDisabled != isDisabled) {
            mIsDisabled = isDisabled;
            updateFilter();
        }
    }

    protected boolean isDisabled() {
        return mIsDisabled;
    }

    public void setBadge(Drawable badge) {
        if (mBadge != null) {
            mBadge.setCallback(null);
        }
        mBadge = badge;
        if (mBadge != null) {
            mBadge.setCallback(this);
        }
        updateBadgeBounds(getBounds());
        updateFilter();
    }

    @VisibleForTesting
    public Drawable getBadge() {
        return mBadge;
    }

    /**
     * Updates the paint to reflect the current brightness and saturation.
     */
    protected void updateFilter() {
        mPaint.setColorFilter(mIsDisabled ? getDisabledColorFilter(mDisabledAlpha) : mColorFilter);
        if (mBadge != null) {
            mBadge.setColorFilter(getColorFilter());
        }
        invalidateSelf();
    }

    protected FastBitmapConstantState newConstantState() {
        return new FastBitmapConstantState(mBitmap, mIconColor);
    }

    @Override
    public final ConstantState getConstantState() {
        FastBitmapConstantState cs = newConstantState();
        cs.mIsDisabled = mIsDisabled;
        if (mBadge != null) {
            cs.mBadgeConstantState = mBadge.getConstantState();
        }
        cs.mCreationFlags = mCreationFlags;
        return cs;
    }

    public static ColorFilter getDisabledColorFilter() {
        return getDisabledColorFilter(1);
    }

    // Returns if the FastBitmapDrawable contains a badge.
    public boolean hasBadge() {
        return (mCreationFlags & FLAG_NO_BADGE) == 0;
    }

    private static ColorFilter getDisabledColorFilter(float disabledAlpha) {
        ColorMatrix tempBrightnessMatrix = new ColorMatrix();
        ColorMatrix tempFilterMatrix = new ColorMatrix();

        tempFilterMatrix.setSaturation(1f - DISABLED_DESATURATION);
        float scale = 1 - DISABLED_BRIGHTNESS;
        int brightnessI =   (int) (255 * DISABLED_BRIGHTNESS);
        float[] mat = tempBrightnessMatrix.getArray();
        mat[0] = scale;
        mat[6] = scale;
        mat[12] = scale;
        mat[4] = brightnessI;
        mat[9] = brightnessI;
        mat[14] = brightnessI;
        mat[18] = disabledAlpha;
        tempFilterMatrix.preConcat(tempBrightnessMatrix);
        return new ColorMatrixColorFilter(tempFilterMatrix);
    }

    protected static final int getDisabledColor(int color) {
        int component = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
        float scale = 1 - DISABLED_BRIGHTNESS;
        int brightnessI = (int) (255 * DISABLED_BRIGHTNESS);
        component = Math.min(Math.round(scale * component + brightnessI), FULLY_OPAQUE);
        return Color.rgb(component, component, component);
    }

    /**
     * Sets the bounds for the badge drawable based on the main icon bounds
     */
    public static void setBadgeBounds(Drawable badge, Rect iconBounds) {
        int size = getBadgeSizeForIconSize(iconBounds.width());
        badge.setBounds(iconBounds.right - size, iconBounds.bottom - size,
                iconBounds.right, iconBounds.bottom);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (who == mBadge) {
            invalidateSelf();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (who == mBadge) {
            scheduleSelf(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    /**
     * Sets whether hover state functionality is enabled.
     */
    public static void setFlagHoverEnabled(boolean isFlagHoverEnabled) {
        sFlagHoverEnabled = isFlagHoverEnabled;
    }

    public static class FastBitmapConstantState extends ConstantState {
        protected final Bitmap mBitmap;
        protected final int mIconColor;

        // These are initialized later so that subclasses don't need to
        // pass everything in constructor
        protected boolean mIsDisabled;
        private ConstantState mBadgeConstantState;

        @DrawableCreationFlags int mCreationFlags = 0;

        public FastBitmapConstantState(Bitmap bitmap, int color) {
            mBitmap = bitmap;
            mIconColor = color;
        }

        protected FastBitmapDrawable createDrawable() {
            return new FastBitmapDrawable(mBitmap, mIconColor);
        }

        @Override
        public final FastBitmapDrawable newDrawable() {
            FastBitmapDrawable drawable = createDrawable();
            drawable.setIsDisabled(mIsDisabled);
            if (mBadgeConstantState != null) {
                drawable.setBadge(mBadgeConstantState.newDrawable());
            }
            drawable.mCreationFlags = mCreationFlags;
            return drawable;
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}

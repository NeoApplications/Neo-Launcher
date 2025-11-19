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

package com.android.launcher3.icons;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;

/**
 * A drawable used for drawing user badge. It draws a circle around the actual badge,
 * and has support for theming.
 */
public class UserBadgeDrawable extends DrawableWrapper {

    private static final float VIEWPORT_SIZE = 24;
    private static final float CENTER = VIEWPORT_SIZE / 2;

    private static final float BG_RADIUS = 11;
    private static final float SHADOW_RADIUS = 11.5f;
    private static final float SHADOW_OFFSET_Y = 0.25f;

    @VisibleForTesting
    static final int SHADOW_COLOR = 0x11000000;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int mBaseColor;
    private final int mBgColor;
    private boolean mShouldDrawBackground = true;

    @VisibleForTesting
    public final boolean mIsThemed;

    public UserBadgeDrawable(Context context, int badgeRes, int colorRes, boolean isThemed) {
        super(context.getDrawable(badgeRes));

        mIsThemed = isThemed;
        if (isThemed) {
            mutate();
            mBaseColor = context.getColor(R.color.themed_badge_icon_color);
            mBgColor = context.getColor(R.color.themed_badge_icon_background_color);
        } else {
            mBaseColor = context.getColor(colorRes);
            mBgColor = Color.WHITE;
        }
        setTint(mBaseColor);
    }

    private UserBadgeDrawable(Drawable base, int bgColor, int baseColor,
            boolean shouldDrawBackground) {
        super(base);
        mIsThemed = false;
        mBgColor = bgColor;
        mBaseColor = baseColor;
        mShouldDrawBackground = shouldDrawBackground;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mShouldDrawBackground) {
            Rect b = getBounds();
            int saveCount = canvas.save();
            canvas.translate(b.left, b.top);
            canvas.scale(b.width() / VIEWPORT_SIZE, b.height() / VIEWPORT_SIZE);

            mPaint.setColor(blendDrawableAlpha(SHADOW_COLOR));
            canvas.drawCircle(CENTER, CENTER + SHADOW_OFFSET_Y, SHADOW_RADIUS, mPaint);

            mPaint.setColor(blendDrawableAlpha(mBgColor));
            canvas.drawCircle(CENTER, CENTER, BG_RADIUS, mPaint);

            canvas.restoreToCount(saveCount);
        }
        super.draw(canvas);
    }

    private @ColorInt int blendDrawableAlpha(@ColorInt int color) {
        int alpha = (int) (Color.valueOf(color).alpha() * getAlpha());
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filter) {
        if (filter == null) {
            super.setTint(mBaseColor);
        } else if (filter instanceof ColorMatrixColorFilter cf) {
            ColorMatrix cm = new ColorMatrix();
            cf.getColorMatrix(cm);

            ColorMatrix cm2 = new ColorMatrix();
            float[] base = cm2.getArray();
            base[0] = Color.red(mBaseColor) / 255f;
            base[6] = Color.green(mBaseColor) / 255f;
            base[12] = Color.blue(mBaseColor) / 255f;
            base[18] = Color.alpha(mBaseColor) / 255f;
            cm2.postConcat(cm);

            super.setColorFilter(new ColorMatrixColorFilter(cm2));
        } else {
            // fail safe
            Paint p = new Paint();
            p.setColorFilter(filter);
            Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            new Canvas(b).drawPaint(p);
            super.setTint(b.getPixel(0, 0));
        }
    }

    public void setShouldDrawBackground(boolean shouldDrawBackground) {
        mutate();
        mShouldDrawBackground = shouldDrawBackground;
    }

    @Override
    public ConstantState getConstantState() {
        return new MyConstantState(
                getDrawable().getConstantState(), mBgColor, mBaseColor, mShouldDrawBackground);
    }

    private static class MyConstantState extends ConstantState {

        private final ConstantState mBase;
        private final int mBgColor;
        private final int mBaseColor;
        private final boolean mShouldDrawBackground;

        MyConstantState(ConstantState base, int bgColor, int baseColor,
                boolean shouldDrawBackground) {
            mBase = base;
            mBgColor = bgColor;
            mBaseColor = baseColor;
            mShouldDrawBackground = shouldDrawBackground;
        }

        @Override
        public int getChangingConfigurations() {
            return mBase.getChangingConfigurations();
        }

        @Override
        @NonNull
        public Drawable newDrawable() {
            return new UserBadgeDrawable(
                    mBase.newDrawable(), mBgColor, mBaseColor, mShouldDrawBackground);
        }

        @Override
        @NonNull
        public Drawable newDrawable(Resources res) {
            return new UserBadgeDrawable(
                    mBase.newDrawable(res), mBgColor, mBaseColor, mShouldDrawBackground);
        }

        @Override
        @NonNull
        public Drawable newDrawable(Resources res, Theme theme) {
            return new UserBadgeDrawable(
                    mBase.newDrawable(res, theme), mBgColor, mBaseColor, mShouldDrawBackground);
        }
    }
}

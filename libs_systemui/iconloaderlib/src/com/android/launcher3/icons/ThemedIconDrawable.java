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
package com.android.launcher3.icons;

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Class to handle monochrome themed app icons
 */
@SuppressWarnings("NewApi")
public class ThemedIconDrawable extends FastBitmapDrawable {

    public static final String TAG = "ThemedIconDrawable";

    final BitmapInfo bitmapInfo;
    final int colorFg, colorBg;

    // The foreground/monochrome icon for the app
    private final Bitmap mMonoIcon;
    private final Paint mMonoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Bitmap mBgBitmap;
    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final ColorFilter mBgFilter, mMonoFilter;

    protected ThemedIconDrawable(ThemedConstantState constantState) {
        super(constantState.mBitmap, constantState.colorFg);
        bitmapInfo = constantState.bitmapInfo;
        colorBg = constantState.colorBg;
        colorFg = constantState.colorFg;

        mMonoIcon = bitmapInfo.mMono;
        mMonoFilter = new BlendModeColorFilter(colorFg, BlendMode.SRC_IN);
        mMonoPaint.setColorFilter(mMonoFilter);

        mBgBitmap = bitmapInfo.mWhiteShadowLayer;
        mBgFilter = new BlendModeColorFilter(colorBg, BlendMode.SRC_IN);
        mBgPaint.setColorFilter(mBgFilter);
    }

    @Override
    protected void drawInternal(Canvas canvas, Rect bounds) {
        canvas.drawBitmap(mBgBitmap, null, bounds, mBgPaint);
        canvas.drawBitmap(mMonoIcon, null, bounds, mMonoPaint);
    }

    @Override
    protected void updateFilter() {
        super.updateFilter();
        int alpha = mIsDisabled ? (int) (mDisabledAlpha * FULLY_OPAQUE) : FULLY_OPAQUE;
        mBgPaint.setAlpha(alpha);
        mBgPaint.setColorFilter(mIsDisabled ? new BlendModeColorFilter(
                getDisabledColor(colorBg), BlendMode.SRC_IN) : mBgFilter);

        mMonoPaint.setAlpha(alpha);
        mMonoPaint.setColorFilter(mIsDisabled ? new BlendModeColorFilter(
                getDisabledColor(colorFg), BlendMode.SRC_IN) : mMonoFilter);
    }

    @Override
    public boolean isThemed() {
        return true;
    }

    @Override
    public FastBitmapConstantState newConstantState() {
        return new ThemedConstantState(bitmapInfo, colorBg, colorFg);
    }

    public void changeBackgroundColor(int colorBg) {
        if (mIsDisabled) return;

        mBgPaint.setColorFilter(new BlendModeColorFilter(colorBg, BlendMode.SRC_IN));
        invalidateSelf();
    }

    static class ThemedConstantState extends FastBitmapConstantState {

        final BitmapInfo bitmapInfo;
        final int colorFg, colorBg;

        public ThemedConstantState(BitmapInfo bitmapInfo, int colorBg, int colorFg) {
            super(bitmapInfo.icon, bitmapInfo.color);
            this.bitmapInfo = bitmapInfo;
            this.colorBg = colorBg;
            this.colorFg = colorFg;
        }

        @Override
        public FastBitmapDrawable createDrawable() {
            return new ThemedIconDrawable(this);
        }
    }

    public static FastBitmapDrawable newDrawable(BitmapInfo info, Context context) {
        int[] colors = getColors(context);
        return new ThemedConstantState(info, colors[0], colors[1]).newDrawable();
    }

    /**
     * Get an int array representing background and foreground colors for themed icons
     */
    public static int[] getColors(Context context) {
        Resources res = context.getResources();
        int[] colors = new int[2];
        colors[0] = res.getColor(R.color.themed_icon_background_color);
        colors[1] = res.getColor(R.color.themed_icon_color);
        return colors;
    }

    @Override
    public int getIconColor() {
        return colorFg;
    }
}

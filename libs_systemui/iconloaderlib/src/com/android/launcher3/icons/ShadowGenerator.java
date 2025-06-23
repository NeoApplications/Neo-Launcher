/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static com.android.launcher3.icons.GraphicsUtils.setColorAlphaBound;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Utility class to add shadows to bitmaps.
 */
public class ShadowGenerator {

    public static final boolean ENABLE_SHADOWS = true;

    public static final float BLUR_FACTOR = 1.68f/48;

    // Percent of actual icon size
    public static final float KEY_SHADOW_DISTANCE = 1f/48;
    private static final int KEY_SHADOW_ALPHA = 7;
    // Percent of actual icon size
    private static final float HALF_DISTANCE = 0.5f;
    private static final int AMBIENT_SHADOW_ALPHA = 25;

    // Amount by which an icon should be scaled down to make room for shadows.
    // We are ignoring KEY_SHADOW_DISTANCE because regular icons also ignore this: b/298203449
    public static final float ICON_SCALE_FOR_SHADOWS =
            (HALF_DISTANCE - BLUR_FACTOR) / HALF_DISTANCE;

    private final int mIconSize;

    private final Paint mBlurPaint;
    private final Paint mDrawPaint;
    private final BlurMaskFilter mDefaultBlurMaskFilter;

    public ShadowGenerator(int iconSize) {
        mIconSize = iconSize;
        mBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDefaultBlurMaskFilter = new BlurMaskFilter(mIconSize * BLUR_FACTOR, Blur.NORMAL);
    }

    public synchronized void drawShadow(Bitmap icon, Canvas out) {
        if (ENABLE_SHADOWS) {
            int[] offset = new int[2];
            mBlurPaint.setMaskFilter(mDefaultBlurMaskFilter);
            Bitmap shadow = icon.extractAlpha(mBlurPaint, offset);

            // Draw ambient shadow
            mDrawPaint.setAlpha(AMBIENT_SHADOW_ALPHA);
            out.drawBitmap(shadow, offset[0], offset[1], mDrawPaint);

            // Draw key shadow
            mDrawPaint.setAlpha(KEY_SHADOW_ALPHA);
            out.drawBitmap(shadow, offset[0], offset[1] + KEY_SHADOW_DISTANCE * mIconSize,
                    mDrawPaint);
        }
    }

    /** package private **/
    void addPathShadow(Path path, Canvas out) {
        if (ENABLE_SHADOWS) {
            mDrawPaint.setMaskFilter(mDefaultBlurMaskFilter);

            // Draw ambient shadow
            mDrawPaint.setAlpha(AMBIENT_SHADOW_ALPHA);
            out.drawPath(path, mDrawPaint);

            // Draw key shadow
            int save = out.save();
            mDrawPaint.setAlpha(KEY_SHADOW_ALPHA);
            out.translate(0, KEY_SHADOW_DISTANCE * mIconSize);
            out.drawPath(path, mDrawPaint);
            out.restoreToCount(save);

            mDrawPaint.setMaskFilter(null);
        }
    }

    public static class Builder {

        public final RectF bounds = new RectF();
        public final int color;

        public int ambientShadowAlpha = AMBIENT_SHADOW_ALPHA;

        public float shadowBlur;

        public float keyShadowDistance;
        public int keyShadowAlpha = KEY_SHADOW_ALPHA;
        public float radius;

        public Builder(int color) {
            this.color = color;
        }

        public Builder setupBlurForSize(int height) {
            if (ENABLE_SHADOWS) {
                shadowBlur = height * 1f / 24;
                keyShadowDistance = height * 1f / 16;
            } else {
                shadowBlur = 0;
                keyShadowDistance = 0;
            }
            return this;
        }

        public Bitmap createPill(int width, int height) {
            return createPill(width, height, height / 2f);
        }

        public Bitmap createPill(int width, int height, float r) {
            radius = r;

            int centerX = Math.round(width / 2f + shadowBlur);
            int centerY = Math.round(radius + shadowBlur + keyShadowDistance);
            int center = Math.max(centerX, centerY);
            bounds.set(0, 0, width, height);
            bounds.offsetTo(center - width / 2f, center - height / 2f);

            int size = center * 2;
            return BitmapRenderer.createHardwareBitmap(size, size, this::drawShadow);
        }

        public void drawShadow(Canvas c) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            p.setColor(color);

            if (ENABLE_SHADOWS) {
                // Key shadow
                p.setShadowLayer(shadowBlur, 0, keyShadowDistance,
                        setColorAlphaBound(Color.BLACK, keyShadowAlpha));
                c.drawRoundRect(bounds, radius, radius, p);

                // Ambient shadow
                p.setShadowLayer(shadowBlur, 0, 0,
                        setColorAlphaBound(Color.BLACK, ambientShadowAlpha));
                c.drawRoundRect(bounds, radius, radius, p);
            }

            if (Color.alpha(color) < 255) {
                // Clear any content inside the pill-rect for translucent fill.
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                p.clearShadowLayer();
                p.setColor(Color.BLACK);
                c.drawRoundRect(bounds, radius, radius, p);

                p.setXfermode(null);
                p.setColor(color);
                c.drawRoundRect(bounds, radius, radius, p);
            }
        }
    }
}

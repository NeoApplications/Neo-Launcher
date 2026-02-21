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
package com.android.launcher3.icons;

import static android.graphics.Paint.FILTER_BITMAP_FLAG;

import static com.android.launcher3.icons.LuminanceComputer.createDefaultLuminanceComputer;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;

import androidx.annotation.WorkerThread;

import java.nio.ByteBuffer;

/**
 * Utility class to generate monochrome icons version for a given drawable.
 */
@TargetApi(Build.VERSION_CODES.TIRAMISU)
public class MonochromeIconFactory extends Drawable {

    private final Bitmap mFlatBitmap;
    private final Canvas mFlatCanvas;
    private final Paint mCopyPaint;

    private final Bitmap mAlphaBitmap;
    private final Canvas mAlphaCanvas;
    private final byte[] mPixels;

    private final int mBitmapSize;

    private final Paint mDrawPaint;
    private final Rect mSrcRect;

    private double mLuminanceDiff = Double.NaN;

    public MonochromeIconFactory(int iconBitmapSize) {
        float extraFactor = AdaptiveIconDrawable.getExtraInsetFraction();
        float viewPortScale = 1 / (1 + 2 * extraFactor);
        mBitmapSize = Math.round(iconBitmapSize * 2 * viewPortScale);
        mPixels = new byte[mBitmapSize * mBitmapSize];

        mFlatBitmap = Bitmap.createBitmap(mBitmapSize, mBitmapSize, Config.ARGB_8888);
        mFlatCanvas = new Canvas(mFlatBitmap);

        mAlphaBitmap = Bitmap.createBitmap(mBitmapSize, mBitmapSize, Config.ALPHA_8);
        mAlphaCanvas = new Canvas(mAlphaBitmap);

        mDrawPaint = new Paint(FILTER_BITMAP_FLAG);
        mDrawPaint.setColor(Color.WHITE);
        mSrcRect = new Rect(0, 0, mBitmapSize, mBitmapSize);

        mCopyPaint = new Paint(FILTER_BITMAP_FLAG);
        mCopyPaint.setBlendMode(BlendMode.SRC);

        // Crate a color matrix which converts the icon to grayscale and then uses the average
        // of RGB components as the alpha component.
        ColorMatrix satMatrix = new ColorMatrix();
        float[] vals = satMatrix.getArray();
        vals[15] = vals[16] = vals[17] = .3333f;
        vals[18] = vals[19] = 0;
        mCopyPaint.setColorFilter(new ColorMatrixColorFilter(vals));
    }

    private void drawDrawable(Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(0, 0, mBitmapSize, mBitmapSize);
            drawable.draw(mFlatCanvas);
        }
    }

    /**
     * Kept to layout lib compilation
     *
     * @deprecated use {@link #wrap(AdaptiveIconDrawable)} instead
     */
    @Deprecated
    public Drawable wrap(AdaptiveIconDrawable icon, Path unused) {
        return wrap(icon);
    }

    /**
     * Creates a monochrome version of the provided drawable
     */
    @WorkerThread
    public Drawable wrap(AdaptiveIconDrawable icon) {
        mFlatCanvas.drawColor(Color.BLACK);
        Drawable bg = icon.getBackground();
        Drawable fg = icon.getForeground();
        if (bg != null && fg != null) {
            LuminanceComputer computer = createDefaultLuminanceComputer();
            // Calculate foreground luminance on black first to account for any transparent pixels
            drawDrawable(fg);
            double fgLuminance = computer.computeLuminance(mFlatBitmap);

            // Start drawing from scratch and calculate background luminance
            mFlatCanvas.drawColor(Color.BLACK);
            drawDrawable(bg);
            double bgLuminance = computer.computeLuminance(mFlatBitmap);

            drawDrawable(fg);
            mLuminanceDiff = fgLuminance - bgLuminance;
        } else {
            // We do not have separate layer information.
            // Try to calculate everything from a single layer
            drawDrawable(bg);
            drawDrawable(fg);

            LuminanceComputer computer = createDefaultLuminanceComputer(ComputationType.SPREAD);
            mLuminanceDiff = computer.computeLuminance(mFlatBitmap, /* scale= */ true);
        }
        generateMono();
        return new InsetDrawable(this, -AdaptiveIconDrawable.getExtraInsetFraction());
    }

    public double getLuminanceDiff() {
        return mLuminanceDiff;
    }

    @WorkerThread
    private void generateMono() {
        mAlphaCanvas.drawBitmap(mFlatBitmap, 0, 0, mCopyPaint);
        ByteBuffer buffer = ByteBuffer.wrap(mPixels);
        buffer.rewind();
        mAlphaBitmap.copyPixelsToBuffer(buffer);

        int min = 0xFF;
        int max = 0;
        for (byte b : mPixels) {
            min = Math.min(min, b & 0xFF);
            max = Math.max(max, b & 0xFF);
        }

        if (min < max) {
            // rescale pixels to increase contrast
            float range = max - min;

            for (int i = 0; i < mPixels.length; i++) {
                int p = mPixels[i] & 0xFF;
                int p2 = Math.round((p - min) * 0xFF / range);
                mPixels[i] = (byte) (p2);
            }

            // Second phase of processing, aimed on increasing the contrast
            for (int i = 0; i < mPixels.length; i++) {
                int p = mPixels[i] & 0xFF;
                int p2;
                double coefficient;
                if (p > 128) {
                    coefficient = (1 - (double) (p - 128) / 128);
                    p2 = 255 - (int) (coefficient * (255 - p));
                } else {
                    coefficient = (1 - (double) (128 - p) / 128);
                    p2 = (int) (coefficient * p);
                }

                if (p2 > 255) {
                    p2 = 255;
                } else if (p2 < 0) {
                    p2 = 0;
                }

                mPixels[i] = (byte) p2;
            }

            buffer.rewind();
            mAlphaBitmap.copyPixelsFromBuffer(buffer);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mAlphaBitmap, mSrcRect, getBounds(), mDrawPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int i) {
        mDrawPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mDrawPaint.setColorFilter(colorFilter);
    }
}

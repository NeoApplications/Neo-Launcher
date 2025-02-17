/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.ColorUtils;

/**
 * Subclass which draws a placeholder icon when the actual icon is not yet loaded
 */
public class PlaceHolderIconDrawable extends FastBitmapDrawable {

    // Path in [0, 100] bounds.
    private final Path mProgressPath;

    public PlaceHolderIconDrawable(BitmapInfo info, Context context) {
        super(info);

        mProgressPath = GraphicsUtils.getShapePath(context, 100);
        mPaint.setColor(ColorUtils.compositeColors(
                GraphicsUtils.getAttrColor(context, R.attr.loadingIconColor), info.color));
    }

    @Override
    protected void drawInternal(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.scale(bounds.width() / 100f, bounds.height() / 100f);
        canvas.drawPath(mProgressPath, mPaint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Updates this placeholder to {@code newIcon} with animation.
     */
    public void animateIconUpdate(Drawable newIcon) {
        int placeholderColor = mPaint.getColor();
        int originalAlpha = Color.alpha(placeholderColor);

        ValueAnimator iconUpdateAnimation = ValueAnimator.ofInt(originalAlpha, 0);
        iconUpdateAnimation.setDuration(375);
        iconUpdateAnimation.addUpdateListener(valueAnimator -> {
            int newAlpha = (int) valueAnimator.getAnimatedValue();
            int newColor = ColorUtils.setAlphaComponent(placeholderColor, newAlpha);

            newIcon.setColorFilter(new PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_ATOP));
        });
        iconUpdateAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                newIcon.setColorFilter(null);
            }
        });
        iconUpdateAnimation.start();
    }

}

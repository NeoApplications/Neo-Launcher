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

package com.android.launcher3.allapps;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Themes;

/**
 * A TextView that is used to display the letter list in the fast scroller.
 */
public class LetterListTextView extends TextView {
    private static final float ABSOLUTE_TRANSLATION_X = 30f;
    private static final float ABSOLUTE_SCALE = 1.4f;
    private final Drawable mLetterBackground;
    private final int mLetterListTextWidthAndHeight;
    private final int mTextColor;
    private final int mBackgroundColor;
    private final int mSelectedColor;

    public LetterListTextView(Context context) {
        this(context, null, 0);
    }

    public LetterListTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterListTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLetterBackground = context.getDrawable(R.drawable.bg_letter_list_text);
        mLetterListTextWidthAndHeight = context.getResources().getDimensionPixelSize(
                R.dimen.fastscroll_list_letter_size);
        mTextColor = Themes.getAttrColor(context, R.attr.materialColorOnSurface);
        mBackgroundColor = Themes.getAttrColor(context, R.attr.materialColorSurfaceContainer);
        mSelectedColor = Themes.getAttrColor(context, R.attr.materialColorOnSecondary);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setBackground(mLetterBackground);
        setTextColor(mTextColor);
        setClickable(false);
        setWidth(mLetterListTextWidthAndHeight);
        setTextSize(mLetterListTextWidthAndHeight);
        setVisibility(VISIBLE);
    }

    /**
     * Animates the letter list text view based on the current finger position.
     *
     * @param currentFingerY The Y position of where the finger is placed on the fastScroller in
     *                       pixels.
     */
    public void animateBasedOnYPosition(int currentFingerY) {
        if (getBackground() == null) {
            return;
        }
        float cutOffMin = currentFingerY - (getHeight() * 2);
        float cutOffMax = currentFingerY + (getHeight() * 2);
        float cutOffDistance = cutOffMax - cutOffMin;
        // Update the background blend color
        boolean isWithinAnimationBounds = getY() < cutOffMax && getY() > cutOffMin;
        if (isWithinAnimationBounds) {
            getBackground().setColorFilter(new PorterDuffColorFilter(
                    getBlendColorBasedOnYPosition(currentFingerY, cutOffDistance),
                    PorterDuff.Mode.MULTIPLY));
        } else {
            getBackground().setColorFilter(new PorterDuffColorFilter(
                    mBackgroundColor, PorterDuff.Mode.MULTIPLY));
        }
        translateBasedOnYPosition(currentFingerY, cutOffDistance, isWithinAnimationBounds);
        scaleBasedOnYPosition(currentFingerY, cutOffDistance, isWithinAnimationBounds);
    }

    private int getBlendColorBasedOnYPosition(int y, float cutOffDistance) {
        float raisedCosineBlend = (float) Math.cos(((y - getY()) / (cutOffDistance)) * Math.PI);
        float blendRatio = Utilities.boundToRange(raisedCosineBlend, 0f, 1f);
        return ColorUtils.blendARGB(mBackgroundColor, mSelectedColor, blendRatio);
    }

    private void scaleBasedOnYPosition(int y, float cutOffDistance,
            boolean isWithinAnimationBounds) {
        float raisedCosineScale = (float) Math.cos(((y - getY()) / (cutOffDistance)) * Math.PI)
                * ABSOLUTE_SCALE;
        if (isWithinAnimationBounds) {
            raisedCosineScale = Utilities.boundToRange(raisedCosineScale, 1f, ABSOLUTE_SCALE);
            setScaleX(raisedCosineScale);
            setScaleY(raisedCosineScale);
        } else {
            setScaleX(1);
            setScaleY(1);
        }
    }

    private void translateBasedOnYPosition(int y, float cutOffDistance,
            boolean isWithinAnimationBounds) {
        float raisedCosineTranslation =
                (float) Math.cos(((y - getY()) / (cutOffDistance)) * Math.PI)
                        * ABSOLUTE_TRANSLATION_X;
        if (isWithinAnimationBounds) {
            raisedCosineTranslation = -1 * Utilities.boundToRange(raisedCosineTranslation,
                    0, ABSOLUTE_TRANSLATION_X);
            setTranslationX(raisedCosineTranslation);
        } else {
            setTranslationX(0);
        }
    }
}

/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.smartspace.superg;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.R;

public class QsbConnector extends View {
    private static final Property<QsbConnector, Integer> sAlphaProperty = new Property<QsbConnector, Integer>(Integer.class, "overlayAlpha") {
        @Override
        public Integer get(QsbConnector qsbConnector) {
            return qsbConnector.mForegroundAlpha;
        }

        @Override
        public void set(QsbConnector qsbConnector, Integer newAlpha) {
            qsbConnector.updateAlpha(newAlpha);
        }
    };

    private int mForegroundAlpha;
    private ObjectAnimator mRevealAnimator;
    private final int mForegroundColor;

    public QsbConnector(Context context) {
        this(context, null);
    }

    public QsbConnector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QsbConnector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mForegroundAlpha = 0;
        mForegroundColor = getResources().getColor(R.color.qsb_background) & 0xFFFFFF;

        //setBackground(getResources().getDrawable(R.drawable.bg_pixel_qsb_connector, getContext().getTheme()));
    }

    private void stopRevealAnimation() {
        if (mRevealAnimator != null) {
            mRevealAnimator.end();
            mRevealAnimator = null;
        }
    }

    private void updateAlpha(int alpha) {
        if (mForegroundAlpha != alpha) {
            mForegroundAlpha = alpha;
            invalidate();
        }
    }

    public void changeVisibility(boolean makeVisible) {
        if (makeVisible) {
            stopRevealAnimation();
            updateAlpha(255);
            mRevealAnimator = ObjectAnimator.ofInt(this, QsbConnector.sAlphaProperty, 0);
            mRevealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mRevealAnimator.start();
        } else {
            updateAlpha(0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mForegroundAlpha > 0) {
            canvas.drawColor(ColorUtils.setAlphaComponent(mForegroundColor, mForegroundAlpha));
        }
    }

    @Override
    protected boolean onSetAlpha(final int alpha) {
        if (alpha == 0) {
            stopRevealAnimation();
        }
        return super.onSetAlpha(alpha);
    }
}
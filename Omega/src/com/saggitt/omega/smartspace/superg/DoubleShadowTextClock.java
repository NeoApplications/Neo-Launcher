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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextClock;

import com.android.launcher3.R;

public class DoubleShadowTextClock extends TextClock {
    private final float ambientShadowBlur;
    private final int ambientShadowColor;
    private final float keyShadowBlur;
    private final int keyShadowColor;
    private final float keyShadowOffset;

    public DoubleShadowTextClock(Context context) {
        this(context, null);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray ta = context.obtainStyledAttributes(attributeSet, new int[]{R.attr.ambientShadowColor,
                R.attr.keyShadowColor, R.attr.ambientShadowBlur, R.attr.keyShadowBlur, R.attr.keyShadowOffsetX}, i, 0);
        ambientShadowColor = ta.getColor(0, 0);
        keyShadowColor = ta.getColor(1, 0);
        ambientShadowBlur = ta.getDimension(2, 0f);
        keyShadowBlur = ta.getDimension(3, 0f);
        keyShadowOffset = ta.getDimension(4, 0f);
        ta.recycle();
        setShadowLayer(Math.max(keyShadowBlur + keyShadowOffset, ambientShadowBlur), 0f, 0f, keyShadowColor);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setShadowLayer(keyShadowBlur, 0f, keyShadowOffset, keyShadowColor);
        super.onDraw(canvas);
        getPaint().setShadowLayer(ambientShadowBlur, 0f, 0f, ambientShadowColor);
        super.onDraw(canvas);
    }

    public void setFormat(CharSequence charSequence) {
        setFormat24Hour(charSequence);
        setFormat12Hour(charSequence);
    }
}
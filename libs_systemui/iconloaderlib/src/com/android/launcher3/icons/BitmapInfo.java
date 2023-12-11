/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.util.FlagOp;

public class BitmapInfo {

    static final int FLAG_WORK = 1 << 0;
    static final int FLAG_INSTANT = 1 << 1;
    static final int FLAG_CLONE = 1 << 2;
    @IntDef(flag = true, value = {
            FLAG_WORK,
            FLAG_INSTANT,
            FLAG_CLONE
    })
    @interface BitmapInfoFlags {}

    public static final int FLAG_THEMED = 1 << 0;
    public static final int FLAG_NO_BADGE = 1 << 1;
    @IntDef(flag = true, value = {
            FLAG_THEMED,
            FLAG_NO_BADGE,
    })
    public @interface DrawableCreationFlags {}

    public static final Bitmap LOW_RES_ICON = Bitmap.createBitmap(1, 1, Config.ALPHA_8);
    public static final BitmapInfo LOW_RES_INFO = fromBitmap(LOW_RES_ICON);

    public static final String TAG = "BitmapInfo";

    public final Bitmap icon;
    public final int color;

    @Nullable
    protected Bitmap mMono;
    protected Bitmap mWhiteShadowLayer;

    public @BitmapInfoFlags int flags;
    private BitmapInfo badgeInfo;

    public BitmapInfo(Bitmap icon, int color) {
        this.icon = icon;
        this.color = color;
    }

    public BitmapInfo withBadgeInfo(BitmapInfo badgeInfo) {
        BitmapInfo result = clone();
        result.badgeInfo = badgeInfo;
        return result;
    }

    /**
     * Returns a bitmapInfo with the flagOP applied
     */
    public BitmapInfo withFlags(@NonNull FlagOp op) {
        if (op == FlagOp.NO_OP) {
            return this;
        }
        BitmapInfo result = clone();
        result.flags = op.apply(result.flags);
        return result;
    }

    protected BitmapInfo copyInternalsTo(BitmapInfo target) {
        target.mMono = mMono;
        target.mWhiteShadowLayer = mWhiteShadowLayer;
        target.flags = flags;
        target.badgeInfo = badgeInfo;
        return target;
    }

    @Override
    public BitmapInfo clone() {
        return copyInternalsTo(new BitmapInfo(icon, color));
    }

    public void setMonoIcon(Bitmap mono, BaseIconFactory iconFactory) {
        mMono = mono;
        mWhiteShadowLayer = iconFactory.getWhiteShadowLayer();
    }

    /**
     * Ideally icon should not be null, except in cases when generating hardware bitmap failed
     */
    public final boolean isNullOrLowRes() {
        return icon == null || icon == LOW_RES_ICON;
    }

    public final boolean isLowRes() {
        return LOW_RES_ICON == icon;
    }

    /**
     * BitmapInfo can be stored on disk or other persistent storage
     */
    public boolean canPersist() {
        return !isNullOrLowRes();
    }

    public Bitmap getMono() {
        return mMono;
    }

    /**
     * Creates a drawable for the provided BitmapInfo
     */
    public FastBitmapDrawable newIcon(Context context) {
        return newIcon(context, 0);
    }

    /**
     * Creates a drawable for the provided BitmapInfo
     */
    public FastBitmapDrawable newIcon(Context context, @DrawableCreationFlags int creationFlags) {
        FastBitmapDrawable drawable;
        if (isLowRes()) {
            drawable = new PlaceHolderIconDrawable(this, context);
        } else  if ((creationFlags & FLAG_THEMED) != 0 && mMono != null) {
            drawable = ThemedIconDrawable.newDrawable(this, context);
        } else {
            drawable = new FastBitmapDrawable(this);
        }
        applyFlags(context, drawable, creationFlags);
        return drawable;
    }

    protected void applyFlags(Context context, FastBitmapDrawable drawable,
            @DrawableCreationFlags int creationFlags) {
        drawable.mDisabledAlpha = GraphicsUtils.getFloat(context, R.attr.disabledIconAlpha, 1f);
        if ((creationFlags & FLAG_NO_BADGE) == 0) {
            if (badgeInfo != null) {
                drawable.setBadge(badgeInfo.newIcon(context, creationFlags));
            } else if ((flags & FLAG_INSTANT) != 0) {
                drawable.setBadge(context.getDrawable(drawable.isThemed()
                        ? R.drawable.ic_instant_app_badge_themed
                        : R.drawable.ic_instant_app_badge));
            } else if ((flags & FLAG_WORK) != 0) {
                drawable.setBadge(context.getDrawable(drawable.isThemed()
                        ? R.drawable.ic_work_app_badge_themed
                        : R.drawable.ic_work_app_badge));
            } else if ((flags & FLAG_CLONE) != 0) {
                drawable.setBadge(context.getDrawable(drawable.isThemed()
                        ? R.drawable.ic_clone_app_badge_themed
                        : R.drawable.ic_clone_app_badge));
            }
        }
    }

    public static BitmapInfo fromBitmap(@NonNull Bitmap bitmap) {
        return of(bitmap, 0);
    }

    public static BitmapInfo of(@NonNull Bitmap bitmap, int color) {
        return new BitmapInfo(bitmap, color);
    }

    /**
     * Interface to be implemented by drawables to provide a custom BitmapInfo
     */
    public interface Extender {

        /**
         * Called for creating a custom BitmapInfo
         */
        BitmapInfo getExtendedInfo(Bitmap bitmap, int color,
                BaseIconFactory iconFactory, float normalizationScale);

        /**
         * Called to draw the UI independent of any runtime configurations like time or theme
         */
        void drawForPersistence(Canvas canvas);
    }
}

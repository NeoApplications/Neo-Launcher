package com.saggitt.omega.icons.clock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.Utilities;

import java.util.TimeZone;

<<<<<<< HEAD
class AutoUpdateClock extends FastBitmapDrawable implements Runnable {
=======
public class AutoUpdateClock extends FastBitmapDrawable implements Runnable {
>>>>>>> ba3d8f4607d1f35bce071eabb638c4e819bb5fbc
    private ClockLayers mLayers;

    AutoUpdateClock(Bitmap bitmap, ClockLayers layers) {
        super(bitmap);
        mLayers = layers;
    }

    private void rescheduleUpdate() {
        long millisInSecond = 1000L;
        unscheduleSelf(this);
        long uptimeMillis = SystemClock.uptimeMillis();
        scheduleSelf(this, uptimeMillis - uptimeMillis % millisInSecond + millisInSecond);
    }

    // Used only by Google Clock
    void updateLayers(ClockLayers layers) {
        mLayers = layers;
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(getBounds());
        }
        invalidateSelf();
    }

    void setTimeZone(TimeZone timeZone) {
        if (mLayers != null) {
            mLayers.setTimeZone(timeZone);
            invalidateSelf();
        }
    }

    @Override
    public void drawInternal(Canvas canvas, Rect rect) {
        if (mLayers == null || !Utilities.ATLEAST_OREO) {
            super.drawInternal(canvas, rect);
        } else {
            canvas.drawBitmap(mLayers.bitmap, null, rect, mPaint);
            mLayers.updateAngles();
            canvas.scale(mLayers.scale, mLayers.scale,
                    rect.exactCenterX() + mLayers.offset,
                    rect.exactCenterY() + mLayers.offset);
            canvas.clipPath(mLayers.mDrawable.getIconMask());
            mLayers.mDrawable.getForeground().draw(canvas);
            rescheduleUpdate();
        }
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(bounds);
        }
    }

    @Override
    public void run() {
        if (mLayers.updateAngles()) {
            invalidateSelf();
        } else {
            rescheduleUpdate();
        }
    }
}

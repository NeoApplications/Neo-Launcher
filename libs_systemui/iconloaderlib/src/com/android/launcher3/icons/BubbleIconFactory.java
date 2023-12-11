package com.android.launcher3.icons;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Factory for creating normalized bubble icons and app badges.
 */
public class BubbleIconFactory extends BaseIconFactory {

    private final int mRingColor;
    private final int mRingWidth;

    private final BaseIconFactory mBadgeFactory;

    /**
     * Creates a bubble icon factory.
     *
     * @param context the context for the factory.
     * @param iconSize the size of the bubble icon (i.e. the large icon for the bubble).
     * @param badgeSize the size of the badge (i.e. smaller icon shown on top of the large icon).
     * @param ringColor the color of the ring optionally shown around the badge.
     * @param ringWidth the width of the ring optionally shown around the badge.
     */
    public BubbleIconFactory(Context context, int iconSize, int badgeSize, int ringColor,
            int ringWidth) {
        super(context, context.getResources().getConfiguration().densityDpi, iconSize);
        mRingColor = ringColor;
        mRingWidth = ringWidth;

        mBadgeFactory = new BaseIconFactory(context,
                context.getResources().getConfiguration().densityDpi,
                badgeSize);
    }

    /**
     * Returns the drawable that the developer has provided to display in the bubble.
     */
    public Drawable getBubbleDrawable(@NonNull final Context context,
            @Nullable final ShortcutInfo shortcutInfo, @Nullable final Icon ic) {
        if (shortcutInfo != null) {
            LauncherApps launcherApps = context.getSystemService(LauncherApps.class);
            int density = context.getResources().getConfiguration().densityDpi;
            return launcherApps.getShortcutIconDrawable(shortcutInfo, density);
        } else {
            if (ic != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ic.getType() == Icon.TYPE_URI
                        || ic.getType() == Icon.TYPE_URI_ADAPTIVE_BITMAP) {
                    context.grantUriPermission(context.getPackageName(),
                            ic.getUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                return ic.loadDrawable(context);
            }
            return null;
        }
    }

    /**
     * Creates the bitmap for the provided drawable and returns the scale used for
     * drawing the actual drawable. This is used for the larger icon shown for the bubble.
     */
    public Bitmap getBubbleBitmap(@NonNull Drawable icon, float[] outScale) {
        if (outScale == null) {
            outScale = new float[1];
        }
        icon = normalizeAndWrapToAdaptiveIcon(icon,
                true /* shrinkNonAdaptiveIcons */,
                null /* outscale */,
                outScale);
        return createIconBitmap(icon, outScale[0], MODE_WITH_SHADOW);
    }

    /**
     * Returns a {@link BitmapInfo} for the app-badge that is shown on top of each bubble. This
     * will include the workprofile indicator on the badge if appropriate.
     */
    public BitmapInfo getBadgeBitmap(Drawable userBadgedAppIcon, boolean isImportantConversation) {
        if (userBadgedAppIcon instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable ad = (AdaptiveIconDrawable) userBadgedAppIcon;
            userBadgedAppIcon = new CircularAdaptiveIcon(ad.getBackground(),
                    ad.getForeground());
        }
        if (isImportantConversation) {
            userBadgedAppIcon = new CircularRingDrawable(userBadgedAppIcon);
        }
        Bitmap userBadgedBitmap = mBadgeFactory.createIconBitmap(
                userBadgedAppIcon, 1, MODE_WITH_SHADOW);
        return mBadgeFactory.createIconBitmap(userBadgedBitmap);
    }

    private class CircularRingDrawable extends CircularAdaptiveIcon {
        final Rect mInnerBounds = new Rect();

        final Drawable mDr;

        CircularRingDrawable(Drawable dr) {
            super(null, null);
            mDr = dr;
        }

        @Override
        public void draw(Canvas canvas) {
            int save = canvas.save();
            canvas.clipPath(getIconMask());
            canvas.drawColor(mRingColor);
            mInnerBounds.set(getBounds());
            mInnerBounds.inset(mRingWidth, mRingWidth);
            canvas.translate(mInnerBounds.left, mInnerBounds.top);
            mDr.setBounds(0, 0, mInnerBounds.width(), mInnerBounds.height());
            mDr.draw(canvas);
            canvas.restoreToCount(save);
        }
    }

    private static class CircularAdaptiveIcon extends AdaptiveIconDrawable {

        final Path mPath = new Path();

        CircularAdaptiveIcon(Drawable bg, Drawable fg) {
            super(bg, fg);
        }

        @Override
        public Path getIconMask() {
            mPath.reset();
            Rect bounds = getBounds();
            mPath.addOval(bounds.left, bounds.top, bounds.right, bounds.bottom, Path.Direction.CW);
            return mPath;
        }

        @Override
        public void draw(Canvas canvas) {
            int save = canvas.save();
            canvas.clipPath(getIconMask());

            Drawable d;
            if ((d = getBackground()) != null) {
                d.draw(canvas);
            }
            if ((d = getForeground()) != null) {
                d.draw(canvas);
            }
            canvas.restoreToCount(save);
        }
    }
}

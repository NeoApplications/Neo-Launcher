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

import static android.graphics.Color.luminance;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.FILTER_BITMAP_FLAG;
import static com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;
import static com.android.systemui.shared.Flags.notificationDotContrastBorder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewDebug;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

/**
 * Used to draw a notification dot on top of an icon.
 */
public class DotRenderer {

    private static final String TAG = "DotRenderer";

    // The dot size is defined as a percentage of the app icon size.
    private static final float SIZE_PERCENTAGE = 0.228f;
    // The black border needs a light notification dot color. This is for accessibility.
    private static final float LUMINENSCE_LIMIT = .70f;

    private final float mCircleRadius;
    private final Paint mCirclePaint = new Paint(ANTI_ALIAS_FLAG | FILTER_BITMAP_FLAG);

    private final Bitmap mBackgroundWithShadow;
    private final float mBitmapOffset;

    private static final int MIN_DOT_SIZE = 1;

    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final float TEXT_SIZE_PERCENTAGE = 0.26f;
    private final int mTextHeight;
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public DotRenderer(int iconSizePx) {
        int size = Math.round(SIZE_PERCENTAGE * iconSizePx);
        if (size <= 0) {
            size = MIN_DOT_SIZE;
        }
        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(Color.TRANSPARENT);
        builder.ambientShadowAlpha = notificationDotContrastBorder() ? 255 : 88;
        mBackgroundWithShadow = builder.setupBlurForSize(size).createPill(size, size);
        mCircleRadius = builder.radius;

        mBitmapOffset = -mBackgroundWithShadow.getHeight() * 0.5f; // Same as width.

        // Measure the text height.
        Rect tempTextHeight = new Rect();
        mTextPaint.setTextSize(iconSizePx * TEXT_SIZE_PERCENTAGE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.getTextBounds("0", 0, 1, tempTextHeight);
        mTextHeight = tempTextHeight.height();
    }

    private static PointF getPathPoint(Path path, float size, float direction) {
        float halfSize = size / 2;
        // Small delta so that we don't get a zero size triangle
        float delta = 1;

        float x = halfSize + direction * halfSize;
        Path trianglePath = new Path();
        trianglePath.moveTo(halfSize, halfSize);
        trianglePath.lineTo(x + delta * direction, 0);
        trianglePath.lineTo(x, -delta);
        trianglePath.close();

        trianglePath.op(path, Path.Op.INTERSECT);
        float[] pos = new float[2];
        new PathMeasure(trianglePath, false).getPosTan(0, pos, null);
        return new PointF(pos[0] / size, pos[1] / size);
    }

    /**
     * Draw a circle on top of the canvas according to the given params.
     */
    public void draw(Canvas canvas, DrawParams params) {
        if (params == null) {
            Log.e(TAG, "Invalid null argument(s) passed in call to draw.");
            return;
        }
        canvas.save();

        Rect iconBounds = params.iconBounds;
        PointF dotPosition = params.getDotPosition();
        float dotCenterX = iconBounds.left + iconBounds.width() * dotPosition.x;
        float dotCenterY = iconBounds.top + iconBounds.height() * dotPosition.y;

        // Ensure dot fits entirely in canvas clip bounds.
        Rect canvasBounds = canvas.getClipBounds();
        float offsetX = params.leftAlign
                ? Math.max(0, canvasBounds.left - (dotCenterX + mBitmapOffset))
                : Math.min(0, canvasBounds.right - (dotCenterX - mBitmapOffset));
        float offsetY = Math.max(0, canvasBounds.top - (dotCenterY + mBitmapOffset));

        // We draw the dot relative to its center.
        canvas.translate(dotCenterX + offsetX, dotCenterY + offsetY);
        canvas.scale(params.scale, params.scale);

        // Draw Background Shadow
        mCirclePaint.setColor(Color.BLACK);
        canvas.drawBitmap(mBackgroundWithShadow, mBitmapOffset, mBitmapOffset, mCirclePaint);

        boolean isText = params.showCount && params.count != 0;
        int backgroundWithShadowSize = mBackgroundWithShadow.getHeight(); // Same as width.
        String count = String.valueOf(params.count);

        if (isText) {
            mBackgroundPaint.setColor(params.mDotColor);
            canvas.drawBitmap(mBackgroundWithShadow, -backgroundWithShadowSize / 2f, -backgroundWithShadowSize / 2f, mBackgroundPaint);
            canvas.drawText(count, 0, mTextHeight / 2f, mTextPaint);
        } else {
            mCirclePaint.setColor(params.mDotColor);
            canvas.drawCircle(0, 0, mCircleRadius, mCirclePaint);
        }
        canvas.restore();
    }

    public static class DrawParams {
        @ViewDebug.ExportedProperty(category = "notification dot", formatToHexString = true)
        private int mDotColor;
        /** The bounds of the icon that the dot is drawn on top of. */
        @ViewDebug.ExportedProperty(category = "notification dot")
        public Rect iconBounds = new Rect();
        /** The progress of the animation, from 0 to 1. */
        @ViewDebug.ExportedProperty(category = "notification dot")
        public float scale;
        /** Whether the dot should align to the top left of the icon rather than the top right. */
        @ViewDebug.ExportedProperty(category = "notification dot")
        public boolean leftAlign;

        //CUSTOM BADGE
        @ViewDebug.ExportedProperty(category = "notification dot")
        public boolean showCount;
        @ViewDebug.ExportedProperty(category = "notification dot")
        public int count;
        @ViewDebug.ExportedProperty(category = "notification dot")
        public int notificationKeys;

        @NonNull
        public IconShapeInfo shapeInfo = IconShapeInfo.DEFAULT;

        public PointF getDotPosition() {
            return leftAlign ? shapeInfo.leftCornerPosition : shapeInfo.rightCornerPosition;
        }

        /**
         * The color (possibly based on the icon) to use for the dot.
         */
        public void setDotColor(int color) {
            mDotColor = color;

            if (notificationDotContrastBorder() && luminance(color) < LUMINENSCE_LIMIT) {
                double[] lab = new double[3];
                ColorUtils.colorToLAB(color, lab);
                mDotColor = ColorUtils.LABToColor(100 * LUMINENSCE_LIMIT, lab[1], lab[2]);
            }
        }
    }

    /**
     * Class stores information about the icon icon shape on which the dot is being rendered.
     * It stores the center x and y position as a percentage (0 to 1) of the icon size
     */
    public record IconShapeInfo(PointF leftCornerPosition, PointF rightCornerPosition) {

        /**
         * Shape when the icon rendered completely fills {@link DrawParams#iconBounds}
         */
        public static IconShapeInfo DEFAULT =
                fromPath(IconShape.EMPTY.path, IconShape.EMPTY.pathSize);

        /**
         * Shape when a normalized icon is rendered within {@link DrawParams#iconBounds}
         */
        public static IconShapeInfo DEFAULT_NORMALIZED = new IconShapeInfo(
                normalizedPosition(DEFAULT.leftCornerPosition),
                normalizedPosition(DEFAULT.rightCornerPosition)
        );

        /**
         * Creates an IconShapeInfo from the provided path in bounds [0, 0, pathSize, pathSize]
         */
        public static IconShapeInfo fromPath(Path path, int pathSize) {
            return new IconShapeInfo(
                    getPathPoint(path, pathSize, -1),
                    getPathPoint(path, pathSize, 1));
        }

        private static PointF normalizedPosition(PointF pos) {
            float center = 0.5f;
            return new PointF(
                    center + ICON_VISIBLE_AREA_FACTOR * (pos.x - center),
                    center + ICON_VISIBLE_AREA_FACTOR * (pos.y - center)
            );
        }
    }
}

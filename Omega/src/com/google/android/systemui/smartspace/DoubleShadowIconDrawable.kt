package com.google.android.systemui.smartspace;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;

import com.android.internal.graphics.ColorUtils;

import com.statix.android.systemui.res.R;

public class DoubleShadowIconDrawable extends Drawable {
    public final int mAmbientShadowRadius;
    public final int mCanvasSize;
    public RenderNode mDoubleShadowNode;
    public InsetDrawable mIconDrawable;
    public final int mIconInsetSize;
    public final int mKeyShadowOffsetX;
    public final int mKeyShadowOffsetY;
    public final int mKeyShadowRadius;
    public boolean mShowShadow = true;

    public DoubleShadowIconDrawable(Context context) {
        this(
                context.getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size),
                context.getResources()
                        .getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_inset),
                context);
    }

    public DoubleShadowIconDrawable(int iconSize, int insetSize, Context context) {
        Resources resources = context.getResources();
        mIconInsetSize = insetSize;
        mCanvasSize = iconSize + 2 * insetSize;
        mAmbientShadowRadius = resources.getDimensionPixelSize(R.dimen.ambient_text_shadow_radius);
        mKeyShadowRadius = resources.getDimensionPixelSize(R.dimen.key_text_shadow_radius);
        mKeyShadowOffsetX = resources.getDimensionPixelSize(R.dimen.key_text_shadow_dx);
        mKeyShadowOffsetY = resources.getDimensionPixelSize(R.dimen.key_text_shadow_dy);
        setBounds(0, 0, mCanvasSize, mCanvasSize);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas.isHardwareAccelerated() && mDoubleShadowNode != null && mShowShadow) {
            if (!mDoubleShadowNode.hasDisplayList()) {
                Canvas recordingCanvas = mDoubleShadowNode.beginRecording();
                if (mIconDrawable != null) {
                    mIconDrawable.draw(recordingCanvas);
                }
                mDoubleShadowNode.endRecording();
            }
            canvas.drawRenderNode(mDoubleShadowNode);
        }
        if (mIconDrawable != null) {
            mIconDrawable.draw(canvas);
        }
    }

    public void setIcon(Drawable drawable) {
        if (drawable == null) {
            mIconDrawable = null;
            return;
        }

        mIconDrawable = new InsetDrawable(drawable, mIconInsetSize);
        mIconDrawable.setBounds(0, 0, mCanvasSize, mCanvasSize);

        if (mIconDrawable != null) {
            RenderNode shadowNode = new RenderNode("DoubleShadowNode");
            shadowNode.setPosition(0, 0, mCanvasSize, mCanvasSize);

            RenderEffect ambientShadowEffect =
                    RenderEffect.createColorFilterEffect(
                            new PorterDuffColorFilter(
                                    Color.argb(48, 0, 0, 0), PorterDuff.Mode.MULTIPLY),
                            RenderEffect.createOffsetEffect(
                                    0f,
                                    0f,
                                    RenderEffect.createBlurEffect(
                                            mAmbientShadowRadius,
                                            mAmbientShadowRadius,
                                            Shader.TileMode.CLAMP)));

            RenderEffect keyShadowEffect =
                    RenderEffect.createColorFilterEffect(
                            new PorterDuffColorFilter(
                                    Color.argb(72, 0, 0, 0), PorterDuff.Mode.MULTIPLY),
                            RenderEffect.createOffsetEffect(
                                    mKeyShadowOffsetX,
                                    mKeyShadowOffsetY,
                                    RenderEffect.createBlurEffect(
                                            mKeyShadowRadius,
                                            mKeyShadowRadius,
                                            Shader.TileMode.CLAMP)));

            if (ambientShadowEffect != null && keyShadowEffect != null) {
                shadowNode.setRenderEffect(
                        RenderEffect.createBlendModeEffect(
                                ambientShadowEffect, keyShadowEffect, BlendMode.DARKEN));
            }
            mDoubleShadowNode = shadowNode;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return mCanvasSize;
    }

    @Override
    public int getIntrinsicWidth() {
        return mCanvasSize;
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        if (mIconDrawable != null) {
            mIconDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mIconDrawable != null) {
            mIconDrawable.setColorFilter(colorFilter);
        }
    }

    public void setTint(int color) {
        if (mIconDrawable != null) {
            mIconDrawable.setTint(color);
        }
        mShowShadow = ColorUtils.calculateLuminance(color) > 0.5;
    }
}

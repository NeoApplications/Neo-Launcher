package com.saggitt.omega.smartspace

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R

open class DoubleShadowTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextView(context, attrs) {

    private var mDrawShadow = true
    private var mAmbientShadowBlur =
        context.resources.getDimensionPixelSize(R.dimen.ambient_text_shadow_radius)
    private var mAmbientShadowColor = context.resources.getColor(R.color.ambient_text_shadow_color)
    private var mKeyShadowBlur =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_radius)
    private var mKeyShadowColor = context.resources.getColor(R.color.key_text_shadow_color)
    private var mKeyShadowOffsetX =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_dx)
    private var mKeyShadowOffsetY =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_dy)

    init {
        updateDrawShadow(currentTextColor);
    }

    override fun onDraw(canvas: Canvas) {
        if (!this.mDrawShadow) {
            paint.clearShadowLayer();
            super.onDraw(canvas);
            return;
        }
        paint.setShadowLayer(mAmbientShadowBlur.toFloat(), 0.0f, 0.0f, this.mAmbientShadowColor);
        super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(scrollX, extendedPaddingTop + scrollY, width + scrollX, height + scrollY);
        paint.setShadowLayer(
            mKeyShadowBlur.toFloat(),
            mKeyShadowOffsetX.toFloat(), mKeyShadowOffsetY.toFloat(), this.mKeyShadowColor
        );
        super.onDraw(canvas);
        canvas.restore();
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        updateDrawShadow(color)
    }

    private fun updateDrawShadow(color: Int) {
        mDrawShadow = ColorUtils.calculateLuminance(color) > 0.5
    }
}

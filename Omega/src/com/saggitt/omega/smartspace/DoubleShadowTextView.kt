package com.saggitt.omega.smartspace

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import com.android.launcher3.views.DoubleShadowBubbleTextView.ShadowInfo


open class DoubleShadowTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextView(context, attrs) {

    private val mShadowInfo = ShadowInfo(context, attrs, 0)

    init {
        setShadowLayer(
            Math.max(
                mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffsetX,
                mShadowInfo.ambientShadowBlur
            ), 0f, 0f, mShadowInfo.keyShadowColor
        );
    }

    override fun onDraw(canvas: Canvas) {
        if (mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas)
            return
        }
        paint.setShadowLayer(
            mShadowInfo.ambientShadowBlur,
            0.0f,
            0.0f,
            mShadowInfo.ambientShadowColor
        )
        super.onDraw(canvas)
        paint.setShadowLayer(
            mShadowInfo.keyShadowBlur,
            0.0f,
            mShadowInfo.keyShadowOffsetX,
            mShadowInfo.keyShadowColor
        )
        super.onDraw(canvas)
    }
}

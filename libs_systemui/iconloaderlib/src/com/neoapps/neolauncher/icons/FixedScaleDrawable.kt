package com.neoapps.neolauncher.icons

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.DrawableWrapper
import android.util.AttributeSet
import androidx.core.graphics.withScale
import com.android.launcher3.icons.BaseIconFactory.Companion.LEGACY_ICON_SCALE
import org.xmlpull.v1.XmlPullParser

class FixedScaleDrawable : DrawableWrapper(ColorDrawable()) {
    private var mScaleX: Float
    private var mScaleY: Float

    init {
        mScaleX = LEGACY_ICON_SCALE
        mScaleY = LEGACY_ICON_SCALE
    }

    override fun draw(canvas: Canvas) {
        canvas.withScale(
            mScaleX, mScaleY,
            getBounds().exactCenterX(), getBounds().exactCenterY()
        ) {
            super.draw(canvas)
        }
    }

    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet) {}

    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Theme?
    ) {
    }

    fun setScale(scale: Float) {
        val h = intrinsicHeight.toFloat()
        val w = intrinsicWidth.toFloat()
        mScaleX = scale * LEGACY_ICON_SCALE
        mScaleY = scale * LEGACY_ICON_SCALE
        if (h > w && w > 0) {
            mScaleX *= w / h
        } else if (w > h && h > 0) {
            mScaleY *= h / w
        }
    }
}
package com.google.android.systemui.smartspace

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R

class DoubleShadowIconDrawable(icon: Drawable, context: Context) : LayerDrawable(emptyArray()) {
    private val iconDrawable: Drawable
    private val shadowDrawable: Drawable
    var mIconDrawable: InsetDrawable? = null
    val iconSize = context.resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size)
    private var iconInsetSize =
        context.resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_inset)
    private var keyShadowOffsetX =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_dx)
    private var keyShadowOffsetY =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_dy)
    private var keyShadowRadius =
        context.resources.getDimensionPixelSize(R.dimen.key_text_shadow_radius)
    private var ambientShadowRadius: Int =
        context.resources.getDimensionPixelSize(R.dimen.ambient_text_shadow_radius)
    private var canvasSize = 0

    init {
        val iconSize =
            context.resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size)
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, iconSize, iconSize)
        icon.draw(canvas)
        iconDrawable = BitmapDrawable(context.resources, bitmap)
        shadowDrawable = generateShadowDrawable(bitmap, context)
        addLayer(shadowDrawable)
        addLayer(iconDrawable)
        setBounds(0, 0, iconSize, iconSize)
    }

    override fun setAlpha(i: Int) {
        mIconDrawable!!.alpha = i
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mIconDrawable!!.colorFilter = colorFilter
    }

    private fun generateShadowDrawable(iconBitmap: Bitmap, context: Context): Drawable {
        val shadowBitmap =
            Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(shadowBitmap)
        val alphaOffset = IntArray(2)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        if (ambientShadowRadius.toFloat() != 0f) {
            shadowPaint.maskFilter =
                BlurMaskFilter(ambientShadowRadius.toFloat(), BlurMaskFilter.Blur.NORMAL)
            val alphaBitmap = iconBitmap.extractAlpha(shadowPaint, alphaOffset)
            alphaPaint.alpha = 64
            canvas.drawBitmap(
                alphaBitmap,
                alphaOffset[0].toFloat(),
                alphaOffset[1].toFloat(),
                alphaPaint
            )
        }
        if (keyShadowRadius.toFloat() != 0f) {
            shadowPaint.maskFilter =
                BlurMaskFilter(keyShadowRadius.toFloat(), BlurMaskFilter.Blur.NORMAL)
            val alphaBitmap = iconBitmap.extractAlpha(shadowPaint, alphaOffset)
            alphaPaint.alpha = 72
            canvas.drawBitmap(
                alphaBitmap,
                alphaOffset[0].toFloat() + keyShadowOffsetX,
                alphaOffset[1].toFloat() + keyShadowOffsetY,
                alphaPaint
            )
        }
        return BitmapDrawable(context.resources, shadowBitmap)
    }

    override fun setTint(tintColor: Int) {
        iconDrawable.setTint(tintColor)
        val tintLuminance = ColorUtils.calculateLuminance(tintColor)
        shadowDrawable.alpha = if (tintLuminance > 0.5) 255 else 0
    }
}

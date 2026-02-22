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
package com.android.launcher3.icons

import android.content.Context
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.UserHandle
import android.util.SparseArray
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.core.graphics.drawable.toDrawable
import com.android.launcher3.icons.BitmapInfo.Extender
import com.android.launcher3.icons.ColorExtractor.findDominantColorByHue
import com.android.launcher3.icons.GraphicsUtils.generateIconShape
import com.android.launcher3.icons.GraphicsUtils.transformed
import com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR
import com.android.launcher3.icons.ShadowGenerator.BLUR_FACTOR
import com.android.launcher3.util.FlagOp
import com.android.launcher3.util.UserIconInfo
import com.android.launcher3.util.UserIconInfo.Companion.TYPE_MAIN
import com.android.launcher3.util.UserIconInfo.Companion.TYPE_WORK
import com.android.systemui.shared.Flags.extendibleThemeManager
import com.neoapps.neolauncher.icons.CustomAdaptiveIconDrawable
import com.neoapps.neolauncher.icons.ExtendedBitmapDrawable.Companion.isFromIconPack
import com.neoapps.neolauncher.icons.FixedScaleDrawable
import com.neoapps.neolauncher.icons.IconPreferences
import java.lang.ref.WeakReference
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt


/**
 * This class will be moved to androidx library. There shouldn't be any dependency outside this
 * package.
 */
open class BaseIconFactory
@JvmOverloads
constructor(
    @JvmField val context: Context,
    @JvmField val fullResIconDpi: Int,
    @JvmField val iconBitmapSize: Int,
    private val drawFullBleedIcons: Boolean = false,
    var themeController: IconThemeController? = null,
) : AutoCloseable {

    private val cachedUserInfo = SparseArray<UserIconInfo>()
    private val prefs = IconPreferences(context)

    private val shadowGenerator: ShadowGenerator by lazy { ShadowGenerator(iconBitmapSize) }

    /** Default IconShape for when custom shape is not needed */
    val defaultIconShape: IconShape by
    lazy(LazyThreadSafetyMode.NONE) { getDefaultIconShape(iconBitmapSize) }

    @Suppress("deprecation")
    fun createIconBitmap(iconRes: ShortcutIconResource): BitmapInfo? {
        try {
            val resources = context.packageManager.getResourcesForApplication(iconRes.packageName)
            if (resources != null) {
                val id = resources.getIdentifier(iconRes.resourceName, null, null)
                // do not stamp old legacy shortcuts as the app may have already forgotten about it
                return createBadgedIconBitmap(resources.getDrawableForDensity(id, fullResIconDpi)!!)
            }
        } catch (e: Exception) {
            // Icon not found.
        }
        return null
    }

    /**
     * Create a placeholder icon using the passed in text.
     *
     * @param placeholder used for foreground element in the icon bitmap
     * @param color used for the foreground text color
     */
    fun createIconBitmap(placeholder: String, color: Int): BitmapInfo =
        createBadgedIconBitmap(
            AdaptiveIconDrawable(
                PLACEHOLDER_BACKGROUND_COLOR.toDrawable(),
                CenterTextDrawable(placeholder, color),
            ),
            IconOptions().setExtractedColor(color),
        )

    fun createIconBitmap(icon: Bitmap, isFullBleed: Boolean): BitmapInfo =
        if (iconBitmapSize != icon.width || iconBitmapSize != icon.height)
            createBadgedIconBitmap(
                icon.toDrawable(context.resources),
                IconOptions()
                    .setWrapNonAdaptiveIcon(prefs.shouldWrapAdaptive())
                    .setIconScale(1f)
                    .assumeFullBleedIcon(isFullBleed && isIconFullBleed(icon))
                    .setDrawFullBleed(isFullBleed && isIconFullBleed(icon)),
            )
        else
            BitmapInfo(
                icon = icon,
                color = findDominantColorByHue(icon),
                defaultIconShape = defaultIconShape,
                flags = if (isFullBleed && isIconFullBleed(icon)) BitmapInfo.FLAG_FULL_BLEED else 0,
            )

    fun createScaledBitmap(icon: Drawable, @BitmapGenerationMode mode: Int): Bitmap {
        val scale = FloatArray(1)
        val newIcon = normalizeAndWrapToAdaptiveIcon(icon, scale)
        val iconOptions = IconOptions().setBitmapGenerationMode(mode).setDrawFullBleed(false)
        iconOptions.iconScale = scale[0]

        return createBadgedIconBitmap(
            newIcon,
            IconOptions().setBitmapGenerationMode(mode).setDrawFullBleed(false),
        ).icon
    }

    fun normalizeAndWrapToAdaptiveIcon(
        icon: Drawable?,
        outScale: FloatArray
    ): Drawable? {
        if (icon == null) {
            return null
        }
        val isFromIconPack = icon.isFromIconPack
        val shouldWrapAdaptive = !isFromIconPack && IconPreferences(context).shouldWrapAdaptive()
        val shrinkNonAdaptiveIcons = IconProvider.ATLEAST_OMR1 && shouldWrapAdaptive

        val scale: Float
        if (shrinkNonAdaptiveIcons && icon !is AdaptiveIconDrawable) {
            scale = IconNormalizer(iconBitmapSize).getScale(icon)
            val wrapperBackgroundColor: Int = IconPreferences(context).getWrapperBackgroundColor(icon)
            val foreground = FixedScaleDrawable()
            foreground.apply {
                drawable = icon
                setScale(scale)
            }
            val wrapper =
                CustomAdaptiveIconDrawable(wrapperBackgroundColor.toDrawable(), foreground)
            outScale[0] = IconNormalizer(iconBitmapSize).getScale(wrapper)
            return wrapper
        } else {
            if (icon is AdaptiveIconDrawable) {
                outScale[0] = ICON_VISIBLE_AREA_FACTOR
                return icon
            }
            if (shouldWrapAdaptive) {
                outScale[0] = ICON_VISIBLE_AREA_FACTOR
                return wrapToAdaptiveIcon(icon)
            } else {
                scale = IconNormalizer(iconBitmapSize).getScale(icon)
                outScale[0] = scale
                return icon
            }
        }
    }

    @JvmOverloads
    @Deprecated("Use createBadgedIconBitmap instead")
    fun createIconBitmap(
        icon: Drawable?,
        scale: Float,
        @BitmapGenerationMode bitmapGenerationMode: Int = MODE_DEFAULT,
        isFullBleed: Boolean = drawFullBleedIcons,
    ): Bitmap =
        createBadgedIconBitmap(
            icon,
            IconOptions()
                .setBitmapGenerationMode(bitmapGenerationMode)
                .setWrapNonAdaptiveIcon(prefs.shouldWrapAdaptive())
                .setDrawFullBleed(isFullBleed)
                .setIconScale(scale),
        )
            .icon

    /**
     * Creates bitmap using the source drawable and various parameters. The bitmap is visually
     * normalized with other icons and has enough spacing to add shadow.
     *
     * @param icon source of the icon
     * @return a bitmap suitable for displaying as an icon at various system UIs.
     */
    @JvmOverloads
    fun createBadgedIconBitmap(icon: Drawable?, options: IconOptions = IconOptions()): BitmapInfo {
        if (icon == null) {
            return BitmapInfo(
                icon =
                    if (options.useHardware)
                        BitmapRenderer.createHardwareBitmap(iconBitmapSize, iconBitmapSize) {}
                    else Bitmap.createBitmap(iconBitmapSize, iconBitmapSize, ARGB_8888),
                color = 0,
            )
        }

        // Create the bitmap first
        val oldBounds = icon.bounds
        val scale = FloatArray(1)
        var tempIcon: Drawable = icon
        if (options.isFullBleed && icon is BitmapDrawable) {
            // If the source is a full-bleed icon, create an adaptive icon by insetting this icon to
            // the extra padding
            var inset = AdaptiveIconDrawable.getExtraInsetFraction()
            inset /= (1 + 2 * inset)
            tempIcon =
                AdaptiveIconDrawable(
                    Color.BLACK.toDrawable(),
                    InsetDrawable(icon, inset, inset, inset, inset),
                )
        }

        options.setWrapperBackgroundColor(IconPreferences(context).getWrapperBackgroundColor(icon))
        if (prefs.shouldWrapAdaptive()) tempIcon = wrapToAdaptiveIcon(tempIcon, options)

        val drawFullBleed = options.drawFullBleed ?: drawFullBleedIcons

        val bitmap = drawableToBitmap(tempIcon, drawFullBleed, options)
        icon.bounds = oldBounds

        val color = options.extractedColor ?: findDominantColorByHue(bitmap)
        var flagOp = getBitmapFlagOp(options)
        if (drawFullBleed) {
            flagOp = flagOp.addFlag(BitmapInfo.FLAG_FULL_BLEED)
            bitmap.setHasAlpha(false)
        }

        var info =
            BitmapInfo(
                icon = bitmap,
                color = color,
                defaultIconShape = defaultIconShape,
                flags = flagOp.apply(0),
            )
        if (icon is Extender) {
            info = icon.getUpdatedBitmapInfo(info, this)
        }

        if (IconProvider.ATLEAST_T && themeController != null) {
            info =
                info.copy(
                    themedBitmap =
                        if (tempIcon is AdaptiveIconDrawable)
                            themeController!!.createThemedBitmap(
                                tempIcon,
                                info,
                                this,
                                options.sourceHint,
                            )
                        else ThemedBitmap.NOT_SUPPORTED
                )
        } else if (extendibleThemeManager()) {
            info = info.copy(themedBitmap = ThemedBitmap.NOT_SUPPORTED)
        }

        return info
    }

    fun getBitmapFlagOp(options: IconOptions?): FlagOp {
        if (options == null) return FlagOp.NO_OP
        var op = FlagOp.NO_OP
        if (options.isInstantApp) op = op.addFlag(BitmapInfo.FLAG_INSTANT)

        val info = options.userIconInfo ?: options.userHandle?.let { getUserInfo(it) }
        if (info != null) op = info.applyBitmapInfoFlags(op)
        return op
    }

    protected open fun getUserInfo(user: UserHandle): UserIconInfo {
        val key = user.hashCode()
        // We do not have the ability to distinguish between different badged users here.
        // As such all badged users will have the work profile badge applied.
        return cachedUserInfo[key]
            ?: UserIconInfo(user, if (user.isWorkUser()) TYPE_WORK else TYPE_MAIN).also {
                cachedUserInfo[key] = it
            }
    }

    /** Simple check to check if the provided user is work profile or not based on badging */
    private fun UserHandle.isWorkUser() =
        NoopDrawable().let { d -> d !== context.packageManager.getUserBadgedIcon(d, this) }

    private fun isIconFullBleed(icon: Bitmap): Boolean {
        return icon.height == icon.width && !icon.hasAlpha()
    }

    /**
     * Wraps this drawable in [InsetDrawable] such that the final drawable has square bounds, while
     * preserving the aspect ratio of the source
     *
     * @param scale additional scale on the source drawable
     */
    private fun Drawable.wrapIntoSquareDrawable(scale: Float): Drawable {
        val h = intrinsicHeight.toFloat()
        val w = intrinsicWidth.toFloat()
        var scaleX = scale
        var scaleY = scale
        if (h > w && w > 0) {
            scaleX *= w / h
        } else if (w > h && h > 0) {
            scaleY *= h / w
        }
        scaleX = (1 - scaleX) / 2
        scaleY = (1 - scaleY) / 2
        return InsetDrawable(this, scaleX, scaleY, scaleX, scaleY)
    }

    /** Wraps the provided icon in an adaptive icon drawable */
    @JvmOverloads
    fun wrapToAdaptiveIcon(icon: Drawable, options: IconOptions? = null): AdaptiveIconDrawable
    {
        if(icon is AdaptiveIconDrawable) return icon
        else{
            val iconBackground = IconPreferences(context).getWrapperBackgroundColor(icon)
            val scale = options?.iconScale ?: IconNormalizer(iconBitmapSize).getScale(icon)
            val dr = CustomAdaptiveIconDrawable(
                iconBackground.toDrawable(),
                createScaledDrawable(icon, scale * LEGACY_ICON_SCALE))
            dr.setBounds(0, 0, 1, 1)
            return dr

        }
    }

    private fun createScaledDrawable(@NonNull main: Drawable, scale: Float): Drawable {
        val h = main.intrinsicHeight.toFloat()
        val w = main.intrinsicWidth.toFloat()
        var scaleX = scale
        var scaleY = scale
        if (h > w && w > 0) {
            scaleX *= w / h
        } else if (w > h && h > 0) {
            scaleY *= h / w
        }
        scaleX = (1 - scaleX) / 2
        scaleY = (1 - scaleY) / 2
        return InsetDrawable(main, scaleX, scaleY, scaleX, scaleY)
    }

    private fun drawableToBitmap(
        icon: Drawable,
        drawFullBleed: Boolean,
        options: IconOptions,
    ): Bitmap {
        if (icon is AdaptiveIconDrawable) {
            // We are ignoring KEY_SHADOW_DISTANCE because regular icons ignore this at the
            // moment b/298203449
            val offset =
                if (drawFullBleed) 0
                else
                    max(
                        (ceil(BLUR_FACTOR * iconBitmapSize)).toInt(),
                        Math.round(iconBitmapSize * (1 - options.iconScale) / 2),
                    )
            // b/211896569: AdaptiveIconDrawable do not work properly for non top-left bounds
            val newBounds = iconBitmapSize - offset * 2
            icon.setBounds(0, 0, newBounds, newBounds)
            return createBitmap(options) { canvas, _ ->
                canvas.transformed {
                    translate(offset.toFloat(), offset.toFloat())
                    if (options.addShadows && !drawFullBleed)
                        shadowGenerator.addPathShadow(icon.iconMask, canvas)
                    if (icon is Extender) icon.drawForPersistence()

                    if (drawFullBleed) {
                        drawColor(Color.BLACK)
                        icon.background?.draw(canvas)
                        icon.foreground?.draw(canvas)
                    } else {
                        icon.draw(canvas)
                    }
                }
            }
        } else {
            if (icon is BitmapDrawable && icon.bitmap?.density == Bitmap.DENSITY_NONE) {
                icon.setTargetDensity(context.resources.displayMetrics)
            }
            val iconToDraw =
                if (icon.intrinsicWidth != icon.intrinsicHeight || options.iconScale != 1f)
                    icon.wrapIntoSquareDrawable(options.iconScale)
                else icon
            iconToDraw.setBounds(0, 0, iconBitmapSize, iconBitmapSize)

            return createBitmap(options) { canvas, bitmap ->
                if (drawFullBleed) canvas.drawColor(Color.BLACK)
                iconToDraw.draw(canvas)

                if (options.addShadows && bitmap != null && !drawFullBleed) {
                    // Shadow extraction only works in software mode
                    shadowGenerator.drawShadow(bitmap, canvas)

                    // Draw the icon again on top
                    iconToDraw.draw(canvas)
                }
            }
        }
    }

    private fun createBitmap(options: IconOptions, block: (Canvas, Bitmap?) -> Unit): Bitmap {
        if (options.useHardware) {
            return BitmapRenderer.createHardwareBitmap(iconBitmapSize, iconBitmapSize) {
                block.invoke(it, null)
            }
        }

        val result = Bitmap.createBitmap(iconBitmapSize, iconBitmapSize, ARGB_8888)
        block.invoke(Canvas(result), result)
        return result
    }

    override fun close() = clear()

    protected fun clear() {}

    fun makeDefaultIcon(iconProvider: IconProvider): BitmapInfo {
        return createBadgedIconBitmap(iconProvider.getFullResDefaultActivityIcon(fullResIconDpi))
    }

    class IconOptions {

        internal var isInstantApp: Boolean = false
        internal var isFullBleed: Boolean = false

        internal var userHandle: UserHandle? = null
        internal var userIconInfo: UserIconInfo? = null

        @ColorInt
        internal var extractedColor: Int? = null
        internal var sourceHint: SourceHint? = null
        internal var wrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND

        internal var useHardware = false
        internal var addShadows = true
        internal var drawFullBleed: Boolean? = null
        internal var iconScale = ICON_VISIBLE_AREA_FACTOR
        internal var wrapNonAdaptiveIcon = true

        /** User for this icon, in case of badging */
        fun setUser(user: UserHandle?) = apply { userHandle = user }

        /** User for this icon, in case of badging */
        fun setUser(user: UserIconInfo?) = apply { userIconInfo = user }

        /** If this icon represents an instant app */
        fun setInstantApp(instantApp: Boolean) = apply { isInstantApp = instantApp }

        /**
         * If the icon is [BitmapDrawable], assumes that it is a full bleed icon and tries to shape
         * it accordingly
         */
        fun assumeFullBleedIcon(isFullBleed: Boolean) = apply { this.isFullBleed = isFullBleed }

        /** Disables auto color extraction and overrides the color to the provided value */
        fun setExtractedColor(@ColorInt color: Int) = apply { extractedColor = color }

        /**
         * Sets the bitmap generation mode to use for the bitmap info. Note that some generation
         * modes do not support color extraction, so consider setting a extracted color manually in
         * those cases.
         */
        fun setBitmapGenerationMode(@BitmapGenerationMode generationMode: Int) =
            setUseHardware((generationMode and MODE_HARDWARE) != 0)
                .setAddShadows((generationMode and MODE_WITH_SHADOW) != 0)

        /** User for this icon, in case of badging */
        fun setSourceHint(sourceHint: SourceHint?) = apply { this.sourceHint = sourceHint }

        /** Sets the background color used for wrapped adaptive icon */
        fun setWrapperBackgroundColor(color: Int) = apply {
            wrapperBackgroundColor =
                if (Color.alpha(color) < 255) DEFAULT_WRAPPER_BACKGROUND else color
        }

        /** Sets if hardware bitmap should be generated as the output */
        fun setUseHardware(hardware: Boolean) = apply { useHardware = hardware }

        /** Sets if shadows should be added as part of BitmapInfo generation */
        fun setAddShadows(shadows: Boolean) = apply { addShadows = shadows }

        /**
         * Sets if the bitmap info should be drawn full-bleed or not. Defaults to the IconFactory
         * constructor parameter.
         */
        fun setDrawFullBleed(fullBleed: Boolean) = apply { drawFullBleed = fullBleed }

        /** Sets how much tos cale down the icon when creating the bitmap */
        fun setIconScale(scale: Float) = apply { iconScale = scale }

        /** Sets if a non-adaptive icon should be wrapped into an adaptive icon or not */
        fun setWrapNonAdaptiveIcon(wrap: Boolean) = apply { wrapNonAdaptiveIcon = wrap }
    }

    private class NoopDrawable : ColorDrawable() {
        override fun getIntrinsicHeight(): Int = 1

        override fun getIntrinsicWidth(): Int = 1
    }

    private class CenterTextDrawable(private val mText: String, color: Int) : ColorDrawable() {
        private val textBounds = Rect()
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).also { it.color = color }

        override fun draw(canvas: Canvas) {
            val bounds = bounds
            textPaint.textSize = bounds.height() / 3f
            textPaint.getTextBounds(mText, 0, mText.length, textBounds)
            canvas.drawText(
                mText,
                bounds.exactCenterX() - textBounds.exactCenterX(),
                bounds.exactCenterY() - textBounds.exactCenterY(),
                textPaint,
            )
        }
    }

    companion object {
        private const val DEFAULT_WRAPPER_BACKGROUND = Color.WHITE

        // Ratio of icon visible area to full icon size for a square shaped icon
        private const val MAX_SQUARE_AREA_FACTOR = 375.0 / 576

        val LEGACY_ICON_SCALE =
            sqrt(MAX_SQUARE_AREA_FACTOR).toFloat() *
                    .7f *
                    (1f / (1 + 2 * AdaptiveIconDrawable.getExtraInsetFraction()))

        const val MODE_DEFAULT: Int = 0
        const val MODE_WITH_SHADOW: Int = 1
        const val MODE_HARDWARE: Int = 1 shl 1
        const val MODE_HARDWARE_WITH_SHADOW: Int = MODE_HARDWARE or MODE_WITH_SHADOW

        @Retention(SOURCE)
        @IntDef(
            value = [MODE_DEFAULT, MODE_WITH_SHADOW, MODE_HARDWARE_WITH_SHADOW, MODE_HARDWARE],
            flag = true,
        )
        annotation class BitmapGenerationMode

        private const val ICON_BADGE_SCALE = 0.444f

        private val PLACEHOLDER_BACKGROUND_COLOR = Color.rgb(245, 245, 245)

        /** Returns the correct badge size given an icon size */
        @JvmStatic
        fun getBadgeSizeForIconSize(iconSize: Int): Int {
            return (ICON_BADGE_SCALE * iconSize).toInt()
        }

        /** Cache of default icon shape keyed to the path size */
        private val defaultIconShapeCache = SparseArray<WeakReference<IconShape>>()

        private fun getDefaultIconShape(size: Int): IconShape {
            synchronized(defaultIconShapeCache) {
                val cachedShape = defaultIconShapeCache[size]?.get()
                if (cachedShape != null) return cachedShape

                val generatedShape =
                    generateIconShape(
                        size,
                        AdaptiveIconDrawable(ColorDrawable(Color.BLACK), null)
                            .apply { setBounds(0, 0, size, size) }
                            .iconMask,
                    )

                defaultIconShapeCache[size] = WeakReference(generatedShape)
                return generatedShape
            }
        }
    }
}

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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import com.android.launcher3.icons.cache.CacheLookupFlag
import com.android.launcher3.util.FlagOp
open class BitmapInfo(
    @JvmField val icon: Bitmap,
    @JvmField val color: Int,
    @BitmapInfoFlags @JvmField var flags: Int = 0,
    var themedBitmap: ThemedBitmap? = null,
) {
    @IntDef(
        flag = true,
        value = [FLAG_WORK, FLAG_INSTANT, FLAG_CLONE, FLAG_PRIVATE, FLAG_WRAPPED_NON_ADAPTIVE],
    )
    internal annotation class BitmapInfoFlags
    @IntDef(flag = true, value = [FLAG_THEMED, FLAG_NO_BADGE, FLAG_SKIP_USER_BADGE])
    annotation class DrawableCreationFlags
    // b/377618519: These are saved to debug why work badges sometimes don't show up on work apps
    @DrawableCreationFlags @JvmField var creationFlags: Int = 0
    private var badgeInfo: BitmapInfo? = null
    fun withBadgeInfo(badgeInfo: BitmapInfo?) = clone().also { it.badgeInfo = badgeInfo }
    /** Returns a bitmapInfo with the flagOP applied */
    fun withFlags(op: FlagOp): BitmapInfo {
        if (op === FlagOp.NO_OP) {
            return this
        }
        return clone().also { it.flags = op.apply(it.flags) }
    }
    @Override
    open fun clone(): BitmapInfo {
        return copyInternalsTo(BitmapInfo(icon, color))
    }
    protected fun copyInternalsTo(target: BitmapInfo): BitmapInfo {
        target.themedBitmap = themedBitmap
        target.flags = flags
        target.badgeInfo = badgeInfo
        return target
    }
    // TODO: rename or remove because icon can no longer be null?
    val isNullOrLowRes: Boolean
        get() = icon == LOW_RES_ICON
    val isLowRes: Boolean
        get() = matchingLookupFlag.useLowRes()
    open val matchingLookupFlag: CacheLookupFlag
        /** Returns the lookup flag to match this current state of this info */
        get() =
            CacheLookupFlag.DEFAULT_LOOKUP_FLAG.withUseLowRes(LOW_RES_ICON == icon)
                .withThemeIcon(themedBitmap != null)
    /** BitmapInfo can be stored on disk or other persistent storage */
    open fun canPersist(): Boolean {
        return !isNullOrLowRes
    }
    /** Creates a drawable for the provided BitmapInfo */
    @JvmOverloads
    fun newIcon(
        context: Context,
        @DrawableCreationFlags creationFlags: Int = 0,
    ): FastBitmapDrawable {
        return newIcon(context, creationFlags, null)
    }
    /**
     * Creates a drawable for the provided BitmapInfo
     *
     * @param context Context
     * @param creationFlags Flags for creating the FastBitmapDrawable
     * @param badgeShape Optional Path for masking icon badges to a shape. Should be 100x100.
     * @return FastBitmapDrawable
     */
    open fun newIcon(
        context: Context,
        @DrawableCreationFlags creationFlags: Int,
        badgeShape: Path?,
    ): FastBitmapDrawable {
        val drawable: FastBitmapDrawable =
            if (isLowRes) {
                PlaceHolderIconDrawable(this, context)
            } else if (
                (creationFlags and FLAG_THEMED) != 0 &&
                themedBitmap != null &&
                themedBitmap !== ThemedBitmap.NOT_SUPPORTED
            ) {
                themedBitmap!!.newDrawable(this, context)
            } else {
                FastBitmapDrawable(this)
            }
        applyFlags(context, drawable, creationFlags, badgeShape)
        return drawable
    }
    protected fun applyFlags(
        context: Context, drawable: FastBitmapDrawable,
        @DrawableCreationFlags creationFlags: Int, badgeShape: Path?
    ) {
        this.creationFlags = creationFlags
        drawable.disabledAlpha = GraphicsUtils.getFloat(context, R.attr.disabledIconAlpha, 1f)
        drawable.creationFlags = creationFlags
        if ((creationFlags and FLAG_NO_BADGE) == 0) {
            val badge = getBadgeDrawable(
                context, (creationFlags and FLAG_THEMED) != 0,
                (creationFlags and FLAG_SKIP_USER_BADGE) != 0, badgeShape
            )
            if (badge != null) {
                drawable.badge = badge
            }
        }
    }
    /**
     * Gets Badge drawable based on current flags
     *
     * @param context Context
     * @param isThemed If Drawable is themed.
     * @param badgeShape Optional Path to mask badges to a shape. Should be 100x100.
     * @return Drawable for the badge.
     */
    fun getBadgeDrawable(context: Context, isThemed: Boolean, badgeShape: Path?): Drawable? {
        return getBadgeDrawable(context, isThemed, false, badgeShape)
    }
    /**
     * Creates a Drawable for an icon badge for this BitmapInfo
     * @param context Context
     * @param isThemed If the drawable is themed.
     * @param skipUserBadge If should skip User Profile badging.
     * @param badgeShape Optional Path to mask badge Drawable to a shape. Should be 100x100.
     * @return Drawable for an icon Badge.
     */
    private fun getBadgeDrawable(
        context: Context, isThemed: Boolean, skipUserBadge: Boolean, badgeShape: Path?
    ): Drawable? {
        if (badgeInfo != null) {
            var creationFlag = if (isThemed) FLAG_THEMED else 0
            if (skipUserBadge) {
                creationFlag = creationFlag or FLAG_SKIP_USER_BADGE
            }
            return badgeInfo!!.newIcon(context, creationFlag, badgeShape)
        }
        if (skipUserBadge) {
            return null
        } else {
            getBadgeDrawableInfo()?.let {
                return UserBadgeDrawable(
                    context,
                    it.drawableRes,
                    it.colorRes,
                    isThemed,
                    badgeShape
                )
            }
        }
        return null
    }
    /**
     * Returns information about the badge to apply based on current flags.
     */
    fun getBadgeDrawableInfo(): BadgeDrawableInfo? {
        return when {
            (flags and FLAG_INSTANT) != 0 -> BadgeDrawableInfo(
                R.drawable.ic_instant_app_badge,
                R.color.badge_tint_instant
            )
            (flags and FLAG_WORK) != 0 -> BadgeDrawableInfo(
                R.drawable.ic_work_app_badge,
                R.color.badge_tint_work
            )
            (flags and FLAG_CLONE) != 0 -> BadgeDrawableInfo(
                R.drawable.ic_clone_app_badge,
                R.color.badge_tint_clone
            )
            (flags and FLAG_PRIVATE) != 0 -> BadgeDrawableInfo(
                R.drawable.ic_private_profile_app_badge,
                R.color.badge_tint_private
            )
            else -> null
        }
    }
    /** Interface to be implemented by drawables to provide a custom BitmapInfo */
    interface Extender {
        /** Called for creating a custom BitmapInfo */
        fun getExtendedInfo(
            bitmap: Bitmap?,
            color: Int,
            iconFactory: BaseIconFactory?,
            normalizationScale: Float,
        ): BitmapInfo?
        /** Called to draw the UI independent of any runtime configurations like time or theme */
        fun drawForPersistence(canvas: Canvas?)
    }
    /**
     * Drawables backing a specific badge shown on app icons.
     * @param drawableRes Drawable resource for the badge.
     * @param colorRes Color resource to tint the badge.
     */
    @JvmRecord
    data class BadgeDrawableInfo(
        @field:DrawableRes @param:DrawableRes val drawableRes: Int,
        @field:ColorRes @param:ColorRes val colorRes: Int
    )
    companion object {
        const val TAG: String = "BitmapInfo"
        // BitmapInfo flags
        const val FLAG_WORK: Int = 1 shl 0
        const val FLAG_INSTANT: Int = 1 shl 1
        const val FLAG_CLONE: Int = 1 shl 2
        const val FLAG_PRIVATE: Int = 1 shl 3
        const val FLAG_WRAPPED_NON_ADAPTIVE: Int = 1 shl 4
        // Drawable creation flags
        const val FLAG_THEMED: Int = 1 shl 0
        const val FLAG_NO_BADGE: Int = 1 shl 1
        const val FLAG_SKIP_USER_BADGE: Int = 1 shl 2
        @JvmField
        val LOW_RES_ICON: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        @JvmField
        val LOW_RES_INFO: BitmapInfo = fromBitmap(LOW_RES_ICON)
        @JvmStatic
        fun fromBitmap(bitmap: Bitmap): BitmapInfo {
            return of(bitmap, 0)
        }
        @JvmStatic
        fun of(bitmap: Bitmap, color: Int): BitmapInfo {
            return BitmapInfo(bitmap, color)
        }
    }
}
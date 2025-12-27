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
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import com.android.launcher3.icons.BitmapInfo.Companion.FLAG_THEMED
import com.android.launcher3.icons.FastBitmapDrawableDelegate.DelegateFactory
import com.android.launcher3.icons.FastBitmapDrawableDelegate.SimpleDelegateFactory
import com.android.launcher3.icons.PlaceHolderDrawableDelegate.PlaceHolderDelegateFactory
import com.android.launcher3.icons.cache.CacheLookupFlag
import com.android.launcher3.util.FlagOp

/**
 * Data class that holds all the information needed to create an icon drawable.
 *
 * @property icon the bitmap of the icon.
 * @property color the color of the icon.
 * @property flags extra source information associated with this icon
 * @property defaultIconShape the fallback shape when no shape is provided during icon creation
 * @property themedBitmap theming information if the icon is created using [FLAG_THEMED]
 * @property delegateFactory factory used for icon creation
 * @property badgeInfo optional badge drawn on the icon
 */
data class BitmapInfo(
    @JvmField val icon: Bitmap,
    @JvmField val color: Int,
    @BitmapInfoFlags var flags: Int = 0,
    val defaultIconShape: IconShape = IconShape.EMPTY,
    var themedBitmap: ThemedBitmap? = null,
    var badgeInfo: BitmapInfo? = null,
    val delegateFactory: DelegateFactory = SimpleDelegateFactory,
) {
    @IntDef(
        flag = true,
        value = [FLAG_WORK, FLAG_INSTANT, FLAG_CLONE, FLAG_PRIVATE, FLAG_FULL_BLEED],
    )
    internal annotation class BitmapInfoFlags

    @IntDef(
        flag = true,
        value = [FLAG_THEMED, FLAG_NO_BADGE, FLAG_SKIP_USER_BADGE, FLAG_CUSTOM_SHAPE]
    )
    annotation class DrawableCreationFlags

    fun withBadgeInfo(badgeInfo: BitmapInfo?) = copy(badgeInfo = badgeInfo)

    protected fun copyInternalsTo(target: BitmapInfo): BitmapInfo {
        target.flags = flags
        target.badgeInfo = badgeInfo
        return target
    }

    fun clone(): BitmapInfo {
        return copyInternalsTo(BitmapInfo(icon, color))
    }

    /** Returns a bitmapInfo with the flagOP applied */
    fun withFlags(op: FlagOp): BitmapInfo =
        if (op === FlagOp.NO_OP) this else copy(flags = op.apply(this.flags))

    val isLowRes: Boolean
        get() = matchingLookupFlag.useLowRes()

    val matchingLookupFlag: CacheLookupFlag
        /** Returns the lookup flag to match this current state of this info */
        get() =
            CacheLookupFlag.DEFAULT_LOOKUP_FLAG.withUseLowRes(LOW_RES_ICON == icon)
                .withThemeIcon(themedBitmap != null)

    /** BitmapInfo can be stored on disk or other persistent storage */
    fun canPersist(): Boolean {
        return !isLowRes && delegateFactory == SimpleDelegateFactory
    }

    /**
     * Creates a drawable for the provided BitmapInfo
     *
     * @param context Context
     * @param creationFlags Flags for creating the FastBitmapDrawable
     * @param iconShape information for custom Icon Shapes, to use with Full-bleed icons.
     * @return FastBitmapDrawable
     */
    @JvmOverloads
    fun newIcon(
        context: Context,
        @DrawableCreationFlags creationFlags: Int = 0,
        iconShape: IconShape? = null,
    ) =
        FastBitmapDrawable(
            info = this,
            iconShape = iconShape ?: defaultIconShape,
            delegateFactory =
                when {
                    isLowRes -> PlaceHolderDelegateFactory(context)
                    creationFlags.hasMask(FLAG_THEMED) &&
                            themedBitmap != null &&
                            themedBitmap !== ThemedBitmap.NOT_SUPPORTED ->
                        themedBitmap!!.newDelegateFactory(this, context)

                    else -> delegateFactory
                },
            disabledAlpha = GraphicsUtils.getFloat(context, R.attr.disabledIconAlpha, 1f),
            creationFlags = if (iconShape != null) {
                creationFlags.or(FLAG_CUSTOM_SHAPE)
            } else {
                creationFlags
            },
            badge =
                if (!creationFlags.hasMask(FLAG_NO_BADGE)) {
                    getBadgeDrawable(
                        context,
                        creationFlags.hasMask(FLAG_THEMED),
                        creationFlags.hasMask(FLAG_SKIP_USER_BADGE),
                    )
                } else null,
        )

    /**
     * Gets Badge drawable based on current flags
     *
     * @param context Context
     * @param isThemed If Drawable is themed.
     */
    fun getBadgeDrawable(context: Context, isThemed: Boolean): Drawable? {
        return getBadgeDrawable(context, isThemed, false)
    }

    /**
     * Creates a Drawable for an icon badge for this BitmapInfo
     *
     * @param context Context
     * @param isThemed If the drawable is themed.
     * @param skipUserBadge If should skip User Profile badging.
     */
    fun getBadgeDrawable(
        context: Context,
        isThemed: Boolean,
        skipUserBadge: Boolean,
    ): Drawable? {
        if (badgeInfo != null) {
            var creationFlag = if (isThemed) FLAG_THEMED else 0
            if (skipUserBadge) {
                creationFlag = creationFlag or FLAG_SKIP_USER_BADGE
            }
            return badgeInfo!!.newIcon(context, creationFlag, null)
        }
        if (skipUserBadge) {
            return null
        } else {
            getBadgeDrawableInfo()?.let {
                return UserBadgeDrawable(context, it.drawableRes, it.colorRes, isThemed)
            }
        }
        return null
    }

    /** Returns information about the badge to apply based on current flags. */
    fun getBadgeDrawableInfo(): BadgeDrawableInfo? {
        return when {
            flags.hasMask(FLAG_INSTANT) ->
                BadgeDrawableInfo(R.drawable.ic_instant_app_badge, R.color.badge_tint_instant)

            flags.hasMask(FLAG_WORK) ->
                BadgeDrawableInfo(R.drawable.ic_work_app_badge, R.color.badge_tint_work)

            flags.hasMask(FLAG_CLONE) ->
                BadgeDrawableInfo(R.drawable.ic_clone_app_badge, R.color.badge_tint_clone)

            flags.hasMask(FLAG_PRIVATE) ->
                BadgeDrawableInfo(
                    R.drawable.ic_private_profile_app_badge,
                    R.color.badge_tint_private,
                )
            else -> null
        }
    }

    /**
     * Checks for FLAG_FULL_BLEED from factory as well as checking bitmap content to verify.
     */
    fun isFullBleed(): Boolean {
        return flags.hasMask(FLAG_FULL_BLEED)
    }

    /** Interface to be implemented by drawables to customize a BitmapInfo */
    interface Extender {

        /** Returns an update [BitmapInfo] replacing the existing [info] */
        fun getUpdatedBitmapInfo(info: BitmapInfo, factory: BaseIconFactory): BitmapInfo

        /** Called to draw the UI independent of any runtime configurations like time or theme */
        fun drawForPersistence()
    }

    /**
     * Drawables backing a specific badge shown on app icons.
     *
     * @param drawableRes Drawable resource for the badge.
     * @param colorRes Color resource to tint the badge.
     */
    @JvmRecord
    data class BadgeDrawableInfo(
        @field:DrawableRes @param:DrawableRes val drawableRes: Int,
        @field:ColorRes @param:ColorRes val colorRes: Int,
    )

    companion object {
        const val TAG: String = "BitmapInfo"

        // Persisted BitmapInfo flags.
        // Reset the cache by changing RELEASE_VERSION whenever making any changes here.
        // LINT.IfChange
        const val FLAG_WORK: Int = 1 shl 0
        const val FLAG_INSTANT: Int = 1 shl 1
        const val FLAG_CLONE: Int = 1 shl 2
        const val FLAG_PRIVATE: Int = 1 shl 3
        const val FLAG_FULL_BLEED: Int = 1 shl 4
        // LINT.ThenChange(src/com/android/launcher3/icons/cache/BaseIconCache.kt:cache_release_version)

        // Drawable creation flags
        const val FLAG_THEMED: Int = 1 shl 0
        const val FLAG_NO_BADGE: Int = 1 shl 1
        const val FLAG_SKIP_USER_BADGE: Int = 1 shl 2
        const val FLAG_CUSTOM_SHAPE: Int = 1 shl 3

        @JvmField
        val LOW_RES_ICON: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        @JvmField
        val LOW_RES_INFO: BitmapInfo = fromBitmap(LOW_RES_ICON)

        @JvmStatic
        fun fromBitmap(bitmap: Bitmap): BitmapInfo {
            return of(bitmap, 0, IconShape.EMPTY)
        }

        @JvmStatic
        fun of(bitmap: Bitmap, color: Int, defaultShape: IconShape = IconShape.EMPTY): BitmapInfo {
            return BitmapInfo(icon = bitmap, color = color, defaultIconShape = defaultShape)
        }

        private inline fun Int.hasMask(mask: Int) = (this and mask) != 0
    }
}

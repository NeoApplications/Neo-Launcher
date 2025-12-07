package com.saggitt.omega.iconpack

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.mono.ThemedIconDrawable
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.minSDK
import com.saggitt.omega.util.prefs
import com.neoapps.neolauncher.icons.ClockMetadata
import com.neoapps.neolauncher.icons.CustomAdaptiveIconDrawable
import com.neoapps.neolauncher.icons.IconPreferences

class IconPackProvider(private val context: Context) {
    private val iconPacks = mutableMapOf<String, IconPack?>()
    private val systemIcon = CustomAdaptiveIconDrawable.wrapNonNull(
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!
    )

    fun getIconPackOrSystem(packageName: String): IconPack? {
        if (packageName.isEmpty()) {
            return SystemIconPack(context, packageName)
        }
        return getIconPack(packageName)
    }

    fun getIconPack(packageName: String): IconPack? {
        if (packageName.isEmpty()) {
            return null
        }
        return iconPacks.getOrPut(packageName) {
            try {
                CustomIconPack(context, packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    fun getIconPackList(): List<IconPackInfo> {
        val pm = context.packageManager

        val iconPacks = Config.ICON_INTENTS
            .flatMap { pm.queryIntentActivities(it, 0) }
            .associateBy { it.activityInfo.packageName }
            .mapTo(mutableSetOf()) { (_, info) ->
                IconPackInfo(
                    info.loadLabel(pm).toString(),
                    info.activityInfo.packageName,
                    info.loadIcon(pm)
                )
            }
        val defaultIconPack =
            IconPackInfo(context.getString(R.string.icon_pack_default), "", systemIcon)
        val themedIconsInfo = if (minSDK(Build.VERSION_CODES.TIRAMISU))
            IconPackInfo(
                context.getString(R.string.title_themed_icons),
                context.getString(R.string.icon_packs_intent_name),
                ContextCompat.getDrawable(context, R.drawable.ic_launcher)!!,
        ) else null
        return listOfNotNull(
            defaultIconPack,
            themedIconsInfo,
        ) + iconPacks.sortedBy { it.name }
    }

    fun getClockMetadata(iconEntry: IconEntry): ClockMetadata? {
        val iconPack = getIconPackOrSystem(iconEntry.packPackageName) ?: return null
        return iconPack.getClock(iconEntry)
    }

    fun getDrawable(iconEntry: IconEntry, iconDpi: Int, user: UserHandle): Drawable? {
        val iconPack = getIconPackOrSystem(iconEntry.packPackageName) ?: return null
        iconPack.loadBlocking()
        val packageManager = context.packageManager
        val drawable = iconPack.getIcon(iconEntry, iconDpi) ?: return null
        val clockMetadata =
            if (user == Process.myUserHandle()) iconPack.getClock(iconEntry) else null
        val shouldTintBackgrounds = context.prefs.profileIconColoredBackground.getValue()
        val prefs = NeoPrefs.getInstance()

        if (clockMetadata != null) {
            val clockDrawable: ClockDrawableWrapper =
                ClockDrawableWrapper.forMeta(Build.VERSION.SDK_INT, clockMetadata) {
                    if (shouldTintBackgrounds)
                        wrapThemedData(
                            packageManager,
                            iconEntry,
                            drawable
                        )
                    else drawable
                }
            return if (shouldTintBackgrounds && prefs.profileTransparentBgIcons.getValue())
                    clockDrawable.foreground
                else
                    CustomAdaptiveIconDrawable(clockDrawable.background, clockDrawable.foreground)
        }

        if (shouldTintBackgrounds) {
            return wrapThemedData(packageManager, iconEntry, drawable)
        }
        return drawable
    }

    private fun wrapThemedData(
        packageManager: PackageManager,
        iconEntry: IconEntry,
        drawable: Drawable,
    ): Drawable? {
        val themedColors: IntArray = ThemedIconDrawable.getThemedColors(context)
        val res = packageManager.getResourcesForApplication(iconEntry.packPackageName)

        val iconPrefs = IconPreferences(context)

        @SuppressLint("DiscouragedApi")
        val resId = res.getIdentifier(iconEntry.name, "drawable", iconEntry.packPackageName)
        val bg: Drawable = ColorDrawable(themedColors[0])
        val td = IconProvider.ThemeData(res, resId)
        return if (drawable is AdaptiveIconDrawable) {
            if (iconPrefs.shouldTransparentBGIcons() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && drawable.monochrome != null) {
                drawable.monochrome?.apply { setTint(themedColors[1]) }
            } else {
                val foregroundDr = drawable.foreground.apply { setTint(themedColors[1]) }
                CustomAdaptiveIconDrawable(bg, foregroundDr)
            }
        } else {
            val iconFromPack = InsetDrawable(drawable, .3f).apply { setTint(themedColors[1]) }
            td.wrapDrawable(CustomAdaptiveIconDrawable(bg, iconFromPack), 0)
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconPackProvider)
    }
}

data class IconPackInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
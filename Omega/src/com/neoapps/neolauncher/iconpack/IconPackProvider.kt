package com.neoapps.neolauncher.iconpack

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppComponent
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.util.DaggerSingletonObject
import com.android.launcher3.util.SafeCloseable
import com.neoapps.neolauncher.icons.ClockMetadata
import com.neoapps.neolauncher.icons.CustomAdaptiveIconDrawable
import com.neoapps.neolauncher.util.Config
import com.neoapps.neolauncher.util.minSDK
import com.neoapps.neolauncher.util.prefs
import jakarta.inject.Inject

@LauncherAppSingleton
class IconPackProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : SafeCloseable {
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
            .mapNotNullTo(mutableSetOf()) { (_, info) ->
                runCatching {
                    IconPackInfo(
                        info.loadLabel(pm).toString(),
                        info.activityInfo.packageName,
                        info.loadIcon(pm)
                    )
                }.getOrNull()
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
        val drawable = iconPack.getIcon(iconEntry, iconDpi) ?: return null
        val shouldTintBackgrounds = context.prefs.profileIconColoredBackground.getValue()
        val clockMetadata =
            if (user == Process.myUserHandle()) iconPack.getClock(iconEntry) else null
        try {
            if (clockMetadata != null) {
                val clockDrawable: ClockDrawableWrapper? =
                    ClockDrawableWrapper.forPackage(context, iconEntry.packPackageName, iconDpi)

                return if (shouldTintBackgrounds) {
                    clockDrawable!!.foreground
                } else {
                    CustomAdaptiveIconDrawable(
                        clockDrawable!!.background,
                        clockDrawable.foreground,
                    )
                }
            }
        } catch (t: Throwable) {
            // Ignore
        }

        return drawable
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmField
        val INSTANCE = DaggerSingletonObject(LauncherAppComponent::getIconPackProvider)
    }
}

data class IconPackInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
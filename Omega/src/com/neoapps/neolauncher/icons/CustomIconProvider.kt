/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.icons

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageItemInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.ArrayMap
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import com.android.launcher3.R
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.icons.ClockDrawableWrapper
import com.android.launcher3.icons.LauncherIconProvider
import com.android.launcher3.icons.mono.ThemedIconDrawable
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.data.IconOverrideRepository
import com.neoapps.neolauncher.iconpack.IconEntry
import com.neoapps.neolauncher.iconpack.IconPackProvider
import com.neoapps.neolauncher.iconpack.IconType
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.Config.Companion.LAWNICONS_PACKAGE_NAME
import com.neoapps.neolauncher.util.getPackageVersionCode
import com.neoapps.neolauncher.util.isPackageInstalled
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject

@LauncherAppSingleton
class CustomIconProvider @JvmOverloads @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager
) : LauncherIconProvider(context, themeManager) {

    private val prefs = NeoPrefs.getInstance()
    private val iconPackPref = prefs.profileIconPack
    private val themedIconsEnabled get() = prefs.profileThemedIcons.getValue()
    private var isOlderLawnIconsInstalled =
        context.packageManager.getPackageVersionCode(LAWNICONS_PACKAGE_NAME) in 1..3
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val overrideRepo = IconOverrideRepository.INSTANCE.get(context)
    private var themeMapName: String = ""
    private var mThemedIconMap: Map<String, ThemeData>? = null
    private val themedIconPack
        get() = iconPackProvider.getIconPack(context.getString(R.string.icon_packs_intent_name))
            ?.apply { loadBlocking() }
    private val iconPack
        get() = iconPackProvider.getIconPack(iconPackPref.getValue())?.apply { loadBlocking() }
    private val themeMap: Map<String, ThemeData>
        get() {
            if (!themedIconsEnabled) {
                mThemedIconMap = DISABLED_MAP
            }
            if (mThemedIconMap == null) {
                mThemedIconMap = getThemedIconMap()
            }
            if (themedIconPack != null && themeMapName == "") {
                mThemedIconMap = super.getThemedIconMap()
            }
            if (themedIconPack != null && themeMapName != themedIconPack!!.packPackageName) {
                themeMapName = themedIconPack!!.packPackageName
                mThemedIconMap = getThemedIconMap()
            }
            return mThemedIconMap!!
        }
    private val supportsIconTheme get() = themeMap != DISABLED_MAP

    init {
        setIconThemeSupported(themeManager.isIconThemeEnabled && supportsIconTheme)
    }

    override fun getThemedIconMap(): MutableMap<String, ThemeData> {
        val themedIconMap = ArrayMap<String, ThemeData>()

        fun ArrayMap<String, ThemeData>.updateFromResources(
            resources: Resources,
            packageName: String,
        ) {
            try {
                @SuppressLint("DiscouragedApi")
                val xmlId = resources.getIdentifier("grayscale_icon_map", "xml", packageName)
                if (xmlId != 0) {
                    val parser = resources.getXml(xmlId)
                    val depth = parser.depth
                    var type: Int
                    while (
                        (
                                parser.next()
                                    .also {
                                        type = it
                                    } != XmlPullParser.END_TAG || parser.depth > depth
                                ) &&
                        type != XmlPullParser.END_DOCUMENT
                    ) {
                        if (type != XmlPullParser.START_TAG) continue
                        if (TAG_ICON == parser.name) {
                            val pkg = parser.getAttributeValue(null, ATTR_PACKAGE)
                            val iconId = parser.getAttributeResourceValue(null, ATTR_DRAWABLE, 0)
                            if (iconId != 0 && pkg.isNotEmpty()) {
                                this[pkg] = ThemeData(resources, iconId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to parse icon map.", e)
            }
        }

        // first, get Lawnchair's internal grayscale icon map
        themedIconMap.updateFromResources(
            resources = context.resources,
            packageName = context.packageName,
        )

        if (context.packageManager.isPackageInstalled(packageName = themeMapName)) {
            // get the grayscale icon map of the supported icon pack
            themedIconMap.updateFromResources(
                resources = context.packageManager.getResourcesForApplication(themeMapName),
                packageName = themeMapName,
            )
        }

        return themedIconMap
    }

    fun setIconThemeSupported(isSupported: Boolean) {
        mThemedIconMap = if (isSupported && isOlderLawnIconsInstalled) null else DISABLED_MAP
    }

    override fun updateSystemState() {
        super.updateSystemState()
        mSystemState += ",${iconPackPref.getValue()},$themedIconsEnabled"
    }
    private fun resolveIconEntry(componentName: ComponentName, user: UserHandle): IconEntry? {
        val componentKey = ComponentKey(componentName, user)
        val overrideItem = overrideRepo.overridesMap[componentKey]
        if (overrideItem != null) {
            return overrideItem.toIconEntry()
        }

        val iconPack = iconPack ?: return null
        val calendarEntry = iconPack.getCalendar(componentName)
        if (calendarEntry != null) {
            return calendarEntry
        }
        return iconPack.getIcon(componentName)
    }

    override fun getIcon(
        info: PackageItemInfo,
        appInfo: ApplicationInfo,
        iconDpi: Int,
    ): Drawable {
        val packageName = appInfo.packageName
        val componentName = context.packageManager.getLaunchIntentForPackage(packageName)?.component
        val user = UserHandle.getUserHandleForUid(appInfo.uid)

        var iconEntry: IconEntry? = null
        if (componentName != null) {
            iconEntry = resolveIconEntry(componentName, user)
        }

        var iconPackEntry = iconEntry

        val themeData = getThemeDataForPackage(packageName)
        var themedIcon: Drawable? = null

        val themedColors = ThemedIconDrawable.getThemedColors(context)

        if (iconEntry != null) {
            val clock = iconPackProvider.getClockMetadata(iconEntry)

            if (iconEntry.type == IconType.Calendar) {
                iconPackEntry = iconEntry.resolveDynamicCalendar(getDay())
            }
            when {
                !themedIconsEnabled -> {
                    themedIcon = null
                }

                clock != null -> {
                    themedIcon =
                        ClockDrawableWrapper.forPackage(mContext, mClock.packageName, iconDpi)!!
                            .getMonochrome()
                }

                packageName == mClock.packageName -> {
                    val clockThemedData =
                        ThemeData(context.resources, R.drawable.themed_icon_static_clock)
                    themedIcon = CustomAdaptiveIconDrawable(
                        themedColors[0].toDrawable(),
                        clockThemedData.loadPaddedDrawable().apply { setTint(themedColors[1]) },
                    )
                }

                packageName == mCalendar.packageName -> {
                    themedIcon = loadCalendarDrawable(iconDpi, themeData)
                }

                else -> {
                    themedIcon = if (themeData != null) {
                        CustomAdaptiveIconDrawable(
                            themedColors[0].toDrawable(),
                            themeData.loadPaddedDrawable()?.apply { setTint(themedColors[1]) },
                        )
                    } else {
                        null
                    }
                }
            }
        }

        val iconPackIcon = iconPackEntry?.let { iconPackProvider.getDrawable(it, iconDpi, user) }

        return themedIcon ?: iconPackIcon ?: super.getIcon(info, appInfo, iconDpi)
    }

    companion object {
        const val TAG = "CustomIconProvider"
    }
}
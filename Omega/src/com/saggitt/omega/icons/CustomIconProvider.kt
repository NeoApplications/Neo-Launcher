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

package com.saggitt.omega.icons

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DATE_CHANGED
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import android.content.Intent.ACTION_TIME_CHANGED
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.UserHandle
import android.os.UserManager
import android.util.ArrayMap
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.LauncherIconProvider
import com.android.launcher3.icons.ThemedIconDrawable
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.SafeCloseable
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.iconpack.IconEntry
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.iconpack.IconType
import com.saggitt.omega.util.ApkAssets
import com.saggitt.omega.util.MultiSafeCloseable
import com.saggitt.omega.util.minSDK
import com.saggitt.omega.util.overrideSdk
import com.saulhdev.neolauncher.icons.CustomAdaptiveIconDrawable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.function.Supplier

class CustomIconProvider @JvmOverloads constructor(
    private val context: Context,
    supportsIconTheme: Boolean = false,
) : LauncherIconProvider(context) {

    private val prefs = Utilities.getNeoPrefs(context)
    private val iconPackPref = prefs.profileIconPack
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val overrideRepo = IconOverrideRepository.INSTANCE.get(context)
    private val iconPack get() = iconPackProvider.getIconPackOrSystem(iconPackPref.getValue())
    private var iconPackVersion = 0L
    private var _themeMapName: String = ""
    private var _themeMap: Map<ComponentName, ThemeData>? = null
    private val themeMap: Map<ComponentName, ThemeData>
        get() {
            if (_themeMap == null) {
                _themeMap = createThemedIconMap()
            }
            if (iconPack != null && _themeMapName != iconPack!!.packPackageName) {
                _themeMapName = iconPack!!.packPackageName
                _themeMap = createThemedIconMap()
            }
            return _themeMap!!
        }
    private val supportsIconTheme get() = themeMap != DISABLED_MAP

    init {
        setIconThemeSupported(supportsIconTheme)
    }

    override fun setIconThemeSupported(isSupported: Boolean) {
        _themeMap =
            if (isSupported) null else DISABLED_MAP
    }

    private fun resolveIconEntry(componentName: ComponentName, user: UserHandle): IconEntry? {
        val componentKey = ComponentKey(componentName, user)
        // first look for user-overridden icon
        val overrideItem = overrideRepo.overridesMap[componentKey]
        if (overrideItem != null) {
            return overrideItem.toIconEntry()
        }

        val iconPack = this.iconPack ?: return null
        // then look for dynamic calendar
        val calendarEntry = iconPack.getCalendar(componentName)
        if (calendarEntry != null) {
            return calendarEntry
        }
        // finally, look for normal icon
        return iconPack.getIcon(componentName)
    }

    override fun getIconWithOverrides(
        packageName: String,
        iconDpi: Int,
        component: String,
        user: UserHandle,
        fallback: Supplier<Drawable>,
    ): Drawable {
        val componentName = ComponentName(packageName, component)
        val iconEntry = resolveIconEntry(componentName, user)
        var resolvedEntry = iconEntry
        //var iconType = ICON_TYPE_DEFAULT
        var themeData: ThemeData? = null
        if (iconEntry != null) {
            val clock = iconPackProvider.getClockMetadata(iconEntry)
            when {
                iconEntry.type == IconType.Calendar -> {
                    resolvedEntry = iconEntry.resolveDynamicCalendar(getDay())
                    themeData = getThemeDataForPackage(mCalendar.packageName)
                    //iconType = ICON_TYPE_CALENDAR
                }

                !supportsIconTheme -> {
                    // theming is disabled, don't populate theme data
                }

                clock != null -> {
                    // the icon supports dynamic clock, use dynamic themed clock
                    themeData = getThemeDataForPackage(mClock.packageName)
                    //iconType = ICON_TYPE_CLOCK
                }

                packageName == mClock.packageName -> {
                    // is clock app but icon might not be adaptive, fallback to static themed clock
                    themeData = ThemeData(
                        context.resources,
                        R.drawable.themed_icon_static_clock
                    )
                }

                packageName == mCalendar.packageName -> {
                    // calendar app, apply the dynamic calendar icon
                    themeData = getThemeDataForPackage(mCalendar.packageName)
                    //iconType = ICON_TYPE_CALENDAR
                }

                else -> {
                    // regular icon
                    themeData = getThemeDataForPackage(componentName.packageName)
                }
            }
        }
        val icon = resolvedEntry?.let { iconPackProvider.getDrawable(it, iconDpi, user) }
        val td = themeData
        if (icon != null) return icon //if (td != null) td.wrapDrawable(icon/*, iconType*/) else icon
        var defaultIcon =
            super.getIconWithOverrides(packageName, iconDpi, component, user, fallback)
        if (prefs.profileThemedIcons.getValue() && defaultIcon is AdaptiveIconDrawable &&
            minSDK(Build.VERSION_CODES.TIRAMISU) && defaultIcon.monochrome != null
        ) {
            defaultIcon = defaultIcon.monochrome
            /*return if (td != null) td.wrapDrawable(defaultIcon, iconType) else {*/
            val themedColors = ThemedIconDrawable.getColors(context)
            if (prefs.profileTransparentBgIcons.getValue()) {
                return defaultIcon.apply { setTint(themedColors[1]) }
            }
            return CustomAdaptiveIconDrawable(
                ColorDrawable(themedColors[0]),
                defaultIcon.apply { setTint(themedColors[1]) })
            //}
        }
        return defaultIcon
    }

    override fun getIcon(info: ActivityInfo?): Drawable {
        return if (prefs.profileShapeLessIcon.getValue()) {
            getLegacyIcon(
                ComponentName(info!!.packageName, info.name),
                context.resources.displayMetrics.densityDpi
            )
                ?: CustomAdaptiveIconDrawable.wrapNonNull(
                    super.getIcon(
                        info,
                        context.resources.displayMetrics.densityDpi
                    )
                )
        } else {
            CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info))
        }
    }

    override fun getIcon(info: ActivityInfo?, iconDpi: Int): Drawable {
        return if (prefs.profileShapeLessIcon.getValue()) {
            getLegacyIcon(ComponentName(info!!.packageName, info.name), iconDpi)
                ?: CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
        } else {
            CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
        }
    }

    override fun getIcon(info: LauncherActivityInfo?, iconDpi: Int): Drawable {
        return if (prefs.profileShapeLessIcon.getValue()) {
            getLegacyIcon(info!!.componentName, iconDpi)
                ?: CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
        } else {
            CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
        }
    }

    private fun getLegacyIcon(componentName: ComponentName, iconDpi: Int): Drawable? {
        var appIcon: String? = null
        val elementTags = HashMap<String, String>()
        try {
            val resourcesForApplication =
                context.packageManager.getResourcesForApplication(componentName.packageName)
            val info = context.packageManager.getApplicationInfo(
                componentName.packageName,
                PackageManager.GET_SHARED_LIBRARY_FILES or PackageManager.GET_META_DATA
            )
            val parseXml = try {
                // For apps which are installed as Split APKs the asset instance we can get via PM won't hold the right Manifest for us.
                ApkAssets(info.publicSourceDir).openXml(MANIFEST_XML)
            } catch (ex: Exception) {
                ex.message
                val assets = resourcesForApplication.assets
                assets.openXmlResourceParser(MANIFEST_XML)
            }

            while (parseXml.next() != XmlPullParser.END_DOCUMENT) {
                if (parseXml.eventType == XmlPullParser.START_TAG) {
                    val name = parseXml.name
                    for (i in 0 until parseXml.attributeCount) {
                        elementTags[parseXml.getAttributeName(i)] = parseXml.getAttributeValue(i)
                    }
                    if (elementTags.containsKey("icon")) {
                        if (name == "application") {
                            appIcon = elementTags["icon"]
                        } else if ((name == "activity" || name == "activity-alias") &&
                            elementTags.containsKey("name") &&
                            elementTags["name"] == componentName.className
                        ) {
                            appIcon = elementTags["icon"]
                            break
                        }
                    }
                    elementTags.clear()
                }
            }
            parseXml.close()
            if (appIcon != null) {
                val resId = Utilities.parseResourceIdentifier(
                    resourcesForApplication,
                    appIcon,
                    componentName.packageName
                )
                if (prefs.profileShapeLessIcon.getValue()) {
                    return resourcesForApplication.overrideSdk(Build.VERSION_CODES.M) {
                        ResourcesCompat.getDrawable(
                            this,
                            resId,
                            null
                        )
                    }
                }
                return resourcesForApplication.getDrawableForDensity(resId, iconDpi, null)
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            ex.printStackTrace()
        } catch (ex: Resources.NotFoundException) {
            ex.printStackTrace()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: XmlPullParserException) {
            ex.printStackTrace()
        }

        return null
    }

    override fun getSystemIconState(): String {
        return super.getSystemIconState() + ",pack:${iconPackPref.getValue()},ver:${iconPackVersion}"
    }

    override fun registerIconChangeListener(
        callback: IconChangeListener,
        handler: Handler,
    ): SafeCloseable {
        return MultiSafeCloseable().apply {
            add(super.registerIconChangeListener(callback, handler))
            add(IconPackChangeReceiver(context, handler, callback))
        }
    }

    private inner class IconPackChangeReceiver(
        private val context: Context,
        private val handler: Handler,
        private val callback: IconChangeListener,
    ) : SafeCloseable {

        private var calendarAndClockChangeReceiver: CalendarAndClockChangeReceiver? = null
            set(value) {
                field?.close()
                field = value
            }

        private val subscription = SafeCloseable {
            val newState = systemIconState
            if (iconState != newState) {
                iconState = newState
                callback.onSystemIconStateChanged(iconState)
                recreateCalendarAndClockChangeReceiver()
            }
        }

        private var iconState = systemIconState
        private val prefs = Utilities.getNeoPrefs(context)
        private val iconPackPref = prefs.profileIconPack

        init {
            recreateCalendarAndClockChangeReceiver()
        }

        private fun recreateCalendarAndClockChangeReceiver() {
            val iconPack =
                IconPackProvider.INSTANCE.get(context).getIconPack(iconPackPref.getValue())
            calendarAndClockChangeReceiver = if (iconPack != null) {
                CalendarAndClockChangeReceiver(context, handler, iconPack, callback)
            } else {
                null
            }
        }

        override fun close() {
            calendarAndClockChangeReceiver = null
            subscription.close()
        }
    }

    private class CalendarAndClockChangeReceiver(
        private val context: Context, handler: Handler,
        private val iconPack: IconPack,
        private val callback: IconChangeListener,
    ) : BroadcastReceiver(), SafeCloseable {

        init {
            val filter = IntentFilter(ACTION_TIMEZONE_CHANGED)
            filter.addAction(ACTION_TIME_TICK)
            filter.addAction(ACTION_TIME_CHANGED)
            filter.addAction(ACTION_DATE_CHANGED)
            context.registerReceiver(this, filter, null, handler)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_TIMEZONE_CHANGED, ACTION_TIME_CHANGED, ACTION_TIME_TICK -> {
                    context.getSystemService<UserManager>()?.userProfiles?.forEach { user ->
                        iconPack.getClocks().forEach { componentName ->
                            callback.onAppIconChanged(
                                componentName.packageName,
                                user
                            )
                        }
                    }
                }

                ACTION_DATE_CHANGED                                            -> {
                    context.getSystemService<UserManager>()?.userProfiles?.forEach { user ->
                        iconPack.getCalendars().forEach { componentName ->
                            callback.onAppIconChanged(componentName.packageName, user)
                        }
                    }
                }
            }
        }

        override fun close() {
            context.unregisterReceiver(this)
        }
    }

    private fun createThemedIconMap(): MutableMap<ComponentName, ThemeData> {
        val map = ArrayMap<ComponentName, ThemeData>()

        fun updateMapFromResources(resources: Resources) {
            try {
                resources.getXml(R.xml.grayscale_icon_map).use { parser ->
                    val depth: Int = parser.depth
                    var type: Int
                    while (parser.next().also { type = it } != XmlPullParser.START_TAG
                        && type != XmlPullParser.END_DOCUMENT
                    );
                    while ((parser.next().also { type = it } != XmlPullParser.END_TAG
                                || parser.depth > depth) && type != XmlPullParser.END_DOCUMENT
                    ) {
                        if (type != XmlPullParser.START_TAG) continue
                        if (TAG_ICON == parser.name) {
                            val pkg = parser.getAttributeValue(null, ATTR_PACKAGE)
                            val cmp = parser.getAttributeValue(null, ATTR_COMPONENT).orEmpty()
                            val iconId = parser.getAttributeResourceValue(null, ATTR_DRAWABLE, 0)
                            if (iconId != 0 && pkg.isNotEmpty()) {
                                map[ComponentName(pkg, cmp)] = ThemeData(resources, iconId)
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Unable to parse icon map", e)
            }
        }

        updateMapFromResources(
            resources = context.resources
        )

        return map
    }

    companion object {
        const val TAG = "CustomIconProvider"
        const val TAG_ICON = "icon"
        const val ATTR_COMPONENT = "component"
        const val ATTR_PACKAGE = "package"
        const val ATTR_DRAWABLE = "drawable"

        val DISABLED_MAP = emptyMap<ComponentName, ThemeData>()
        const val MANIFEST_XML = "AndroidManifest.xml"
    }
}
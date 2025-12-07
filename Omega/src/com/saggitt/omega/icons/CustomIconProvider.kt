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
import android.content.Intent.ACTION_PACKAGE_ADDED
import android.content.Intent.ACTION_PACKAGE_CHANGED
import android.content.Intent.ACTION_PACKAGE_REMOVED
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import android.content.Intent.ACTION_TIME_CHANGED
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageItemInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.UserHandle
import android.os.UserManager
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.LauncherIconProvider.ATTR_DRAWABLE
import com.android.launcher3.icons.LauncherIconProvider.ATTR_PACKAGE
import com.android.launcher3.icons.LauncherIconProvider.TAG_ICON
import com.android.launcher3.icons.mono.ThemedIconDrawable
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.SafeCloseable
import com.neoapps.neolauncher.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.iconpack.IconEntry
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.iconpack.IconType
import com.saggitt.omega.util.ApkAssets
import com.saggitt.omega.util.Config.Companion.LAWNICONS_PACKAGE_NAME
import com.saggitt.omega.util.MultiSafeCloseable
import com.saggitt.omega.util.overrideSdk
import com.neoapps.neolauncher.util.getPackageVersionCode
import com.neoapps.neolauncher.util.isPackageInstalled
import com.saggitt.omega.preferences.NeoPrefs
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.function.Supplier

class CustomIconProvider @JvmOverloads constructor(
    private val context: Context,
    supportsIconTheme: Boolean = false,
) : IconProvider(context) {

    private val prefs = NeoPrefs.getInstance()
    private val iconPackPref = prefs.profileIconPack
    private val drawerThemedIcons = prefs.profileThemedIcons.getValue()
    private var isOlderLawnIconsInstalled =
        context.packageManager.getPackageVersionCode(LAWNICONS_PACKAGE_NAME) in 1..3
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val overrideRepo = IconOverrideRepository.INSTANCE.get(context)
    private val iconPack get() = iconPackProvider.getIconPackOrSystem(iconPackPref.getValue())
    private var iconPackVersion = 0L
    private var themeMapName: String = ""
    private var _themeMap: Map<String, ThemeData>? = null
    private val mThemeManager: ThemeManager? = null
    private val themedIconPack
        get() = iconPackProvider.getIconPack(context.getString(R.string.icon_packs_intent_name))
            ?.apply { loadBlocking() }
    private val themeMap: Map<String, ThemeData>
        get() {
            if (drawerThemedIcons && !(isOlderLawnIconsInstalled)) {
                _themeMap = DISABLED_MAP
            }
            if (_themeMap == null) {
                _themeMap = createThemedIconMap()
            }
            if (isOlderLawnIconsInstalled && iconPackPref.getValue() == LAWNICONS_PACKAGE_NAME) {
                themeMapName = iconPackPref.getValue()
                _themeMap = createThemedIconMap()
            }
            if (themedIconPack != null && themeMapName != themedIconPack!!.packPackageName) {
                themeMapName = themedIconPack!!.packPackageName
                _themeMap = createThemedIconMap()
            }
            return _themeMap!!
        }
    private val supportsIconTheme get() = themeMap != DISABLED_MAP

    init {
        setIconThemeSupported(supportsIconTheme)
    }

    fun setIconThemeSupported(isSupported: Boolean) {
        _themeMap = if (isSupported && isOlderLawnIconsInstalled) null else DISABLED_MAP
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

    fun isThemeEnabled(): Boolean {
        return _themeMap != DISABLED_MAP
    }

    override fun getThemeDataForPackage(packageName: String?): ThemeData? {
        return getThemedIconMap().get(packageName)
    }

    fun getThemedIconMap(): MutableMap<String, ThemeData> {
        if (_themeMap != null) {
            return _themeMap!!.toMutableMap()
        }
        val map = ArrayMap<String, ThemeData>()
        val res = mContext.resources
        try {
            res.getXml(R.xml.grayscale_icon_map).use { parser ->
                val depth = parser.getDepth()
                var type: Int
                while ((parser.next().also { type = it }) != XmlPullParser.START_TAG &&
                    type != XmlPullParser.END_DOCUMENT
                );
                while ((
                            (parser.next().also { type = it }) != XmlPullParser.END_TAG ||
                                    parser.getDepth() > depth
                            ) &&
                    type != XmlPullParser.END_DOCUMENT
                ) {
                    if (type != XmlPullParser.START_TAG) {
                        continue
                    }
                    if (TAG_ICON == parser.getName()) {
                        val pkg = parser.getAttributeValue(null, ATTR_PACKAGE)
                        val iconId = parser.getAttributeResourceValue(
                            null,
                            ATTR_DRAWABLE,
                            0,
                        )
                        if (iconId != 0 && !TextUtils.isEmpty(pkg)) {
                            map.put(pkg, ThemeData(res, iconId))
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Unable to parse icon map", e)
        }
        _themeMap = map
        return _themeMap!!.toMutableMap()
    }


    override fun getIcon(info: ComponentInfo?): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info))
    }

    override fun getIcon(info: ComponentInfo?, iconDpi: Int): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
    }

    override fun getIcon(info: ApplicationInfo?): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info))
    }

    override fun getIcon(info: ApplicationInfo?, iconDpi: Int): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
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

    override fun registerIconChangeListener(
        callback: IconChangeListener,
        handler: Handler,
    ): SafeCloseable {
        return MultiSafeCloseable().apply {
            add(super.registerIconChangeListener(callback, handler))
            add(IconPackChangeReceiver(context, handler, callback))
            add(CustomIconChangeReceiver(context, handler, callback))
        }
    }

    private inner class IconPackChangeReceiver(
        private val context: Context,
        private val handler: Handler,
        private val callback: IconChangeListener,
    ) : SafeCloseable {

        private var iconState = mThemeManager?.iconState
        private var calendarAndClockChangeReceiver: CalendarAndClockChangeReceiver? = null
            set(value) {
                field?.close()
                field = value
            }

        private val subscription = SafeCloseable {
            val newState = mThemeManager?.iconState
            if (iconState != newState) {
                iconState = newState
                updateSystemState()
                recreateCalendarAndClockChangeReceiver()
            }
        }

        private val prefs = NeoPrefs.getInstance()
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

    private inner class CustomIconChangeReceiver(
        private val context: Context,
        handler: Handler,
        private val callback: IconChangeListener,
    ) : BroadcastReceiver(), SafeCloseable {

        init {
            val filter = IntentFilter(ACTION_PACKAGE_ADDED)
            filter.addAction(ACTION_PACKAGE_CHANGED)
            filter.addAction(ACTION_PACKAGE_REMOVED)
            filter.addDataScheme("package")
            filter.addDataSchemeSpecificPart(themeMapName, 0)
            context.registerReceiver(this, filter, null, handler)
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (isThemeEnabled()) {
                setIconThemeSupported(true)
            }
            updateSystemState()
        }

        override fun close() {
            context.unregisterReceiver(this)
        }
    }

    private fun createThemedIconMap(): MutableMap<String, ThemeData> {
        val map = ArrayMap<String, ThemeData>()

        fun updateMapFromResources(resources: Resources, packageName: String) {
            try {
                val xmlId = resources.getIdentifier("grayscale_icon_map", "xml", packageName)
                if (xmlId != 0) {
                    val parser = resources.getXml(xmlId)
                    val depth = parser.depth
                    var type: Int
                    while (
                        (parser.next()
                            .also { type = it } != XmlPullParser.END_TAG || parser.depth > depth) &&
                        type != XmlPullParser.END_DOCUMENT
                    ) {
                        if (type != XmlPullParser.START_TAG) continue
                        if (TAG_ICON == parser.name) {
                            val pkg = parser.getAttributeValue(null, ATTR_PACKAGE)
                            val iconId = parser.getAttributeResourceValue(null, ATTR_DRAWABLE, 0)
                            if (iconId != 0 && pkg.isNotEmpty()) {
                                map[pkg] = ThemeData(resources, iconId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to parse icon map.", e)
            }
        }

        updateMapFromResources(
            resources = context.resources,
            packageName = context.packageName
        )
        if (context.packageManager.isPackageInstalled(packageName = themeMapName)) {
            iconPackVersion = context.packageManager.getPackageVersionCode(themeMapName)
            updateMapFromResources(
                resources = context.packageManager.getResourcesForApplication(themeMapName),
                packageName = themeMapName
            )
        }

        return map
    }

    companion object {
        const val TAG = "CustomIconProvider"

        val DISABLED_MAP = emptyMap<String, ThemeData>()
        const val MANIFEST_XML = "AndroidManifest.xml"
    }
}
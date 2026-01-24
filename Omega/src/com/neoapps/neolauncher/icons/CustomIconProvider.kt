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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.graphics.drawable.Drawable
import com.android.launcher3.R
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.icons.LauncherIconProvider
import com.neoapps.neolauncher.iconpack.IconPackProvider
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.Config.Companion.LAWNICONS_PACKAGE_NAME
import com.neoapps.neolauncher.util.getPackageVersionCode
import javax.inject.Inject

@LauncherAppSingleton
class CustomIconProvider @JvmOverloads @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager
) : LauncherIconProvider(context, themeManager) {

    private val prefs = NeoPrefs.getInstance()
    private val iconPackPref = prefs.profileIconPack
    private val drawerThemedIcons = prefs.profileThemedIcons.getValue()
    private var isOlderLawnIconsInstalled =
        context.packageManager.getPackageVersionCode(LAWNICONS_PACKAGE_NAME) in 1..3
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private var iconPackVersion = 0L
    private var themeMapName: String = ""
    private var mThemedIconMap: Map<String, ThemeData>? = null
    private val themedIconPack
        get() = iconPackProvider.getIconPack(context.getString(R.string.icon_packs_intent_name))
            ?.apply { loadBlocking() }

    private val themeMap: Map<String, ThemeData>
        get() {
            if (drawerThemedIcons && !(isOlderLawnIconsInstalled)) {
                mThemedIconMap = DISABLED_MAP
            }
            if (mThemedIconMap == null) {
                mThemedIconMap = getThemedIconMap()
            }
            if (isOlderLawnIconsInstalled && iconPackPref.getValue() == LAWNICONS_PACKAGE_NAME) {
                themeMapName = iconPackPref.getValue()
                mThemedIconMap = getThemedIconMap()
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

    fun setIconThemeSupported(isSupported: Boolean) {
        mThemedIconMap = if (isSupported && isOlderLawnIconsInstalled) null else DISABLED_MAP
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

    companion object {
        const val TAG = "CustomIconProvider"
    }
}
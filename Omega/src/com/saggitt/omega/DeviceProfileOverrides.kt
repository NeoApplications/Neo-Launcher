/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.InvariantDeviceProfile.INDEX_DEFAULT
import com.android.launcher3.Utilities
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.preferences.NLPrefs

class DeviceProfileOverrides(context: Context) {
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    fun getOverrides(defaultGrid: InvariantDeviceProfile.GridOption) = Options(prefs, defaultGrid)

    data class Options(
        var numHotseatColumns: Int,
        var numRows: Int,
        var numColumns: Int,
        var numAllAppsColumns: Int,
        var numFolderRows: Int,
        var numFolderColumns: Int,

        var iconSizeFactor: Float,
        var enableIconText: Boolean,
        var iconTextSizeFactor: Float,

        var allAppsIconSizeFactor: Float,
        var enableAllAppsIconText: Boolean,
        var allAppsIconTextSizeFactor: Float,

        var typeIndex: Int = 0,

        val dbFile: String = "launcher_${numRows}_${numColumns}_${numHotseatColumns}.db"
    ) {

        constructor(
            prefs: NLPrefs,
            defaultGrid: InvariantDeviceProfile.GridOption,
        ) : this(
            numHotseatColumns = prefs.dockNumIcons.getValue(),
            numRows = prefs.desktopGridRows.get(defaultGrid),
            numColumns = prefs.desktopGridColumns.get(defaultGrid),
            numAllAppsColumns = prefs.drawerGridColumns.get(defaultGrid),
            numFolderRows = prefs.desktopFolderRows.getValue(),
            numFolderColumns = prefs.desktopFolderColumns.getValue(),

            iconSizeFactor = prefs.desktopIconScale.getValue(),
            enableIconText = !prefs.desktopHideAppLabels.getValue(),
            iconTextSizeFactor = prefs.desktopLabelScale.getValue(),

            allAppsIconSizeFactor = prefs.drawerIconScale.getValue(),
            enableAllAppsIconText = !prefs.drawerHideLabels.getValue(),
            allAppsIconTextSizeFactor = prefs.drawerLabelScale.getValue()
        )

        fun apply(idp: InvariantDeviceProfile) {
            // apply grid size
            idp.numShownHotseatIcons = numHotseatColumns
            idp.numDatabaseHotseatIcons = numHotseatColumns
            idp.numRows = numRows
            idp.numColumns = numColumns
            idp.numAllAppsColumns = numAllAppsColumns
            idp.numFolderRows = numFolderRows
            idp.numFolderColumns = numFolderColumns

            // apply icon and text size
            //TODO: Add support for landscape and large devices
            idp.iconSize[INDEX_DEFAULT] *= iconSizeFactor
            idp.iconTextSize[INDEX_DEFAULT] *= (if (enableIconText) iconTextSizeFactor else 0f)
            idp.allAppsIconSize[INDEX_DEFAULT] *= allAppsIconSizeFactor
            idp.allAppsIconTextSize[INDEX_DEFAULT] *= (if (enableAllAppsIconText) allAppsIconTextSizeFactor else 0f)

            idp.dbFile = dbFile
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DeviceProfileOverrides)
    }
}

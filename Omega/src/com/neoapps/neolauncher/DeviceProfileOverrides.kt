package com.neoapps.neolauncher

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.InvariantDeviceProfile.INDEX_DEFAULT
import com.android.launcher3.InvariantDeviceProfile.INDEX_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_PORTRAIT
import com.android.launcher3.util.DisplayController
import com.neoapps.neolauncher.preferences.NeoPrefs
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.java.KoinJavaComponent.inject

class DeviceProfileOverrides(context: Context) {
    private val prefs: NeoPrefs by inject(NeoPrefs::class.java)

    private val predefinedGrids =
        InvariantDeviceProfile.parseAllDefinedGridOptions(
            context,
            DisplayController.INSTANCE.get(context).info
        )
            .map { option ->
                val gridInfo = DBGridInfo(
                    numHotseatIcons = option.numHotseatIcons,
                    numHotseatRows = 1, //option.numHotseatRows TODO Fix
                    numRows = option.numRows,
                    numColumns = option.numColumns
                )
                gridInfo to option.name
            }

    fun getGridInfo() = DBGridInfo(prefs)

    fun getGridInfo(gridName: String) = predefinedGrids
        .first { it.second == gridName }
        .first

    fun getGridName(gridInfo: DBGridInfo): String {
        val match = predefinedGrids
            .firstOrNull { it.first.numRows >= gridInfo.numRows && it.first.numColumns >= gridInfo.numColumns }
            ?: predefinedGrids.last()
        return match.second
    }

    fun getCurrentGridName() = getGridName(getGridInfo())

    fun setCurrentGrid(gridName: String) {
        val gridInfo = getGridInfo(gridName)
        prefs.desktopGridRows.setValue(gridInfo.numRows)
        prefs.desktopGridColumns.setValue(gridInfo.numColumns)
        prefs.dockNumIcons.setValue(gridInfo.numHotseatIcons)
        prefs.dockNumRows.setValue(gridInfo.numHotseatRows)
    }

    fun getOverrides(defaultGrid: InvariantDeviceProfile.GridOption) =
        Options(
            prefs = prefs,
            defaultGrid = defaultGrid,
        )

    fun getTextFactors() = TextFactors(prefs)

    data class DBGridInfo(
        val numHotseatIcons: Int,
        val numHotseatRows: Int,
        val numRows: Int,
        val numColumns: Int,
    ) {
        val dbFile get() = "launcher_${numRows}_${numColumns}_${numHotseatIcons}.db"

        constructor(prefs: NeoPrefs) : this(
            numHotseatIcons = prefs.dockNumIcons.getValue(),
            numHotseatRows = prefs.dockNumRows.getValue(),
            numRows = prefs.desktopGridRows.getValue(),
            numColumns = prefs.desktopGridColumns.getValue()
        )
    }

    data class Options(
        val numAllAppsColumns: Int,
        val numFolderRows: Int,
        val numFolderColumns: Int,

        val iconSizeFactor: Float,
        val allAppsIconSizeFactor: Float,

        val enableTaskbarOnPhone: Boolean,
        val numDesktopRows: Int,
        val numDesktopColumns: Int,
        val numHotseatIcons: Int,
    ) {
        constructor(
            prefs: NeoPrefs,
            defaultGrid: InvariantDeviceProfile.GridOption,
        ) : this(
            numAllAppsColumns = prefs.drawerGridColumns.get(defaultGrid),
            numFolderRows = prefs.desktopFolderRows.getValue(),
            numFolderColumns = prefs.desktopFolderColumns.getValue(),

            iconSizeFactor = prefs.desktopIconScale.getValue(),
            allAppsIconSizeFactor = prefs.drawerIconScale.getValue(),

            enableTaskbarOnPhone = false, // TODO pref for this
            numDesktopRows = prefs.desktopGridRows.get(defaultGrid),
            numDesktopColumns = prefs.desktopGridColumns.get(defaultGrid),
            numHotseatIcons = prefs.dockNumIcons.get(defaultGrid),
        )

        fun applyUi(idp: InvariantDeviceProfile) {
            // grid sizes
            idp.numRows = numDesktopRows
            idp.numColumns = numDesktopColumns
            idp.numSearchContainerColumns = numDesktopColumns

            idp.numShownHotseatIcons = numHotseatIcons
            idp.numDatabaseHotseatIcons =
                if (idp.deviceType == InvariantDeviceProfile.TYPE_MULTI_DISPLAY) {
                    numHotseatIcons * 2
                } else {
                    numHotseatIcons
                }

            // Set dbFile to match custom grid dimensions
            idp.dbFile = "launcher_${numDesktopRows}_${numDesktopColumns}_${numHotseatIcons}.db"

            idp.numAllAppsColumns = numAllAppsColumns
            idp.numDatabaseAllAppsColumns = numAllAppsColumns
            idp.numFolderRows[INDEX_DEFAULT] = numFolderRows
            idp.numFolderColumns[INDEX_DEFAULT] = numFolderColumns

            // apply icon and text size multipliers
            idp.iconSize[INDEX_DEFAULT] *= iconSizeFactor
            idp.iconSize[INDEX_LANDSCAPE] *= iconSizeFactor
            idp.iconSize[INDEX_TWO_PANEL_PORTRAIT] *= iconSizeFactor
            idp.iconSize[INDEX_TWO_PANEL_LANDSCAPE] *= iconSizeFactor
            idp.allAppsIconSize[INDEX_DEFAULT] *= allAppsIconSizeFactor
        }
    }

    data class TextFactors(
        val iconTextSizeFactor: Float,
        val allAppsIconTextSizeFactor: Float,
    ) {
        constructor(
            prefs: NeoPrefs,
        ) : this(
            enableIconText = !prefs.desktopHideAppLabels.getValue(),
            iconTextSizeFactor = prefs.desktopLabelScale.getValue(),
            enableAllAppsIconText = !prefs.drawerHideLabels.getValue(),
            allAppsIconTextSizeFactor = prefs.drawerLabelScale.getValue(),
        )

        constructor(
            enableIconText: Boolean,
            iconTextSizeFactor: Float,
            enableAllAppsIconText: Boolean,
            allAppsIconTextSizeFactor: Float,
        ) : this(
            iconTextSizeFactor = if (enableIconText) iconTextSizeFactor else 0f,
            allAppsIconTextSizeFactor = if (enableAllAppsIconText) allAppsIconTextSizeFactor else 0f,
        )
    }

    companion object {
        @JvmStatic
        fun getInstance(): DeviceProfileOverrides = getKoin().get()
    }
}

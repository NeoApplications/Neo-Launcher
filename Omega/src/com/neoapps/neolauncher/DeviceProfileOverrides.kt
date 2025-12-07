package com.neoapps.neolauncher

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.InvariantDeviceProfile.INDEX_DEFAULT
import com.android.launcher3.InvariantDeviceProfile.INDEX_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_LANDSCAPE
import com.android.launcher3.InvariantDeviceProfile.INDEX_TWO_PANEL_PORTRAIT
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.preferences.NeoPrefs
import org.koin.java.KoinJavaComponent.inject

class DeviceProfileOverrides(context: Context) {
    private val prefs: NeoPrefs by inject(NeoPrefs::class.java)

    private val predefinedGrids = InvariantDeviceProfile.parseAllDefinedGridOptions(context)
        .map { option ->
            val gridInfo = DBGridInfo(
                numHotseatIcons = option.numHotseatIcons,
                numHotseatRows = option.numHotseatRows,
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

        val enableTaskbarOnPhone: Boolean
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

            enableTaskbarOnPhone = false,
        )

        fun applyUi(idp: InvariantDeviceProfile) {
            // apply grid size
            idp.numAllAppsColumns = numAllAppsColumns
            idp.numDatabaseAllAppsColumns = numAllAppsColumns
            idp.numFolderRows = numFolderRows
            idp.numFolderColumns = numFolderColumns

            // apply icon and text size
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
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::DeviceProfileOverrides)
    }
}

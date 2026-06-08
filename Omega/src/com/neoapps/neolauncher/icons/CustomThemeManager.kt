package com.neoapps.neolauncher.icons

import android.content.Context
import android.util.Log
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.concurrent.annotations.Ui
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.graphics.theme.IconThemeFactory
import com.android.launcher3.graphics.theme.ThemePreference
import com.android.launcher3.icons.GraphicsUtils.generateIconShape
import com.android.launcher3.util.DaggerSingletonTracker
import com.android.launcher3.util.LooperExecutor
import com.neoapps.neolauncher.preferences.NeoPrefs
import javax.inject.Inject
import javax.inject.Named

@LauncherAppSingleton
class CustomThemeManager @Inject
constructor(
    @ApplicationContext context: Context,
    @Ui uiExecutor: LooperExecutor,
    prefs: LauncherPrefs,
    themePreference: ThemePreference,
    @Named(ThemeManager.ICON_FACTORY_DAGGER_KEY)
    iconThemeFactories: Map<String, @JvmSuppressWildcards IconThemeFactory>,
    @Ui mainExecutor: LooperExecutor,
    lifecycle: DaggerSingletonTracker,
) : ThemeManager(
    context,
    uiExecutor,
    prefs,
    themePreference,
    iconThemeFactories,
    mainExecutor,
    lifecycle
) {
    override fun verifyIconState() {
        val newState = parseIconStateV2(iconState)
        if (newState == iconState) return
        val hasThemeChanged = newState.toUniqueId() != iconState.toUniqueId()
        iconState = newState

        if (hasThemeChanged) {
            listeners.forEach { it.onThemeChanged() }
        }
        val iconSize = iconShapeData.value.pathSize
        _iconShapeData.dispatchValue(
            generateIconShape(
                iconSize,
                iconShape.getPath(iconSize.toFloat())
            )
        )
    }

    private fun parseIconStateV2(oldState: ThemeManager.IconState?): ThemeManager.IconState {
        val neoPrefs = NeoPrefs.getInstance()

        val currentAppShape: IconShape = try {
            IconShape.fromString(neoPrefs.profileIconShape.getValue())
        } catch (e: Exception) {
            Log.d(TAG, "Error getting icon shape", e)
            IconShape.Circle
        }

        val currentFolderShape: IconShape = try {
            //TODO: Create folder shape preference
            IconShape.fromString(neoPrefs.profileIconShape.getValue())
        } catch (e: Exception) {
            Log.d(TAG, "Error getting icon shape", e)
            IconShape.Circle
        }

        val appShapeKey = currentAppShape.getHashString()
        val folderShapeKey = currentFolderShape.getHashString()

        val appShape =
            if (oldState != null && oldState.iconMask == appShapeKey) {
                oldState.iconShape
            } else {
                PathShapeDelegate(currentAppShape)
            }

        val folderShape =
            if (oldState != null && oldState.iconMask == folderShapeKey) {
                oldState.folderShape
            } else {
                PathShapeDelegate(currentFolderShape)
            }

        val themeKey = themePreference.value
        val themeCode = themeKey?.toString() ?: "no-theme"
        val iconControllerFactory =
            if (oldState?.themeCode == themeCode) {
                oldState.themeController
            } else {
                oldState?.closeController()
                themeKey?.run { iconThemeFactories[factoryId]?.createController(themeId) }
            }

        return IconState(
            key = currentAppShape.toString(),
            iconMask = appShapeKey,
            folderRadius = 1f,
            themeController = iconControllerFactory,
            themeCode = themeCode,
            iconShape = appShape,
            folderShape = folderShape,
            shapeRadius = currentAppShape.qsbEdgeRadius.toFloat(),
        )
    }

    companion object {
        private const val TAG = "CustomThemeManager"
    }
}

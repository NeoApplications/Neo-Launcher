package com.neoapps.neolauncher.shortcuts

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.model.data.LauncherAppWidgetInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.NeoLauncher
import com.neoapps.neolauncher.compose.components.ComposeBottomSheet
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.hasFlag
import com.neoapps.neolauncher.util.hasFlags
import java.net.URISyntaxException

class OmegaShortcuts {
    class Customize(
        private val launcher: NeoLauncher,
        private val appInfo: com.android.launcher3.model.data.AppInfo,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_edit_no_shadow,
        R.string.action_preferences, launcher, itemInfo,
        originalView
    ) {

        val prefs: NeoPrefs = NeoPrefs.Companion.getInstance()
        override fun onClick(v: View?) {
            val outObj = Array<Any?>(1) { null }
            var icon = Utilities.getFullDrawable(launcher, appInfo, 0, 0, true)
            if (mItemInfo.screenId != NO_ID && icon is BitmapInfo.Extender) {
                //icon = icon.getThemedDrawable(launcher)
            }

            val launcherActivityInfo = outObj[0] as LauncherActivityInfo
            val defaultTitle = launcherActivityInfo.label.toString()

            if (launcher.isInState(LauncherState.ALL_APPS)) {
                if (prefs.drawerPopupEdit) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    ComposeBottomSheet.Companion.show(launcher) {
                        /*CustomizeIconPage(
                            defaultTitle = defaultTitle,
                            componentKey = appInfo.toComponentKey(),
                            appInfo = appInfo,
                            onClose = { close(true) }
                        )*/
                    }
                }
            } else {
                if (prefs.desktopPopupEdit && !prefs.desktopLock.getValue()) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    ComposeBottomSheet.Companion.show(launcher) {
                        /*CustomizeIconPage(
                            defaultTitle = defaultTitle,
                            componentKey = appInfo.toComponentKey(),
                            appInfo = appInfo,
                            onClose = { close(true) }
                        )*/
                    }
                }
            }
        }
    }

    class AppRemove(
        private val launcher: NeoLauncher,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_remove_no_shadow,
        R.string.remove_drop_target_label, launcher, itemInfo,
        originalView
    ) {

        override fun onClick(v: View?) {
            dismissTaskMenuView()
            launcher.removeItem(v, mItemInfo, true)
            launcher.workspace.stripEmptyScreens()
            launcher.model.forceReload()
        }
    }

    class AppUninstall(
        private val launcher: NeoLauncher,
        itemInfo: ItemInfo,
        originalView: View,
    ) : SystemShortcut<NeoLauncher>(
        R.drawable.ic_uninstall_no_shadow,
        R.string.uninstall_drop_target_label, launcher, itemInfo, originalView
    ) {

        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(mTarget)
            try {
                val componentName: ComponentName? = getUninstallTarget(launcher, mItemInfo)
                val i: Intent =
                    Intent.parseUri(mTarget.getString(R.string.delete_package_intent), 0)
                        .setData(
                            Uri.fromParts(
                                "package",
                                componentName?.packageName,
                                componentName?.className
                            )
                        )
                        .putExtra(Intent.EXTRA_USER, mItemInfo.user)
                mTarget.startActivity(i)
            } catch (e: URISyntaxException) {
                Toast.makeText(launcher, R.string.uninstall_failed, Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUninstallTarget(launcher: Launcher, item: ItemInfo): ComponentName? {
            if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && item.id == NO_ID) {
                val intent = item.intent
                val user = item.user
                if (intent != null) {
                    val info = launcher
                        .getSystemService(LauncherApps::class.java)
                        .resolveActivity(intent, user)
                    if (info != null && !info.applicationInfo.flags.hasFlag(ApplicationInfo.FLAG_SYSTEM))
                        return info.componentName
                }
            } else {
                return item.targetComponent
            }
            return null
        }
    }

    companion object {
        val CUSTOMIZE = SystemShortcut.Factory<NeoLauncher> { activity, itemInfo, originalView ->
            val prefs = NeoPrefs.Companion.getInstance()
            var customize: Customize? = null
            if (Launcher.getLauncher(activity).isInState(LauncherState.NORMAL)) {
                if (prefs.desktopPopupEdit && !prefs.desktopLock.getValue()) {
                    getAppInfo(activity, itemInfo)?.let {
                        customize = Customize(activity, it, itemInfo, originalView)
                    }
                }
            } else {
                if (prefs.drawerPopupEdit) {
                    getAppInfo(activity, itemInfo)?.let {
                        customize = Customize(activity, it, itemInfo, originalView)
                    }
                }
            }
            customize
        }

        private fun getAppInfo(launcher: NeoLauncher, itemInfo: ItemInfo): AppInfo? {
            if (itemInfo is AppInfo) return itemInfo
            if (itemInfo.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) return null
            val key = ComponentKey(itemInfo.targetComponent, itemInfo.user)
            return launcher.appsView.appsStore.getApp(key)
        }

        val APP_REMOVE = SystemShortcut.Factory<NeoLauncher> { launcher, itemInfo, originalView ->
            val prefs = NeoPrefs.Companion.getInstance()
            var appRemove: AppRemove? = null
            if (Launcher.getLauncher(launcher).isInState(LauncherState.NORMAL)) {
                if (itemInfo is WorkspaceItemInfo
                    || itemInfo is LauncherAppWidgetInfo
                    || itemInfo is FolderInfo
                ) {
                    if (prefs.desktopPopupRemove && !prefs.desktopLock.getValue()
                    ) {
                        appRemove = AppRemove(launcher, itemInfo, originalView)
                    }
                }
            }
            appRemove
        }

        val APP_UNINSTALL =
            SystemShortcut.Factory<NeoLauncher> { launcher, itemInfo, originalView ->
                val prefs = NeoPrefs.Companion.getInstance()
                val inUninstallState =
                    (prefs.drawerPopupUninstall && launcher.isInState(LauncherState.ALL_APPS)) ||
                            (prefs.desktopPopupUninstall && !launcher.isInState(LauncherState.ALL_APPS))

                if (inUninstallState && itemInfo is ItemInfoWithIcon && !itemInfo.runtimeStatusFlags.hasFlags(
                        ItemInfoWithIcon.FLAG_SYSTEM_YES
                    )
                ) {
                    AppUninstall(launcher, itemInfo, originalView)
                } else {
                    null
                }
            }
    }
}
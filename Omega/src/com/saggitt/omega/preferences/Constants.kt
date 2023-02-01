package com.saggitt.omega.preferences

import com.android.launcher3.R


const val PREFS_DESKTOP_POPUP_EDIT = "desktop_popup_edit"
const val PREFS_DESKTOP_POPUP_REMOVE = "desktop_popup_remove"

const val PREFS_DRAWER_POPUP_EDIT = "drawer_popup_edit"
const val PREFS_DRAWER_POPUP_UNINSTALL = "drawer_popup_uninstall"

val desktopPopupOptions = mutableMapOf(
    PREFS_DESKTOP_POPUP_REMOVE to R.string.remove_drop_target_label,
    PREFS_DESKTOP_POPUP_EDIT to R.string.action_preferences,
)

val drawerPopupOptions = mutableMapOf(
    PREFS_DRAWER_POPUP_UNINSTALL to R.string.uninstall_drop_target_label,
    PREFS_DRAWER_POPUP_EDIT to R.string.action_preferences,
)

val iconIds = mapOf(
    // Desktop Popup
    PREFS_DESKTOP_POPUP_REMOVE to R.drawable.ic_remove_no_shadow,
    PREFS_DESKTOP_POPUP_EDIT to R.drawable.ic_edit_no_shadow,
    // Drawer Popup
    PREFS_DRAWER_POPUP_UNINSTALL to R.drawable.ic_uninstall_no_shadow,
    PREFS_DRAWER_POPUP_EDIT to R.drawable.ic_edit_no_shadow,
)
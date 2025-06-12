package com.saggitt.omega.encrypt

import androidx.compose.runtime.Composable
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.preferences.NeoPrefs

@Composable
fun EncryptAppPage(
    defaultTitle: String,
    componentKey: ComponentKey,
    appInfo: AppInfo,
    onClose: () -> Unit,
) {
    val prefs = NeoPrefs.getInstance();


}
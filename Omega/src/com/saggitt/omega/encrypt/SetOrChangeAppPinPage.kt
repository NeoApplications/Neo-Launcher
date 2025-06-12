package com.saggitt.omega.encrypt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.launcher3.util.ComponentKey
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun SetOrChangeAppPinPage(
    componentKey: ComponentKey,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val pinManager = remember { AppPinManager(context) }
    var step by remember { mutableStateOf("check") }

    if (step == "check" && pinManager.isPinSet(componentKey.componentName.packageName)) {
        VerifyOldPinPage(
            componentKey = componentKey,
            onSuccess = { step = "set" },
            onCancel = onClose
        )
    } else {
        SetNewPinPage(
            componentKey = componentKey,
            onDone = onClose,
            onCancel = onClose
        )
    }
}

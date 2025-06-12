package com.saggitt.omega.encrypt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.launcher3.util.ComponentKey

@Composable
fun SetNewPinPage(
    componentKey: ComponentKey,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val pinManager = remember { AppPinManager(context) }

    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("设置新密码")

        OutlinedTextField(
            value = pin1,
            onValueChange = {
                pin1 = it
                mismatch = false
            },
            label = { Text("新密码") },
            singleLine = true
        )
        OutlinedTextField(
            value = pin2,
            onValueChange = {
                pin2 = it
                mismatch = false
            },
            label = { Text("再次输入") },
            singleLine = true,
            isError = mismatch
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (pin1.isNotBlank() && pin1 == pin2) {
                    pinManager.setPin(componentKey.componentName.packageName, pin1)
                    onDone()
                } else {
                    mismatch = true
                }
            }) {
                Text("保存")
            }
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
        }

        if (mismatch) {
            Text("密码不一致", color = Color.Red)
        }
    }
}

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
fun VerifyOldPinPage(
    componentKey: ComponentKey,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val pinManager = remember { AppPinManager(context) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("请输入旧密码")
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                error = false
            },
            isError = error,
            singleLine = true,
            label = { Text("旧密码") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (pinManager.verifyPin(componentKey.componentName.packageName, input)) {
                    onSuccess()
                } else {
                    error = true
                }
            }) {
                Text("确认")
            }
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
        }
        if (error) {
            Text("密码错误", color = Color.Red)
        }
    }
}

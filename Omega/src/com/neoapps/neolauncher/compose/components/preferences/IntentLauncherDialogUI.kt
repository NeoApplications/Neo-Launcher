package com.neoapps.neolauncher.compose.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.DialogPositiveButton
import com.neoapps.neolauncher.preferences.IntentLauncherPref
import com.neoapps.neolauncher.preferences.NeoPrefs

@Composable
fun IntentLauncherDialogUI(
    pref: IntentLauncherPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(id = pref.summaryId),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                DialogPositiveButton(
                    textId = pref.positiveAnswerId,
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        context.startActivity(pref.intent())
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}
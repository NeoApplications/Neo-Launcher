/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neoapps.neolauncher.preferences.PREFS_DESKTOP_POPUP_REMOVE
import com.neoapps.neolauncher.preferences.iconIds

@Composable
fun SingleSelectionListItem(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    endWidget: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick, enabled = isEnabled),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            enabled = isEnabled,
            onClick = onClick,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            modifier = Modifier
                .weight(1f),
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (endWidget != null) {
            Spacer(modifier = Modifier.width(8.dp))
            endWidget()
        }
    }
}

@Composable
fun MultiSelectionListItem(
    modifier: Modifier = Modifier,
    text: String,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    withIcon: Boolean = false,
    iconId: String? = null,
    onClick: (Boolean) -> Unit = {}
) {
    val checkbox = @Composable {
        Checkbox(
            checked = isChecked,
            enabled = isEnabled,
            onCheckedChange = onClick,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface,
                checkmarkColor = Color.White
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = { onClick(!isChecked) }, enabled = isEnabled),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (withIcon) iconIds[iconId]?.let {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                    modifier = Modifier
                            .size(32.dp),
                painter = painterResource(id = it),
                contentDescription = text,
            )
        }
        else checkbox()
        Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (withIcon) checkbox()
    }
}

@Preview
@Composable
fun MultiSelectionListItemPreview() {
    MultiSelectionListItem(
        text = "Test",
        isChecked = true,
        withIcon = true,
        iconId = PREFS_DESKTOP_POPUP_REMOVE
    )
}
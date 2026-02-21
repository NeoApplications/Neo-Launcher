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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.addIf

@Composable
fun ComposeSwitchView(
    title: String,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
    summary: String = "",
    iconId: Int = 0,
    index: Int = 0,
    groupSize: Int = 1,
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
) {
    val (checked, check) = remember { mutableStateOf(isChecked) }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                GroupItemShape(index, groupSize - 1)
            )
            .clickable(enabled = isEnabled) {
                check(!checked)
                onCheckedChange(!checked)
            },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        leadingContent = iconId.takeIf { it != 0 }?.let {
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = "",
                    tint = Color(NeoPrefs.getInstance().profileAccentColor.getColor()),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))

                )
            }
        },
        headlineContent = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            if (summary.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .addIf(!isEnabled) {
                            alpha(0.3f)
                        },
                    text = summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        trailingContent = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    check(it)
                    onCheckedChange(it)
                },
                enabled = isEnabled,
            )
        },
    )
}
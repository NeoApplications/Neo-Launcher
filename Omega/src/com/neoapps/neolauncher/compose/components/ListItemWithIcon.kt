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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.BracketsCurly
import com.neoapps.neolauncher.theme.GroupItemShape

@Composable
fun ListItemWithIcon(
    modifier: Modifier = Modifier,
    title: String,
    summary: String = "",
    index: Int = 0,
    groupSize: Int = 1,
    startIcon: (@Composable () -> Unit)? = null,
    endCheckbox: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier
            .clip(GroupItemShape(index, groupSize - 1)),
        leadingContent = startIcon?.apply {} ?: {},
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            if (summary.isNotEmpty()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        trailingContent = endCheckbox?.apply {} ?: {},
        colors = ListItemDefaults.colors(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
        )
    )
}

@Preview
@Composable
fun PreviewListItemWithIcon() {
    Column {
    ListItemWithIcon(
        title = "System Iconpack",
        modifier = Modifier.clickable { },
        summary = "com.neoapps.neolauncher",
        startIcon = {
            Image(
                Phosphor.BracketsCurly,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45F)
                    )
            )

        },
        endCheckbox = {
            RadioButton(
                selected = false,
                onClick = null
            )
        },
        index = 0,
        groupSize = 2
    )
        Spacer(modifier = Modifier.height(2.dp))
        ListItemWithIcon(
            title = "System Iconpack",
            modifier = Modifier.clickable { },
            summary = "com.neoapps.neolauncher",
            startIcon = {
                Image(
                    Phosphor.BracketsCurly,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45F)
                        )
                )

            },
            endCheckbox = {
                RadioButton(
                    selected = false,
                    onClick = null
                )
            },
            index = 1,
            groupSize = 2
        )
    }
}
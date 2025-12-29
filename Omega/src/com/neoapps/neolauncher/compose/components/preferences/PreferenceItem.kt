/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.neoapps.neolauncher.compose.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.addIf

@Composable
fun PreferenceItem(
    title: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    index: Int = 0,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                GroupItemShape(index, groupSize - 1)
            )
            .addIf(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        leadingContent = startWidget,
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
        trailingContent = endWidget,
    )
}

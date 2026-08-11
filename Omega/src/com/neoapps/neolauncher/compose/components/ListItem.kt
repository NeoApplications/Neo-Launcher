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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.preferences.PREFS_DESKTOP_POPUP_EDIT
import com.neoapps.neolauncher.preferences.iconIds
import com.neoapps.neolauncher.theme.GroupItemShape

@Composable
fun SingleSelectionListItem(
    modifier: Modifier = Modifier,
    title: String,
    secondaryText: String? = null,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    index: Int = 1,
    groupSize: Int = 1,
    endWidget: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {

    val startWidget: @Composable (() -> Unit) = {
        RadioButton(
            selected = isSelected,
            enabled = isEnabled,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(24.dp)
        )
    }
    ListItem(
        modifier = modifier
            .clip(
                GroupItemShape(index, groupSize - 1)
            )
            .clickable(
                enabled = isEnabled,
                onClick = { onClick() }
            ),
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (!isEnabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = if (secondaryText != null) {
            {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        leadingContent = startWidget,
        trailingContent = endWidget,
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectionListItem(
    modifier: Modifier = Modifier,
    text: String,
    secondaryText: String? = null,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    index: Int = 1,
    groupSize: Int = 1,
    withIcon: Boolean = false,
    iconId: String? = null,
    onClick: (Boolean) -> Unit = {}
) {
    val leadingContent: @Composable (() -> Unit)? = if (withIcon && iconId != null) {
        {
            iconIds[iconId]?.let { iconResId ->
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = if (isEnabled) {
                        if (isChecked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    } else null

    val trailingContent: @Composable (() -> Unit) = {
        Checkbox(
            checked = isChecked,
            enabled = isEnabled,
            onCheckedChange = onClick,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.size(24.dp)
        )
    }

    ListItem(
        modifier = modifier
            .clip(
                GroupItemShape(index, groupSize - 1)
            )
            .clickable(
                enabled = isEnabled,
                onClick = { onClick(!isChecked) }
            ),
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (!isEnabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = if (secondaryText != null) {
            {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isEnabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    )
}

@Preview
@Composable
fun MultiSelectionListItemPreview() {
    MultiSelectionListItem(
        text = "Test",
        isChecked = true,
        withIcon = true,
        iconId = PREFS_DESKTOP_POPUP_EDIT
    )
}

@Preview
@Composable
fun SingleSelectionListItemPreview() {
    SingleSelectionListItem(
        title = "Test",
        isSelected = false
    )
}
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "containerColor"
    )

    val startWidget: @Composable (() -> Unit) = {
        RadioButton(
            selected = isSelected,
            enabled = isEnabled,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        leadingContent = startWidget,
        trailingContent = endWidget,
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        isChecked -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    } else null

    val trailingContent: @Composable (() -> Unit) = {
        Checkbox(
            checked = isChecked,
            enabled = isEnabled,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }

    ListItem(
        modifier = modifier
            .clip(GroupItemShape(index, groupSize - 1))
            .toggleable(
                value = isChecked,
                enabled = isEnabled,
                role = Role.Checkbox,
                onValueChange = onClick
            ),
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    isChecked -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = secondaryText?.let { supporting ->
            {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        isChecked -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            }
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
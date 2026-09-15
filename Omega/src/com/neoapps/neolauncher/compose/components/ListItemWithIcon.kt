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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    startIcon: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier
            .clip(GroupItemShape(index, groupSize - 1)),
        leadingContent = startIcon,
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
        trailingContent = icon?.let {
            @Composable {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = contentColor,
            leadingIconColor = contentColor,
            supportingColor = contentColor,
            trailingIconColor = contentColor,
        )
    )
}

@Composable
fun ListItemWithRadioButton(
    modifier: Modifier = Modifier,
    title: String,
    summary: String = "",
    index: Int = 0,
    groupSize: Int = 1,
    radioButton: Boolean = true,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    startIcon: (@Composable () -> Unit)? = null,
) {
    val actualContainerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            containerColor
        },
        label = "containerColor"
    )

    val itemModifier = modifier
        .clip(GroupItemShape(index, groupSize - 1))
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )

    ListItem(
        modifier = itemModifier,
        leadingContent = startIcon,
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (!enabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = if (summary.isNotEmpty()) {
            {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        } else null,
        trailingContent = if (radioButton) {
            {
                RadioButton(
                    selected = selected,
                    enabled = enabled,
                    onClick = null,
                    modifier = Modifier.size(24.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                )
            }
        } else null,
        colors = ListItemDefaults.colors(
            containerColor = actualContainerColor,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListItemWithCheckbox(
    modifier: Modifier = Modifier,
    title: String,
    summary: String = "",
    index: Int = 0,
    groupSize: Int = 1,
    checkBox: Boolean = true,
    checked: Boolean = false,
    enabled: Boolean = true,
    onCheck: ((Boolean) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    startIcon: (@Composable () -> Unit)? = null,
    onClick: (Boolean) -> Unit = {}
) {
    val actualContainerColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            containerColor
        },
        label = "containerColor"
    )

    val itemModifier = modifier
        .clip(GroupItemShape(index, groupSize - 1))
        .toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = { newValue ->
                onClick(newValue)
                onCheck?.invoke(newValue)
            }
        )

    ListItem(
        modifier = itemModifier,
        leadingContent = startIcon,
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = if (!enabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = if (summary.isNotEmpty()) {
            {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        } else null,
        trailingContent = if (checkBox) {
            {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    enabled = enabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                        disabledCheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .clearAndSetSemantics {}
                )
            }
        } else null,
        colors = ListItemDefaults.colors(
            containerColor = actualContainerColor,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Preview
@Composable
fun PreviewListItemWithIcon() {
    Column {
        ListItemWithRadioButton(
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
            radioButton = true,
            selected = false,
            index = 0,
            groupSize = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        ListItemWithRadioButton(
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
            radioButton = true,
            selected = false,
            index = 1,
            groupSize = 2
        )
    }
}
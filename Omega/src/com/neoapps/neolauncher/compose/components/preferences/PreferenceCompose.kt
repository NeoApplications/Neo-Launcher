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

package com.neoapps.neolauncher.compose.components.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.navigation.LocalPaneNavigator
import com.neoapps.neolauncher.compose.navigation.NavRoute
import com.neoapps.neolauncher.preferences.BooleanPref
import com.neoapps.neolauncher.preferences.ColorIntPref
import com.neoapps.neolauncher.preferences.DialogPref
import com.neoapps.neolauncher.preferences.FloatPref
import com.neoapps.neolauncher.preferences.GridSize
import com.neoapps.neolauncher.preferences.GridSize2D
import com.neoapps.neolauncher.preferences.IntSelectionPref
import com.neoapps.neolauncher.preferences.IntentLauncherPref
import com.neoapps.neolauncher.preferences.LongSelectionPref
import com.neoapps.neolauncher.preferences.NavigationPref
import com.neoapps.neolauncher.preferences.StringMultiSelectionPref
import com.neoapps.neolauncher.preferences.StringPref
import com.neoapps.neolauncher.preferences.StringSelectionPref
import com.neoapps.neolauncher.preferences.StringSetPref
import com.neoapps.neolauncher.preferences.StringTextPref
import com.neoapps.neolauncher.theme.AccentColorOption
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.addIf
import kotlinx.coroutines.launch

@Composable
fun BasePreference(
    @StringRes titleId: Int,
    modifier: Modifier = Modifier,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    isEnabled: Boolean = true,
    index: Int = 1,
    groupSize: Int = 1,
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
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
                text = stringResource(id = titleId),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                if (summaryId != -1 || summary != null) {
                    Text(
                        text = summary ?: stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                bottomWidget?.let {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    bottomWidget()
                }
            }
        },
        trailingContent = endWidget,
    )
}

@Composable
fun StringPreference(
    modifier: Modifier = Modifier,
    pref: StringPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            pref.onClick?.invoke()
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NavigationPreference(
    modifier: Modifier = Modifier,
    pref: NavigationPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val paneNavigator = LocalPaneNavigator.current
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        endWidget = {
            pref.endIcon(pref.getValue())
        },
        onClick = {
            scope.launch {
                paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, pref.navRoute)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ColorIntPreference(
    modifier: Modifier = Modifier,
    pref: ColorIntPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val paneNavigator = LocalPaneNavigator.current
    val currentColor by remember(pref) {
        mutableIntStateOf(AccentColorOption.fromString(pref.getValue()).accentColor)
    }
    val scope = rememberCoroutineScope()

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            scope.launch {
                paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, pref.navRoute)
            }
        },
        endWidget = {
            Canvas(
                modifier = Modifier
                    .size(40.dp),
                onDraw = {
                    drawCircle(color = Color.Black, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = Color(currentColor))
                }
            )
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeekBarPreference(
    modifier: Modifier = Modifier,
    pref: FloatPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onValueChange: ((Float) -> Unit) = {},
) {
    var currentValue by remember(pref) { mutableFloatStateOf(pref.getValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        bottomWidget = {
            Row {

                var menuExpanded by remember { mutableStateOf(false) }
                Text(
                    text = pref.specialOutputs(currentValue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .widthIn(min = 52.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                menuExpanded = !menuExpanded
                            }
                        )
                )
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        onClick = {
                            currentValue = pref.defaultValue
                            pref.setValue(currentValue)
                            onValueChange(currentValue)
                            menuExpanded = false
                        },
                        text = { Text(text = stringResource(id = R.string.reset_to_default)) }
                    )
                }

                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = currentValue,
                    valueRange = pref.minValue..pref.maxValue,
                    onValueChange = { currentValue = it },
                    steps = pref.steps,
                    onValueChangeFinished = {
                        pref.setValue(currentValue)
                        onValueChange(currentValue)
                    },
                    enabled = isEnabled
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StringSetPreference(
    modifier: Modifier = Modifier,
    pref: StringSetPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val paneNavigator = LocalPaneNavigator.current
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            scope.launch {
                paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, pref.navRoute)
            }
        }
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    pref: BooleanPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val (checked, check) = remember(pref) { mutableStateOf(pref.getValue()) }
    val coroutineScope = rememberCoroutineScope()

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
            coroutineScope.launch { pref.setValue(!checked) }
        },
        endWidget = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                    coroutineScope.launch { pref.setValue(it) }
                },
                thumbContent = {
                    if (checked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = Color.White
                        )
                    }
                },
                enabled = isEnabled,
            )
        }
    )
}

@Composable
fun IntSelectionPreference(
    modifier: Modifier = Modifier,
    pref: IntSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val value = pref.get().collectAsState(initial = pref.defaultValue)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[value.value]?.let { stringResource(id = it) },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun LongSelectionPreference(
    modifier: Modifier = Modifier,
    pref: LongSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val value = pref.get().collectAsState(initial = pref.defaultValue)
    val entries = pref.entries()
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = entries[value.value],
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringSelectionPreference(
    modifier: Modifier = Modifier,
    pref: StringSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.getValue()],
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun GridSizePreference(
    modifier: Modifier = Modifier,
    pref: GridSize,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summary = pref.numColumnsPref.getValue().toString(),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun GridSize2DPreference(
    modifier: Modifier = Modifier,
    pref: GridSize2D,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val rows = pref.numRowsPref.get().collectAsState(initial = pref.numRowsPref.defaultValue)
    val columns =
        pref.numColumnsPref.get().collectAsState(initial = pref.numColumnsPref.defaultValue)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summary = "${columns.value} x ${rows.value}",
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringMultiSelectionPreference(
    modifier: Modifier = Modifier,
    pref: StringMultiSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries
            .filter { pref.getValue().contains(it.key) }
            .values.let {
                it.map { stringResource(id = it) }.joinToString(separator = ", ")
            },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun AlertDialogPreference(
    modifier: Modifier = Modifier,
    pref: DialogPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringTextPreference(
    modifier: Modifier = Modifier,
    pref: StringTextPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.getValue(),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PagePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    route: NavRoute,
    icon: ImageVector? = null,
    index: Int = 1,
    groupSize: Int = 1,
) {
    val scope = rememberCoroutineScope()
    val paneNavigator = LocalPaneNavigator.current
    BasePreference(
        modifier = modifier,
        titleId = titleId,
        startWidget = icon?.let {
            { Icon(imageVector = icon, contentDescription = stringResource(id = titleId)) }
        },
        index = index,
        groupSize = groupSize,
        onClick = {
            scope.launch {
                paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, route)
            }
        }
    )
}

@Composable
fun IntentLauncherPreference(
    modifier: Modifier = Modifier,
    pref: IntentLauncherPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val summaryId = if (pref.getValue()) R.string.notification_dots_desc_on
    else R.string.notification_dots_desc_off

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}
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

package com.saggitt.omega.compose.components.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.subRoute
import com.saggitt.omega.preferences.BooleanPref
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.util.addIf
import kotlinx.coroutines.launch

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
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
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp))
            .heightIn(min = 64.dp)
            .addIf(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }, verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startWidget?.let {
                startWidget()
                Spacer(modifier = Modifier.requiredWidth(8.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = stringResource(id = titleId),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
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
            endWidget?.let {
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                endWidget()
            }
        }
    }
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
    val checked = pref.get().collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            onCheckedChange(!checked.value)
            check(!checked.value)
            coroutineScope.launch { pref.set(!checked.value) }
        },
        endWidget = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked.value,
                onCheckedChange = {
                    onCheckedChange(it)
                    coroutineScope.launch { pref.set(it) }
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
fun PagePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @DrawableRes iconId: Int = -1,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    route: String
) {
    val navController = LocalNavController.current
    val destination = subRoute(route)
    BasePreference(
        modifier = modifier,
        titleId = titleId,
        startWidget =
        if (iconId != -1) {
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = stringResource(id = titleId),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = { navController.navigate(destination) }
    )
}
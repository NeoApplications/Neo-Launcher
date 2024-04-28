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

package com.saggitt.omega.groups.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.saggitt.omega.theme.GroupItemShape

@Composable
fun CategoryTabItem(
    @StringRes titleId: Int,
    @StringRes summaryId: Int,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(GroupItemShape(0, 0))
            .clickable(onClick = onClick),
        tonalElevation = 12.dp,
        leadingContent = {
            if (iconId != null) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = "",
                )
            }
        },
        headlineContent = {
            Text(
                text = stringResource(id = titleId),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        supportingContent = {
            Text(
                text = stringResource(id = summaryId),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = "",
            )
        }
    )
}

@Composable
@Preview
fun CategoryTabItemPreview() {
    CategoryTabItem(
        titleId = R.string.tab_type_smart,
        summaryId = R.string.custom
    ) {}
}
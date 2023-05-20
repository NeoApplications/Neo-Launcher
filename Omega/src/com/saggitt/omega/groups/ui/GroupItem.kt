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


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.saggitt.omega.theme.GroupItemShape

@Composable
fun GroupItem(
    title: String,
    summary: String?,
    modifier: Modifier = Modifier,
    removable: Boolean,
    index: Int = 0,
    groupSize: Int = 1,
    onClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
) {
    val mShape = GroupItemShape(index, groupSize - 1)

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(mShape)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                .copy(alpha = 0.7f)
        ),
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = title)
                Text(
                    text = summary ?: "",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (removable) IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = onRemoveClick
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_uninstall_no_shadow),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFDA0831))
                    )
                }
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        //TODO drag group action
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_drag_handle),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun GroupItemPreview() {
    GroupItem(
        title = "Tab 1",
        summary = "--5 APPS",
        removable = true,
        index = 0,
        groupSize = 2
    )
}
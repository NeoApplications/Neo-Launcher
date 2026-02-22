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
package com.neoapps.neolauncher.compose.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.theme.GroupItemShape

@Composable
fun ExpandableListItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: Bitmap,
    onClick: () -> Unit = {},
    index: Int = 1,
    groupSize: Int = 1,
    content: @Composable (ColumnScope.() -> Unit),
) {
    var isContentVisible by rememberSaveable { mutableStateOf(false) }

    val boxShape = GroupItemShape(index, groupSize - 1)
    val backgroundColor by animateColorAsState(
        targetValue = if (isContentVisible) MaterialTheme.colorScheme.surfaceVariant
        else Color.Transparent,
        label = "expandableBackground"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(boxShape)
            .background(backgroundColor)
            .clickable {
                onClick()
                isContentVisible = !isContentVisible
            },
        verticalArrangement = Arrangement.Center
    ) {
        ListItemWithIcon(
            modifier = Modifier
                .clip(GroupItemShape(index, if (isContentVisible) groupSize else groupSize - 1)),
            title = title,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            startIcon = {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                )
            },
            endCheckbox = {
                val arrow = if (isContentVisible) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
                Image(
                    painter = painterResource(id = arrow),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            },
            index = index,
            groupSize = groupSize
        )
        AnimatedVisibility(visible = isContentVisible) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

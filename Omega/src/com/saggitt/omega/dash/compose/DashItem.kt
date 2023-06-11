/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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
package com.saggitt.omega.dash.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.CaretRight
import com.saggitt.omega.compose.icons.phosphor.ImageSquare
import com.saggitt.omega.compose.icons.phosphor.Wrench

// TODO include padding in the items to insure real block-ratio

@Composable
fun ControlDashItem(
    modifier: Modifier = Modifier,
    ratio: Float = 3f,
    icon: ImageVector,
    description: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    isExtendable: Boolean = true,
    enabled: Boolean = false,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(ratio),
        shape = MaterialTheme.shapes.medium,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (enabled) tint
            else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            contentColor = if (enabled) MaterialTheme.colorScheme.background
            else tint
        ),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .aspectRatio(1f),
                imageVector = icon,
                contentDescription = description
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                if (isExtendable) Icon(
                    imageVector = Phosphor.CaretRight,
                    contentDescription = description
                ) else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun ControlDashItemPreview() {
    ControlDashItem(
        icon = Phosphor.ImageSquare,
        description = "ControlThis"
    ) {

    }
}

@Composable
fun ActionDashItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = false,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (enabled) tint
            else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            contentColor = if (enabled) MaterialTheme.colorScheme.background
            else tint
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(0.5f),
                imageVector = icon,
                contentDescription = description
            )
        }
    }
}

@Preview
@Composable
fun ActionDashItemPreview() {
    ActionDashItem(icon = Phosphor.Wrench, description = "ActionThat") {

    }
}

@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    ratio: Float = 1f,
    icon: Painter,
    tint: Color = MaterialTheme.colorScheme.primary,
    description: String,
    enabled: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit) = {
        Icon(painter = icon, contentDescription = description)
    },
) {
    ElevatedButton(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(ratio),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (enabled) MaterialTheme.colorScheme.background else tint
        ),
        contentPadding = PaddingValues(8.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() },
        content = content
    )
}




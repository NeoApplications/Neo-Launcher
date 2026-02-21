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
package com.neoapps.neolauncher.dash.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.CaretRight
import com.neoapps.neolauncher.compose.icons.phosphor.ImageSquare
import com.neoapps.neolauncher.compose.icons.phosphor.Wrench

// TODO include padding in the items to insure real block-ratio

@Composable
fun ControlDashItem(
    modifier: Modifier = Modifier,
    ratio: Float = 3f,
    icon: ImageVector,
    description: String,
    tint: Color = MaterialTheme.colorScheme.primaryContainer,
    isExtendable: Boolean = true,
    enabled: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        if (enabled) tint
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "container"
    )
    val contentColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
        else tint,
        label = "content"
    )

    FilledTonalIconButton(
        modifier = modifier
            .aspectRatio(ratio),
        shape = MaterialTheme.shapes.medium,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight(0.4f)
                    .aspectRatio(1f),
                imageVector = icon,
                contentDescription = description
            )
            Text(
                modifier = Modifier.weight(1f),
                text = description,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (isExtendable) Icon(
                imageVector = Phosphor.CaretRight,
                contentDescription = description
            )
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
    tint: Color = MaterialTheme.colorScheme.primaryContainer,
    enabled: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        if (enabled) tint
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "container"
    )
    val contentColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
        else tint,
        label = "content"
    )

    FilledTonalIconButton(
        modifier = modifier
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(0.4f),
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
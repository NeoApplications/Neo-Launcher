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

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.objects.MusicControlItem

// Make the each button a ControlDashProvider??
@Composable
fun MusicBar(
    ratio: Float,
    audioManager: AudioManager,
) {
    val (playing, play) = remember {
        mutableStateOf(audioManager.isMusicActive)
    }

    Row(
        modifier = Modifier
            .background(Color.Transparent, MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            MusicControlItem.PREVIOUS,
            if (playing) MusicControlItem.PAUSE else MusicControlItem.PLAY,
            MusicControlItem.NEXT
        ).forEach {
            FilledTonalIconButton(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(ratio / 2.8f),
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    it.onClick(audioManager)
                    play(it != MusicControlItem.PAUSE)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .aspectRatio(1f),
                        imageVector = it.icon,
                        contentDescription = stringResource(id = it.description)
                    )
                }
            }
        }
    }
}
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
package com.neoapps.neolauncher.dash

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.dash.actionprovider.AudioPlayer
import com.neoapps.neolauncher.dash.compose.ActionDashItem
import com.neoapps.neolauncher.dash.compose.ControlDashItem
import com.neoapps.neolauncher.dash.compose.MusicBar
import com.neoapps.neolauncher.util.prefs

// TODO add better support for horizontal
@Composable
fun DashPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val activeDashProviders = prefs.dashProvidersItems.getAll()

    val actionItems = getDashActionProviders(context)
        .filter {
            it.javaClass.name in activeDashProviders && it.javaClass.name != AudioPlayer::class.java.name
    }.associateBy { it.javaClass.name }
    val controlItems = getDashControlProviders(
        context
    ).filter {
        it.javaClass.name in activeDashProviders
    }.associateBy { it.javaClass.name }
    val musicManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val lineSize = prefs.dashLineSize.getValue()

    val displayItems = actionItems + controlItems
    val displayItemsSorted = activeDashProviders.mapNotNull { displayItems[it] }
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(lineSize),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        if (activeDashProviders.contains(AudioPlayer::class.java.name)) item(
            span = { GridItemSpan(lineSize) }) { // TODO abstract DashProviders to Constants
            MusicBar(
                ratio = lineSize.toFloat(),
                audioManager = musicManager,
            )
        }
        itemsIndexed(
            items = displayItemsSorted,
            span = { _, item ->
                when (item) {
                    is DashControlProvider -> GridItemSpan(2)
                    else                   -> GridItemSpan(1)
                }
            },
            key = { _: Int, item: DashProvider -> item.javaClass.name }) { _, item ->
            when (item) {
                is DashControlProvider -> {
                    val enabled = remember {
                        mutableStateOf(item.state)
                    }
                    ControlDashItem(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        icon = item.icon,
                        description = item.name,
                        ratio = 2.15f,
                        isExtendable = item.extendable,
                        enabled = enabled.value,
                        onClick = {
                            item.state = !enabled.value
                            enabled.value = !enabled.value
                        }
                    )
                }

                is DashActionProvider  -> ActionDashItem(
                    icon = item.icon,
                    description = item.name,
                    onClick = { item.runAction(context) }
                )
            }
        }
    }
}
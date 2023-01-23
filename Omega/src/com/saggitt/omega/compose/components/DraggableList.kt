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

package com.saggitt.omega.compose.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun <T> DraggableList(
    list: SnapshotStateList<T>,
    onDragEnd: () -> Unit = {},
    content: LazyListScope.((Int) -> Modifier) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val onMove: (Int, Int) -> Unit = { from, to ->
        list.move(from, to)
    }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    val jitModifier = { index: Int ->
        val offsetOrNull =
            dragDropListState.elementDisplacement.takeIf {
                index == dragDropListState.currentIndexOfDraggedItem
            }

        Modifier
            .graphicsLayer {
                translationY = offsetOrNull ?: 0f
            }

    }

    LazyColumn(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropListState.onDrag(offset)

                        if (overscrollJob?.isActive == true)
                            return@detectDragGesturesAfterLongPress

                        dragDropListState.checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                overscrollJob = coroutineScope.launch {
                                    dragDropListState.lazyListState.scrollBy(it)
                                }
                            }
                            ?: run { overscrollJob?.cancel() }
                    },
                    onDragStart = { offset -> dragDropListState.onDragStart(offset) },
                    onDragEnd = {
                        dragDropListState.onDragInterrupted()
                        onDragEnd()
                    },
                    onDragCancel = { dragDropListState.onDragInterrupted() }
                )
            },
        state = dragDropListState.lazyListState
    ) {
        content(jitModifier)
    }
}
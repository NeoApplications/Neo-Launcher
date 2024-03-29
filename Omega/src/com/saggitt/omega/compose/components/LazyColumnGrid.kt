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

package com.saggitt.omega.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun LazyListScope.verticalGridItems(
    modifier: Modifier = Modifier,
    count: Int,
    numColumns: Int,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    itemContent: @Composable GridItemScope.(index: Int) -> Unit
) {
    if (numColumns == 0) return
    val numRows = (count - 1) / numColumns + 1
    items(numRows) { row ->
        val gridItemScope = object : GridItemScope {
            override suspend fun LazyListState.scrollToThisItem() {
                animateScrollToItem(row, 0)
            }
        }
        if (row != 0) {
            Spacer(modifier = Modifier.height(verticalGap))
        }
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (col in 0 until numColumns) {
                if (col != 0) {
                    Spacer(modifier = Modifier.requiredWidth(horizontalGap))
                }
                val index = row * numColumns + col
                if (index < count) {
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(gridItemScope, index)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

fun <T> LazyListScope.verticalGridItems(
    modifier: Modifier = Modifier,
    items: List<T>,
    numColumns: Int,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    itemContent: @Composable GridItemScope.(index: Int, item: T) -> Unit
) {
    verticalGridItems(
        modifier = modifier,
        count = items.size,
        numColumns = numColumns,
        horizontalGap = horizontalGap,
        verticalGap = verticalGap
    ) { index ->
        itemContent(index, items[index])
    }
}

interface GridItemScope {
    suspend fun LazyListState.scrollToThisItem()
}

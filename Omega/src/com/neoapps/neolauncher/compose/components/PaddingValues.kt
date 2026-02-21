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
package com.neoapps.neolauncher.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
operator fun PaddingValues.minus(b: PaddingValues): PaddingValues {
    val a = this
    return remember(a, b) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                val aLeft = a.calculateLeftPadding(layoutDirection)
                val bLeft = b.calculateRightPadding(layoutDirection)
                return (aLeft - bLeft).coerceAtLeast(0.dp)
            }

            override fun calculateTopPadding(): Dp {
                val aTop = a.calculateTopPadding()
                val bTop = b.calculateTopPadding()
                return (aTop - bTop).coerceAtLeast(0.dp)
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                val aRight = a.calculateRightPadding(layoutDirection)
                val bRight = b.calculateRightPadding(layoutDirection)
                return (aRight - bRight).coerceAtLeast(0.dp)
            }

            override fun calculateBottomPadding(): Dp {
                val aBottom = a.calculateBottomPadding()
                val bBottom = b.calculateBottomPadding()
                return (aBottom - bBottom).coerceAtLeast(0.dp)
            }
        }
    }
}

@Composable
operator fun PaddingValues.plus(b: PaddingValues): PaddingValues {
    val a = this
    return remember(a, b) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                val aLeft = a.calculateLeftPadding(layoutDirection)
                val bLeft = b.calculateRightPadding(layoutDirection)
                return (aLeft + bLeft).coerceAtLeast(0.dp)
            }

            override fun calculateTopPadding(): Dp {
                val aTop = a.calculateTopPadding()
                val bTop = b.calculateTopPadding()
                return (aTop + bTop).coerceAtLeast(0.dp)
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                val aRight = a.calculateRightPadding(layoutDirection)
                val bRight = b.calculateRightPadding(layoutDirection)
                return (aRight + bRight).coerceAtLeast(0.dp)
            }

            override fun calculateBottomPadding(): Dp {
                val aBottom = a.calculateBottomPadding()
                val bBottom = b.calculateBottomPadding()
                return (aBottom + bBottom).coerceAtLeast(0.dp)
            }
        }
    }
}

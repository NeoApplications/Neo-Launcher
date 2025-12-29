/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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
 */

package com.neoapps.neolauncher.allapps

import androidx.compose.ui.graphics.vector.ImageVector
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Asterisk
import com.neoapps.neolauncher.compose.icons.phosphor.CirclesFour
import com.neoapps.neolauncher.compose.icons.phosphor.ImageSquare
import com.neoapps.neolauncher.compose.icons.phosphor.MapPin
import com.neoapps.neolauncher.compose.icons.phosphor.PaintBrush
import com.neoapps.neolauncher.compose.icons.phosphor.Pizza
import com.neoapps.neolauncher.compose.icons.phosphor.SpeakerHigh
import com.neoapps.neolauncher.compose.icons.phosphor.Swatches
import com.neoapps.neolauncher.compose.icons.phosphor.Wrench

const val FILTER_CATEGORY_ALL = "All"
val String.appCategoryIcon: ImageVector
    get() = when (this.lowercase()) {
        FILTER_CATEGORY_ALL.lowercase() -> Phosphor.CirclesFour
        "art and design" -> Phosphor.PaintBrush
        "food and drink" -> Phosphor.Pizza
        "photography" -> Phosphor.ImageSquare
        "music" -> Phosphor.SpeakerHigh
        "personalization" -> Phosphor.Swatches
        "tools" -> Phosphor.Wrench
        "travel and navigation" -> Phosphor.MapPin
        else -> Phosphor.Asterisk

    }
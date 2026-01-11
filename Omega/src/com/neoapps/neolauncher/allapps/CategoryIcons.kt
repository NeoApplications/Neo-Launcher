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
import com.neoapps.neolauncher.compose.icons.phosphor.Bank
import com.neoapps.neolauncher.compose.icons.phosphor.Book
import com.neoapps.neolauncher.compose.icons.phosphor.Briefcase
import com.neoapps.neolauncher.compose.icons.phosphor.ChatCircle
import com.neoapps.neolauncher.compose.icons.phosphor.CirclesFour
import com.neoapps.neolauncher.compose.icons.phosphor.Code
import com.neoapps.neolauncher.compose.icons.phosphor.GameController
import com.neoapps.neolauncher.compose.icons.phosphor.Heatbeat
import com.neoapps.neolauncher.compose.icons.phosphor.ImageSquare
import com.neoapps.neolauncher.compose.icons.phosphor.MapPin
import com.neoapps.neolauncher.compose.icons.phosphor.MusicNote
import com.neoapps.neolauncher.compose.icons.phosphor.PaintBrush
import com.neoapps.neolauncher.compose.icons.phosphor.Pizza
import com.neoapps.neolauncher.compose.icons.phosphor.ShoppingCart
import com.neoapps.neolauncher.compose.icons.phosphor.Student
import com.neoapps.neolauncher.compose.icons.phosphor.Swatches
import com.neoapps.neolauncher.compose.icons.phosphor.Television
import com.neoapps.neolauncher.compose.icons.phosphor.Wrench

const val FILTER_CATEGORY_ALL = "All"
val String.appCategoryIcon: ImageVector
    get() = when (this.lowercase()) {
        FILTER_CATEGORY_ALL.lowercase() -> Phosphor.CirclesFour
        "art_and_design" -> Phosphor.PaintBrush
        "business_and_productivity" -> Phosphor.Briefcase
        "communication" -> Phosphor.ChatCircle
        "development" -> Phosphor.Code
        "education" -> Phosphor.Student
        "entertainment" -> Phosphor.Television
        "finance" -> Phosphor.Bank
        "food_and_drink" -> Phosphor.Pizza
        "game" -> Phosphor.GameController
        "health_and_fitness" -> Phosphor.Heatbeat
        "knowledge_and_reference" -> Phosphor.Book
        "lifestyle" -> Phosphor.ShoppingCart
        "music" -> Phosphor.MusicNote
        "personalization" -> Phosphor.Swatches
        "photography" -> Phosphor.ImageSquare
        "tools" -> Phosphor.Wrench
        "travel_and_navigation" -> Phosphor.MapPin
        else -> Phosphor.Asterisk

    }
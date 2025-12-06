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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.saggitt.omega.util.vertical

@Composable
fun AllAppsCategories(
    categories: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    onClick: (Pair<String, String>) -> Unit
) {
    val items = categories.map {
        Pair(it.second, it.second.appCategoryIcon)
    }

    val selectedKey = remember { mutableStateOf("All") }
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .horizontalScroll(rememberScrollState()),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { item ->
            CategoryItem(
                modifier = Modifier
                    .vertical()
                    .rotate(-90f)
                    .wrapContentHeight(),
                icon = item.second,
                label = item.first,
                selected = item.first == selectedKey.value,
                onClick = {
                    selectedKey.value = item.first
                    onClick(categories.find { it.second == item.first }!!)
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.primary,
        iconColor = MaterialTheme.colorScheme.onSurface,
        labelColor = MaterialTheme.colorScheme.onSurface,
    ),
    onClick: () -> Unit
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = null,
        colors = colors,
        onClick = onClick,
        selected = selected,
        label = {
            Text(
                text = label,
                maxLines = 1,
            )
        },
        leadingIcon = {
            AnimatedVisibility(visible = selected) {
                Icon(
                    modifier = Modifier,
                    imageVector = icon,
                    contentDescription = label,
                )
            }
        }
    )
}
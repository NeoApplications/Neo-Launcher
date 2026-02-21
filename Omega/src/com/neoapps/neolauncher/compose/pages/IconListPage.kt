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

package com.neoapps.neolauncher.compose.pages

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroupDescription
import com.neoapps.neolauncher.data.models.IconPickerItem
import com.neoapps.neolauncher.iconpack.IconPack
import com.neoapps.neolauncher.iconpack.filter
import com.neoapps.neolauncher.icons.drawableToBitmap
import com.neoapps.neolauncher.util.getIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconListPage(
    iconPack: IconPack,
    searchQuery: String,
    onClickItem: (item: IconPickerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var loadFailed by remember { mutableStateOf(false) }
    val categoriesFlow = remember(iconPack) {
        iconPack.getAllIcons()
            .catch { loadFailed = true }
    }
    val categories by categoriesFlow.collectAsState(emptyList())
    val filteredCategories by remember(searchQuery) {
        derivedStateOf {
            categories.mapNotNull {
                it.filter(searchQuery)
                    .takeUnless { cat -> cat.items.isEmpty() }
            }
        }
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.FixedSize(72.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        filteredCategories.forEach { category ->
            // TODO restoring stickyHeaders in future
            item(
                span = { GridItemSpan(this.maxLineSpan) },
            ) {
                Text(
                    text = category.title,
                    modifier = Modifier
                        .padding(12.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(
                items = category.items,
                key = { it.drawableName },
            ) { item ->
                IconPreview(
                    iconPack = iconPack,
                    iconItem = item,
                    onClick = {
                        onClickItem(item)
                    }
                )
            }
        }
        if (loadFailed) {
            item(
                span = { GridItemSpan(this.maxLineSpan) },
            ) {
                PreferenceGroupDescription(
                    description = stringResource(id = R.string.icon_picker_load_failed)
                )
            }
        }
    }
}

@Composable
fun IconPreview(
    iconPack: IconPack,
    iconItem: IconPickerItem,
    onClick: () -> Unit,
) {
    val drawable by produceState<Drawable?>(initialValue = null, iconPack, iconItem) {
        launch(Dispatchers.IO) {
            value = iconPack.getIcon(iconItem.toIconEntry(), 0)
        }
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Image(
            bitmap = drawableToBitmap(drawable ?: LocalContext.current.getIcon()).asImageBitmap(),
            contentDescription = iconItem.drawableName,
            modifier = Modifier.aspectRatio(1f),
        )
    }
}
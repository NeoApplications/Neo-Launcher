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
package com.neoapps.neolauncher.compose.pages.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.move
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Plus
import com.neoapps.neolauncher.data.SearchProviderRepository
import com.neoapps.neolauncher.preferences.NeoPrefs
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchProvidersPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val searchProviderRepository: SearchProviderRepository = getKoin().get()
    val searchProviders = NeoPrefs.getInstance().searchProviders
    val openDialog = remember { mutableStateOf(false) }
    var selectedProvider by remember {
        mutableLongStateOf(0L)
    }

    val enabledItems by searchProviderRepository.enabledProviders.collectAsState()
    val disabledItems by searchProviderRepository.disabledProviders.collectAsState()

    val saveList = {
        val enabledKeys = enabledItems.map { it.id }
        searchProviders.setAll(enabledKeys)
    }

    val reorderableListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newList = enabledItems.toMutableList()
        val fromIndex = enabledItems.indexOfFirst { it.id == from.key }
        val toIndex = enabledItems.indexOfFirst { it.id == to.key }
        newList.move(fromIndex, toIndex)
        searchProviderRepository.updateProvidersOrder(newList)
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.search_provider),
        onBackAction = saveList,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        imageVector = Phosphor.Plus,
                        contentDescription = stringResource(id = R.string.add_search_provider)
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_search_provider))
                },
                onClick = {
                    scope.launch {
                        selectedProvider = searchProviderRepository.insertNew()
                        openDialog.value = true
                    }
                }
            )
        }
    ) { paddingValues ->
        /*LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp),
            state = lazyListState,
        ) {
            stickyHeader {
                Text(
                    text = stringResource(id = R.string.enabled_events),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(enabledItems, key = { _, it -> it.id }) { subIndex, item ->
                ReorderableItem(
                    reorderableListState,
                    key = item.id,
                ) { isDragging ->
                    val elevation by animateDpAsState(
                        if (isDragging) 16.dp else 0.dp,
                        label = "elevation",
                    )
                    val bgColor by animateColorAsState(
                        if (isDragging) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer,
                        label = "bgColor",
                    )

                    ListItemWithIcon(
                        modifier = Modifier
                            .longPressDraggableHandle()
                            .shadow(elevation)
                            .clip(GroupItemShape(subIndex, enabledItems.size - 1))
                            .combinedClickable(
                                onClick = {
                                    searchProviderRepository.disableProvider(item)
                                },
                                onLongClick = {
                                    selectedProvider = item.id
                                    openDialog.value = true
                                }
                            ),
                        containerColor = bgColor,
                        title = item.name,
                        startIcon = {
                            Image(
                                painter = painterResource(id = item.iconId),
                                contentDescription = item.name,
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        endCheckbox = {
                            IconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = {
                                    searchProviderRepository.disableProvider(item)
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                    )
                }
            }

            stickyHeader {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.tap_to_enable),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(disabledItems, key = { _, it -> it.id }) { subIndex, item ->
                ListItemWithIcon(
                    modifier = Modifier
                        .clip(GroupItemShape(subIndex, disabledItems.size - 1))
                        .combinedClickable(
                            onClick = {
                                searchProviderRepository.enableProvider(item)
                            },
                            onLongClick = {
                                selectedProvider = item.id
                                openDialog.value = true
                            }
                        ),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    title = item.name,
                    startIcon = {
                        Image(
                            painter = painterResource(id = item.iconId),
                            contentDescription = item.name,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    endCheckbox = {
                        Spacer(modifier = Modifier.height(32.dp))
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }*/
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            SearchProviderDialogUI(
                repositoryId = selectedProvider,
                openDialogCustom = openDialog,
                onDelete = { searchProviderRepository.delete(it) },
                onSave = { searchProviderRepository.update(it) }
            )
        }
    }

    DisposableEffect(key1 = null) {
        onDispose { saveList() }
    }
}
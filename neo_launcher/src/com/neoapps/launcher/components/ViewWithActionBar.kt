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
 */

package com.neoapps.launcher.components


import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWithActionBar(
    modifier: Modifier = Modifier,
    title: String,
    floatingActionButton: @Composable () -> Unit = {},
    showBackButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    onBackAction: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    if (showBackButton) {
                        val backDispatcher =
                            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                        IconButton(
                            onClick = {
                                onBackAction.invoke()
                                backDispatcher?.onBackPressed()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.gesture_press_back),
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    )
}
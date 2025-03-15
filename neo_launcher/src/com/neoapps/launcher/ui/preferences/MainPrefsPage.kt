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

package com.neoapps.launcher.ui.preferences

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.neoapps.launcher.components.OverflowMenu
import com.neoapps.launcher.components.ViewWithActionBar
import com.neoapps.launcher.components.plus
import com.neoapps.launcher.util.CoreUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPrefsPage() {
    val context = LocalContext.current


    fun resolveDefaultHome(): String? {
        val homeIntent: Intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
        val info: ResolveInfo? = context.packageManager
            .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (info?.activityInfo != null) {
            info.activityInfo.packageName
        } else {
            null
        }
    }
    ViewWithActionBar(
        title = stringResource(R.string.settings_button_text),
        showBackButton = false,
        actions = {
            OverflowMenu {
                if (BuildConfig.APPLICATION_ID != resolveDefaultHome()) {
                    DropdownMenuItem(
                        onClick = {
                            //TODO: Implement change default home
                            hideMenu()
                        },
                        text = { Text(text = stringResource(id = R.string.change_default_home)) }
                    )
                }
                DropdownMenuItem(
                    onClick = {
                        CoreUtils.killLauncher()
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.title__restart_launcher)) }
                )
                DropdownMenuItem(
                    onClick = {
                        //TODO: Implement developer options
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.developer_options_title)) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues + PaddingValues(8.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        }
    }
}

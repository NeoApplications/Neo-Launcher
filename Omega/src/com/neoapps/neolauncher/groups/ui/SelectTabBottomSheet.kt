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

package com.neoapps.neolauncher.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.Config

@Composable
fun SelectTabBottomSheet(
    onClose: (Int, AppGroupsManager.Category) -> Unit,
) {
    val prefs = NeoPrefs.getInstance()
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(id = R.string.default_tab_name),
                modifier = Modifier.fillMaxWidth(),
                color = Color(prefs.profileAccentColor.getColor()),
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            CategoryTabItem(
                titleId = R.string.tab_type_smart,
                summaryId = R.string.pref_appcategorization_flowerpot_summary,
                iconId = R.drawable.ic_category,
                onClick = {
                    onClose(Config.BS_CREATE_GROUP, AppGroupsManager.Category.FLOWERPOT)
                }
            )
        }
        item {
            CategoryTabItem(
                titleId = R.string.custom,
                summaryId = R.string.tab_type_custom_desc,
                iconId = R.drawable.ic_squares_four,
                onClick = {
                    onClose(Config.BS_CREATE_GROUP, AppGroupsManager.Category.TAB)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.launcher3.R
import com.neoapps.neolauncher.backup.BackupManager
import com.neoapps.neolauncher.compose.components.ComposeSwitchView
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.plus
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.neoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupRestorePage(){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentOperation by remember { mutableStateOf<BackupOperation?>(null) }

    var restoreSettings by rememberSaveable { mutableStateOf(true) }
    var restoreHomeIcons by rememberSaveable { mutableStateOf(true) }
    var restoreDatabases by rememberSaveable { mutableStateOf(true) }
    var restoreWallpaper by rememberSaveable { mutableStateOf(true) }

    var selectedRestoreUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRestoreContents by remember { mutableIntStateOf(0) }

    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            currentOperation = null
            return@rememberLauncherForActivityResult
        }

        selectedRestoreUri = uri.toString()
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                BackupManager(context, uri).restore(pendingRestoreContents)
            }
            currentOperation = null
            if (success) {
                Toast.makeText(context, ContextCompat.getString(context,R.string.restore_success), Toast.LENGTH_SHORT).show()
                context.neoApp.restart(false)
            } else {
                Toast.makeText(context, ContextCompat.getString(context,R.string.restore_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    ViewWithActionBar(
        title = stringResource(R.string.backup_restoring)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues + PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(heading = stringResource(id = R.string.restore_contents)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_settings),
                            onChange = { restoreSettings = it },
                            index = 0,
                            groupSize = 4,
                            isChecked = restoreSettings,
                            isEnabled = currentOperation == null
                        )

                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_homescreen),
                            onChange = { restoreHomeIcons = it },
                            index = 1,
                            groupSize = 4,
                            isChecked = restoreHomeIcons,
                            isEnabled = currentOperation == null
                        )

                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_databases),
                            onChange = { restoreDatabases = it },
                            index = 2,
                            groupSize = 4,
                            isChecked = restoreDatabases,
                            isEnabled = currentOperation == null
                        )
                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_wallpaper),
                            onChange = { restoreWallpaper = it },
                            index = 3,
                            groupSize = 4,
                            isChecked = restoreWallpaper,
                            isEnabled = currentOperation == null
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val contents = buildBackupContents(
                            settings = restoreSettings,
                            homeIcons = restoreHomeIcons,
                            databases = restoreDatabases,
                            wallpaper = restoreWallpaper,
                        )
                        if (contents == 0) {
                            Toast.makeText(
                                context,
                                ContextCompat.getString(context,R.string.backup_error_blank_contents),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        pendingRestoreContents = contents
                        currentOperation = BackupOperation.Restore
                        openBackupLauncher.launch(BackupManager.EXTRA_MIME_TYPES)
                    },
                    enabled = currentOperation == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentOperation) {
                            BackupOperation.Restore -> stringResource(id = R.string.backup_restoring)
                            else -> stringResource(id = R.string.restore_backup)
                        }
                    )
                }
            }
        }
    }
}

@Preview()
@Composable
fun BackupRestorePagePreview(){
    BackupRestorePage()
}

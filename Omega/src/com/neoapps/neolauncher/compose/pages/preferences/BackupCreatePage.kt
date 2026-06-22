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

import android.provider.Settings
import android.content.Intent
import android.os.Environment
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.plus
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.compose.components.preferences.PreferenceItem
import androidx.core.net.toUri
import com.neoapps.neolauncher.backup.BackupManager
import com.neoapps.neolauncher.compose.components.ComposeSwitchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupCreatePage(){
    val context = LocalContext.current
    val lifecycleOwner =LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var hasManageExternalStorage by remember { mutableStateOf(hasManageExternalStoragePermission()) }

    var currentOperation by remember { mutableStateOf<BackupOperation?>(null) }

    var backupName by rememberSaveable { mutableStateOf("") }
    var createSettings by rememberSaveable { mutableStateOf(true) }
    var createHomeIcons by rememberSaveable { mutableStateOf(true) }
    var createDatabases by rememberSaveable { mutableStateOf(true) }
    var createWallpaper by rememberSaveable { mutableStateOf(true) }
    var pendingCreateContents by remember { mutableIntStateOf(0) }
    var pendingCreateName by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasManageExternalStorage = hasManageExternalStoragePermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupManager.MIME_TYPE)
    ) { uri ->
        if (uri == null) {
            currentOperation = null
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val success = withContext(Dispatchers.IO) {
                BackupManager(context, uri).create(context, pendingCreateName, uri, pendingCreateContents)
            }
            currentOperation = null
            if (success) {
                Toast.makeText(context, ContextCompat.getString(context,R.string.backup_created), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, ContextCompat.getString(context,R.string.backup_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    ViewWithActionBar(
        title = stringResource(R.string.backup_creating)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues + PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hasManageExternalStorage) {
                item {
                    ManageExternalStoragePermissionSection(
                        enabled = currentOperation == null,
                        onGrantClick = {
                            context.startActivity(createManageStorageIntent(context.packageName))
                        }
                    )
                }
            }
            item {
                PreferenceGroup(heading = stringResource(id = R.string.title_create)) {
                    OutlinedTextField(
                        value = backupName,
                        onValueChange = { backupName = it },
                        enabled = currentOperation == null && hasManageExternalStorage,
                        label = { Text(text = stringResource(id = R.string.backup_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
            }
            item {
                PreferenceGroup(heading = stringResource(id = R.string.backup_contents)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_settings),
                            onChange = { createSettings = it },
                            index = 0,
                            groupSize = 4,
                            isChecked = createSettings,
                            isEnabled = currentOperation == null && hasManageExternalStorage,
                        )

                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_homescreen),
                            onChange = { createHomeIcons = it },
                            index = 1,
                            groupSize = 4,
                            isChecked = createHomeIcons,
                            isEnabled = currentOperation == null && hasManageExternalStorage,
                        )

                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_databases),
                            onChange = { createDatabases = it },
                            index = 2,
                            groupSize = 4,
                            isChecked = createDatabases,
                            isEnabled = currentOperation == null && hasManageExternalStorage,
                        )
                        ComposeSwitchView(
                            title = stringResource(id = R.string.backup_wallpaper),
                            onChange = { createWallpaper = it },
                            index = 3,
                            groupSize = 4,
                            isChecked = createWallpaper,
                            isEnabled = currentOperation == null && hasManageExternalStorage,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (!hasManageExternalStorage) {
                            context.startActivity(createManageStorageIntent(context.packageName))
                            return@Button
                        }

                        val name = backupName.trim()
                        if (name.isEmpty()) {
                            Toast.makeText(
                                context,
                                ContextCompat.getString(context,R.string.backup_error_blank_name),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val contents = buildBackupContents(
                            settings = createSettings,
                            homeIcons = createHomeIcons,
                            databases = createDatabases,
                            wallpaper = createWallpaper,
                        )
                        if (contents == 0) {
                            Toast.makeText(
                                context,
                                ContextCompat.getString(context,R.string.backup_error_blank_contents),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        pendingCreateName = name
                        pendingCreateContents = contents
                        currentOperation = BackupOperation.Create
                        createDocumentLauncher.launch(defaultBackupFileName(name))
                    },
                    enabled = currentOperation == null && hasManageExternalStorage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentOperation) {
                            BackupOperation.Create -> stringResource(id = R.string.backup_creating)
                            else -> stringResource(id = R.string.backup_create_new)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageExternalStoragePermissionSection(
    enabled: Boolean,
    onGrantClick: () -> Unit,
) {
    PreferenceGroup(heading = stringResource(id = R.string.title_storage_permission_required)) {
        PreferenceItem(
            title = stringResource(id = R.string.backup_manage_storage_permission_title),
            summary = stringResource(id = R.string.backup_manage_storage_permission_summary),
            index = 0,
            groupSize = 1,
            isEnabled = enabled,
            onClick = onGrantClick,
        )
        Button(
            onClick = onGrantClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.backup_grant_storage_permission))
        }
    }
}

private fun hasManageExternalStoragePermission(): Boolean {
    return  Environment.isExternalStorageManager()
}

private fun createManageStorageIntent(packageName: String): Intent {
    return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

private fun defaultBackupFileName(baseName: String): String {
    val safeName = baseName
        .trim()
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    return "${safeName.ifEmpty { "backup" }}-$timestamp.${BackupManager.EXTENSION}"
}

@Preview()
@Composable
fun BackupCreatePagePreview(){
    BackupCreatePage()
}

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

package com.saggitt.omega.icons

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.neoapps.neolauncher.util.DrawableUtils
import com.saggitt.omega.compose.components.ComposeSwitchView
import com.saggitt.omega.compose.components.preferences.PreferenceItem
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.groups.ui.AppTabDialog
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.util.addIfNotNull
import com.neoapps.neolauncher.util.getPackageVersion
import com.saggitt.omega.preferences.NeoPrefs
import kotlinx.coroutines.launch

@Composable
fun CustomizeIconPage(
    defaultTitle: String,
    componentKey: ComponentKey,
    appInfo: AppInfo,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    var title by remember { mutableStateOf("") }
    val request =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
            onClose()
        }

    val openEditIcon = {
        request.launch(
            PreferenceActivity.navigateIntent(context, "${Routes.EDIT_ICON}/$componentKey")
        )
    }

    DisposableEffect(key1 = null) {
        title = prefs.customAppName[componentKey] ?: defaultTitle
        onDispose {
            val previousTitle = prefs.customAppName[componentKey]
            val newTitle = if (title != defaultTitle) title else null
            if (newTitle != previousTitle) {
                prefs.customAppName[componentKey] = newTitle
                val model = LauncherAppState.getInstance(context).model
                model.onPackageChanged(componentKey.componentName.packageName, componentKey.user)
            }
        }
    }

    CustomizeIconView(
        title = title,
        onTitleChange = { title = it },
        defaultTitle = defaultTitle,
        componentKey = componentKey,
        appInfo = appInfo,
        launchSelectIcon = openEditIcon,
    )
}

fun getAppIcon(context: Context, appInfo: AppInfo): Drawable {
    val outObj = Array<Any?>(1) { null }
    var icon: Drawable =
        DrawableUtils.loadFullDrawableWithoutTheme(context, appInfo, 0, 0, outObj)!!
    if (appInfo.screenId != SystemShortcut.NO_ID && icon is BitmapInfo.Extender) {
        icon = icon.getThemedDrawable(context)
    }
    return icon
}

@Composable
fun CustomizeIconView(
    title: String,
    onTitleChange: (String) -> Unit,
    defaultTitle: String,
    componentKey: ComponentKey,
    appInfo: AppInfo,
    launchSelectIcon: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val repo = IconOverrideRepository.INSTANCE.get(context)
    val overrideItem by repo.observeTarget(componentKey).collectAsState(initial = null)
    val hasOverride = overrideItem != null
    var icon = getAppIcon(context, appInfo)
    val hiddenApps = prefs.drawerHiddenAppSet.get().collectAsState(initial = emptySet())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .clip(MaterialTheme.shapes.small)
                .addIfNotNull(launchSelectIcon) {
                    clickable(onClick = it)
                }
        ) {
            Image(
                painter = rememberDrawablePainter(icon),
                contentDescription = title,
                modifier = Modifier
                    .requiredSize(64.dp)
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                if (title != defaultTitle) {
                    IconButton(
                        onClick = { onTitleChange(defaultTitle) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.undo),
                            contentDescription = stringResource(id = R.string.accessibility_close)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            shape = MaterialTheme.shapes.large,
            label = { Text(text = stringResource(id = R.string.app_name)) },
            isError = title.isEmpty()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!componentKey.componentName.equals("com.saggitt.omega.folder")) {
                val stringKey = componentKey.toString()
                ComposeSwitchView(
                    title = stringResource(R.string.hide_app),
                    isChecked = hiddenApps.value.contains(stringKey),
                    index = 0,
                    groupSize = if (hasOverride || prefs.drawerTabs.isEnabled) 2 else 1,
                    onCheckedChange = { newValue ->
                        val newSet = hiddenApps.value.toMutableSet()
                        if (newValue) newSet.add(stringKey) else newSet.remove(stringKey)
                        scope.launch {
                            prefs.drawerHiddenAppSet.setValue(newSet)
                        }
                    }
                )

                if (hasOverride) {
                    PreferenceItem(
                        title = stringResource(R.string.reset_custom_icon),
                        index = 1,
                        groupSize = if (prefs.drawerTabs.isEnabled) 3 else 2,
                        modifier = Modifier.clickable {
                            scope.launch {
                                repo.deleteOverride(componentKey)
                                icon = getAppIcon(context, appInfo)
                            }
                        }
                    )
                }

                if (prefs.drawerTabs.isEnabled) {
                    val openDialogCustom = remember { mutableStateOf(false) }
                    PreferenceItem(
                        title = stringResource(R.string.app_categorization_tabs),
                        index = 2,
                        groupSize = 3,
                        modifier = Modifier.clickable {
                            openDialogCustom.value = true
                        }
                    )
                    if (openDialogCustom.value) {
                        AppTabDialog(
                            componentKey = componentKey,
                            openDialogCustom = openDialogCustom
                        )
                    }
                }
            }
        }
        if (prefs.showDebugInfo.getValue()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val component =
                    componentKey.componentName.packageName + "/" + componentKey.componentName.className
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = stringResource(id = R.string.debug_options_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
                PreferenceItem(
                    title = stringResource(id = R.string.debug_component_name),
                    index = 1,
                    groupSize = 4,
                    summary = component
                )
                PreferenceItem(
                    title = stringResource(id = R.string.app_version),
                    index = 2,
                    groupSize = 4,
                    summary = context.packageManager.getPackageVersion(componentKey.componentName.packageName)
                )
            }
        }
    }
}
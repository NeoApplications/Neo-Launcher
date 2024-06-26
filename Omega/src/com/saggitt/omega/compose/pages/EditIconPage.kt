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

package com.saggitt.omega.compose.pages

import SearchTextField
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.SearchBarUI
import com.saggitt.omega.compose.navigation.OnResult
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.navigation.resultSender
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.data.models.IconPickerItem
import com.saggitt.omega.iconpack.CustomIconPack
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.util.blockBorder
import com.saggitt.omega.util.getUserForProfileId
import com.saulhdev.neolauncher.icons.drawableToBitmap
import kotlinx.coroutines.launch

@Composable
fun EditIconPage(
    componentKey: ComponentKey,
) {
    val context = LocalContext.current
    val iconPacks = IconPackProvider.INSTANCE.get(context).getIconPackList()
    val isFolder = componentKey.componentName.packageName.contains("com.saulhdev.omega.folder")
    val launcherApps = context.getSystemService<LauncherApps>()!!
    val intent = Intent().setComponent(componentKey.componentName)
    val activity = launcherApps.resolveActivity(intent, componentKey.user)
    // TODO get the set icon
    val originalIcon: Drawable = activity.getIcon(context.resources.displayMetrics.densityDpi)
    val title = remember(componentKey) {
        activity.label.toString()
    }
    val onClickItem = resultSender<IconPickerItem>()

    val iconPackName: MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    val iconPack by remember(iconPackName) {
        derivedStateOf {
            iconPackName.value?.let { name ->
                IconPackProvider.INSTANCE.get(context).getIconPackOrSystem(name)
            }
        }
    }
    val showIconPack by remember(iconPack) { derivedStateOf { iconPack != null } }
    var searchQuery by remember { mutableStateOf("") }

    val pickerComponent = remember(iconPack) {
        val launcherApps = context.getSystemService<LauncherApps>()!!
        launcherApps
            .getActivityList(iconPack?.packPackageName, Process.myUserHandle())
            .firstOrNull()?.componentName
    }
    val pickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val icon = it.data?.getParcelableExtra<Intent.ShortcutIconResource>(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE
            ) ?: return@rememberLauncherForActivityResult
            val entry = (iconPack as CustomIconPack).createFromExternalPicker(icon)
                ?: return@rememberLauncherForActivityResult
            onClickItem(entry)
        }

    val scope = rememberCoroutineScope()
    val repo = IconOverrideRepository.INSTANCE.get(context)
    OnResult<IconPickerItem> { item ->
        scope.launch {
            repo.setOverride(componentKey, item)
            (context as Activity).let {
                it.setResult(Activity.RESULT_OK)
                it.finish()
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            val scrollState = rememberScrollState()

            if (showIconPack) {
                SearchBarUI(
                    searchInput = {
                        SearchTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = {
                                Text(
                                    text = iconPack?.label ?: title,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            singleLine = true
                        )
                    },
                    actions = {
                        if (pickerComponent != null) {
                            OverflowMenu {
                                DropdownMenuItem(
                                    onClick = {
                                        val intent = Intent("com.novalauncher.THEME")
                                            .addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER")
                                            .setComponent(pickerComponent)
                                        pickerLauncher.launch(intent)
                                        hideMenu()
                                    },
                                    text = { Text(text = stringResource(id = R.string.icon_pack_external_picker)) }
                                )
                            }
                        }
                    },
                    onBack = {
                        iconPackName.value = null
                    }
                )
            } else { // TODO wrap in own components
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TOPBAR_PADDING)
                        .height(TOPBAR_HEIGHT)
                        .horizontalScroll(scrollState)
                ) {
                    //Original Icon
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = MaterialTheme.shapes.medium
                            )
                            .requiredSize(60.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = originalIcon.toBitmap(128, 128).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.requiredSize(48.dp)
                        )
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    //Package Icons
                    val iconDpi = LocalContext.current.resources.configuration.densityDpi
                    val ip = IconPackProvider.INSTANCE.get(LocalContext.current)

                    if (isFolder) { // TODO
                        iconPacks.forEach() {
                            val pack: IconPack? = ip.getIconPack(it.packageName)
                            if (pack != null) {
                                pack.loadBlocking()
                                val iconEntry = pack.getIcon(componentKey.componentName)
                            }
                        }
                    } else {
                        iconPacks.forEach {
                            val pack: IconPack? = ip.getIconPackOrSystem(it.packageName)
                            if (pack != null) {
                                pack.loadBlocking()
                                val iconEntry = pack.getIcon(componentKey.componentName)
                                if (iconEntry != null) {
                                    val mIcon: Drawable? = ip.getDrawable(
                                        iconEntry,
                                        iconDpi,
                                        componentKey.user
                                    )
                                    if (mIcon != null) {
                                        Image(
                                            bitmap = drawableToBitmap(mIcon).asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .requiredSize(60.dp)
                                                .padding(start = 8.dp, end = 8.dp)
                                                .clickable {
                                                    val iconPickerItem = IconPickerItem(
                                                        pack.packPackageName,
                                                        iconEntry.name,
                                                        iconEntry.name,
                                                        iconEntry.type
                                                    )
                                                    scope.launch {
                                                        repo.setOverride(
                                                            componentKey,
                                                            iconPickerItem
                                                        )
                                                        (context as Activity).finish()
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //Icon Packs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .blockBorder()
            ) {
                if (showIconPack) {
                    if (iconPack == null) {
                        SideEffect {
                            iconPackName.value = null
                        }
                    }

                    iconPack?.let {
                        IconListPage(iconPackName, it, searchQuery, onClickItem)
                    }
                } else LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(iconPacks) { _, iconPack ->
                        if (iconPack.packageName != context.getString(R.string.icon_packs_intent_name)) {
                            ListItemWithIcon(
                                modifier = Modifier
                                    .clickable {
                                        iconPackName.value = iconPack.packageName
                                    },
                                title = iconPack.name,
                                startIcon = {
                                    Image(
                                        bitmap = drawableToBitmap(iconPack.icon).asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(44.dp)
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.editIconGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{packageName}/{nameAndUser}"),
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("nameAndUser") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val packageName = args.getString("packageName") ?: ""
            val nameAndUser = (args.getString("nameAndUser") ?: "#0").split("#")
            val user = UserCache.INSTANCE.get(LocalContext.current)
                .getUserForProfileId(nameAndUser[1].toInt())
            val key = ComponentKey(ComponentName(packageName, nameAndUser[0]), user)
            EditIconPage(key)
        }
    }
}

val TOPBAR_HEIGHT = 60.dp
val TOPBAR_PADDING = 8.dp
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
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.compose.components.ElevatedIcon
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.SearchBarUI
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.data.models.IconPickerItem
import com.saggitt.omega.iconpack.CustomIconPack
import com.saggitt.omega.iconpack.IconPackInfo
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.util.blockBorder
import com.saulhdev.neolauncher.icons.drawableToBitmap
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EditIconPage(
    componentKey: ComponentKey,
) {
    val context = LocalContext.current
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()
    val repo = IconOverrideRepository.INSTANCE.get(context)

    val ipp = IconPackProvider.INSTANCE.get(LocalContext.current)
    val iconPacks = ipp.getIconPackList()
    val launcherApps = context.getSystemService<LauncherApps>()!!
    val intent = Intent().setComponent(componentKey.componentName)
    val activity = launcherApps.resolveActivity(intent, componentKey.user)

    // TODO get the set icon
    val originalIcon: Drawable = activity.getIcon(context.resources.displayMetrics.densityDpi)
    val title = remember(componentKey) {
        activity.label.toString()
    }
    var searchQuery by remember { mutableStateOf("") }
    val iconPackName: MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    val iconPack by remember(iconPackName) {
        derivedStateOf {
            iconPackName.value?.let { name -> ipp.getIconPackOrSystem(name) }
        }
    }
    val showIconPack by remember(iconPack) { derivedStateOf { iconPack != null } }

    val pickerComponent = remember(iconPack) {
        context.getSystemService<LauncherApps>()
            ?.getActivityList(iconPack?.packPackageName, Process.myUserHandle())
            ?.firstOrNull()?.componentName
    }
    val onItemCLick: (IconPickerItem) -> Unit = { iconPickerItem: IconPickerItem ->
        scope.launch {
            repo.setOverride(
                componentKey,
                iconPickerItem
            )
            (context as Activity).finish()
        }
    }
    val pickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val icon = it.data?.getParcelableExtra<Intent.ShortcutIconResource>(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE
            ) ?: return@rememberLauncherForActivityResult
            val entry = (iconPack as CustomIconPack).createFromExternalPicker(icon)
                ?: return@rememberLauncherForActivityResult
            onItemCLick(entry)
        }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
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

            //Icon Packs
            NavigableListDetailPaneScaffold(
                navigator = paneNavigator,
                listPane = {
                    Column {
                        AppPacksIconsBar(
                            originalIcon = originalIcon,
                            iconPacks = iconPacks,
                            ipp = ipp,
                            componentKey = componentKey,
                            onItemCLick = onItemCLick,
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .blockBorder(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            itemsIndexed(iconPacks) { _, iconPack ->
                                if (iconPack.packageName != context.getString(R.string.icon_packs_intent_name)) {
                                    ListItemWithIcon(
                                        modifier = Modifier
                                            .clickable {
                                                paneNavigator.navigateTo(
                                                    ListDetailPaneScaffoldRole.Detail,
                                                    iconPack.packageName
                                                )
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
                },
                detailPane = {
                    iconPackName.value = paneNavigator.currentDestination
                        ?.takeIf { it.pane == this.role }?.content.toString()

                    if (showIconPack) {
                        LaunchedEffect(iconPack) {
                            if (iconPack == null) iconPackName.value = null
                        }
                        iconPack?.let { iconPack ->
                            AnimatedPane {
                                Column {
                                    SearchBarUI(
                                        searchInput = {
                                            SearchTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                modifier = Modifier.fillMaxSize(),
                                                placeholder = {
                                                    Text(
                                                        text = iconPack.label.ifEmpty { title },
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
                                                            Intent("com.novalauncher.THEME")
                                                                .addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER")
                                                                .setComponent(pickerComponent).let {
                                                                    pickerLauncher.launch(it)
                                                                }
                                                            hideMenu()
                                                        },
                                                        text = { Text(text = stringResource(id = R.string.icon_pack_external_picker)) }
                                                    )
                                                }
                                            }
                                        },
                                        onBack = {
                                            paneNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
                                        }
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .blockBorder(),
                                    ) {
                                        IconListPage(
                                            iconPack = iconPack,
                                            searchQuery = searchQuery,
                                            onClickItem = onItemCLick,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AppPacksIconsBar(
    originalIcon: Drawable,
    iconPacks: List<IconPackInfo>,
    ipp: IconPackProvider,
    componentKey: ComponentKey,
    onItemCLick: (IconPickerItem) -> Unit
) {
    val scrollState = rememberScrollState()
    val isFolder = componentKey.componentName.packageName.contains("com.saulhdev.omega.folder")
    val iconDpi = LocalConfiguration.current.densityDpi

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(TOPBAR_PADDING)
            .height(TOPBAR_HEIGHT)
            .horizontalScroll(scrollState)
    ) {
        ElevatedIcon(originalIcon)

        VerticalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        if (isFolder) { // TODO implement folder icons support
            iconPacks.forEach {
                ipp.getIconPack(it.packageName)?.let { iconPack ->
                    iconPack.loadBlocking()
                    val iconEntry = iconPack.getIcon(componentKey.componentName)
                }
            }
        } else {
            iconPacks.forEach {
                ipp.getIconPackOrSystem(it.packageName)?.let { iconPack ->
                    iconPack.loadBlocking()
                    val iconEntry = iconPack.getIcon(componentKey.componentName)
                    if (iconEntry != null) {
                        val mIcon: Drawable? = ipp.getDrawable(
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
                                            iconPack.packPackageName,
                                            iconEntry.name,
                                            iconEntry.name,
                                            iconEntry.type
                                        )
                                        onItemCLick(iconPickerItem)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

val TOPBAR_HEIGHT = 60.dp
val TOPBAR_PADDING = 8.dp
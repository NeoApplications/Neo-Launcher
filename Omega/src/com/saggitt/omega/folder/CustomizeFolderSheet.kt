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

package com.saggitt.omega.folder


@Composable
fun CustomizeFolderSheet(
    launcher: Launcher,
    folder: FolderInfo,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    val infoProvider: CustomInfoProvider<ItemInfo>? =
        CustomInfoProvider.forItem(context, folder)

    var title by remember { mutableStateOf("") }
    val defaultTitle by remember { mutableStateOf("") }

    DisposableEffect(key1 = null) {
        title = folder.title?.toString() ?: defaultTitle
        onDispose {
            val previousTitle = infoProvider!!.getCustomTitle(folder)
            val newTitle = if (title != defaultTitle) title else null
            if (newTitle != previousTitle) {
                folder.setTitle(newTitle)
            }
            val model = LauncherAppState.getInstance(context).model
            model.onPackageChanged(
                folder.toComponentKey().componentName.toString(),
                folder.toComponentKey().user
            )
        }
    }

    CustomizeFolderView(
        launcher = launcher,
        folder = folder,
        title = title,
        onTitleChange = { title = it },
        defaultTitle = defaultTitle,
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomizeFolderView(
    launcher: Launcher,
    folder: FolderInfo,
    title: String,
    onTitleChange: (String) -> Unit,
    defaultTitle: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coverMode = remember { mutableStateOf(folder.isCoverMode) }
    val swipeUpHandler = createGestureHandler(
        launcher, folder.swipeUpAction, BlankGestureHandler(launcher, null)
    )
    val handlerName = remember { mutableStateOf(swipeUpHandler.displayName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .clip(MaterialTheme.shapes.small)
        ) {
            Image(
                painter = rememberDrawablePainter(folder.getIcon(launcher)),
                contentDescription = title,
                modifier = Modifier
                    .requiredSize(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                textColor = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            shape = MaterialTheme.shapes.large,
            label = { Text(text = stringResource(id = R.string.folder_name)) },
            isError = title.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ComposeSwitchView(
            title = stringResource(R.string.folder_cover_mode),
            summary = stringResource(R.string.folder_cover_mode_desc),
            isChecked = folder.isCoverMode,
            onCheckedChange = { newValue ->
                folder.setCoverMode(newValue, launcher.modelWriter)
                coverMode.value = newValue
            }
        )

        val openDialogCustom = remember { mutableStateOf(false) }
        PreferenceItem(
            title = stringResource(R.string.gesture_swipe_up),
            summary = handlerName.value,
            modifier = Modifier.clickable {
                openDialogCustom.value = true
            },
            enabled = !coverMode.value
        )
        if (openDialogCustom.value) {
            FolderListDialog(
                folder = folder,
                openDialogCustom = openDialogCustom
            )
        }
    }
}
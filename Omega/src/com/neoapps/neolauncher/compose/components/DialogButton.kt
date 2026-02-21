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

package com.neoapps.neolauncher.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DialogPositiveButton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    textId: Int = android.R.string.ok,
    onClick: () -> Unit = {},
) {
    TextButton(
        shape = RoundedCornerShape(cornerRadius),
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = stringResource(id = textId),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
        )
    }
}

@Composable
fun DialogNegativeButton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    textId: Int = android.R.string.cancel,
    onClick: () -> Unit = {},
) {
    TextButton(
        shape = RoundedCornerShape(cornerRadius),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = textId),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp)
        )
    }
}

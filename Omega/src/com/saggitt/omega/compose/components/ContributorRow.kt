/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.compose.components

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.saggitt.omega.compose.components.preferences.BasePreference

@Composable
fun ContributorRow(
    @StringRes nameId: Int,
    @StringRes roleId: Int,
    photoUrl: String,
    url: String,
    index: Int = 0,
    groupSize: Int = 1,
) {
    val context = LocalContext.current

    BasePreference(
        titleId = nameId,
        onClick = {
            val webpage = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        },
        summaryId = roleId,
        index = index,
        groupSize = groupSize,
        startWidget = {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = photoUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                        }).build()
                ),
                contentDescription = stringResource(id = nameId),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))
            )
        }
    )
}

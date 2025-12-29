/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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
package com.neoapps.neolauncher.dash.actionprovider

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.SpeakerHigh
import com.neoapps.neolauncher.dash.DashActionProvider
import java.util.Objects

class ManageVolume(context: Context) : DashActionProvider(context) {
    override val itemId = 8
    override val name = context.getString(R.string.dash_volume_title)
    override val description = context.getString(R.string.dash_volume_summary)
    override val icon = Phosphor.SpeakerHigh

    override fun runAction(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            Objects.requireNonNull(audioManager).setStreamVolume(
                AudioManager.STREAM_RING, audioManager.getStreamVolume(
                    AudioManager.STREAM_RING
                ), AudioManager.FLAG_SHOW_UI
            )
        } catch (e: Exception) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!Objects.requireNonNull(mNotificationManager).isNotificationPolicyAccessGranted) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        }
    }
}
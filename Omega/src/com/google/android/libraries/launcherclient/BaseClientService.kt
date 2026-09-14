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

package com.google.android.libraries.launcherclient

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.neoapps.neolauncher.preferences.NeoPrefs

open class BaseClientService(val context: Context, flags: Int) : ServiceConnection {
    private var isConnected = false
    private val mFlags = flags
    val prefs = NeoPrefs.getInstance()
    private var isEnabled = false
    fun connect(): Boolean {
        if (!isConnected) {
            try {
                isEnabled = prefs.feedProvider.getValue()
                if (isEnabled) {
                    isConnected = context.bindService(
                        LauncherClient.getIntent(context, false),
                        this,
                        mFlags
                    )
                }
            } catch (e: Throwable) {
                Log.e("LauncherClient", "Unable to connect to overlay service", e)
            }
        }
        return isConnected
    }

    fun disconnect() {
        if (isConnected) {
            context.unbindService(this)
            isConnected = false
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}

    override fun onServiceDisconnected(name: ComponentName?) {}
}
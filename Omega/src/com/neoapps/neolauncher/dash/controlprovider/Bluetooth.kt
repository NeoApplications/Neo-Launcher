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
package com.neoapps.neolauncher.dash.controlprovider

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Bluetooth
import com.neoapps.neolauncher.dash.DashControlProvider
import com.neoapps.neolauncher.util.minSDK

class Bluetooth(context: Context) : DashControlProvider(context) {
    private val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    override val itemId = 13
    override val name = context.getString(R.string.dash_bluetooth)
    override val description = context.getString(R.string.dash_bluetooth_summary)
    override val extendable = minSDK(Build.VERSION_CODES.TIRAMISU)
    override val icon = Phosphor.Bluetooth

    override var state: Boolean
        get() = bm.adapter?.isEnabled == true
        set(value) {
            if (extendable) context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            else if (value) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (minSDK(Build.VERSION_CODES.S)) {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            34
                        )
                    }
                    return
                }
                bm.adapter?.enable()
            } else {
                bm.adapter?.disable()
            }
        }
}
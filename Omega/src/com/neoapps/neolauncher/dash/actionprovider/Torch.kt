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

import android.content.Context
import android.hardware.camera2.CameraManager
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.Flashlight
import com.neoapps.neolauncher.dash.DashActionProvider
import com.neoapps.neolauncher.util.prefs

class Torch(context: Context) : DashActionProvider(context) { // TODO convert to DashControlProvider
    override val itemId = 11
    override val name = context.getString(R.string.dash_torch)
    override val description = context.getString(R.string.dash_torch_summary)
    override val icon = Phosphor.Flashlight

    override fun runAction(context: Context) {
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId = camManager!!.cameraIdList[0]
        context.prefs.dashTorchState.let {
            camManager.setTorchMode(cameraId, !it.getValue())
        }
    }
}
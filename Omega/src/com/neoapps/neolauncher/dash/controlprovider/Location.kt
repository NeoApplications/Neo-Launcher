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

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.MapPin
import com.neoapps.neolauncher.dash.DashControlProvider
import com.neoapps.neolauncher.util.minSDK

class Location(context: Context) : DashControlProvider(context) {
    override val itemId = 14
    override val name = context.getString(R.string.dash_location)
    override val description = context.getString(R.string.dash_location_summary)
    override val extendable = true
    override val icon = Phosphor.MapPin

    var locationManager =
        context.getSystemService(LOCATION_SERVICE) as LocationManager

    override var state: Boolean
        get() = if (minSDK(Build.VERSION_CODES.P)) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
        set(value) {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
}
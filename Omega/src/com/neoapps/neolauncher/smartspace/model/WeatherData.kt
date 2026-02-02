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

package com.neoapps.neolauncher.smartspace.model

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import com.neoapps.neolauncher.smartspace.Temperature

data class WeatherData(
    val icon: Bitmap,
    private val temperature: Temperature,
    val forecastUrl: String? = "https://www.google.com/search?q=weather",
    val forecastIntent: Intent? = null,
    val pendingIntent: PendingIntent? = null
) {

    fun getTitle(unit: Temperature.Unit = temperature.unit): String {
        return "${temperature.inUnit(unit)}${unit.suffix}"
    }
}
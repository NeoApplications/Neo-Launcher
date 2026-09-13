/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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
package com.neoapps.neolauncher.smartspace

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.launcher3.R

object WeatherCode {

    @StringRes
    fun getDescriptionRes(code: Int): Int {
        return when (code) {
            0 -> R.string.weather_code_clear
            1 -> R.string.weather_code_mainly_clear
            2 -> R.string.weather_code_partly_cloudy
            3 -> R.string.weather_code_overcast
            45, 48 -> R.string.weather_code_fog
            51, 53, 55, 56, 57 -> R.string.weather_code_drizzle
            61, 63, 66, 67, 80, 81 -> R.string.weather_code_rain
            65, 82 -> R.string.weather_code_heavy_rain
            71, 73, 75, 77, 85, 86 -> R.string.weather_code_snow
            95, 96, 99 -> R.string.weather_code_thunderstorm
            else -> R.string.weather_code_partly_cloudy
        }
    }

    @DrawableRes
    fun getIconRes(code: Int, isDay: Boolean = true): Int {
        if (isDay) {
            return when (code) {
                0 -> R.drawable.weather_01
                1 -> R.drawable.weather_02
                2 -> R.drawable.weather_03
                3 -> R.drawable.weather_04
                45, 48 -> R.drawable.weather_50
                51, 53, 55, 56, 57 -> R.drawable.weather_13
                61, 63, 66, 67, 80, 81 -> R.drawable.weather_09
                65, 82 -> R.drawable.weather_08
                71, 73, 75, 77, 85, 86 -> R.drawable.weather_13
                95, 96, 99 -> R.drawable.weather_11
                else -> R.drawable.weather_03
            }
        } else {
            return when (code) {
                0 -> R.drawable.weather_01n
                1 -> R.drawable.weather_02n
                2 -> R.drawable.weather_03n
                3 -> R.drawable.weather_04n
                45, 48 -> R.drawable.weather_50
                51, 53, 55, 56, 57 -> R.drawable.weather_13n
                61, 63, 66, 67, 80, 81 -> R.drawable.weather_09
                65, 82 -> R.drawable.weather_08
                71, 73, 75, 77, 85, 86 -> R.drawable.weather_13n
                95, 96, 99 -> R.drawable.weather_11
                else -> R.drawable.weather_03n
            }
        }
    }
}

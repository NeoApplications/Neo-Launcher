/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   NeoApplications Team
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

package com.neoapps.neolauncher.data.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Long,
    val humidity: Double,
    val windSpeed: Double,
    val precipitation: Double,
    val isDay: Boolean,
    val maxTemp: Double,
    val minTemp: Double,
    val unit: String = "°C",
    val hourly: List<HourlyWeather> = emptyList(),
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

@Serializable
data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean = true
)

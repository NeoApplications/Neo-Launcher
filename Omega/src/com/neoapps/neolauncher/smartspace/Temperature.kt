/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.smartspace

import kotlin.math.roundToInt

class Temperature(val value: Int, val unit: Unit) {

    fun inUnit(other: Unit): Int {
        if (other == unit) return value
        return ((value.toFloat() - unit.freezingPoint) / unit.range * other.range + other.freezingPoint).roundToInt()
    }

    enum class Unit(val freezingPoint: Float, boilingPoint: Float, val suffix: String) {
        Celsius(0f, 100f, "°C"),
        Fahrenheit(32f, 212f, "°F"),
        Kelvin(273f, 373f, "K"),
        Rakine(491.67f, 671.64102f, "°R"),
        Delisle(-100f, 50f, "°D"),
        Newton(0f, 33f, "°N"),
        Reaumur(0f, 80f, "°Ré"),
        Romer(7.5f, 60f, "°Rø");

        val range = boilingPoint - freezingPoint

        override fun toString(): String {
            return when (this) {
                Celsius    -> "metric"
                Fahrenheit -> "imperial"
                Kelvin -> "kelvin"
                Rakine -> "rakine"
                Delisle -> "delisle"
                Newton -> "newton"
                Reaumur -> "reaumur"
                Romer -> "romer"
            }
        }
    }

    companion object {
        fun unitFromString(unit: String): Unit {
            return when (unit) {
                "", "metric" -> Unit.Celsius
                "imperial" -> Unit.Fahrenheit
                "kelvin" -> Unit.Kelvin
                "rakine" -> Unit.Rakine
                "delisle" -> Unit.Delisle
                "newton" -> Unit.Newton
                "reaumur" -> Unit.Reaumur
                "romer" -> Unit.Romer
                else -> throw IllegalArgumentException("unknown unit $unit")
            }
        }
    }
}
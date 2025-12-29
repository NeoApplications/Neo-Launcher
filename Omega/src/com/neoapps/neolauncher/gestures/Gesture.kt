/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.gestures

import com.neoapps.neolauncher.preferences.GesturePref
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

open class Gesture(val controller: GestureController, private val handlerPref: GesturePref) {

    val isEnabled: Boolean = true

    val handler = handlerPref.get()
        .map { controller.createGestureHandler(it) }
        .stateIn(
            controller.scope,
            SharingStarted.Eagerly,
            controller.blankGestureHandler
        )
    val custom get() = handlerPref.getValue() != handlerPref.defaultValue
}
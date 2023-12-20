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

package android.window

import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi

interface OnBackAnimationCallback {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun onBackStarted(backEvent: BackEvent) {
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun onBackProgressed(backEvent: BackEvent) {
    }

    fun onBackProgressed(@FloatRange(from = 0.0, to = 1.0) backProgress: Float) {}

    fun onBackCancelled() {}

    fun onBackInvoked()
}
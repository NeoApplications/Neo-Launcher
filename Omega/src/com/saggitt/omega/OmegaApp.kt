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
package com.saggitt.omega

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import org.chickenhook.restrictionbypass.Unseal

class OmegaApp : Application() {
    private val TAG = "OmegaApp"

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Unseal.unseal()
                Log.i(TAG, "Unseal success!")
            } catch (e: Exception) {
                Log.e(TAG, "Unseal fail!")
                e.printStackTrace()
            }
        }
    }
}
/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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
 */

package com.neoapps.launcher.util

import android.annotation.ChecksSdkIntAtLeast
import android.os.Build
import android.os.Build.VERSION_CODES;
import kotlin.system.exitProcess

class CoreUtils {
    companion object {

        @ChecksSdkIntAtLeast(api = VERSION_CODES.O_MR1)
        val AT_LEAST_OREO_MR1: Boolean = Build.VERSION.SDK_INT >= VERSION_CODES.O_MR1

        @ChecksSdkIntAtLeast(api = VERSION_CODES.P)
        val AT_LEAST_P = Build.VERSION.SDK_INT >= VERSION_CODES.P

        @ChecksSdkIntAtLeast(api = VERSION_CODES.S)
        val AT_LEAST_S: Boolean = Build.VERSION.SDK_INT >= VERSION_CODES.S

        fun killLauncher() {
            exitProcess(0)
        }
    }
}
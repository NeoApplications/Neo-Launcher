/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.util

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R
import com.saggitt.omega.preferences.NeoPrefs

fun AlertDialog.applyAccent() {
    val color = NeoPrefs.getInstance().profileAccentColor.getColor()
    val buttons = listOf(
        getButton(DialogInterface.BUTTON_NEGATIVE),
        getButton(DialogInterface.BUTTON_NEUTRAL),
        getButton(DialogInterface.BUTTON_POSITIVE)
    )
    buttons.forEach {
        it?.setTextColor(color)
    }
}

val Configuration.usingNightMode get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
val Int.luminance get() = ColorUtils.calculateLuminance(this)

val Int.isDark get() = luminance < 0.5f

fun getWindowCornerRadius(context: Context): Float {
    val prefs = NeoPrefs.getInstance()
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        return prefs.profileWindowCornerRadius.getValue()
    }
    return context.resources.getDimension(R.dimen.enforced_rounded_corner_max_radius)
}
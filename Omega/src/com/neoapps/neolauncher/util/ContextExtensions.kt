/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.neoapps.neolauncher.util

import android.R
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.ColorInt
import com.android.launcher3.Launcher
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.preferences.NeoPrefs
import java.util.Locale

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun Context.getLauncherOrNull(): Launcher? {
    return try {
        Launcher.getLauncher(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun Context.getIcon(): Drawable = packageManager.getApplicationIcon(applicationInfo)

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
    return { it -> creator(it.applicationContext) }
}

val Context.locale: Locale
    get() = this.resources.configuration.locales[0]

val Context.prefs: NeoPrefs
    get() = NeoPrefs.getInstance()


@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

@Suppress("DEPRECATION")
fun Context.getSystemAccent(darkTheme: Boolean): Int {
    val res = resources
    return if (Utilities.ATLEAST_S) {
        val colorName = if (darkTheme) "system_accent1_100" else "system_accent1_600"
        val colorId = res.getIdentifier(colorName, "color", "android")
        res.getColor(colorId)
    } else {
        var propertyValue = Utilities.getSystemProperty("persist.sys.theme.accentcolor", "")
        if (!TextUtils.isEmpty(propertyValue)) {
            if (!propertyValue.startsWith('#')) {
                propertyValue = "#$propertyValue"
            }
            try {
                return Color.parseColor(propertyValue)
            } catch (e: IllegalArgumentException) {
                // do nothing
            }
        }

        val typedValue = TypedValue()
        val theme =
            if (darkTheme) R.style.Theme_DeviceDefault else R.style.Theme_DeviceDefault_Light
        val contextWrapper = ContextThemeWrapper(this, theme)
        contextWrapper.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        typedValue.data
    }
}
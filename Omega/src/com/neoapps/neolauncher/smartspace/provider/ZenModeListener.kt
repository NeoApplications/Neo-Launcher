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

package com.neoapps.neolauncher.smartspace.provider

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import java.io.Closeable

class ZenModeListener(
    private val contentResolver: ContentResolver,
    private val listener: (Boolean) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())), Closeable {

    fun startListening() {
        contentResolver.registerContentObserver(Settings.Global.getUriFor(ZEN_MODE), false, this)
        notifyZenModeChange()
    }

    fun stopListening() {
        contentResolver.unregisterContentObserver(this)
    }

    private fun notifyZenModeChange() {
        try {
            val zenModeEnabled = Settings.Global.getInt(contentResolver, ZEN_MODE) != 0
            listener(zenModeEnabled)
        } catch (e: Settings.SettingNotFoundException) {
            listener(false)
        }
    }

    override fun close() {
        stopListening()
    }

    companion object {
        private const val ZEN_MODE = "zen_mode"
    }
}

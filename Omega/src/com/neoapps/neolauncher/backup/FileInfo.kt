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

package com.neoapps.neolauncher.backup

import android.graphics.Bitmap
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

data class FileInfo(val name: String, val contents: Int, val timestamp: String) {

    val timestampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
    val localizedTimestamp: String? = SimpleDateFormat
        .getDateTimeInstance()
        .format(timestampFormat.parse(timestamp)!!)
    var preview: Pair<Bitmap?, Bitmap?>? = null
    override fun toString(): String {
        val arr = JSONArray()
        arr.put(VERSION)
        arr.put(name)
        arr.put(contents)
        arr.put(timestamp)
        return arr.toString()
    }

    fun recycle() {
        preview?.first?.recycle()
        preview?.second?.recycle()
    }

    companion object {

        const val VERSION = 1
        const val FILE_NAME = "lcbkp"

        private const val NAME_INDEX = 1
        private const val CONTENTS_INDEX = 2
        private const val TIMESTAMP_INDEX = 3

        fun fromString(string: String): FileInfo {
            val arr = JSONArray(string)
            return FileInfo(
                name = arr.getString(NAME_INDEX),
                contents = arr.getInt(CONTENTS_INDEX),
                timestamp = arr.getString(TIMESTAMP_INDEX)
            )
        }
    }
}
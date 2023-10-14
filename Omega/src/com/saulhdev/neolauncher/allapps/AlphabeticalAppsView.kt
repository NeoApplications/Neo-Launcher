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
package com.saulhdev.neolauncher.allapps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.android.launcher3.model.data.AppInfo
import com.saggitt.omega.preferences.NeoPrefs


class AlphabeticalAppsView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {
    var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    var sectionText: TextView? = null
    var prefs = NeoPrefs.getInstance(context)

    fun setData(list: List<AppInfo>, isVisible: Boolean, sectionTitle: String) {
        setSectionTitle(isVisible, sectionTitle)
    }

    private fun setSectionTitle(isVisible: Boolean, sectionTitle: String) {
        sectionText?.setTextColor(prefs.profileAccentColor.getColor())
        sectionText?.text = sectionTitle
        sectionText?.visibility = if (isVisible) VISIBLE else INVISIBLE
    }
}
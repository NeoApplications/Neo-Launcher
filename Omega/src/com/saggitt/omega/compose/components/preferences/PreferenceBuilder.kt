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

package com.saggitt.omega.compose.components.preferences

import androidx.compose.runtime.Composable
import com.saggitt.omega.compose.objects.PageItem
import com.saggitt.omega.preferences.BooleanPref
import com.saggitt.omega.preferences.ColorIntPref
import com.saggitt.omega.preferences.FloatPref
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.preferences.IntentLauncherPref
import com.saggitt.omega.preferences.NavigationPref
import com.saggitt.omega.preferences.StringMultiSelectionPref
import com.saggitt.omega.preferences.StringPref
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.preferences.StringSetPref
import com.saggitt.omega.preferences.StringTextPref

val PreferenceBuilder =
    @Composable { pref: Any, onDialogPref: (Any) -> Unit, index: Int, size: Int ->
        when (pref) {
            is IntentLauncherPref -> IntentLauncherPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            /*
            is GridSize2D -> GridSize2DPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            is GridSize -> GridSizePreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }

            */
            is BooleanPref -> SwitchPreference(pref = pref, index = index, groupSize = size)

            is NavigationPref ->
                NavigationPreference(pref = pref, index = index, groupSize = size)

            is ColorIntPref ->
                ColorIntPreference(pref = pref, index = index, groupSize = size)

            is StringPref ->
                StringPreference(pref = pref, index = index, groupSize = size)

            is StringSetPref ->
                StringSetPreference(pref = pref, index = index, groupSize = size)

            is FloatPref ->
                SeekBarPreference(pref = pref, index = index, groupSize = size)
            /*
            is IdpIntPref ->
                IntSeekBarPreference(pref = pref, index = index, groupSize = size)

            is DialogPref ->
                AlertDialogPreference(pref = pref, index = index, groupSize = size) {
                    onDialogPref(
                        pref
                    )
                }
                */
            is IntSelectionPref ->
                IntSelectionPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }

            is StringSelectionPref ->
                StringSelectionPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }

            is StringTextPref ->
                StringTextPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }

            is StringMultiSelectionPref -> StringMultiSelectionPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }

            is PageItem ->
                PagePreference(
                    titleId = pref.titleId,
                    iconId = pref.iconId,
                    route = pref.route,
                    index = index,
                    groupSize = size
                )
        }
    }

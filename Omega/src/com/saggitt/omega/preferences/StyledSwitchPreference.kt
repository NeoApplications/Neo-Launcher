/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyColor

open class StyledSwitchPreference(context: Context, attrs: AttributeSet?) : SwitchPreference(context, attrs) {

    private var checkableView: View? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkableView = holder?.findViewById(android.R.id.switch_widget)
        (checkableView as Switch).applyColor(Utilities.getOmegaPrefs(context).accentColor)
    }
}

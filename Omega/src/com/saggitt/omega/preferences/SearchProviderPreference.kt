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
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.saggitt.omega.search.SearchProviderController

class SearchProviderPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs),
        SharedPreferences.OnSharedPreferenceChangeListener {

    var value = ""
    var defaultValue = ""

    override fun onAttached() {
        super.onAttached()

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetached() {
        super.onDetached()

        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        if (key == this.key) {
            value = getPersistedString(defaultValue)
            notifyChanged()
        }
    }

    override fun getSummary() = SearchProviderController.INSTANCE.get(context).searchProvider.providerName

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) {
            getPersistedString(defaultValue as String?) ?: ""
        } else {
            defaultValue as String? ?: ""
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        defaultValue = a.getString(index)!!
        return defaultValue
    }

    override fun getDialogLayoutResource() = R.layout.dialog_preference_recyclerview
}
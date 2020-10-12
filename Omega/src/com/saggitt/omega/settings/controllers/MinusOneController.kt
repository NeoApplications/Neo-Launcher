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

package com.saggitt.omega.settings.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.preferences.PreferenceController
import com.saggitt.omega.smartspace.FeedBridge
import com.saggitt.omega.util.Config

@Keep
class MinusOneController(context: Context) : PreferenceController(context) {

    override val title get() = getDisplayGoogleTitle()

    override val onChange = Preference.OnPreferenceChangeListener { pref, newValue ->
        if (newValue == true && !FeedBridge.getInstance(context).isInstalled()) {
            pref.preferenceManager.showDialog(pref)
            false
        } else {
            true
        }
    }


    @SuppressLint("StringFormatInvalid")
    private fun getDisplayGoogleTitle(): String {
        var charSequence: CharSequence? = null
        try {
            val resourcesForApplication = context.packageManager
                    .getResourcesForApplication(Config.GOOGLE_QSB)
            val identifier = resourcesForApplication.getIdentifier("title_google_home_screen", "string", Config.GOOGLE_QSB)
            if (identifier != 0) {
                charSequence = resourcesForApplication.getString(identifier)
            }
        } catch (ex: PackageManager.NameNotFoundException) {
        }

        if (TextUtils.isEmpty(charSequence)) {
            charSequence = context.getString(R.string.google_app)
        }
        return context.getString(R.string.title_show_google_app, charSequence)
    }
}
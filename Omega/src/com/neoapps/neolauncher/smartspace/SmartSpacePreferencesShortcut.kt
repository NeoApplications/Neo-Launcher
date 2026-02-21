package com.neoapps.neolauncher.smartspace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.preferences.PreferenceActivity

class SmartSpacePreferencesShortcut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(PreferenceActivity.navigateIntent(this, Routes.PREFS_WIDGETS))
        finish()
    }
}
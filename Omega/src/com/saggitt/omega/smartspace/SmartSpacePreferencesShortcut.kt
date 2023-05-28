package com.saggitt.omega.smartspace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.preferences.PreferenceActivity

class SmartSpacePreferencesShortcut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(PreferenceActivity.createIntent(this, "/${Routes.PREFS_WIDGETS}/"))
        finish()
    }
}
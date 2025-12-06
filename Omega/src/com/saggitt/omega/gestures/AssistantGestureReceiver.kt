package com.saggitt.omega.gestures

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neoapps.neolauncher.NeoLauncher
import org.koin.android.ext.android.inject

class AssistantGestureReceiver : AppCompatActivity() {
    private val neoLauncher: NeoLauncher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_SEARCH_LONG_PRESS) {
            neoLauncher.gestureController.onLaunchAssistant()
        }
        finish()
    }
}
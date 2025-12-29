package com.neoapps.neolauncher.gestures

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neoapps.neolauncher.NeoLauncher

class AssistantGestureReceiver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_SEARCH_LONG_PRESS) {
            val neoLauncher = runCatching { NeoLauncher.getLauncher(this) }.getOrNull()
            neoLauncher?.gestureController?.onLaunchAssistant()
        }
        finish()
    }
}
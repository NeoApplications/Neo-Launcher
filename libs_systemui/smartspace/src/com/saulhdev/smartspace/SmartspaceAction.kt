package com.saulhdev.smartspace

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle

class SmartspaceAction(
    val id: String,
    val icon: Icon? = null,
    val title: CharSequence,
    val subtitle: CharSequence? = null,
    val contentDescription: CharSequence? = null,
    val pendingIntent: PendingIntent? = null,
    val intent: Intent? = null,
    val onClick: Runnable? = null,
    val extras: Bundle? = null
) {
    class Builder(val id: String, val title: String) {
        val icon: Icon? = null
        val subtitle: CharSequence? = null
        val contentDescription: CharSequence? = null
        val pendingIntent: PendingIntent? = null
        private var intent: Intent? = null
        val extras: Bundle? = null

        fun setIntent(mIntent: Intent?): Builder {
            intent = mIntent
            return this
        }

        fun build(): SmartspaceAction {
            return SmartspaceAction(
                id = id,
                icon = icon,
                title = title,
                subtitle = subtitle,
                contentDescription = contentDescription,
                pendingIntent = pendingIntent,
                intent = intent,
                extras = extras
            )
        }
    }
}

val SmartspaceAction?.hasIntent get() = this != null && (intent != null || pendingIntent != null || onClick != null)

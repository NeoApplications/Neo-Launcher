package com.saggitt.omega.smartspace.uitemplatedata

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle

data class TapAction(
    val id: CharSequence?,
    val intent: Intent?,
    val pendingIntent: PendingIntent?,
    val userHandle: UserHandle?,
    val extras: Bundle?,
    val shouldShowOnLockscreen: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TapAction) return false
        return SmartspaceUtils.isEqual(id, other.id)
    }
}
package com.google.android.systemui.smartspace

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.view.View
import com.android.systemui.plugins.Plugin
import com.android.systemui.plugins.annotations.ProvidesInterface
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTargetEvent

@ProvidesInterface(action = BcSmartspaceDataPlugin.ACTION, version = BcSmartspaceDataPlugin.VERSION)
class BcSmartspaceDataPlugin : Plugin {

    interface SmartspaceEventNotifier {
        fun notifySmartspaceEvent(event: SmartspaceTargetEvent?)
    }

    interface IntentStarter {
        fun startFromAction(action: SmartspaceAction, v: View?, showOnLockscreen: Boolean) {
            try {
                if (action.intent != null) {
                    startIntent(v, action.intent, showOnLockscreen)
                } else if (action.pendingIntent != null) {
                    startPendingIntent(action.pendingIntent, showOnLockscreen)
                }
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "Could not launch intent for action: $action", e)
            }
        }

        /** Start the intent  */
        fun startIntent(v: View?, i: Intent?, showOnLockscreen: Boolean)

        /** Start the PendingIntent  */
        fun startPendingIntent(pi: PendingIntent?, showOnLockscreen: Boolean)
    }


    companion object {
        const val ACTION = "com.android.systemui.action.PLUGIN_BC_SMARTSPACE_DATA"
        const val VERSION = 1
        var TAG = "BcSmartspaceDataPlugin"
    }
}


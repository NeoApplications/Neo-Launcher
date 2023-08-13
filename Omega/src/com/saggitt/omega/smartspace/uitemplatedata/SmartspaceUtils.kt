package com.saggitt.omega.smartspace.uitemplatedata

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import com.saggitt.omega.smartspace.bcsmartspace.BcSmartspaceDataPlugin.IntentStarter


class SmartspaceUtils {

    class SmartspaceIntentStarter(val tag: String?) : IntentStarter {
        override fun startIntent(view: View?, intent: Intent?, z: Boolean) {
            try {
                view!!.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(tag, "Cannot invoke smartspace intent", e)
            } catch (e: NullPointerException) {
                Log.e(tag, "Cannot invoke smartspace intent", e)
            } catch (e: SecurityException) {
                Log.e(tag, "Cannot invoke smartspace intent", e)
            }
        }

        override fun startPendingIntent(pendingIntent: PendingIntent?, z: Boolean) {
            try {
                pendingIntent!!.send()
            } catch (e: PendingIntent.CanceledException) {
                Log.e(tag, "Cannot invoke canceled smartspace intent", e)
            }
        }
    }

    companion object {

        fun isEmpty(@Nullable text: Text?): Boolean {
            return text == null || TextUtils.isEmpty(text.text)
        }

        fun isEqual(@Nullable text1: Text?, @Nullable text2: Text?): Boolean {
            if (text1 == null && text2 == null) return true
            return if (text1 == null || text2 == null) false else text1 == text2
        }

        fun isEqual(@Nullable cs1: CharSequence?, @Nullable cs2: CharSequence?): Boolean {
            if (cs1 == null && cs2 == null) return true
            return if (cs1 == null || cs2 == null) false else cs1.toString().contentEquals(cs2)
        }
    }
}
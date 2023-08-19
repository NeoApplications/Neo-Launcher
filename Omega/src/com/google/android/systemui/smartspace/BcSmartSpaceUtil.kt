package com.google.android.systemui.smartspace

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.saulhdev.smartspace.SmartspaceAction

object BcSmartSpaceUtil {

    @JvmStatic
    fun getIconDrawable(context: Context, icon: Icon?): Drawable? {
        if (icon == null) return null
        val drawable = icon.loadDrawable(context) ?: return null
        val iconSize =
            context.resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size)
        drawable.setBounds(0, 0, iconSize, iconSize)
        return drawable
    }

    fun setOnClickListener(
        view: View?,
        action: SmartspaceAction?,
        onClickListener: View.OnClickListener? = null,
        str: String?,
    ) {
        if (view == null || action == null) {
            Log.e(str, "No tap action can be set up")
            return
        }
        view.setOnClickListener {

        }
    }

    fun getOpenCalendarIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).setData(
            ContentUris.appendId(
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
                System.currentTimeMillis()
            ).build()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }

    @JvmStatic
    fun getDimensionRatio(bundle: Bundle): String? {
        if (bundle.containsKey("imageRatioWidth") && bundle.containsKey("imageRatioHeight")) {
            val i = bundle.getInt("imageRatioWidth")
            val i2 = bundle.getInt("imageRatioHeight")
            return if (i > 0 && i2 > 0) {
                "$i:$i2"
            } else null
        }
        return null
    }

    fun getLoggingDisplaySurface(packageName: String, isDreaming: Boolean, dozeAmount: Float): Int {
        if (packageName == "com.google.android.apps.nexuslauncher" || packageName == BuildConfig.APPLICATION_ID) {
            return 1
        }
        return if (packageName == "com.android.systemui") {
            if (dozeAmount == 1.0f) {
                return 3
            }
            if (dozeAmount == 0.0f) 2 else -1
        } else if (isDreaming) {
            5
        } else {
            0
        }
    }
}

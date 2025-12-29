package com.neoapps.neolauncher.smartspace

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.android.launcher3.CheckLongPressHelper
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager
import com.android.launcher3.views.OptionsPopupView
import com.google.android.systemui.smartspace.BcSmartspaceView
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.preferences.PreferenceActivity

class SmartspaceViewContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, private val previewMode: Boolean = false
) : FrameLayout(context, attrs) {

    private val longPressHelper = CheckLongPressHelper(this) { performLongClick() }
    private val smartspaceView: View

    init {
        val inflater = LayoutInflater.from(context)
        smartspaceView =
            inflater.inflate(R.layout.smartspace_enhanced, this, false) as BcSmartspaceView
        smartspaceView.previewMode = previewMode
        setOnLongClickListener {
            openOptions()
            true
        }
        addView(smartspaceView)
    }

    private fun openOptions() {
        if (previewMode) return

        val launcher = Launcher.getLauncher(context)
        val pos = Rect()
        launcher.dragLayer.getDescendantRectRelativeToSelf(smartspaceView, pos)
        OptionsPopupView.show<Launcher>(launcher, RectF(pos), listOf(getCustomizeOption()), true)
    }

    private fun getCustomizeOption() = OptionsPopupView.OptionItem(
        context, R.string.customize, R.drawable.ic_setting,
        StatsLogManager.LauncherEvent.IGNORE
    ) {
        context.startActivity(PreferenceActivity.navigateIntent(context, Routes.PREFS_WIDGETS))
        true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        longPressHelper.onTouchEvent(ev)
        return longPressHelper.hasPerformedLongPress()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        longPressHelper.onTouchEvent(ev)
        return true
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        longPressHelper.cancelLongPress()
    }
}

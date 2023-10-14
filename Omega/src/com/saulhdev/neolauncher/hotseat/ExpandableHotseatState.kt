package com.saulhdev.neolauncher.hotseat

import android.content.Context
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.views.ActivityContext

class ExpandableHotseatState(id: Int) :
    LauncherState(id, 8, FLAG_WORKSPACE_ICONS_CAN_BE_DRAGGED or 2 or 2) {

    var mIsHotseatOpened = false
    val PAGE_ALPHA_PROVIDER: PageAlphaProvider =
        object : PageAlphaProvider(Interpolators.DEACCEL_2) {
            override fun getPageAlpha(i2: Int): Float {
                return 1f
            }
        }

    override fun <DEVICE_PROFILE_CONTEXT> getTransitionDuration(
        context: DEVICE_PROFILE_CONTEXT,
        isToState: Boolean
    ): Int where DEVICE_PROFILE_CONTEXT : Context?, DEVICE_PROFILE_CONTEXT : ActivityContext? {
        return 250
    }

    override fun getWorkspaceScaleAndTranslation(launcher: Launcher): ScaleAndTranslation {
        return ScaleAndTranslation(
            1.0f,
            0f,
            -launcher.mHotseatController.shiftRange * 0.125f
        )
    }

    fun getVerticalProgress(): Float {
        return 0f
    }

    fun getVisibleElements(): Int {
        return 65
    }
}
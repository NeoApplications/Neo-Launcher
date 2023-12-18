package com.saulhdev.neolauncher.hotseat

import android.view.MotionEvent
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.Utilities
import com.android.launcher3.states.StateAnimationConfig
import com.android.launcher3.touch.AbstractStateChangeTouchController
import com.android.launcher3.touch.SingleAxisSwipeDetector
import com.saggitt.omega.touch.SwipeDetector

class ExpandableHotseatSwipeController(val launcher: Launcher) :
    AbstractStateChangeTouchController(launcher, SingleAxisSwipeDetector.VERTICAL) {
    val isHorizontalRtl =
        Utilities.isRtl(launcher.resources) && mSwipeDirection == SwipeDetector.HORIZONTAL

    override fun canInterceptTouch(ev: MotionEvent?): Boolean {
        return launcher.isInState(LauncherState.NORMAL)
    }

    override fun getTargetState(
        fromState: LauncherState?,
        isDragTowardPositive: Boolean
    ): LauncherState {
        val isPositive = isDragTowardPositive xor isHorizontalRtl
        return if (fromState == LauncherState.NORMAL && isPositive)
            LauncherState.EXPANDABLE_HOTSEAT
        else if (fromState == LauncherState.EXPANDABLE_HOTSEAT && !isPositive)
            LauncherState.NORMAL
        else fromState!!
    }

    override fun initCurrentAnimation(): Float {
        val shiftRange = shiftRange
        mCurrentAnimation = mLauncher.mStateManager.createAnimationToNewWorkspace(
            mToState,
            (2.0f * shiftRange).toLong(),
            0 //StateAnimationConfig.ANIM_HOTSEAT_SCALE
        )
        var verticalProgress =
            mToState.getVerticalProgress(mLauncher) * shiftRange - mFromState.getVerticalProgress(
                mLauncher
            ) * shiftRange
        if (isHorizontalRtl) {
            verticalProgress = -verticalProgress
        }
        return 1.0f / verticalProgress
    }
}
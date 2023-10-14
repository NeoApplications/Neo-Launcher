package com.saulhdev.neolauncher.hotseat

import com.android.launcher3.Hotseat
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.anim.PendingAnimation
import com.android.launcher3.statemanager.StateManager.StateHandler
import com.android.launcher3.states.StateAnimationConfig

open class HotseatTransitionController<T : Hotseat?>(launcher: Launcher) :
    StateHandler<LauncherState?> {
    var mHotseat: T? = null
    val mLauncher: Launcher

    init {
        mLauncher = launcher
    }

    val progress: Float
        get() = 1.0f

    val shiftRange: Float
        get() = 0.0f

    override fun setState(state: LauncherState?) {
    }

    override fun setStateWithAnimation(
        toState: LauncherState?,
        config: StateAnimationConfig?,
        animation: PendingAnimation?
    ) {
    }
}


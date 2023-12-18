package com.saulhdev.neolauncher.hotseat

import android.view.View
import android.view.animation.Interpolator
import com.android.app.animation.Interpolators
import com.android.launcher3.DeviceProfile
import com.android.launcher3.Hotseat
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.ShortcutAndWidgetContainer
import com.android.launcher3.anim.PropertySetter
import com.android.launcher3.compat.AccessibilityManagerCompat
import com.android.launcher3.statemanager.StateManager
import com.android.launcher3.states.StateAnimationConfig
import com.android.launcher3.views.ScrimView
import com.saggitt.omega.preferences.NeoPrefs
import java.util.Objects


class ExpandableHotseatTransitionController(launcher: Launcher) :
    HotseatTransitionController<ExpandableHotseat?>(launcher),
    DeviceProfile.OnDeviceProfileChangeListener,
    StateManager.StateListener<LauncherState>,
    StateManager.StateHandler<LauncherState?> {

    var mScrimView: ScrimView? = null
    var mScrollRangeDelta = 0f
    var mProgress = 1f
    var mIsExpandableHotseatCollapsing = false
    var mShiftRange = 0f
    val prefs = NeoPrefs.getInstance(launcher)

    init {
        launcher.onDeviceProfileChangeListeners.add(this)
        launcher.stateManager.addStateListener(this)
        setScrollRangeDelta(mScrollRangeDelta)
    }

    override fun onDeviceProfileChanged(profile: DeviceProfile?) {
    }

    override fun setState(launcherState: LauncherState?) {
        var verticalProgress = launcherState?.getVerticalProgress(mLauncher)
        if (launcherState === LauncherState.ALL_APPS) {
            verticalProgress = 1.0f
        }
        setProgress(verticalProgress)
        val stateAnimationConfig = StateAnimationConfig()
        val propertySetter = PropertySetter.NO_ANIM_PROPERTY_SETTER
        setAlphas(launcherState!!, stateAnimationConfig, propertySetter)
        setAlphas(launcherState, StateAnimationConfig(), propertySetter)
    }

    override fun onStateTransitionStart(launcherState: LauncherState) {
        mIsExpandableHotseatCollapsing = false
        if (mHotseat == null) {
            return
        }
        val launcherState3 = LauncherState.EXPANDABLE_HOTSEAT
        if (launcherState === launcherState3) {
            val expandableHotseat = mHotseat
            if (AccessibilityManagerCompat.isAccessibilityEnabled(expandableHotseat!!.context)) {
                AccessibilityManagerCompat.sendCustomAccessibilityEvent(
                    expandableHotseat,
                    16384,
                    expandableHotseat.context.getString(R.string.hotseat_accessibility_expand)
                )
            }
        }
        if (launcherState === launcherState3 || mProgress >= 1.0f) {
            return
        }
        val expandableHotseat2 = mHotseat
        if (AccessibilityManagerCompat.isAccessibilityEnabled(expandableHotseat2!!.context)) {
            AccessibilityManagerCompat.sendCustomAccessibilityEvent(
                expandableHotseat2,
                16384,
                expandableHotseat2.context.getString(R.string.hotseat_accessibility_collapse)
            )
        }
        mIsExpandableHotseatCollapsing = true
    }

    override fun onStateTransitionComplete(launcherState: LauncherState) {
        if (mHotseat == null) {
            return
        }
        if (launcherState === LauncherState.EXPANDABLE_HOTSEAT) {
            val expandableHotseat = mHotseat
            expandableHotseat!!.removeCallbacks(expandableHotseat.handler)
            expandableHotseat.postDelayed(expandableHotseat.handler, 1000L)
        } else if (mIsExpandableHotseatCollapsing) {
            val expandableHotseat = mHotseat
            expandableHotseat!!.removeCallbacks(expandableHotseat.handler2)
            expandableHotseat.postDelayed(expandableHotseat.handler2, 1000L)
            mIsExpandableHotseatCollapsing = false
        }
    }

    fun setupViews(hotseat: Hotseat?) {
        mHotseat = hotseat as ExpandableHotseat?
        mScrimView = mLauncher.findViewById<View>(R.id.scrim_view) as ScrimView
        setScrollRangeDelta(mScrollRangeDelta)
    }

    fun setAlphas(
        launcherState: LauncherState,
        stateAnimationConfig: StateAnimationConfig,
        propertySetter: PropertySetter
    ) {
        var f: Float
        if (mHotseat == null) {
            return
        }
        val visibleElements = launcherState.getVisibleElements(mLauncher)
        val z2 = visibleElements and 64 != 0
        val interpolator: Interpolator =
            stateAnimationConfig.getInterpolator(
                StateAnimationConfig.ANIM_HOTSEAT_FADE,
                Interpolators.LINEAR
            )
        val shortcutsAndWidgets: ShortcutAndWidgetContainer =
            mHotseat!!.shortcutsAndWidgets
        val childCount = shortcutsAndWidgets.childCount
        val dragInfo = mLauncher.workspace.dragInfo
        var i2 = 0
        while (true) {
            f = 0f
            if (i2 >= childCount) {
                break
            }
            val childAt = shortcutsAndWidgets.getChildAt(i2)
            if (dragInfo == null || dragInfo.cell !== childAt) {
                if (mHotseat!!.m(childAt)) {
                    if (z2) {
                        f = 1.0f
                    }
                    propertySetter.setViewAlpha(childAt, f, interpolator)
                } else {
                    propertySetter.setViewAlpha(childAt, 1.0f, interpolator)
                }
            }
            i2++
        }
        val settingsIcon: View = mHotseat?.settingIcon!!
        if (z2) {
            f = 1.0f
        }
        propertySetter.setViewAlpha(settingsIcon, f, interpolator)
        /*propertySetter.setInt<ScrimView?>(
            mScrimView,
            ScrimView.DRAG_HANDLE_ALPHA,
            if (visibleElements and 32 != 0) 255 else 0,
            Interpolators.LINEAR
        )*/
    }


    fun setProgress(f: Float?) {
        /*var title: BubbleTextView
        val expandableHotseat: ExpandableHotseat?
        if (mHotseat == null) {
            return
        }
        mProgress = f!!
        if (!mScrimView.mBlurEffectHelper.shouldFallbackToSolidColor()) {
            mScrimView.setProgress(f)
        }
        val f2 = mShiftRange
        var f3 = f * f2
        val f4 = -f2 + f3
        if (this.mIsVerticalLayout) {
            if (mLauncher.mDeviceProfile.isSeascape) {
                expandableHotseat = mHotseat
                f3 = -f3
            } else {
                expandableHotseat = mHotseat
            }
            expandableHotseat!!.setTranslationX(f3)
            (mHotseat as ExpandableHotseat?)!!.translationY = CameraView.FLASH_ALPHA_END
        } else {
            (mHotseat as ExpandableHotseat?)!!.setTranslationX(CameraView.FLASH_ALPHA_END)
            (mHotseat as ExpandableHotseat?)!!.translationY = f3
            mLauncher.mWorkspace.pageIndicator.translationY = f4
        }
        Objects.requireNonNull(mHotseat)
        if (!t.e(v8.L(), "GadernSalad", "switch_for_enable_dock_background", false)) {
            mHotseat.getBackgroundComponent()
                .setBackgroundColor(ViewUtils.j(mHotseat.getBackgroundColor(), 1.0f - f))
        }
        mLauncher.getSystemUiController().updateUiState(5, 0)
        val intValue =
            ArgbEvaluator().evaluate(
                f, Integer.valueOf(j.f().e.getTextColorPrimary()), Integer.valueOf(
                    CanvasUtils.getAttrColor(
                        mLauncher, R.attr.workspaceTextColor
                    )
                )
            ) as Int
        val shortcutsAndWidgets: ShortcutAndWidgetContainer =
            mHotseat.getLayout().getShortcutsAndWidgets()
        val childCount = shortcutsAndWidgets.childCount
        for (i2 in 0 until childCount) {
            val childAt = shortcutsAndWidgets.getChildAt(i2)
            if (childAt is BubbleTextView) {
                title = childAt
                title.setTextColor(intValue)
            } else if (childAt is FolderIcon) {
                val folderIcon: FolderIcon = childAt as FolderIcon
                folderIcon.getTitle().setTextColor(intValue)
                title = folderIcon.getTitle()
            }
            title.paint.clearShadowLayer()
        }*/
    }

    fun setScrollRangeDelta(f: Float) {
        this.mScrollRangeDelta = f
        if (mHotseat != null) {
            val deviceProfile = mLauncher.deviceProfile
            mShiftRange =
                mHotseat!!.getExpandableHotseatHeight() - (if (!prefs.dockExpandable.getValue()) deviceProfile.hotseatBarSizePx
                else 0) - mHotseat!!.getTopRowHeight() - this.mScrollRangeDelta
        }
        val scrimView = mScrimView
        if (scrimView != null) {
            Objects.requireNonNull(scrimView)
        }
    }

}
/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.qsb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.android.launcher3.LauncherRootView.WindowStateListener;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager.StateListener;
import com.android.launcher3.anim.Interpolators;
import com.saggitt.omega.OmegaLauncher;

public class QsbAnimationController implements WindowStateListener, StateListener {

    public AnimatorSet mAnimatorSet;
    public boolean mQsbHasFocus;
    public OmegaLauncher mLauncher;
    public boolean mSearchRequested;

    public QsbAnimationController(OmegaLauncher launcher) {
        mLauncher = launcher;
        mLauncher.getStateManager().addStateListener(this);
        mLauncher.getRootView().setWindowStateListener(this);
    }

    public void playQsbAnimation() {
        if (mLauncher.hasWindowFocus()) {
            mSearchRequested = true;
        } else {
            openQsb();
        }
    }

    public AnimatorSet openQsb() {
        mSearchRequested = false;
        mQsbHasFocus = true;
        playAnimation(true, true);
        return mAnimatorSet;
    }

    public void prepareAnimation(boolean hasFocus) {
        mSearchRequested = false;
        if (mQsbHasFocus) {
            mQsbHasFocus = false;
            playAnimation(false, hasFocus);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            if (mSearchRequested) {
                openQsb();
                return;
            }
        }
        if (hasFocus) {
            prepareAnimation(true);
        }
    }

    public void onWindowVisibilityChanged(int visibility) {
        prepareAnimation(false);
    }

    public void playAnimation(boolean checkHotseat, boolean hasFocus) {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
        View view = mLauncher.getDragLayer();
        if (mLauncher.isInState(LauncherState.ALL_APPS)) {
            view.setAlpha(1.0f);
            view.setTranslationY(0.0f);
            return;
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animation == mAnimatorSet) {
                    mAnimatorSet = null;
                }
            }
        });
        Animator animator;
        if (checkHotseat) {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f));
            animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, (float) ((-mLauncher.getHotseat().getHeight()) / 2));
            animator.setInterpolator(Interpolators.ACCEL);
            mAnimatorSet.play(animator);
            mAnimatorSet.setDuration(200);
        } else {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f));
            animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0.0f);
            animator.setInterpolator(Interpolators.DEACCEL);
            mAnimatorSet.play(animator);
            mAnimatorSet.setDuration(200);
        }
        mAnimatorSet.start();
        if (!hasFocus) {
            mAnimatorSet.end();
        }
    }

    @Override
    public void onStateTransitionStart(LauncherState launcherState) {
    }

    @Override
    public void onStateTransitionComplete(LauncherState launcherState) {
        reattachFocus(launcherState);
    }

    public void reattachFocus(LauncherState launcherState) {
        if (mQsbHasFocus && launcherState != LauncherState.ALL_APPS && !mLauncher.hasWindowFocus()) {
            playAnimation(true, false);
        }
    }
}

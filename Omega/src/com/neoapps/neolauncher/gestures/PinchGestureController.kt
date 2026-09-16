/*
 *     This file is part of Neo Launcher.
 *
 *     Neo Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Neo Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Neo Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.gestures

import android.view.MotionEvent
import android.view.ViewConfiguration
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.util.TouchController
import com.neoapps.neolauncher.launcher
import kotlin.math.hypot

class PinchGestureController(private val launcher: Launcher) : TouchController {

    private val controller get() = launcher.launcher.gestureController

    private val touchSlop = ViewConfiguration.get(launcher).scaledTouchSlop
    private val minPinchDistance = touchSlop * 1.5f
    private val pinchInThresholdRatio = 0.85f
    private val pinchOutThresholdRatio = 1.15f

    private var startSpan = 0f
    private var gestureTriggered = false
    private var isIntercepting = false

    private fun canInterceptTouch(): Boolean {
        val hasPinchIn = controller.pinchInGesture.handler.value !is BlankGestureHandler
        val hasPinchOut = controller.pinchOutGesture.handler.value !is BlankGestureHandler
        if (!hasPinchIn && !hasPinchOut) {
            return false
        }
        return AbstractFloatingView.getTopOpenView(launcher) == null &&
                launcher.isInState(LauncherState.NORMAL) &&
                !launcher.workspace.isHandlingTouch
    }

    private fun getSpan(ev: MotionEvent): Float {
        if (ev.pointerCount < 2) return 0f
        val dx = ev.getX(0) - ev.getX(1)
        val dy = ev.getY(0) - ev.getY(1)
        return hypot(dx, dy)
    }

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            startSpan = 0f
            gestureTriggered = false
            isIntercepting = false
        }

        if (!canInterceptTouch()) {
            startSpan = 0f
            gestureTriggered = false
            isIntercepting = false
            return false
        }

        if (ev.pointerCount >= 2) {
            if (startSpan <= 0f) {
                startSpan = getSpan(ev)
            }
            isIntercepting = true
            return true
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            startSpan = 0f
            gestureTriggered = false
            isIntercepting = false
        }

        return isIntercepting
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            startSpan = 0f
            gestureTriggered = false
            isIntercepting = false
            return true
        }

        if (action == MotionEvent.ACTION_POINTER_UP) {
            if (!gestureTriggered) {
                startSpan = 0f
            }
            return true
        }

        if (ev.pointerCount >= 2) {
            val currentSpan = getSpan(ev)
            if (startSpan <= 0f) {
                startSpan = currentSpan
            } else if (!gestureTriggered) {
                val hasPinchIn = controller.pinchInGesture.handler.value !is BlankGestureHandler
                val hasPinchOut = controller.pinchOutGesture.handler.value !is BlankGestureHandler

                if (currentSpan < startSpan && hasPinchIn) {
                    val spanDelta = startSpan - currentSpan
                    val ratio = currentSpan / startSpan

                    if (ratio <= pinchInThresholdRatio && spanDelta >= minPinchDistance) {
                        gestureTriggered = true
                        controller.onPinchIn()
                    }
                } else if (currentSpan > startSpan && hasPinchOut) {
                    val spanDelta = currentSpan - startSpan
                    val ratio = currentSpan / startSpan

                    if (ratio >= pinchOutThresholdRatio && spanDelta >= minPinchDistance) {
                        gestureTriggered = true
                        controller.onPinchOut()
                    }
                }
            }
        }

        return true
    }
}

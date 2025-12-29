package com.neoapps.neolauncher.gestures.gestures

import com.neoapps.neolauncher.gestures.Gesture
import com.neoapps.neolauncher.gestures.GestureController

class LaunchAssistantGesture(controller: GestureController) :
    Gesture(controller, controller.launcher.prefs.gestureLaunchAssistant)

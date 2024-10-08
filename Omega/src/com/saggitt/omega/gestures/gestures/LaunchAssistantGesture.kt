package com.saggitt.omega.gestures.gestures

import com.saggitt.omega.gestures.Gesture
import com.saggitt.omega.gestures.GestureController

class LaunchAssistantGesture(controller: GestureController) :
    Gesture(controller, controller.launcher.prefs.gestureLaunchAssistant)

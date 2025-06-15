package com.saulhdev.smartspace

import androidx.annotation.IntDef

class SmartspaceTargetEvent(
    val smartspaceTarget: SmartspaceTarget?,
    val smartspaceActionId: String?,
    @EventType val eventType: Int

) {

    @IntDef(
        value = [EVENT_TARGET_INTERACTION, EVENT_TARGET_SHOWN, EVENT_TARGET_HIDDEN, EVENT_TARGET_DISMISS, EVENT_TARGET_BLOCK, EVENT_UI_SURFACE_SHOWN, EVENT_UI_SURFACE_HIDDEN]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class EventType

    companion object {
        /**
         * User interacted with the target.
         */
        const val EVENT_TARGET_INTERACTION = 1

        /**
         * Smartspace target was brought into view.
         */
        const val EVENT_TARGET_SHOWN = 2

        /**
         * Smartspace target went out of view.
         */
        const val EVENT_TARGET_HIDDEN = 3

        /**
         * A dismiss action was issued by the user.
         */
        const val EVENT_TARGET_DISMISS = 4

        /**
         * A block action was issued by the user.
         */
        const val EVENT_TARGET_BLOCK = 5

        /**
         * The Ui surface came into view.
         */
        const val EVENT_UI_SURFACE_SHOWN = 6

        /**
         * The Ui surface went out of view.
         */
        const val EVENT_UI_SURFACE_HIDDEN = 7
    }
}
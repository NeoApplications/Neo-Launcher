package com.saggitt.omega.smartspace.bcsmartspace

import com.android.launcher3.logging.StatsLogManager.EventEnum


enum class BcSmartspaceEvent(private val mId: Int) : EventEnum {
    IGNORE(-1),
    SMARTSPACE_CARD_RECEIVED(759),
    SMARTSPACE_CARD_CLICK(760),
    SMARTSPACE_CARD_DISMISS(761),
    SMARTSPACE_CARD_SEEN(800),
    ENABLED_SMARTSPACE(822),
    DISABLED_SMARTSPACE(823);

    override fun getId(): Int {
        return mId
    }
}


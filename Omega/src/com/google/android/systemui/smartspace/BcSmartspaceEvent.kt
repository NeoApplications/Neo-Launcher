package com.google.android.systemui.smartspace

import com.android.launcher3.logging.StatsLogManager


enum class BcSmartspaceEvent(private val mId: Int) : StatsLogManager.EventEnum {
    IGNORE(-1) {
        override val id: Int
            get() = -1
    },
    SMARTSPACE_CARD_RECEIVED(759) {
        override val id: Int
            get() = 759
    },
    SMARTSPACE_CARD_CLICK(760) {
        override val id: Int
            get() = 760
    },
    SMARTSPACE_CARD_DISMISS(761) {
        override val id: Int
            get() = 761
    },
    SMARTSPACE_CARD_SEEN(800) {
        override val id: Int
            get() = 800
    },
    ENABLED_SMARTSPACE(822) {
        override val id: Int
            get() = 822
    },
    DISABLED_SMARTSPACE(823) {
        override val id: Int
            get() = 823
    };

    /*
    fun getId(): Int {
        return mId
    }*/
}


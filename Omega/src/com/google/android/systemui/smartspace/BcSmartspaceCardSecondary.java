package com.google.android.systemui.smartspace;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.plugin.BcSmartspaceDataPlugin;
import com.saulhdev.smartspace.SmartspaceTarget;

public abstract class BcSmartspaceCardSecondary extends ConstraintLayout {
    public String mPrevSmartspaceTargetId;

    public BcSmartspaceCardSecondary(Context context) {
        super(context);
        this.mPrevSmartspaceTargetId = "";
    }

    public BcSmartspaceCardSecondary(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPrevSmartspaceTargetId = "";
    }

    public final void reset(String str) {
        if (this.mPrevSmartspaceTargetId.equals(str)) {
            return;
        }
        this.mPrevSmartspaceTargetId = str;
        resetUi();
    }

    public void resetUi() {}

    public abstract boolean setSmartspaceActions(
            SmartspaceTarget smartspaceTarget,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier smartspaceEventNotifier,
            BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo);

    public abstract void setTextColor(int i);
}

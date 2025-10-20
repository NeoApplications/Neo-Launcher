package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceTarget;

import java.util.UUID;

public abstract class InstanceId {
    public static int create(SmartspaceTarget smartspaceTarget) {
        if (smartspaceTarget == null) {
            return SmallHash.hash(UUID.randomUUID().toString());
        }
        String smartspaceTargetId = smartspaceTarget.getSmartspaceTargetId();
        return (smartspaceTargetId == null || smartspaceTargetId.isEmpty())
                ? SmallHash.hash(String.valueOf(smartspaceTarget.getCreationTimeMillis()))
                : SmallHash.hash(smartspaceTargetId);
    }

    public static int create(String str) {
        return (str == null || str.isEmpty())
                ? SmallHash.hash(UUID.randomUUID().toString())
                : SmallHash.hash(str);
    }
}

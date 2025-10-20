package com.google.android.systemui.smartspace.logging;

import java.util.Objects;

public final class BcSmartspaceCardMetadataLoggingInfo {
    public final int mCardTypeId;
    public final int mInstanceId;

    public BcSmartspaceCardMetadataLoggingInfo(Builder builder) {
        this.mInstanceId = builder.mInstanceId;
        this.mCardTypeId = builder.mCardTypeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BcSmartspaceCardMetadataLoggingInfo)) {
            return false;
        }
        BcSmartspaceCardMetadataLoggingInfo bcSmartspaceCardMetadataLoggingInfo =
                (BcSmartspaceCardMetadataLoggingInfo) obj;
        return mInstanceId == bcSmartspaceCardMetadataLoggingInfo.mInstanceId
                && mCardTypeId == bcSmartspaceCardMetadataLoggingInfo.mCardTypeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mInstanceId, mCardTypeId);
    }

    @Override
    public String toString() {
        return "BcSmartspaceCardMetadataLoggingInfo{mInstanceId="
                + mInstanceId
                + ", mCardTypeId="
                + mCardTypeId
                + "}";
    }

    public static final class Builder {
        public int mCardTypeId;
        public int mInstanceId;

        public Builder setCardTypeId(int cardTypeId) {
            this.mCardTypeId = cardTypeId;
            return this;
        }

        public Builder setInstanceId(int instanceId) {
            this.mInstanceId = instanceId;
            return this;
        }

        public BcSmartspaceCardMetadataLoggingInfo build() {
            return new BcSmartspaceCardMetadataLoggingInfo(this);
        }
    }
}

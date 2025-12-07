package com.google.android.systemui.smartspace.logging;

import com.android.systemui.smartspace.nano.SmartspaceProto;

import java.util.Objects;

public final class BcSmartspaceCardLoggingInfo {
    public int mInstanceId;
    public int mDisplaySurface;
    public int mRank;
    public int mCardinality;
    public int mFeatureType;
    public int mReceivedLatency;
    public int mUid;
    public BcSmartspaceSubcardLoggingInfo mSubcardInfo;
    public SmartspaceProto.SmartspaceCardDimensionalInfo mDimensionalInfo;

    public static final class Builder {
        public int mInstanceId;
        public int mDisplaySurface = 1;
        public int mRank;
        public int mCardinality;
        public int mFeatureType;
        public int mReceivedLatency;
        public int mUid;
        public BcSmartspaceSubcardLoggingInfo mSubcardInfo;
        public SmartspaceProto.SmartspaceCardDimensionalInfo mDimensionalInfo;

        public Builder() {}

        public Builder setInstanceId(int instanceId) {
            this.mInstanceId = instanceId;
            return this;
        }

        public Builder setDisplaySurface(int displaySurface) {
            this.mDisplaySurface = displaySurface;
            return this;
        }

        public Builder setRank(int rank) {
            this.mRank = rank;
            return this;
        }

        public Builder setCardinality(int cardinality) {
            this.mCardinality = cardinality;
            return this;
        }

        public Builder setFeatureType(int featureType) {
            this.mFeatureType = featureType;
            return this;
        }

        public Builder setReceivedLatency(int receivedLatency) {
            this.mReceivedLatency = receivedLatency;
            return this;
        }

        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        public Builder setSubcardInfo(BcSmartspaceSubcardLoggingInfo subcardInfo) {
            this.mSubcardInfo = subcardInfo;
            return this;
        }

        public Builder setDimensionalInfo(
                SmartspaceProto.SmartspaceCardDimensionalInfo dimensionalInfo) {
            this.mDimensionalInfo = dimensionalInfo;
            return this;
        }

        public BcSmartspaceCardLoggingInfo build() {
            return new BcSmartspaceCardLoggingInfo(this);
        }
    }

    public BcSmartspaceCardLoggingInfo(Builder builder) {
        this.mInstanceId = builder.mInstanceId;
        this.mDisplaySurface = builder.mDisplaySurface;
        this.mRank = builder.mRank;
        this.mCardinality = builder.mCardinality;
        this.mFeatureType = builder.mFeatureType;
        this.mReceivedLatency = builder.mReceivedLatency;
        this.mUid = builder.mUid;
        this.mSubcardInfo = builder.mSubcardInfo;
        this.mDimensionalInfo = builder.mDimensionalInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BcSmartspaceCardLoggingInfo)) {
            return false;
        }
        BcSmartspaceCardLoggingInfo other = (BcSmartspaceCardLoggingInfo) obj;
        return mInstanceId == other.mInstanceId
                && mDisplaySurface == other.mDisplaySurface
                && mRank == other.mRank
                && mCardinality == other.mCardinality
                && mFeatureType == other.mFeatureType
                && mReceivedLatency == other.mReceivedLatency
                && mUid == other.mUid
                && Objects.equals(mSubcardInfo, other.mSubcardInfo)
                && Objects.equals(mDimensionalInfo, other.mDimensionalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mInstanceId,
                mDisplaySurface,
                mRank,
                mCardinality,
                mFeatureType,
                mReceivedLatency,
                mUid,
                mSubcardInfo,
                mDimensionalInfo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("instance_id = ")
                .append(mInstanceId)
                .append(", feature type = ")
                .append(mFeatureType)
                .append(", display surface = ")
                .append(mDisplaySurface)
                .append(", rank = ")
                .append(mRank)
                .append(", cardinality = ")
                .append(mCardinality)
                .append(", receivedLatencyMillis = ")
                .append(mReceivedLatency)
                .append(", uid = ")
                .append(mUid)
                .append(", subcardInfo = ")
                .append(mSubcardInfo)
                .append(", dimensionalInfo = ")
                .append(mDimensionalInfo);
        return sb.toString();
    }
}

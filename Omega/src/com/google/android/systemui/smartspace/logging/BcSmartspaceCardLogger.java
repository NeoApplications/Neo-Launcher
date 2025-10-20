package com.google.android.systemui.smartspace.logging;

import android.util.StatsEvent;
import android.util.StatsLog;

import com.android.systemui.smartspace.SmartspaceProtoLite;

import com.google.android.systemui.smartspace.BcSmartSpaceUtil;
import com.google.android.systemui.smartspace.BcSmartspaceEvent;
import com.google.protobuf.nano.MessageNano;

import java.util.ArrayList;
import java.util.List;

public abstract class BcSmartspaceCardLogger {
    static {
        BcSmartSpaceUtil.sIntentStarter = null;
    }

    public static void log(BcSmartspaceEvent event, BcSmartspaceCardLoggingInfo loggingInfo) {
        byte[] subcardsData = null;
        byte[] dimensionalInfoData = null;

        BcSmartspaceSubcardLoggingInfo subcardInfo = loggingInfo.mSubcardInfo;
        if (subcardInfo != null
                && subcardInfo.mSubcards != null
                && !subcardInfo.mSubcards.isEmpty()) {
            List<SmartspaceProtoLite.SmartSpaceCardMetadata> subcardMetadataList =
                    new ArrayList<>();
            for (BcSmartspaceCardMetadataLoggingInfo metadata : subcardInfo.mSubcards) {
                SmartspaceProtoLite.SmartSpaceCardMetadata.Builder builder =
                        SmartspaceProtoLite.SmartSpaceCardMetadata.newBuilder();
                builder.setInstanceId(metadata.mInstanceId);
                builder.setCardTypeId(metadata.mCardTypeId);
                subcardMetadataList.add(builder.build());
            }

            SmartspaceProtoLite.SmartSpaceSubcards.Builder subcardsBuilder =
                    SmartspaceProtoLite.SmartSpaceSubcards.newBuilder();
            subcardsBuilder.setClickedSubcardIndex(subcardInfo.mClickedSubcardIndex);
            subcardsBuilder.addAllSubcards(subcardMetadataList);
            SmartspaceProtoLite.SmartSpaceSubcards subcards = subcardsBuilder.build();
            subcardsData = subcards.toByteArray();
        }

        if (loggingInfo.mDimensionalInfo != null) {
            dimensionalInfoData = MessageNano.toByteArray(loggingInfo.mDimensionalInfo);
        }

        StatsEvent.Builder statsBuilder = StatsEvent.newBuilder();
        statsBuilder.setAtomId(0x160);
        statsBuilder.writeInt(event.getId());
        statsBuilder.writeInt(loggingInfo.mInstanceId);
        statsBuilder.writeInt(0);
        statsBuilder.writeInt(loggingInfo.mDisplaySurface);
        statsBuilder.writeInt(loggingInfo.mRank);
        statsBuilder.writeInt(loggingInfo.mCardinality);
        statsBuilder.writeInt(loggingInfo.mFeatureType);
        statsBuilder.writeInt(loggingInfo.mUid);
        statsBuilder.addBooleanAnnotation((byte) 1, true);
        statsBuilder.writeInt(0);
        statsBuilder.writeInt(0);
        statsBuilder.writeInt(loggingInfo.mReceivedLatency);

        if (subcardsData == null) {
            subcardsData = new byte[0];
        }
        statsBuilder.writeByteArray(subcardsData);

        if (dimensionalInfoData == null) {
            dimensionalInfoData = new byte[0];
        }
        statsBuilder.writeByteArray(dimensionalInfoData);

        statsBuilder.usePooledBuffer();
        StatsEvent statsEvent = statsBuilder.build();
        StatsLog.write(statsEvent);
    }
}

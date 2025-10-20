package com.google.android.systemui.smartspace.logging;

import android.os.Bundle;

import com.android.systemui.smartspace.nano.SmartspaceProto;

import com.google.android.systemui.smartspace.InstanceId;
import com.saulhdev.smartspace.uitemplatedata.BaseTemplateData;

import java.util.ArrayList;
import java.util.List;

public abstract class BcSmartspaceCardLoggerUtil {

    public static boolean containsValidTemplateType(BaseTemplateData data) {
        if (data != null) {
            int templateType = data.getTemplateType();
            if (templateType != 0 && templateType != 8) {
                return true;
            }
        }
        return false;
    }

    public static SmartspaceProto.SmartspaceCardDimensionalInfo createDimensionalLoggingInfo(
            BaseTemplateData data) {
        if (data == null
                || data.getPrimaryItem() == null
                || data.getPrimaryItem().getTapAction() == null) {
            return null;
        }

        Bundle extras = data.getPrimaryItem().getTapAction().getExtras();
        List<SmartspaceProto.SmartspaceFeatureDimension> dimensions = new ArrayList<>();

        if (extras != null && !extras.isEmpty()) {
            ArrayList<Integer> ids = extras.getIntegerArrayList("ss_card_dimension_ids");
            ArrayList<Integer> values = extras.getIntegerArrayList("ss_card_dimension_values");

            if (ids != null && values != null && ids.size() == values.size()) {
                for (int i = 0; i < ids.size(); i++) {
                    SmartspaceProto.SmartspaceFeatureDimension dimension =
                            new SmartspaceProto.SmartspaceFeatureDimension();
                    dimension.featureDimensionId = ids.get(i);
                    dimension.featureDimensionValue = values.get(i);
                    dimensions.add(dimension);
                }
            }
        }

        if (dimensions.isEmpty()) {
            return null;
        }

        SmartspaceProto.SmartspaceCardDimensionalInfo info =
                new SmartspaceProto.SmartspaceCardDimensionalInfo();
        info.featureDimensions =
                dimensions.toArray(new SmartspaceProto.SmartspaceFeatureDimension[0]);
        return info;
    }

    public static BcSmartspaceSubcardLoggingInfo createSubcardLoggingInfo(SmartspaceTarget target) {
        if (target.getBaseAction() == null
                || target.getBaseAction().getExtras() == null
                || target.getBaseAction().getExtras().isEmpty()) {
            return null;
        }

        Bundle extras = target.getBaseAction().getExtras();
        int subcardType = extras.getInt("subcardType", -1);
        if (subcardType == -1) {
            return null;
        }

        int instanceId = InstanceId.create(extras.getString("subcardId"));
        int cardTypeId = extras.getInt("subcardType");

        BcSmartspaceCardMetadataLoggingInfo.Builder builder =
                new BcSmartspaceCardMetadataLoggingInfo.Builder();
        builder.mInstanceId = instanceId;
        builder.mCardTypeId = cardTypeId;

        BcSmartspaceCardMetadataLoggingInfo metadata =
                new BcSmartspaceCardMetadataLoggingInfo(builder);
        List<BcSmartspaceCardMetadataLoggingInfo> subcards = new ArrayList<>();
        subcards.add(metadata);

        BcSmartspaceSubcardLoggingInfo subcardInfo = new BcSmartspaceSubcardLoggingInfo();
        subcardInfo.mSubcards = subcards;
        subcardInfo.mClickedSubcardIndex = 0;
        return subcardInfo;
    }

    public static BcSmartspaceSubcardLoggingInfo createSubcardLoggingInfo(BaseTemplateData data) {
        if (data == null) {
            return null;
        }

        List<BcSmartspaceCardMetadataLoggingInfo> subcards = new ArrayList<>();
        createSubcardLoggingInfoHelper(subcards, data.getSubtitleItem());
        createSubcardLoggingInfoHelper(subcards, data.getSubtitleSupplementalItem());
        createSubcardLoggingInfoHelper(subcards, data.getSupplementalLineItem());

        if (subcards.isEmpty()) {
            return null;
        }

        BcSmartspaceSubcardLoggingInfo subcardInfo = new BcSmartspaceSubcardLoggingInfo();
        subcardInfo.mSubcards = subcards;
        subcardInfo.mClickedSubcardIndex = 0;
        return subcardInfo;
    }

    public static void createSubcardLoggingInfoHelper(
            List<BcSmartspaceCardMetadataLoggingInfo> subcards,
            BaseTemplateData.SubItemInfo subItemInfo) {
        if (subItemInfo != null && subItemInfo.getLoggingInfo() != null) {
            BaseTemplateData.SubItemLoggingInfo loggingInfo = subItemInfo.getLoggingInfo();
            BcSmartspaceCardMetadataLoggingInfo.Builder builder =
                    new BcSmartspaceCardMetadataLoggingInfo.Builder();
            builder.mCardTypeId = loggingInfo.getFeatureType();
            builder.mInstanceId = loggingInfo.getInstanceId();
            subcards.add(new BcSmartspaceCardMetadataLoggingInfo(builder));
        }
    }

    public static void tryForcePrimaryFeatureTypeAndInjectWeatherSubcard(
            BcSmartspaceCardLoggingInfo loggingInfo, SmartspaceTarget target) {
        if (loggingInfo.mFeatureType != 1) {
            return;
        }

        loggingInfo.mFeatureType = 39; // 0x27
        loggingInfo.mInstanceId = InstanceId.create("date_card_794317_92634");

        if ("date_card_794317_92634".equals(target.getSmartspaceTargetId())) {
            return;
        }

        if (loggingInfo.mSubcardInfo == null) {
            loggingInfo.mSubcardInfo = new BcSmartspaceSubcardLoggingInfo();
            loggingInfo.mSubcardInfo.mSubcards = new ArrayList<>();
            loggingInfo.mSubcardInfo.mClickedSubcardIndex = 0;
        }

        if (loggingInfo.mSubcardInfo.mSubcards == null) {
            loggingInfo.mSubcardInfo.mSubcards = new ArrayList<>();
        }

        List<BcSmartspaceCardMetadataLoggingInfo> subcards = loggingInfo.mSubcardInfo.mSubcards;
        if (subcards.isEmpty() || subcards.get(0) == null || subcards.get(0).mCardTypeId != 1) {
            BcSmartspaceCardMetadataLoggingInfo.Builder builder =
                    new BcSmartspaceCardMetadataLoggingInfo.Builder();
            builder.mInstanceId = InstanceId.create(target);
            builder.mCardTypeId = 1;
            subcards.add(0, new BcSmartspaceCardMetadataLoggingInfo(builder));

            if (loggingInfo.mSubcardInfo.mClickedSubcardIndex > 0) {
                loggingInfo.mSubcardInfo.mClickedSubcardIndex++;
            }
        }
    }

    public static void tryForcePrimaryFeatureTypeOrUpdateLogInfoFromTemplateData(
            BcSmartspaceCardLoggingInfo loggingInfo, BaseTemplateData data) {
        if (loggingInfo.mFeatureType == 1) {
            loggingInfo.mFeatureType = 39; // 0x27
            loggingInfo.mInstanceId = InstanceId.create("date_card_794317_92634");
        } else if (data != null
                && data.getPrimaryItem() != null
                && data.getPrimaryItem().getLoggingInfo() != null) {
            BaseTemplateData.SubItemLoggingInfo subItemLoggingInfo =
                    data.getPrimaryItem().getLoggingInfo();
            int featureType = subItemLoggingInfo.getFeatureType();
            if (featureType > 0) {
                loggingInfo.mFeatureType = featureType;
            }
            int instanceId = subItemLoggingInfo.getInstanceId();
            if (instanceId > 0) {
                loggingInfo.mInstanceId = instanceId;
            }
        }
    }
}

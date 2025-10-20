package com.google.android.systemui.smartspace.uitemplate;

import android.app.smartspace.uitemplatedata.BaseTemplateData;
import android.app.smartspace.uitemplatedata.CombinedCardsTemplateData;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.systemui.plugins.BcSmartspaceDataPlugin;

import com.google.android.systemui.smartspace.BcSmartspaceCardSecondary;
import com.google.android.systemui.smartspace.BcSmartspaceTemplateDataUtils;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.saulhdev.smartspace.SmartspaceTarget;

import java.util.List;

public class CombinedCardsTemplateCard extends BcSmartspaceCardSecondary {
    public ConstraintLayout mFirstSubCard;
    public ConstraintLayout mSecondSubCard;

    public CombinedCardsTemplateCard(Context context) {
        super(context);
    }

    public CombinedCardsTemplateCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFirstSubCard = findViewById(R.id.first_sub_card_container);
        mSecondSubCard = findViewById(R.id.second_sub_card_container);
    }

    @Override
    public void resetUi() {
        BcSmartspaceTemplateDataUtils.updateVisibility(mFirstSubCard, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondSubCard, View.GONE);
    }

    @Override
    public boolean setSmartspaceActions(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        reset(target.getSmartspaceTargetId());

        BaseTemplateData templateData = target.getTemplateData();
        CombinedCardsTemplateData combinedData = (CombinedCardsTemplateData) templateData;

        if (!BcSmartspaceCardLoggerUtil.containsValidTemplateType(combinedData)) {
            Log.w(
                    "CombinedCardsTemplateCard",
                    "TemplateData is null or empty or invalid template type");
            return false;
        }

        List<BaseTemplateData> cardDataList = combinedData.getCombinedCardDataList();
        if (cardDataList.isEmpty()) {
            Log.w(
                    "CombinedCardsTemplateCard",
                    "TemplateData is null or empty or invalid template type");
            return false;
        }

        BaseTemplateData firstCardData = cardDataList.get(0);
        BaseTemplateData secondCardData = cardDataList.size() > 1 ? cardDataList.get(1) : null;

        boolean firstCardSet =
                setupSubCard(mFirstSubCard, firstCardData, target, eventNotifier, loggingInfo);
        if (firstCardSet
                && (secondCardData == null
                        || setupSubCard(
                                mSecondSubCard,
                                secondCardData,
                                target,
                                eventNotifier,
                                loggingInfo))) {
            return true;
        }
        return false;
    }

    public boolean setupSubCard(
            ViewGroup container,
            BaseTemplateData templateData,
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        if (templateData == null) {
            BcSmartspaceTemplateDataUtils.updateVisibility(container, View.GONE);
            Log.w("CombinedCardsTemplateCard", "Sub-card templateData is null or empty");
            return false;
        }

        Integer subCardResId =
                BcSmartspaceTemplateDataUtils.TEMPLATE_TYPE_TO_SECONDARY_CARD_RES.get(
                        templateData.getTemplateType());
        if (subCardResId == null) {
            BcSmartspaceTemplateDataUtils.updateVisibility(container, View.GONE);
            Log.w("CombinedCardsTemplateCard", "Combined sub-card res is null. Cannot set it up");
            return false;
        }

        BcSmartspaceCardSecondary subCard =
                (BcSmartspaceCardSecondary)
                        LayoutInflater.from(container.getContext())
                                .inflate(subCardResId, container, false);
        SmartspaceTarget subCardTarget =
                new SmartspaceTarget.Builder(
                                target.getSmartspaceTargetId(),
                                target.getComponentName(),
                                target.getUserHandle())
                        .setTemplateData(templateData)
                        .build();

        boolean success = subCard.setSmartspaceActions(subCardTarget, eventNotifier, loggingInfo);
        container.removeAllViews();
        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        getResources()
                                .getDimensionPixelSize(R.dimen.enhanced_smartspace_card_height));
        params.startToStart = 0;
        params.endToEnd = 0;
        params.topToTop = 0;
        params.bottomToBottom = 0;
        BcSmartspaceTemplateDataUtils.updateVisibility(subCard, View.VISIBLE);
        container.addView(subCard, params);
        BcSmartspaceTemplateDataUtils.updateVisibility(container, View.VISIBLE);
        return success;
    }

    @Override
    public void setTextColor(int color) {
        if (mFirstSubCard.getChildCount() > 0) {
            BcSmartspaceCardSecondary firstSubCard =
                    (BcSmartspaceCardSecondary) mFirstSubCard.getChildAt(0);
            firstSubCard.setTextColor(color);
        }
        if (mSecondSubCard.getChildCount() > 0) {
            BcSmartspaceCardSecondary secondSubCard =
                    (BcSmartspaceCardSecondary) mSecondSubCard.getChildAt(0);
            secondSubCard.setTextColor(color);
        }
    }
}

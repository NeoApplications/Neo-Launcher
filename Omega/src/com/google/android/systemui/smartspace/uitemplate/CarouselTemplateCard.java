package com.google.android.systemui.smartspace.uitemplate;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.systemui.smartspace.BcSmartSpaceUtil;
import com.google.android.systemui.smartspace.BcSmartspaceCardSecondary;
import com.google.android.systemui.smartspace.BcSmartspaceTemplateDataUtils;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.saulhdev.smartspace.uitemplatedata.CarouselTemplateData;

import java.util.List;

public class CarouselTemplateCard extends BcSmartspaceCardSecondary {

    public CarouselTemplateCard(Context context) {
        super(context);
    }

    public CarouselTemplateCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ConstraintLayout[] columns = new ConstraintLayout[4];
        for (int i = 0; i < 4; i++) {
            ConstraintLayout column =
                    (ConstraintLayout)
                            ViewGroup.inflate(
                                    getContext(),
                                    R.layout.smartspace_carousel_column_template_card,
                                    null);
            column.setId(View.generateViewId());
            columns[i] = column;
        }

        for (int i = 0; i < 4; i++) {
            ConstraintLayout.LayoutParams params =
                    new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT, 0);
            ConstraintLayout prevColumn = i > 0 ? columns[i - 1] : null;
            ConstraintLayout nextColumn = i < 3 ? columns[i + 1] : null;

            if (i == 0) {
                params.startToStart = 0;
                params.horizontalChainStyle = 1;
            } else {
                params.startToEnd = prevColumn.getId();
            }

            if (i == 3) {
                params.endToEnd = 0;
            } else {
                params.endToStart = nextColumn.getId();
            }

            params.topToTop = 0;
            params.bottomToBottom = 0;
            addView(columns[i], params);
        }
    }

    @Override
    public void resetUi() {
        for (int i = 0; i < getChildCount(); i++) {
            View column = getChildAt(i);
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    column.findViewById(R.id.upper_text), View.GONE);
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    column.findViewById(R.id.icon), View.GONE);
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    column.findViewById(R.id.lower_text), View.GONE);
        }
    }

    @Override
    public boolean setSmartspaceActions(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        BaseTemplateData templateData = target.getTemplateData();
        CarouselTemplateData carouselData = (CarouselTemplateData) templateData;

        if (!BcSmartspaceCardLoggerUtil.containsValidTemplateType(carouselData)) {
            Log.w(
                    "CarouselTemplateCard",
                    "CarouselTemplateData is null or has no CarouselItem or invalid template type");
            return false;
        }

        List<CarouselTemplateData.CarouselItem> carouselItems = carouselData.getCarouselItems();
        if (carouselItems == null) {
            Log.w(
                    "CarouselTemplateCard",
                    "CarouselTemplateData is null or has no CarouselItem or invalid template type");
            return false;
        }

        long validItemsCount =
                carouselItems.stream()
                        .filter(
                                item ->
                                        item.getImage() != null
                                                && item.getLowerText() != null
                                                && item.getUpperText() != null)
                        .count();
        int validItems = (int) validItemsCount;

        if (validItems < 4) {
            Log.w("CarouselTemplateCard", "Hiding " + (4 - validItems) + " incomplete column(s).");
            for (int i = 0; i < 4; i++) {
                View column = getChildAt(i);
                BcSmartspaceTemplateDataUtils.updateVisibility(
                        column, i <= (3 - (4 - validItems)) ? View.VISIBLE : View.GONE);
            }
            ConstraintLayout firstColumn = (ConstraintLayout) getChildAt(0);
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) firstColumn.getLayoutParams();
            params.horizontalChainStyle = (4 - validItems) == 0 ? 1 : 0;
        }

        for (int i = 0; i < validItems; i++) {
            View column = getChildAt(i);
            TextView upperText = column.findViewById(R.id.upper_text);
            ImageView icon = column.findViewById(R.id.icon);
            TextView lowerText = column.findViewById(R.id.lower_text);

            CarouselTemplateData.CarouselItem item = carouselItems.get(i);
            BcSmartspaceTemplateDataUtils.setText(upperText, item.getUpperText());
            BcSmartspaceTemplateDataUtils.updateVisibility(upperText, View.VISIBLE);
            BcSmartspaceTemplateDataUtils.setIcon(icon, item.getImage());
            BcSmartspaceTemplateDataUtils.updateVisibility(icon, View.VISIBLE);
            BcSmartspaceTemplateDataUtils.setText(lowerText, item.getLowerText());
            BcSmartspaceTemplateDataUtils.updateVisibility(lowerText, View.VISIBLE);
        }

        if (carouselData.getCarouselAction() != null) {
            BcSmartSpaceUtil.setOnClickListener(
                    this,
                    target,
                    carouselData.getCarouselAction(),
                    eventNotifier,
                    "CarouselTemplateCard",
                    loggingInfo,
                    0);
        }

        for (CarouselTemplateData.CarouselItem item : carouselItems) {
            if (item.getTapAction() != null) {
                BcSmartSpaceUtil.setOnClickListener(
                        this,
                        target,
                        item.getTapAction(),
                        eventNotifier,
                        "CarouselTemplateCard",
                        loggingInfo,
                        0);
            }
        }

        return true;
    }

    @Override
    public void setTextColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            View column = getChildAt(i);
            TextView upperText = column.findViewById(R.id.upper_text);
            upperText.setTextColor(color);
            TextView lowerText = column.findViewById(R.id.lower_text);
            lowerText.setTextColor(color);
        }
    }
}

package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.app.smartspace.SmartspaceUtils;
import android.app.smartspace.uitemplatedata.BaseTemplateData;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import androidx.viewpager.widget.PagerAdapter;

import com.android.internal.graphics.ColorUtils;
import com.android.launcher3.icons.GraphicsUtils;
import com.android.systemui.plugins.BcSmartspaceConfigPlugin;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.logging.BcSmartspaceSubcardLoggingInfo;
import com.google.android.systemui.smartspace.uitemplate.BaseTemplateCard;
import com.statix.android.systemui.res.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    public static class Companion {
        public int getBaseLegacyCardRes(int featureType) {
            switch (featureType) {
                case -2:
                case -1:
                case 1:
                case 2:
                case 3:
                case 4:
                case 6:
                case 9:
                case 10:
                case 18:
                case 20:
                case 30:
                case 13:
                case 14:
                case 15:
                    return R.layout.smartspace_card;
                default:
                    return R.layout.smartspace_card;
            }
        }

        public boolean useRecycledViewForAction(
                SmartspaceAction newAction, SmartspaceAction recycledAction) {
            if (newAction == null && recycledAction == null) {
                return true;
            }
            if (newAction == null || recycledAction == null) {
                return false;
            }
            Bundle newExtras = newAction.getExtras();
            Bundle recycledExtras = recycledAction.getExtras();
            if (newExtras == null && recycledExtras == null) {
                return true;
            }
            if (newExtras == null || recycledExtras == null) {
                return false;
            }
            Set<String> newKeys = newExtras.keySet();
            Set<String> recycledKeys = recycledExtras.keySet();
            return Objects.equals(newKeys, recycledKeys);
        }

        public boolean useRecycledViewForActionsList(
                List<SmartspaceAction> newActions, List<SmartspaceAction> recycledActions) {
            if (newActions == null && recycledActions == null) {
                return true;
            }
            if (newActions == null
                    || recycledActions == null
                    || newActions.size() != recycledActions.size()) {
                return false;
            }
            for (int i = 0; i < newActions.size(); i++) {
                if (!useRecycledViewForAction(newActions.get(i), recycledActions.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public boolean useRecycledViewForNewTarget(
                SmartspaceTarget newTarget, SmartspaceTarget recycledTarget) {
            if (recycledTarget == null) {
                return false;
            }
            if (!newTarget.getSmartspaceTargetId().equals(recycledTarget.getSmartspaceTargetId())) {
                return false;
            }
            if (!useRecycledViewForAction(
                    newTarget.getHeaderAction(), recycledTarget.getHeaderAction())) {
                return false;
            }
            if (!useRecycledViewForAction(
                    newTarget.getBaseAction(), recycledTarget.getBaseAction())) {
                return false;
            }
            if (!useRecycledViewForActionsList(
                    newTarget.getActionChips(), recycledTarget.getActionChips())) {
                return false;
            }
            if (!useRecycledViewForActionsList(
                    newTarget.getIconGrid(), recycledTarget.getIconGrid())) {
                return false;
            }
            BaseTemplateData newTemplateData = newTarget.getTemplateData();
            BaseTemplateData recycledTemplateData = recycledTarget.getTemplateData();
            if (newTemplateData == null && recycledTemplateData == null) {
                return true;
            }
            return newTemplateData != null
                    && recycledTemplateData != null
                    && newTemplateData.equals(recycledTemplateData);
        }
    }

    public static final Companion Companion = new Companion();
    private final List<SmartspaceTarget> _aodTargets = new ArrayList<>();
    private final List<SmartspaceTarget> _lockscreenTargets = new ArrayList<>();
    private Handler bgHandler;
    private BcSmartspaceConfigPlugin configProvider;
    private int currentTextColor;
    private BcSmartspaceDataPlugin dataProvider;
    private float dozeAmount;
    private final int dozeColor = -1;
    private final LazyServerFlagLoader enableCardRecycling =
            new LazyServerFlagLoader("enable_card_recycling");
    private final LazyServerFlagLoader enableReducedCardRecycling =
            new LazyServerFlagLoader("enable_reduced_card_recycling");
    private boolean hasAodLockscreenTransition;
    private boolean hasDifferentTargets;
    private boolean keyguardBypassEnabled;
    private final List<SmartspaceTarget> mediaTargets = new ArrayList<>();
    private Integer nonRemoteViewsHorizontalPadding;
    private float previousDozeAmount;
    private int primaryTextColor;
    private final SparseArray<BaseTemplateCard> recycledCards = new SparseArray<>();
    private final SparseArray<BcSmartspaceCard> recycledLegacyCards = new SparseArray<>();
    private final SparseArray<BcSmartspaceRemoteViewsCard> recycledRemoteViewsCards =
            new SparseArray<>();
    private final BcSmartspaceView root;
    private List<SmartspaceTarget> smartspaceTargets = new ArrayList<>();
    private BcSmartspaceDataPlugin.TimeChangedDelegate timeChangedDelegate;
    private TransitionType transitioningTo = TransitionType.NOT_IN_TRANSITION;
    private String uiSurface;
    private final SparseArray<ViewHolder> viewHolders = new SparseArray<>();

    public CardPagerAdapter(BcSmartspaceView root, BcSmartspaceConfigPlugin configProvider) {
        this.root = root;
        this.configProvider = configProvider;
        int color = GraphicsUtils.getAttrColor(root.getContext(), android.R.attr.textColorPrimary);
        this.primaryTextColor = color;
        this.currentTextColor = color;
    }

    public void addDefaultDateCardIfEmpty(List<SmartspaceTarget> targets) {
        if (targets.isEmpty()) {
            SmartspaceTarget target =
                    new SmartspaceTarget.Builder(
                                    "date_card_794317_92634",
                                    new ComponentName(root.getContext(), CardPagerAdapter.class),
                                    root.getContext().getUser())
                            .setFeatureType(1)
                            .setTemplateData(new BaseTemplateData.Builder(1).build())
                            .build();
            targets.add(target);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewHolder holder = (ViewHolder) object;
        if (holder.legacyCard != null && holder.target != null && enableCardRecycling.get()) {
            recycledLegacyCards.put(
                    BcSmartSpaceUtil.getFeatureType(holder.target), holder.legacyCard);
        }
        if (holder.legacyCard != null) {
            container.removeView(holder.legacyCard);
        }
        if (holder.card != null && holder.target != null && enableCardRecycling.get()) {
            recycledCards.put(holder.target.getFeatureType(), holder.card);
        }
        if (holder.card != null) {
            container.removeView(holder.card);
        }
        if (holder.remoteViewsCard != null) {
            if (enableCardRecycling.get()) {
                Log.d("SsCardPagerAdapter", "[rmv] Caching RemoteViews card");
                recycledRemoteViewsCards.put(
                        BcSmartSpaceUtil.getFeatureType(holder.target), holder.remoteViewsCard);
            }
            Log.d("SsCardPagerAdapter", "[rmv] Removing RemoteViews card");
            container.removeView(holder.remoteViewsCard);
        }
        if (viewHolders.get(position) == holder) {
            viewHolders.remove(position);
        }
    }

    @Override
    public SmartspaceCard getCardAtPosition(int position) {
        ViewHolder holder = viewHolders.get(position);
        if (holder != null) {
            if (holder.card != null) {
                return holder.card;
            }
            if (holder.legacyCard != null) {
                return holder.legacyCard;
            }
            return holder.remoteViewsCard;
        }
        return null;
    }

    @Override
    public int getCount() {
        return smartspaceTargets.size();
    }

    @Override
    public float getDozeAmount() {
        return dozeAmount;
    }

    @Override
    public boolean getHasAodLockscreenTransition() {
        return hasAodLockscreenTransition;
    }

    @Override
    public boolean getHasDifferentTargets() {
        return hasDifferentTargets;
    }

    @Override
    public BcSmartspaceCard getLegacyCardAtPosition(int position) {
        ViewHolder holder = viewHolders.get(position);
        return holder != null ? holder.legacyCard : null;
    }

    @Override
    public List<SmartspaceTarget> getLockscreenTargets() {
        if (!mediaTargets.isEmpty() && keyguardBypassEnabled) {
            return mediaTargets;
        }
        return _lockscreenTargets;
    }

    @Override
    public BcSmartspaceRemoteViewsCard getRemoteViewsCardAtPosition(int position) {
        ViewHolder holder = viewHolders.get(position);
        return holder != null ? holder.remoteViewsCard : null;
    }

    @Override
    public List<SmartspaceTarget> getSmartspaceTargets() {
        return smartspaceTargets;
    }

    @Override
    public SmartspaceTarget getTargetAtPosition(int position) {
        if (!smartspaceTargets.isEmpty() && position >= 0 && position < smartspaceTargets.size()) {
            return smartspaceTargets.get(position);
        }
        return null;
    }

    @Override
    public BaseTemplateCard getTemplateCardAtPosition(int position) {
        ViewHolder holder = viewHolders.get(position);
        return holder != null ? holder.card : null;
    }

    @Override
    public String getUiSurface() {
        return uiSurface;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SmartspaceTarget target = smartspaceTargets.get(position);
        RemoteViews remoteViews = target.getRemoteViews();
        Log.i("SsCardPagerAdapter", "[rmv] Rendering flag - enabled: true rmv: " + remoteViews);
        View cardView = null;
        if (remoteViews != null) {
            Log.i(
                    "SsCardPagerAdapter",
                    "[rmv] Use RemoteViews for the feature: " + target.getFeatureType());
            BcSmartspaceRemoteViewsCard remoteViewsCard =
                    enableCardRecycling.get()
                            ? recycledRemoteViewsCards.removeReturnOld(
                                    BcSmartSpaceUtil.getFeatureType(target))
                            : null;
            if (remoteViewsCard == null) {
                remoteViewsCard = new BcSmartspaceRemoteViewsCard(container.getContext());
                remoteViewsCard.mUiSurface = uiSurface;
                remoteViewsCard.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
            }
            cardView = remoteViewsCard;
            ViewHolder holder = new ViewHolder(position, null, target, null, remoteViewsCard);
            container.addView(cardView);
            viewHolders.put(position, holder);
            onBindViewHolder(holder);
            return holder;
        } else if (BcSmartspaceCardLoggerUtil.containsValidTemplateType(target.getTemplateData())) {
            Log.i(
                    "SsCardPagerAdapter",
                    "Use UI template for the feature: " + target.getFeatureType());
            BaseTemplateCard templateCard =
                    enableCardRecycling.get()
                            ? recycledCards.removeReturnOld(target.getFeatureType())
                            : null;
            if (templateCard != null
                    && enableReducedCardRecycling.get()
                    && !Companion.useRecycledViewForNewTarget(target, templateCard.mTarget)) {
                templateCard = null;
            }
            if (templateCard == null) {
                // STX comment-out // Always use smartspace_base_template_card to avoid date view in
                // secondary cards
                // STX comment-out int layoutRes = R.layout.smartspace_base_template_card;
                BaseTemplateData templateData = target.getTemplateData();
                BaseTemplateData.SubItemInfo primaryItem =
                        templateData != null ? templateData.getPrimaryItem() : null; // STX edit
                int layoutRes =
                        (primaryItem == null
                                        || (SmartspaceUtils.isEmpty(primaryItem.getText())
                                                && primaryItem.getIcon() == null))
                                ? R.layout.smartspace_base_template_card_with_date
                                : R.layout.smartspace_base_template_card; // STX edit
                templateCard =
                        (BaseTemplateCard)
                                LayoutInflater.from(container.getContext())
                                        .inflate(layoutRes, container, false);
                templateCard.mUiSurface = uiSurface;
                if (templateCard.mDateView != null && "lockscreen".equals(uiSurface)) {
                    if (!templateCard.mDateView.isAttachedToWindow()) {
                        templateCard.mDateView.mUpdatesOnAod = true;
                    } else {
                        throw new IllegalStateException(
                                "Must call before attaching view to window.");
                    }
                }
                if (nonRemoteViewsHorizontalPadding != null) {
                    templateCard.setPaddingRelative(
                            nonRemoteViewsHorizontalPadding,
                            templateCard.getPaddingTop(),
                            nonRemoteViewsHorizontalPadding,
                            templateCard.getPaddingBottom());
                }
                templateCard.mBgHandler = bgHandler;
                if (templateCard.mDateView != null) {
                    templateCard.mDateView.mBgHandler = bgHandler;
                    if (!templateCard.mDateView.isAttachedToWindow()) {
                        templateCard.mDateView.mTimeChangedDelegate = timeChangedDelegate;
                    } else {
                        throw new IllegalStateException(
                                "Must call before attaching view to window.");
                    }
                }
                Map<Integer, Integer> templateTypeToSecondaryCardRes =
                        BcSmartspaceTemplateDataUtils.TEMPLATE_TYPE_TO_SECONDARY_CARD_RES;
                Integer secondaryRes =
                        templateTypeToSecondaryCardRes.get(
                                target.getTemplateData().getTemplateType());
                if (secondaryRes != null) {
                    BcSmartspaceCardSecondary secondaryCard =
                            (BcSmartspaceCardSecondary)
                                    LayoutInflater.from(container.getContext())
                                            .inflate(secondaryRes, container, false);
                    Log.i("SsCardPagerAdapter", "Secondary card is found");
                    templateCard.setSecondaryCard(secondaryCard);
                }
            }
            cardView = templateCard;
            ViewHolder holder = new ViewHolder(position, null, target, templateCard, null);
            container.addView(cardView);
            viewHolders.put(position, holder);
            onBindViewHolder(holder);
            return holder;
        } else {
            BcSmartspaceCard legacyCard =
                    enableCardRecycling.get()
                            ? recycledLegacyCards.removeReturnOld(
                                    BcSmartSpaceUtil.getFeatureType(target))
                            : null;
            if (legacyCard != null
                    && enableReducedCardRecycling.get()
                    && !Companion.useRecycledViewForNewTarget(target, legacyCard.mTarget)) {
                legacyCard = null;
            }
            if (legacyCard == null) {
                int featureType = BcSmartSpaceUtil.getFeatureType(target);
                int layoutRes = Companion.getBaseLegacyCardRes(featureType);
                if (layoutRes == 0) {
                    Log.w(
                            "SsCardPagerAdapter",
                            "No legacy card can be created for feature type: " + featureType);
                } else {
                    legacyCard =
                            (BcSmartspaceCard)
                                    LayoutInflater.from(container.getContext())
                                            .inflate(layoutRes, container, false);
                    legacyCard.mUiSurface = uiSurface;
                    if (nonRemoteViewsHorizontalPadding != null) {
                        legacyCard.setPaddingRelative(
                                nonRemoteViewsHorizontalPadding,
                                legacyCard.getPaddingTop(),
                                nonRemoteViewsHorizontalPadding,
                                legacyCard.getPaddingBottom());
                    }
                    Integer secondaryRes =
                            (Integer)
                                    BcSmartSpaceUtil.FEATURE_TYPE_TO_SECONDARY_CARD_RESOURCE_MAP
                                            .get(featureType);
                    if (secondaryRes != null) {
                        BcSmartspaceCardSecondary secondaryCard =
                                (BcSmartspaceCardSecondary)
                                        LayoutInflater.from(container.getContext())
                                                .inflate(secondaryRes, container, false);
                        legacyCard.setSecondaryCard(secondaryCard);
                    }
                }
            }
            cardView = legacyCard;
            ViewHolder holder = new ViewHolder(position, legacyCard, target, null, null);
            if (cardView != null) {
                container.addView(cardView);
            }
            viewHolders.put(position, holder);
            onBindViewHolder(holder);
            return holder;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewHolder holder = (ViewHolder) object;
        return view == holder.legacyCard || view == holder.card || view == holder.remoteViewsCard;
    }

    @Override
    public int getItemPosition(Object object) {
        ViewHolder holder = (ViewHolder) object;
        SmartspaceTarget currentTarget = getTargetAtPosition(holder.position);
        if (holder.target == currentTarget) {
            return POSITION_UNCHANGED;
        }
        if (currentTarget != null
                && BcSmartSpaceUtil.getFeatureType(currentTarget)
                        == BcSmartSpaceUtil.getFeatureType(holder.target)
                && currentTarget
                        .getSmartspaceTargetId()
                        .equals(holder.target.getSmartspaceTargetId())) {
            holder.setTarget(currentTarget);
            onBindViewHolder(holder);
            return POSITION_UNCHANGED;
        }
        return POSITION_NONE;
    }

    public void onBindViewHolder(ViewHolder holder) {
        SmartspaceTarget target = smartspaceTargets.get(holder.position);
        boolean hasValidTemplate =
                BcSmartspaceCardLoggerUtil.containsValidTemplateType(target.getTemplateData());
        BcSmartspaceCardLoggingInfo.Builder loggingInfoBuilder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setInstanceId(InstanceId.create(target))
                        .setFeatureType(target.getFeatureType())
                        .setDisplaySurface(
                                BcSmartSpaceUtil.getLoggingDisplaySurface(uiSurface, dozeAmount))
                        .setRank(holder.position)
                        .setCardinality(smartspaceTargets.size())
                        .setUid(-1);
        BcSmartspaceSubcardLoggingInfo subcardInfo =
                hasValidTemplate
                        ? BcSmartspaceCardLoggerUtil.createSubcardLoggingInfo(
                                target.getTemplateData())
                        : BcSmartspaceCardLoggerUtil.createSubcardLoggingInfo(target);
        loggingInfoBuilder.setSubcardInfo(subcardInfo);
        loggingInfoBuilder.setDimensionalInfo(
                BcSmartspaceCardLoggerUtil.createDimensionalLoggingInfo(target.getTemplateData()));
        BcSmartspaceCardLoggingInfo loggingInfo =
                new BcSmartspaceCardLoggingInfo(loggingInfoBuilder);
        if (target.getRemoteViews() != null) {
            if (holder.remoteViewsCard == null) {
                Log.w("SsCardPagerAdapter", "[rmv] No RemoteViews card view can be binded");
                return;
            }
            Log.d("SsCardPagerAdapter", "[rmv] Refreshing RemoteViews card");
            BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                    dataProvider != null
                            ? event -> dataProvider.notifySmartspaceEvent(event)
                            : null;
            holder.remoteViewsCard.bindData(
                    target, notifier, loggingInfo, smartspaceTargets.size() > 1);
        } else if (hasValidTemplate) {
            if (holder.card == null) {
                Log.w("SsCardPagerAdapter", "No ui-template card view can be binded");
                return;
            }
            if (target.getTemplateData() == null) {
                throw new IllegalStateException("Required value was null.");
            }
            BcSmartspaceCardLoggerUtil.tryForcePrimaryFeatureTypeOrUpdateLogInfoFromTemplateData(
                    loggingInfo, target.getTemplateData());
            BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                    dataProvider != null
                            ? event -> dataProvider.notifySmartspaceEvent(event)
                            : null;
            holder.card.bindData(target, notifier, loggingInfo, smartspaceTargets.size() > 1);
            holder.card.setPrimaryTextColor(currentTextColor);
            holder.card.setDozeAmount(dozeAmount);
        } else {
            if (holder.legacyCard == null) {
                Log.w("SsCardPagerAdapter", "No legacy card view can be binded");
                return;
            }
            BcSmartspaceCardLoggerUtil.tryForcePrimaryFeatureTypeAndInjectWeatherSubcard(
                    loggingInfo, target);
            BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                    dataProvider != null
                            ? event -> dataProvider.notifySmartspaceEvent(event)
                            : null;
            holder.legacyCard.bindData(target, notifier, loggingInfo, smartspaceTargets.size() > 1);
            holder.legacyCard.setPrimaryTextColor(currentTextColor);
            holder.legacyCard.setDozeAmount(dozeAmount);
        }
    }

    @Override
    public void setBgHandler(Handler handler) {
        bgHandler = handler;
    }

    @Override
    public void setConfigProvider(BcSmartspaceConfigPlugin configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public void setDataProvider(BcSmartspaceDataPlugin dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void setDozeAmount(float dozeAmount) {
        this.dozeAmount = dozeAmount;
        TransitionType newTransition =
                previousDozeAmount > dozeAmount
                        ? TransitionType.TO_LOCKSCREEN
                        : previousDozeAmount < dozeAmount
                                ? TransitionType.TO_AOD
                                : TransitionType.NOT_IN_TRANSITION;
        transitioningTo = newTransition;
        previousDozeAmount = dozeAmount;
        updateTargetVisibility();
        updateCurrentTextColor();
    }

    @Override
    public void setKeyguardBypassEnabled(boolean enabled) {
        keyguardBypassEnabled = enabled;
        updateTargetVisibility();
    }

    @Override
    public void setMediaTarget(SmartspaceTarget target) {
        mediaTargets.clear();
        if (target != null) {
            mediaTargets.add(target);
        }
        updateTargetVisibility();
        notifyDataSetChanged();
    }

    @Override
    public void setNonRemoteViewsHorizontalPadding(Integer padding) {
        nonRemoteViewsHorizontalPadding = padding;
        for (int i = 0; i < viewHolders.size(); i++) {
            int key = viewHolders.keyAt(i);
            BcSmartspaceCard legacyCard = getLegacyCardAtPosition(key);
            if (legacyCard != null && padding != null) {
                legacyCard.setPaddingRelative(
                        padding,
                        legacyCard.getPaddingTop(),
                        padding,
                        legacyCard.getPaddingBottom());
            }
            BaseTemplateCard templateCard = getTemplateCardAtPosition(key);
            if (templateCard != null && padding != null) {
                templateCard.setPaddingRelative(
                        padding,
                        templateCard.getPaddingTop(),
                        padding,
                        templateCard.getPaddingBottom());
            }
        }
    }

    @Override
    public void setPrimaryTextColor(int color) {
        primaryTextColor = color;
        updateCurrentTextColor();
    }

    @Override
    public void setScreenOn(boolean screenOn) {
        for (int i = 0; i < viewHolders.size(); i++) {
            ViewHolder holder = viewHolders.valueAt(i);
            if (holder != null && holder.card != null) {
                holder.card.setScreenOn(screenOn);
            }
        }
    }

    @Override
    public void setTargets(List<SmartspaceTarget> targets) {
        _aodTargets.clear();
        _lockscreenTargets.clear();
        hasDifferentTargets = false;
        for (SmartspaceTarget target : targets) {
            if (target.getFeatureType() == 34) {
                continue;
            }
            int screenExtra =
                    target.getBaseAction() != null && target.getBaseAction().getExtras() != null
                            ? target.getBaseAction().getExtras().getInt("SCREEN_EXTRA", 3)
                            : 3;
            if ((screenExtra & 2) != 0) {
                _aodTargets.add(target);
            }
            if ((screenExtra & 1) != 0) {
                _lockscreenTargets.add(target);
            }
            if (screenExtra != 3) {
                hasDifferentTargets = true;
            }
        }
        if (!configProvider.isDefaultDateWeatherDisabled()) {
            addDefaultDateCardIfEmpty(_aodTargets);
            addDefaultDateCardIfEmpty(_lockscreenTargets);
        }
        updateTargetVisibility();
        notifyDataSetChanged();
    }

    @Override
    public void setTimeChangedDelegate(BcSmartspaceDataPlugin.TimeChangedDelegate delegate) {
        timeChangedDelegate = delegate;
    }

    @Override
    public void setUiSurface(String uiSurface) {
        this.uiSurface = uiSurface;
    }

    public void updateCurrentTextColor() {
        currentTextColor = ColorUtils.blendARGB(primaryTextColor, dozeColor, dozeAmount);
        for (int i = 0; i < viewHolders.size(); i++) {
            ViewHolder holder = viewHolders.valueAt(i);
            if (holder != null) {
                if (holder.legacyCard != null) {
                    holder.legacyCard.setPrimaryTextColor(currentTextColor);
                    holder.legacyCard.setDozeAmount(dozeAmount);
                }
                if (holder.card != null) {
                    holder.card.setPrimaryTextColor(currentTextColor);
                    holder.card.setDozeAmount(dozeAmount);
                }
            }
        }
    }

    public void updateTargetVisibility() {
        List<SmartspaceTarget> targetList =
                !mediaTargets.isEmpty()
                        ? mediaTargets
                        : hasDifferentTargets ? _aodTargets : getLockscreenTargets();
        List<SmartspaceTarget> lockscreenTargets = getLockscreenTargets();
        boolean shouldUpdate =
                smartspaceTargets != targetList
                        && (dozeAmount == 1f
                                || (dozeAmount >= 0.36f
                                        && transitioningTo == TransitionType.TO_AOD));
        boolean shouldUpdateLockscreen =
                smartspaceTargets != lockscreenTargets
                        && (dozeAmount == 0f
                                || (1f - dozeAmount >= 0.36f
                                        && transitioningTo == TransitionType.TO_LOCKSCREEN));
        if (shouldUpdate || shouldUpdateLockscreen) {
            smartspaceTargets = shouldUpdate ? targetList : lockscreenTargets;
            notifyDataSetChanged();
        }
        hasAodLockscreenTransition = targetList != lockscreenTargets;
        if (configProvider.isDefaultDateWeatherDisabled() && !"home".equals(uiSurface)) {
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    root, smartspaceTargets.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    public enum TransitionType {
        NOT_IN_TRANSITION,
        TO_LOCKSCREEN,
        TO_AOD
    }

    public static class ViewHolder {
        public final BcSmartspaceCard legacyCard;
        public SmartspaceTarget target;
        public final BaseTemplateCard card;
        public final BcSmartspaceRemoteViewsCard remoteViewsCard;
        public final int position;

        public ViewHolder(
                int position,
                BcSmartspaceCard legacyCard,
                SmartspaceTarget target,
                BaseTemplateCard card,
                BcSmartspaceRemoteViewsCard remoteViewsCard) {
            this.position = position;
            this.legacyCard = legacyCard;
            this.target = target;
            this.card = card;
            this.remoteViewsCard = remoteViewsCard;
        }

        public void setTarget(SmartspaceTarget target) {
            this.target = target;
        }
    }
}

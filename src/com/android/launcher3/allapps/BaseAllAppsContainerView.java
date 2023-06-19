/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;

import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_QUIET_MODE_ENABLED;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DeviceProfile.OnDeviceProfileChangeListener;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.Insettable;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.search.SearchAdapterProvider;
import com.android.launcher3.keyboard.FocusedItemDecorator;
import com.android.launcher3.model.StringCache;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.views.BaseDragLayer;
import com.android.launcher3.views.RecyclerViewFastScroller;
import com.android.launcher3.views.ScrimView;
import com.android.launcher3.views.SpringRelativeLayout;
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip.OnActivePageChangedListener;
import com.saggitt.omega.allapps.AllAppsPages;
import com.saggitt.omega.allapps.AllAppsPagesController;
import com.saggitt.omega.allapps.AllAppsTabsLayout;
import com.saggitt.omega.allapps.AllAppsTabs;
import com.saggitt.omega.allapps.AllAppsTabsController;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base all apps view container.
 *
 * @param <T> Type of context inflating all apps.
 */
public abstract class BaseAllAppsContainerView<T extends BaseDraggingActivity> extends SpringRelativeLayout implements DragSource, Insettable,
        OnDeviceProfileChangeListener, OnActivePageChangedListener,
        ScrimView.ScrimDrawingController {

    protected static final String BUNDLE_KEY_CURRENT_PAGE = "launcher.allapps.current_page";

    public static final float PULL_MULTIPLIER = .02f;
    public static final float FLING_VELOCITY_MULTIPLIER = 1200f;

    private final Paint mHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mInsets = new Rect();

    /**
     * Context of an activity or window that is inflating this container.
     */
    protected final T mActivityContext;
    protected AdapterHolder[] mAH;
    protected final Predicate<ItemInfo> mPersonalMatcher = ItemInfoMatcher.ofUser(
            Process.myUserHandle());
    private final SearchAdapterProvider<?> mMainAdapterProvider;
    private final AllAppsStore mAllAppsStore = new AllAppsStore();

    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    updateHeaderScroll(((AllAppsRecyclerView) recyclerView).getCurrentScrollY());
                }
            };
    private final WorkProfileManager mWorkManager;

    private final Paint mNavBarScrimPaint;
    private int mNavBarScrimHeight = 0;

    private AllAppsPagedView mViewPager;
    private SearchRecyclerView mSearchRecyclerView;

    protected FloatingHeaderView mHeader;
    private View mBottomSheetBackground;
    private View mBottomSheetHandleArea;

    protected boolean mUsingTabs;
    private boolean mHasWorkApps;
    private WorkModeSwitch mWorkModeSwitch;

    protected RecyclerViewFastScroller mTouchHandler;
    protected final Point mFastScrollerOffset = new Point();

    private final int mScrimColor;
    private final int mHeaderProtectionColor;
    protected final float mHeaderThreshold;
    private int mHeaderBottomAdjustment;
    private ScrimView mScrimView;
    private int mHeaderColor;
    private int mTabsProtectionAlpha;

    private final AllAppsTabsController mTabsController;
    private final AllAppsPagesController mPagesController;

    protected BaseAllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivityContext = ActivityContext.lookupContext(context);
        mMainAdapterProvider = createMainAdapterProvider();

        mScrimColor = Themes.getAttrColor(context, R.attr.allAppsScrimColor);
        mHeaderThreshold = getResources().getDimensionPixelSize(
                R.dimen.dynamic_grid_cell_border_spacing);
        mHeaderBottomAdjustment = getResources().getDimensionPixelSize(
                R.dimen.all_apps_header_bottom_adjustment);
        mHeaderProtectionColor = Themes.getAttrColor(context, R.attr.allappsHeaderProtectionColor);

        mAllAppsStore.addUpdateListener(this::onAppsUpdated);
        mActivityContext.addOnDeviceProfileChangeListener(this);

        AllAppsTabs allAppsTabs = new AllAppsTabs(context);
        mTabsController = new AllAppsTabsController(allAppsTabs, this);

        AllAppsPages allAppsPages = new AllAppsPages(context);
        mPagesController = new AllAppsPagesController(allAppsPages, this);
        createHolders();

        mWorkManager = new WorkProfileManager(
                mActivityContext.getSystemService(UserManager.class),
                this,
                Utilities.getPrefs(mActivityContext), mActivityContext.getDeviceProfile(), () -> {
            for (AdapterHolder holder : mAH) {
                if (holder.mType == AdapterHolder.TYPE_WORK) {
                    holder.mAppsList.updateAdapterItems();
                }
            }
        });

        mNavBarScrimPaint = new Paint();
        mNavBarScrimPaint.setColor(Themes.getAttrColor(context, R.attr.allAppsNavBarScrimColor));
    }

    /**
     * Creates the adapter provider for the main section.
     */
    protected abstract SearchAdapterProvider<?> createMainAdapterProvider();

    /**
     * The adapter provider for the main section.
     */
    public final SearchAdapterProvider<?> getMainAdapterProvider() {
        return mMainAdapterProvider;
    }

    private boolean isPagedView() {
        return false; // TODO restore drawer layout pref
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        try {
            // Many slice view id is not properly assigned, and hence throws null
            // pointer exception in the underneath method. Catching the exception
            // simply doesn't restore these slice views. This doesn't have any
            // user visible effect because because we query them again.
            super.dispatchRestoreInstanceState(sparseArray);
        } catch (Exception e) {
            Log.e("AllAppsContainerView", "restoreInstanceState viewId = 0", e);
        }

        Bundle state = (Bundle) sparseArray.get(R.id.work_tab_state_id, null);
        if (state != null) {
            int currentPage = state.getInt(BUNDLE_KEY_CURRENT_PAGE, 0);
            if (currentPage == AdapterHolder.TYPE_WORK && mViewPager != null) {
                mViewPager.setCurrentPage(currentPage);
                rebindAdapters();
            } else {
                reset(true);
            }
        }

    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
        Bundle state = new Bundle();
        state.putInt(BUNDLE_KEY_CURRENT_PAGE, getCurrentPage());
        container.put(R.id.work_tab_state_id, state);
    }

    /**
     * Sets the long click listener for icons
     */
    public void setOnIconLongClickListener(OnLongClickListener listener) {
        for (AdapterHolder holder : mAH) {
            holder.mAdapter.setOnIconLongClickListener(listener);
        }
    }

    private void createHolders() {
        mAH = isPagedView() ? mPagesController.createHolders() : mTabsController.createHolders();
    }

    public AllAppsStore getAppsStore() {
        return mAllAppsStore;
    }

    public WorkProfileManager getWorkManager() {
        return mWorkManager;
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
        for (AdapterHolder holder : mAH) {
            holder.mAdapter.setAppsPerRow(dp.numShownAllAppsColumns);
            if (holder.mRecyclerView != null) {
                // Remove all views and clear the pool, while keeping the data same. After this
                // call, all the viewHolders will be recreated.
                holder.mRecyclerView.swapAdapter(holder.mRecyclerView.getAdapter(), true);
                holder.mRecyclerView.getRecycledViewPool().clear();
            }
        }
        updateBackground(dp);
    }

    protected void updateBackground(DeviceProfile deviceProfile) {
        mBottomSheetBackground.setVisibility(deviceProfile.isTablet ? View.VISIBLE : View.GONE);
    }

    private void onAppsUpdated() {
        mHasWorkApps = Stream.of(mAllAppsStore.getApps()).anyMatch(mWorkManager.getMatcher());
        if (!isSearching()) {
            rebindAdapters();
            if (mHasWorkApps) {
                mWorkManager.reset();
            }
        }
    }

    /**
     * Returns whether the view itself will handle the touch event or not.
     */
    public boolean shouldContainerScroll(MotionEvent ev) {
        BaseDragLayer dragLayer = mActivityContext.getDragLayer();
        // Scroll if not within the container view (e.g. over large-screen scrim).
        if (!dragLayer.isEventOverView(this, ev)) {
            return true;
        }
        if (dragLayer.isEventOverView(mBottomSheetHandleArea, ev)) {
            return true;
        }
        AllAppsRecyclerView rv = getActiveRecyclerView();
        if (rv == null) {
            return true;
        }
        if (rv.getScrollbar() != null
                && rv.getScrollbar().getThumbOffsetY() >= 0
                && dragLayer.isEventOverView(rv.getScrollbar(), ev)) {
            return false;
        }
        return rv.shouldContainerScroll(ev, dragLayer);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AllAppsRecyclerView rv = getActiveRecyclerView();
            if (rv != null && rv.getScrollbar() != null
                    && rv.getScrollbar().isHitInParent(ev.getX(), ev.getY(), mFastScrollerOffset)) {
                mTouchHandler = rv.getScrollbar();
            } else {
                mTouchHandler = null;
            }
        }
        if (mTouchHandler != null) {
            return mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AllAppsRecyclerView rv = getActiveRecyclerView();
            if (rv != null && rv.getScrollbar() != null
                    && rv.getScrollbar().isHitInParent(ev.getX(), ev.getY(), mFastScrollerOffset)) {
                mTouchHandler = rv.getScrollbar();
            } else {
                mTouchHandler = null;

            }
        }
        if (mTouchHandler != null) {
            mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
            return true;
        }
        if (isSearching()) {
            // if in search state, consume touch event.
            return true;
        }
        return false;
    }

    /**
     * Description of the container view based on its current state.
     */
    public String getDescription() {
        StringCache cache = mActivityContext.getStringCache();
        if (mUsingTabs) {
            if (cache != null) {
                return isPersonalTab()
                        ? cache.allAppsPersonalTabAccessibility
                        : cache.allAppsWorkTabAccessibility;
            } else {
                return isPersonalTab()
                        ? getContext().getString(R.string.all_apps_button_personal_label)
                        : getContext().getString(R.string.all_apps_button_work_label);
            }
        }
        return getContext().getString(R.string.all_apps_button_label);
    }

    /**
     * The current active recycler view (A-Z list from one of the profiles, or search results).
     */
    public AllAppsRecyclerView getActiveRecyclerView() {
        if (isSearching()) {
            return getSearchRecyclerView();
        }
        return getActiveAppsRecyclerView();
    }

    /**
     * The current apps recycler view in the container.
     */
    private AllAppsRecyclerView getActiveAppsRecyclerView() {
        if (!mUsingTabs || isPersonalTab()) {
            return mAH[AdapterHolder.TYPE_MAIN].mRecyclerView;
        } else {
            return mAH[mViewPager.getNextPage()].mRecyclerView;
        }
    }

    /**
     * The container for A-Z apps (the ViewPager for main+work tabs, or main RV). This is currently
     * hidden while searching.
     **/
    private View getAppsRecyclerViewContainer() {
        return mViewPager != null ? mViewPager : findViewById(R.id.apps_list_view);
    }

    /**
     * The RV for search results, which is hidden while A-Z apps are visible.
     */
    public SearchRecyclerView getSearchRecyclerView() {
        return mSearchRecyclerView;
    }

    protected boolean isPersonalTab() {
        return mViewPager == null || mViewPager.getNextPage() == 0;
    }

    /**
     * Switches the current page to the provided {@code tab} if tabs are supported, otherwise does
     * nothing.
     */
    public void switchToTab(int tab) {
        if (mUsingTabs) {
            mViewPager.setCurrentPage(tab);
        }
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }

    /**
     * Resets the state of AllApps.
     */
    public void reset(boolean animate) {
        if (!Utilities.getOmegaPrefs(getContext()).getDrawerSaveScrollPosition().getValue()) {
            for (AdapterHolder adapterHolder : mAH) {
                if (adapterHolder.mRecyclerView != null) {
                    adapterHolder.mRecyclerView.scrollToTop();
                }
            }
            if (isHeaderVisible()) {
                mHeader.reset(animate);
            }
            // Reset the base recycler view after transitioning home.
            updateHeaderScroll(0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && getActiveRecyclerView() != null) {
                getActiveRecyclerView().requestFocus();
            }
        });

        mHeader = findViewById(R.id.all_apps_header);
        /*mHeader.setContent(() -> {
                TabsBarKt.TabsBar(mTabsController.getTabs().getTabs(),(tab -> ) );
            }
        );*/
        mSearchRecyclerView = findViewById(R.id.search_results_list_view);
        rebindAdapters(true);

        mBottomSheetBackground = findViewById(R.id.bottom_sheet_background);
        updateBackground(mActivityContext.getDeviceProfile());

        mBottomSheetHandleArea = findViewById(R.id.bottom_sheet_handle_area);
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean success) {
    }

    @Override
    public void setInsets(Rect insets) {
        mInsets.set(insets);
        DeviceProfile grid = mActivityContext.getDeviceProfile();

        applyAdapterSideAndBottomPaddings(grid);

        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.leftMargin = insets.left;
        mlp.rightMargin = insets.right;
        setLayoutParams(mlp);

        if (grid.isVerticalBarLayout()) {
            setPadding(grid.workspacePadding.left, 0, grid.workspacePadding.right, 0);
        } else {
            setPadding(grid.allAppsLeftRightMargin, grid.allAppsTopPadding,
                    grid.allAppsLeftRightMargin, 0);
        }

        InsettableFrameLayout.dispatchInsets(this, insets);
    }

    /**
     * Returns a padding in case a scrim is shown on the bottom of the view and a padding is needed.
     */
    protected int getNavBarScrimHeight(WindowInsets insets) {
        return 0;
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        mNavBarScrimHeight = getNavBarScrimHeight(insets);
        applyAdapterSideAndBottomPaddings(mActivityContext.getDeviceProfile());
        return super.dispatchApplyWindowInsets(insets);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mNavBarScrimHeight > 0) {
            canvas.drawRect(0, getHeight() - mNavBarScrimHeight, getWidth(), getHeight(),
                    mNavBarScrimPaint);
        }
    }

    protected void rebindAdapters() {
        rebindAdapters(false /* force */);
    }

    protected void rebindAdapters(boolean force) {
        //updateSearchResultsVisibility();

        boolean showTabs = shouldShowTabs();
        if (showTabs == mUsingTabs && !force) {
            return;
        }

        if (isSearching()) {
            mUsingTabs = showTabs;
            mWorkManager.detachWorkModeSwitch();
            return;
        }
        int currentTab = mViewPager != null ? mViewPager.getNextPage() : 0;

        mTabsController.unregisterIconContainers(mAllAppsStore);
        mPagesController.unregisterIconContainers(mAllAppsStore);
        // replaceAppsRVcontainer() needs to use both mUsingTabs value to remove the old view AND
        // showTabs value to create new view. Hence the mUsingTabs new value assignment MUST happen
        // after this call.
        createHolders();
        replaceAppsRVContainer(showTabs);
        mUsingTabs = showTabs;

        if (mTabsController.getTabs().getHasWorkApps()) {
            setupWorkToggle();
        }

        if (isPagedView()) {
            //mPagesController.setup(mHorizontalViewPager); TODO
        } else {
            if (mUsingTabs) {
                mTabsController.setup(mViewPager);
                AllAppsTabsLayout tabStrip = findViewById(R.id.tabs);
                tabStrip.inflateButtons(mTabsController.getTabs(), this::switchToTab);
                onTabChanged(currentTab);
            } else {
                mTabsController.setup((View) findViewById(R.id.apps_list_view));
            }
        }
        setupHeader();

        mPagesController.registerIconContainers(mAllAppsStore);
        mTabsController.registerIconContainers(mAllAppsStore);
        if (isPagedView()) {
            /*if (mHorizontalViewPager != null) { TODO restore paged layout
                mHorizontalViewPager.snapToPage(Math.min(mPagesController.getPagesCount() - 1, currentTab), 0);
            }*/
        } else {
            if (mViewPager != null) {
                mViewPager.snapToPage(Math.min(mTabsController.getTabsCount() - 1, currentTab), 0);
            }
        }
    }

    private void updateSearchResultsVisibility() {
        if (isSearching()) {
            getSearchRecyclerView().setVisibility(VISIBLE);
            getAppsRecyclerViewContainer().setVisibility(GONE);
        } else {
            getSearchRecyclerView().setVisibility(GONE);
            getAppsRecyclerViewContainer().setVisibility(VISIBLE);
        }
        if (mHeader.isSetUp()) {
            mHeader.setActiveRV(getCurrentPage());
        }
    }

    private void applyAdapterSideAndBottomPaddings(DeviceProfile grid) {
        int bottomPadding = Math.max(mInsets.bottom, mNavBarScrimHeight);
        for (AdapterHolder adapterHolder : mAH) {
            adapterHolder.mPadding.bottom = bottomPadding;
            adapterHolder.mPadding.left =
                    adapterHolder.mPadding.right = grid.allAppsLeftRightPadding;
            adapterHolder.applyPadding();
        }
    }

    protected boolean shouldShowTabs() {
        return mHasWorkApps || mUsingTabs;
    }

    protected boolean isSearching() {
        return false;
    }

    private void resetWorkProfile() {
        boolean isEnabled = !mAllAppsStore.hasModelFlag(FLAG_QUIET_MODE_ENABLED);
        for (AdapterHolder adapterHolder : mAH) {
            if (adapterHolder.mType == AdapterHolder.TYPE_WORK) {
                mWorkModeSwitch.updateCurrentState(isEnabled);
                adapterHolder.applyPadding();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mWorkManager.setWorkProfileEnabled(isEnabled);
        }
    }

    private void setupWorkToggle() {
        removeWorkToggle();
        if (Utilities.ATLEAST_P) {
            mWorkModeSwitch = (WorkModeSwitch) mActivityContext.getLayoutInflater().inflate(
                    R.layout.work_mode_fab, this, false);
            this.addView(mWorkModeSwitch);
            mWorkModeSwitch.setInsets(mInsets);
            mWorkModeSwitch.post(() -> {
                for (AdapterHolder adapterHolder : mAH) {
                    if (adapterHolder.mType == AdapterHolder.TYPE_WORK) {
                        adapterHolder.applyPadding();
                    }
                }
                resetWorkProfile();
            });
        }
    }

    private void removeWorkToggle() {
        if (mWorkModeSwitch == null) return;
        if (mWorkModeSwitch.getParent() == this) {
            this.removeView(mWorkModeSwitch);
        }
        mWorkModeSwitch = null;
    }

    protected View replaceAppsRVContainer(boolean showTabs) {
        for (AdapterHolder adapterHolder : mAH) {
            if (adapterHolder.mRecyclerView != null) {
                adapterHolder.mRecyclerView.setLayoutManager(null);
                adapterHolder.mRecyclerView.setAdapter(null);
            }
        }
        View oldView = getAppsRecyclerViewContainer();
        int index = indexOfChild(oldView);
        removeView(oldView);
        int layout = showTabs ? R.layout.all_apps_tabs : R.layout.all_apps_rv_layout; // TODO add horizontal option
        View newView = getLayoutInflater().inflate(layout, this, false);
        addView(newView, index);
        if (isPagedView()) {
            /*mHorizontalViewPager = (AllAppsPagedView) newView; // TODO add horizontal option
            mHorizontalViewPager.addTabs(mPagesController.getPagesCount());
            mHorizontalViewPager.initParentViews(this);
            mHorizontalViewPager.getPageIndicator().setOnActivePageChangedListener(this);*/
            removeWorkToggle();
        } else {
            if (showTabs) {
                mViewPager = (AllAppsPagedView) newView;
                mViewPager.addTabs(mTabsController.getTabsCount());
                mViewPager.initParentViews(this);
                mViewPager.getPageIndicator().setOnActivePageChangedListener(this);
                if (mTabsController.getTabs().getHasWorkApps()) {
                    setupWorkToggle();
                }
            } else {
                mWorkManager.detachWorkModeSwitch();
                mViewPager = null;
                removeWorkToggle();
            }
        }
        return newView;
    }

    public void onTabChanged(int pos) {
        pos = Utilities.boundToRange(pos, 0, mTabsController.getTabsCount() - 1);
        mHeader.setActiveRV(pos);
        if (mAH[pos].mRecyclerView != null) {
            mAH[pos].mRecyclerView.bindFastScrollbar();
            //mAH[pos].mRecyclerView.setScrollbarColor(Utilities.getOmegaPrefs(getContext()).getThemeAccentColor().onGetValue());
            mTabsController.bindButtons(findViewById(R.id.tabs), mViewPager);
        }
        reset(true);
    }

    @Override
    public void onActivePageChanged(int currentActivePage) {
        if (mAH[currentActivePage].mRecyclerView != null) {
            mAH[currentActivePage].mRecyclerView.bindFastScrollbar();
        }
        reset(true /* animate */);

        mWorkManager.onActivePageChanged(currentActivePage);
    }

    public FloatingHeaderView getFloatingHeaderView() {
        return mHeader;
    }

    @VisibleForTesting
    public View getContentView() {
        return isSearching() ? getSearchRecyclerView() : getAppsRecyclerViewContainer();
    }

    /**
     * The current page visible in all apps.
     */
    public int getCurrentPage() {
        return isSearching()
                ? AdapterHolder.TYPE_SEARCH
                : mViewPager == null ? AdapterHolder.TYPE_MAIN : mViewPager.getNextPage();
    }

    /**
     * The scroll bar for the active apps recycler view.
     */
    public RecyclerViewFastScroller getScrollBar() {
        AllAppsRecyclerView rv = getActiveAppsRecyclerView();
        return rv == null ? null : rv.getScrollbar();
    }

    void setupHeader() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setup(mAH, !mUsingTabs);
        int padding = mHeader.getMaxTranslation();
        for (AdapterHolder adapterHolder : mAH) {
            adapterHolder.mPadding.top = padding;
            adapterHolder.applyPadding();
        }
    }

    public boolean isHeaderVisible() {
        return mHeader != null && mHeader.getVisibility() == View.VISIBLE;
    }

    /**
     * Adds an update listener to animator that adds springs to the animation.
     */
    public void addSpringFromFlingUpdateListener(ValueAnimator animator,
                                                 float velocity /* release velocity */,
                                                 float progress /* portion of the distance to travel*/) {
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                float distance = (1 - progress) * getHeight(); // px
                float settleVelocity = Math.min(0, distance
                        / (AllAppsTransitionController.INTERP_COEFF * animator.getDuration())
                        + velocity);
                absorbSwipeUpVelocity(Math.max(1000, Math.abs(
                        Math.round(settleVelocity * FLING_VELOCITY_MULTIPLIER))));
            }
        });
    }

    /**
     * Invoked when the container is pulled.
     */
    public void onPull(float deltaDistance, float displacement) {
        absorbPullDeltaDistance(PULL_MULTIPLIER * deltaDistance, PULL_MULTIPLIER * displacement);
        // Current motion spec is to actually push and not pull
        // on this surface. However, until EdgeEffect.onPush (b/190612804) is
        // implemented at view level, we will simply pull
    }

    @Override
    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        outRect.offset(0, (int) getTranslationY());
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        invalidateHeader();
    }

    public void setScrimView(ScrimView scrimView) {
        mScrimView = scrimView;
    }

    @Override
    public void drawOnScrim(Canvas canvas) {
        if (!mHeader.isHeaderProtectionSupported()) {
            return;
        }
        mHeaderPaint.setColor(mHeaderColor);
        mHeaderPaint.setAlpha((int) (getAlpha() * Color.alpha(mHeaderColor)));
        if (mHeaderPaint.getColor() != mScrimColor && mHeaderPaint.getColor() != 0) {
            int bottom = getHeaderBottom();
            if (!mUsingTabs) {
                bottom += getFloatingHeaderView().getPaddingBottom() - mHeaderBottomAdjustment;
            }
            canvas.drawRect(0, 0, canvas.getWidth(), bottom, mHeaderPaint);
            int tabsHeight = getFloatingHeaderView().getPeripheralProtectionHeight();
            if (mTabsProtectionAlpha > 0 && tabsHeight != 0) {
                mHeaderPaint.setAlpha((int) (getAlpha() * mTabsProtectionAlpha));
                canvas.drawRect(0, bottom, canvas.getWidth(), bottom + tabsHeight, mHeaderPaint);
            }
        }
    }

    /**
     * redraws header protection
     */
    public void invalidateHeader() {
        if (mScrimView != null && mHeader.isHeaderProtectionSupported()) {
            mScrimView.invalidate();
        }
    }

    protected void updateHeaderScroll(int scrolledOffset) {
        /*float prog = Utilities.boundToRange((float) scrolledOffset / mHeaderThreshold, 0f, 1f);
        int headerColor = getHeaderColor(prog);
        int tabsAlpha = mHeader.getPeripheralProtectionHeight() == 0 ? 0
                : (int) (Utilities.boundToRange(
                (scrolledOffset + mHeader.mSnappedScrolledY) / mHeaderThreshold, 0f, 1f)
                * 255);
        if (headerColor != mHeaderColor || mTabsProtectionAlpha != tabsAlpha) {
            mHeaderColor = headerColor;
            mTabsProtectionAlpha = tabsAlpha;
            invalidateHeader();
        }*/
        // TODO implement search related changes
    }

    protected int getHeaderColor(float blendRatio) {
        return ColorUtils.blendARGB(mScrimColor, mHeaderProtectionColor, blendRatio);
    }

    protected abstract BaseAllAppsAdapter<T> createAdapter(AlphabeticalAppsList<T> mAppsList,
                                                           BaseAdapterProvider[] adapterProviders);

    public int getHeaderBottom() {
        return (int) getTranslationY();
    }

    /**
     * Returns a view that denotes the visible part of all apps container view.
     */
    public View getVisibleContainerView() {
        return mActivityContext.getDeviceProfile().isTablet ? mBottomSheetBackground : this;
    }

    public AdapterHolder createHolder(int type) {
        return new AdapterHolder(type);
    }

    /**
     * Holds a {@link BaseAllAppsAdapter} and related fields.
     */
    public class AdapterHolder {
        public static final int TYPE_MAIN = 0;
        public static final int TYPE_WORK = 1;
        public static final int TYPE_SEARCH = 2;

        private int mType;
        public final BaseAllAppsAdapter<T> mAdapter;
        final RecyclerView.LayoutManager mLayoutManager;
        final AlphabeticalAppsList<T> mAppsList;
        final Rect mPadding = new Rect();
        public AllAppsRecyclerView mRecyclerView;

        AdapterHolder(int type) {
            mType = type;
            mAppsList = new AlphabeticalAppsList(
                    mActivityContext,
                    mAllAppsStore,
                    isWork() ? mWorkManager.getAdapterProvider() : null
            );

            BaseAdapterProvider[] adapterProviders =
                    isWork() ? new BaseAdapterProvider[]{mMainAdapterProvider,
                            mWorkManager.getAdapterProvider()}
                            : new BaseAdapterProvider[]{mMainAdapterProvider};

            mAdapter = createAdapter(mAppsList, adapterProviders);
            mAppsList.setAdapter(mAdapter);
            mLayoutManager = mAdapter.getLayoutManager();
        }

        public void setup(@NonNull View rv, @Nullable Predicate<ItemInfo> matcher) {
            mAppsList.updateItemFilter(matcher);
            mRecyclerView = (AllAppsRecyclerView) rv;
            mRecyclerView.setEdgeEffectFactory(createEdgeEffectFactory());
            mRecyclerView.setApps(mAppsList);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setHasFixedSize(true);
            // No animations will occur when changes occur to the items in this RecyclerView.
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.addOnScrollListener(mScrollListener);
            FocusedItemDecorator focusedItemDecorator = new FocusedItemDecorator(mRecyclerView);
            mRecyclerView.addItemDecoration(focusedItemDecorator);
            mAdapter.setIconFocusListener(focusedItemDecorator.getFocusListener());
            applyPadding();
        }

        void applyPadding() {
            if (mRecyclerView != null) {
                int bottomOffset = 0;
                if (isWork() && mWorkManager.getWorkModeSwitch() != null) {
                    bottomOffset = mInsets.bottom + mWorkManager.getWorkModeSwitch().getHeight();
                }
                mRecyclerView.setPadding(mPadding.left, mPadding.top, mPadding.right,
                        mPadding.bottom + bottomOffset);
            }
        }

        public void setType(int type) {
            mType = type;
        }

        boolean isWork() {
            return mType == TYPE_WORK;
        }

        boolean isSearch() {
            return mType == TYPE_SEARCH;
        }
    }
}

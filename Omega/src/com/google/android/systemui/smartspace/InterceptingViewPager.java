package com.google.android.systemui.smartspace;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.viewpager.widget.ViewPager;

import com.statix.android.systemui.res.R;

public class InterceptingViewPager extends ViewPager {
    public boolean mHasPerformedLongPress;
    public boolean mHasPostedLongPress;
    public final Runnable mLongPressCallback;
    public final MotionEventHandler mSuperOnIntercept;
    public final MotionEventHandler mSuperOnTouch;

    public InterceptingViewPager(Context context) {
        super(context);
        mSuperOnTouch = new MotionEventHandler(this, 0);
        mSuperOnIntercept = new MotionEventHandler(this, 1);
        mLongPressCallback =
                () -> {
                    mHasPerformedLongPress = true;
                    if (performLongClick()) {
                        ViewParent parent = getParent();
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                };
    }

    public InterceptingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSuperOnTouch = new MotionEventHandler(this, 0);
        mSuperOnIntercept = new MotionEventHandler(this, 1);
        mLongPressCallback =
                () -> {
                    mHasPerformedLongPress = true;
                    if (performLongClick()) {
                        ViewParent parent = getParent();
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                };
    }

    public void cancelScheduledLongPress() {
        if (mHasPostedLongPress) {
            mHasPostedLongPress = false;
            removeCallbacks(mLongPressCallback);
        }
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        AccessibilityNodeInfo info = super.createAccessibilityNodeInfo();
        String roleDescription = getContext().getString(R.string.smartspace_role_desc);
        info.getExtras().putCharSequence("AccessibilityNodeInfo.roleDescription", roleDescription);
        return info;
    }

    public boolean handleTouchOverride(MotionEvent event, MotionEventHandler handler) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasPerformedLongPress = false;
                if (isLongClickable()) {
                    cancelScheduledLongPress();
                    mHasPostedLongPress = true;
                    postDelayed(mLongPressCallback, ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelScheduledLongPress();
                break;
        }

        if (mHasPerformedLongPress) {
            cancelScheduledLongPress();
            return true;
        }

        boolean result = handler.handleEvent(event);
        if (result) {
            cancelScheduledLongPress();
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return handleTouchOverride(event, mSuperOnIntercept);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouchOverride(event, mSuperOnTouch);
    }

    public static class MotionEventHandler {
        public final InterceptingViewPager viewPager;
        public final int type;

        public MotionEventHandler(InterceptingViewPager viewPager, int type) {
            this.viewPager = viewPager;
            this.type = type;
        }

        public boolean handleEvent(MotionEvent event) {
            return type == 0
                    ? viewPager.superOnTouchEvent(event)
                    : viewPager.superOnInterceptTouchEvent(event);
        }
    }

    // Helper methods to call superclass methods
    public boolean superOnTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public boolean superOnInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event);
    }
}

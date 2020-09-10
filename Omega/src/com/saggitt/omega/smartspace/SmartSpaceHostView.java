package com.saggitt.omega.smartspace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.android.launcher3.CheckLongPressHelper;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.qsb.QsbWidgetHostView;
import com.android.launcher3.views.BaseDragLayer;
import com.android.launcher3.views.OptionsPopupView;
import com.saggitt.omega.util.Config;

import java.util.Collections;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

public class SmartSpaceHostView extends QsbWidgetHostView implements View.OnLongClickListener, BaseDragLayer.TouchCompleteListener {
    private static final String SETTINGS_INTENT_ACTION = "com.google.android.apps.gsa.smartspace.SETTINGS";
    private static final int SETTINGS_SMARTSPACE = -1;

    private final Launcher mLauncher;
    private final CheckLongPressHelper mLongPressHelper = new CheckLongPressHelper(this, this);
    private final float mSlop = ((float) ViewConfiguration.get(getContext()).getScaledTouchSlop());

    public SmartSpaceHostView(Context context) {
        super(context);
        mLauncher = Launcher.getLauncher(context);
    }

    public static boolean hasSettings(Context context) {
        return context.getPackageManager().resolveActivity(createSettingsIntent(), 0) != null;
    }

    public static Intent createSettingsIntent() {
        return new Intent(SETTINGS_INTENT_ACTION)
                .setPackage(Config.GOOGLE_QSB)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    }

    @Override
    protected View getErrorView() {
        return SmartspaceQsbWidget.getDateView(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if (!hasSettings(v.getContext())) {
            return false;
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Rect rect = new Rect();
        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, rect);
        RectF rectF = new RectF();
        float midX = rect.exactCenterX();
        rectF.right = midX;
        rectF.left = midX;
        rectF.top = 0f;
        rectF.bottom = (float) rect.bottom;
        rectF.bottom = Math.min((float) findBottomRecur(this, rect.top, rect), rectF.bottom);

        OptionsPopupView.OptionItem item = new OptionsPopupView.OptionItem(
                R.string.smartspace_preferences,
                R.drawable.ic_smartspace_preferences,
                SETTINGS_SMARTSPACE,
                this::openSettings
        );

        OptionsPopupView.show(mLauncher, rectF, Collections.singletonList(item));
        return true;
    }

    private int findBottomRecur(View view, int i, Rect rect) {
        if (view.getVisibility() != View.VISIBLE) {
            return i;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                i = Math.max(findBottomRecur(viewGroup.getChildAt(childCount), i, rect), i);
            }
        }
        if (view.willNotDraw()) {
            return i;
        }
        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(view, rect);
        return Math.max(i, rect.bottom);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            mLongPressHelper.cancelLongPress();
        }
        if (!mLongPressHelper.hasPerformedLongPress()) {
            return onTouchEvent(ev);
        }
        mLongPressHelper.cancelLongPress();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == ACTION_DOWN) {
            mLongPressHelper.cancelLongPress();
            mLongPressHelper.postCheckForLongPress();
            mLauncher.getDragLayer().setTouchCompleteListener(this);
            return false;
        }
        if (action == ACTION_UP) {
            mLongPressHelper.cancelLongPress();
            return false;
        }

        if (action != ACTION_MOVE) {
            if (action != ACTION_CANCEL) {
                return false;
            }
        } else if (Utilities.pointInView(this, ev.getX(), ev.getY(), mSlop)) {
            return false;
        }

        mLongPressHelper.cancelLongPress();
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public void onTouchComplete() {
        if (!mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
        }
    }

    private boolean openSettings(View v) {
        v.getContext().startActivity(createSettingsIntent());
        return true;
    }
}
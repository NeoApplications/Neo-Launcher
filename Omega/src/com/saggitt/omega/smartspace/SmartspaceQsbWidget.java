package com.saggitt.omega.smartspace;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.qsb.QsbContainerView;
import com.saggitt.omega.util.Config;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_BIND;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;

public class SmartspaceQsbWidget extends QsbContainerView {
    private static final String WIDGET_CLASS_NAME = "com.google.android.apps.gsa.staticplugins.smartspace.widget.SmartspaceWidgetProvider";

    public SmartspaceQsbWidget(Context context) {
        this(context, null);
    }

    public SmartspaceQsbWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SmartspaceQsbWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static View getDateView(ViewGroup root) {
        return LayoutInflater.from(root.getContext())
                .inflate(R.layout.smart_space_date_view, root, false);
    }

    public static class SmartSpaceFragment extends QsbContainerView.QsbFragment {
        private static final int SMART_SPACE_WIDGET_HOST_ID = 1027;
        private static final int REQUEST_BIND_QSB = 1;

        private QsbWidgetHost mQsbWidgetHost;

        public SmartSpaceFragment() {
            mKeyWidgetId = "smart_space_widget_id";
        }

        @Override
        protected QsbContainerView.QsbWidgetHost createHost() {
            mQsbWidgetHost = new QsbContainerView.QsbWidgetHost(getContext(),
                    SMART_SPACE_WIDGET_HOST_ID,
                    SmartSpaceHostView::new);
            return mQsbWidgetHost;
        }

        @Override
        protected AppWidgetProviderInfo getSearchWidgetProvider() {
            for (AppWidgetProviderInfo next : AppWidgetManager.getInstance(getContext()).getInstalledProviders()) {
                if (next.getProfile().equals(Process.myUserHandle())
                        && Config.GOOGLE_QSB.equals(next.provider.getPackageName())
                        && SmartspaceQsbWidget.WIDGET_CLASS_NAME.equals(next.provider.getClassName())) {
                    return next;
                }
            }
            return null;
        }

        @Override
        protected View getDefaultView(ViewGroup container, boolean showSetupIcon) {
            View v = SmartspaceQsbWidget.getDateView(container);

            if (showSetupIcon) {
                Intent intent = new Intent(ACTION_APPWIDGET_BIND)
                        .putExtra(EXTRA_APPWIDGET_ID, mQsbWidgetHost.allocateAppWidgetId())
                        .putExtra(EXTRA_APPWIDGET_PROVIDER, getSearchWidgetProvider().provider);

                v.setOnClickListener(v2 -> startActivityForResult(intent, REQUEST_BIND_QSB));

                // Try binding immediately when creating the view.
                v.callOnClick();
            } else {
                v.setOnClickListener(this::openCalendar);
            }

            return v;
        }

        private void openCalendar(View v) {
            Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
            ContentUris.appendId(timeUri, System.currentTimeMillis());
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(timeUri.build())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Launcher.getLauncher(getContext())
                    .startActivitySafely(v, intent, null, null);
        }

        @Override
        protected Bundle createBindOptions() {
            Bundle createBindOptions = super.createBindOptions();
            createBindOptions.putString("attached-launcher-identifier", getContext().getPackageName());
            createBindOptions.putBoolean("com.google.android.apps.gsa.widget.PREINSTALLED", true);
            return createBindOptions;
        }
    }
}
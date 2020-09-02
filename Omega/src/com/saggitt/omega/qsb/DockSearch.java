/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.qsb;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class DockSearch {
    public static final String KEY_DOCK_SEARCH = "pref_dock_search";

    public static AppWidgetProviderInfo getWidgetInfo(Context context) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        String val = prefs.getString(KEY_DOCK_SEARCH, "");
        for (AppWidgetProviderInfo info : validWidgets(context)) {
            if (val.equals(info.provider.flattenToShortString())) {
                return info;
            }
        }
        return null;
    }

    public static List<AppWidgetProviderInfo> validWidgets(Context context) {
        int highestMinHeight = context.getResources()
                .getDimensionPixelSize(R.dimen.qsb_widget_height);
        List<AppWidgetProviderInfo> widgets = new ArrayList<>();
        AppWidgetManagerCompat widgetManager = AppWidgetManagerCompat.getInstance(context);
        for (AppWidgetProviderInfo widgetInfo : widgetManager.getAllProviders(null)) {
            if (widgetInfo.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL
                    && Math.min(widgetInfo.minHeight, widgetInfo.minResizeHeight) <= highestMinHeight) {
                widgets.add(widgetInfo);
            }
        }
        return widgets;
    }
}

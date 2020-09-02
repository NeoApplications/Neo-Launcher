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
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.preference.ListPreference;

import com.android.launcher3.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DockSearchPrefSetter implements ReloadingListPreference.OnReloadListener {
    private final Context mContext;
    private final PackageManager mPm;

    public DockSearchPrefSetter(Context context) {
        mContext = context;
        mPm = context.getPackageManager();
    }

    @Override
    public void updateList(ListPreference pref) {
        List<AppWidgetProviderInfo> widgets = DockSearch.validWidgets(mContext);

        CharSequence[] keys = new String[widgets.size() + 1];
        CharSequence[] values = new String[keys.length];
        int i = 0;

        // First value, system default
        keys[i] = mContext.getResources().getString(R.string.pref_value_disabled);
        values[i++] = "";

        Collections.sort(widgets,
                (o1, o2) -> normalize(o1.loadLabel(mPm)).compareTo(normalize(o2.loadLabel(mPm))));
        for (AppWidgetProviderInfo widget : widgets) {
            keys[i] = widget.loadLabel(mPm);
            String pkg = widget.provider.getPackageName();
            try {
                CharSequence app = mPm.getApplicationInfo(pkg, 0).loadLabel(mPm);
                if (!keys[i].toString().startsWith(app.toString())) {
                    keys[i] = mContext.getString(R.string.dock_search_value, app, keys[i]);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            values[i++] = widget.provider.flattenToShortString();
        }

        pref.setEntries(keys);
        pref.setEntryValues(values);

        String v = pref.getValue();
        if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
            pref.setValue("");
        }
    }

    private String normalize(String title) {
        return title.toLowerCase();
    }
}
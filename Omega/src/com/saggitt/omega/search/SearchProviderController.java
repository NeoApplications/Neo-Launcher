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

package com.saggitt.omega.search;

import android.content.Context;
import android.view.ContextThemeWrapper;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.MainThreadInitializedObject;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.providers.AppSearchSearchProvider;
import com.saggitt.omega.search.providers.BingSearchProvider;
import com.saggitt.omega.search.providers.GoogleSearchProvider;
import com.saggitt.omega.search.webproviders.BingWebSearchProvider;
import com.saggitt.omega.theme.ThemeOverride;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchProviderController {
    public static final MainThreadInitializedObject<SearchProviderController> INSTANCE =
            new MainThreadInitializedObject<>(SearchProviderController::new);
    protected SearchProvider cache = null;
    private OmegaPreferences prefs;
    private String cached = "";
    private Context mContext;
    private HashSet<OnProviderChangeListener> listeners;

    private ThemeOverride themeOverride = new ThemeOverride(new ThemeOverride.Launcher(), new ThemeListener());
    private int themeRes = 0;

    public SearchProviderController(Context context) {
        prefs = Utilities.getOmegaPrefs(context);
        mContext = context;
        listeners = new HashSet<>();
        //ThemeManager.Companion.getInstance(context).addOverride(themeOverride);
    }

    public List<SearchProvider> getSearchProviders(Context context) {
        List<SearchProvider> providerList = new ArrayList<>();
        providerList.add(new AppSearchSearchProvider(context));
        providerList.add(new GoogleSearchProvider(context));
        providerList.add(new BingSearchProvider(context));

        //WEB
        providerList.add(new BingWebSearchProvider(context));

        return providerList;
    }

    public SearchProvider getSearchProvider() {
        String current = prefs.getSearchProvider();
        if (cache == null || !cached.equals(current)) {
            try {
                Constructor<?> constructor = Class.forName(prefs.getSearchProvider()).getConstructor(mContext.getClass());
                ContextThemeWrapper themedContext = new ContextThemeWrapper(mContext, themeRes);
                SearchProvider prov = (SearchProvider) constructor.newInstance(themedContext);
                if (prov.isAvailable) {
                    cache = prov;
                }

            } catch (Exception ignored) {
            }
            if (cache == null)
                cache = new GoogleSearchProvider(mContext);

            cached = cached.getClass().getName();
            notifyProviderChanged();
        }
        return cache;
    }

    public void onSearchProviderChanged() {
        cache = null;
        notifyProviderChanged();
    }

    private void notifyProviderChanged() {
        for (OnProviderChangeListener listener : listeners) {
            listener.onSearchProviderChanged();
        }
    }

    public Boolean isGoogle() {
        return false;
    }

    public void addOnProviderChangeListener(OnProviderChangeListener listener) {
        listeners.add(listener);
    }

    public void removeOnProviderChangeListener(OnProviderChangeListener listener) {
        listeners.remove(listener);
    }

    public interface OnProviderChangeListener {
        void onSearchProviderChanged();
    }

    class ThemeListener implements ThemeOverride.ThemeOverrideListener {
        SearchProviderController controller;

        public ThemeListener() {
        }

        public void applyTheme(int themeRes) {
            controller = new SearchProviderController(mContext);
            controller.themeRes = themeRes;
        }

        @Override
        public boolean isAlive() {
            return true;
        }

        @Override
        public void reloadTheme() {
            cache = null;
            applyTheme(themeOverride.getTheme(mContext));
            onSearchProviderChanged();
        }
    }
}

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

package com.saggitt.omega.search.webproviders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherState;
import com.android.launcher3.Utilities;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.util.OkHttpClientBuilder;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.saggitt.omega.util.OmegaUtilsKt.toArrayList;
import static java.util.Collections.emptyList;

public abstract class WebSearchProvider extends SearchProvider {
    protected String searchUrl = "";
    protected String suggestionsUrl = "";
    protected OkHttpClient client;
    protected int MAX_SUGGESTIONS = 5;

    public WebSearchProvider(Context context) {
        super(context);
        client = new OkHttpClientBuilder().build(context);
        supportsVoiceSearch = false;
        supportsAssistant = false;
        supportsFeed = false;
    }

    public List<String> getSuggestions(String query) {
        if (suggestionsUrl == null)
            return emptyList();
        try {
            Response response = client.newCall(new Request.Builder().url(suggestionsUrl.format(query)).build()).execute();
            JSONArray array = new JSONArray(Objects.requireNonNull(response.body()).string())
                    .getJSONArray(1);

            ArrayList<String> suggestions = toArrayList(array);
            suggestions.subList(0, MAX_SUGGESTIONS);
            return suggestions;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return emptyList();
    }

    public void openResults(String query) {
        Utilities.openURLinBrowser(mContext, getResultUrl(query));
    }

    protected String getResultUrl(String query) {
        return searchUrl.format(query);
    }


    public void startSearch(Function1<Intent, Unit> callback) {
        Launcher launcher = LauncherAppState.getInstanceNoCreate().getLauncher();
        launcher.getStateManager().goToState(LauncherState.ALL_APPS, true, (Runnable) (new Runnable() {
            public final void run() {
                launcher.getAppsView().getSearchUiManager().startSearch();
            }
        }));
    }

    @Override
    public Drawable getIcon() {
        return null;
    }

    @Override
    public Drawable getVoiceIcon() {
        return null;
    }

    @Override
    public Drawable getAssistantIcon() {
        return null;
    }
}

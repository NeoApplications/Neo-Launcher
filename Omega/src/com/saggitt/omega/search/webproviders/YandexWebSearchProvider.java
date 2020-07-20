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
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;

import org.jetbrains.annotations.NotNull;

public class YandexWebSearchProvider extends WebSearchProvider {
    public YandexWebSearchProvider(@NotNull Context context) {
        super(context);
        searchUrl = "https://yandex.com/search/?text=%s";
        suggestionsUrl = "https://suggest.yandex.com/suggest-ff.cgi?part=%s&uil=${context.locale.language}";
        name = context.getString(R.string.web_search_yandex);
    }

    @NotNull
    @Override
    public Drawable getIcon() {
        return mContext.getResources().getDrawable(R.drawable.ic_yandex);
    }
}

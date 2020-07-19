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

package com.saggitt.omega.search.providers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.util.PackageManagerHelper;
import com.saggitt.omega.search.SearchProvider;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class BingSearchProvider extends SearchProvider {
    private String PACKAGE = "com.microsoft.bing";
    private String PACKAGE_CORTANA = "com.microsoft.cortana";
    private String PACKAGE_ALEXA = "com.amazon.dee.app";

    public BingSearchProvider(Context context) {
        super(context);
        supportsVoiceSearch = true;
        supportsFeed = false;
        supportsAssistant = isCortanaInstalled() || isAlexaInstalled();
        providerName = mContext.getString(R.string.search_provider_bing);
    }

    public void startSearch(Function1<Intent, Unit> callback) {
        Intent intent = (new Intent()).setClassName(PACKAGE, "com.microsoft.clients.bing.widget.WidgetSearchActivity").setPackage(PACKAGE);
        callback.invoke(intent);
    }

    @Override
    public Drawable getIcon() {
        return mContext.getDrawable(R.drawable.ic_bing);
    }

    @Override
    public Drawable getVoiceIcon() {
        Drawable voiceIcon = mContext.getDrawable(R.drawable.ic_qsb_mic);
        Objects.requireNonNull(voiceIcon).mutate().setTint(Color.parseColor("#00897B"));
        return voiceIcon;
    }

    @Override
    public boolean isAvailable() {
        return PackageManagerHelper.isAppEnabled(mContext.getPackageManager(), PACKAGE, 0);
    }

    private Boolean isCortanaInstalled() {
        return PackageManagerHelper.isAppEnabled(mContext.getPackageManager(), PACKAGE_CORTANA, 0);
    }

    private Boolean isAlexaInstalled() {
        return PackageManagerHelper.isAppEnabled(mContext.getPackageManager(), PACKAGE_ALEXA, 0);
    }

    @Override
    public Drawable getAssistantIcon() {
        return (isCortanaInstalled() ?
                mContext.getDrawable(R.drawable.ic_cortana) : mContext.getDrawable(R.drawable.ic_alexa));
    }

    @Override
    public Drawable getShadowAssistantIcon() {
        if (isCortanaInstalled()) {
            return wrapInShadowDrawable(mContext.getDrawable(R.drawable.ic_cortana));
        }
        return super.getShadowAssistantIcon();
    }
}

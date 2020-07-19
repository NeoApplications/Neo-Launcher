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
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.graphics.ShadowDrawable;
import com.saggitt.omega.settings.search.SettingsSearchActivity;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

abstract public class SearchProvider {
    protected Boolean supportsVoiceSearch;
    protected Boolean supportsAssistant;
    protected Boolean supportsFeed;
    protected Context mContext;
    public String providerName = "";
    protected Boolean isAvailable = false;

    public SearchProvider(Context context) {
        mContext = context;
    }

    public abstract Drawable getIcon();

    public abstract Drawable getVoiceIcon();

    public abstract Drawable getAssistantIcon();

    public abstract void startSearch(Function1<Intent, Unit> callback);

    public Intent getSettingsIntent() {
        return new Intent().setClass(mContext, SettingsSearchActivity.class);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean isBroadCast() {
        return false;
    }

    public Drawable getAssistantIcon(Boolean colored) {
        if (colored)
            return getAssistantIcon();
        else
            return getShadowAssistantIcon();
    }

    public Drawable getShadowAssistantIcon() {
        return wrapInShadowDrawable(getAssistantIcon());
    }

    public Drawable getIcon(Boolean colored) {
        if (colored)
            return getIcon();
        else
            return getShadowIcon();
    }

    public Drawable getVoiceIcon(Boolean colored) {
        if (colored)
            return getVoiceIcon();
        else
            return getShadowVoiceIcon();
    }

    public Drawable getShadowIcon() {
        return wrapInShadowDrawable(getIcon());
    }

    public Drawable getShadowVoiceIcon() {
        return wrapInShadowDrawable(getVoiceIcon());
    }

    protected Drawable wrapInShadowDrawable(Drawable d) {
        return ShadowDrawable.wrap(mContext, d,
                R.color.qsb_icon_shadow_color,
                4f, R.color.qsb_dark_icon_tint);
    }

    public void startFeed(Function1<Intent, Unit> callback) {
        if (getSupportsFeed()) throw new RuntimeException("Feed supported but not implemented");
    }

    public void startVoiceSearch(Function1<Intent, Unit> callback) {
        if (getSupportsVoiceSearch())
            throw new RuntimeException("Voice search supported but not implemented");
    }

    public void startAssistant(Function1<Intent, Unit> callback) {
        if (getSupportsAssistant())
            throw new RuntimeException("Assistant supported but not implemented");
    }

    public boolean getSupportsVoiceSearch() {
        return supportsVoiceSearch;
    }

    public boolean getSupportsAssistant() {
        return supportsAssistant;
    }

    public boolean getSupportsFeed() {
        return supportsFeed;
    }
}

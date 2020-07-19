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
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.search.SearchProvider;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.saggitt.omega.util.Config.GOOGLE_QSB;

public class GoogleSearchProvider extends SearchProvider {
    public GoogleSearchProvider(Context context) {
        super(context);
        supportsVoiceSearch = true;
        supportsAssistant = true;
        supportsFeed = true;
        providerName = mContext.getString(R.string.google_app);
    }

    @Override
    public Intent getSettingsIntent() {
        return new Intent("com.google.android.googlequicksearchbox.TEXT_ASSIST")
                .setPackage(GOOGLE_QSB).addFlags(268435456);
    }

    @Override
    public boolean isBroadCast() {
        return true;
    }

    public void startSearch(Function1<Intent, Unit> callback) {
        Intent intent = (new Intent()).setClassName(GOOGLE_QSB, GOOGLE_QSB + ".SearchActivity");
        callback.invoke(intent);
    }

    @Override
    public void startVoiceSearch(@NotNull Function1<Intent, Unit> callback) {
        Intent intent = (new Intent("android.intent.action.VOICE_ASSIST")).setPackage(GOOGLE_QSB);
        callback.invoke(intent);
    }

    @Override
    public void startAssistant(@NotNull Function1<Intent, Unit> callback) {
        Intent intent = (new Intent(Intent.ACTION_VOICE_COMMAND)).setPackage(GOOGLE_QSB);
        callback.invoke(intent);
    }

    @Override
    public void startFeed(@NotNull Function1<Intent, Unit> callback) {
        OmegaLauncher launcher = OmegaLauncher.getLauncher(mContext);
        if (launcher.getGoogleNow() != null) {
            //launcher.getGoogleNow().showOverlay(true);
        } else {
            Intent intent = new Intent();
            intent.setClassName(GOOGLE_QSB, GOOGLE_QSB + ".SearchActivity");
            intent.setAction(Intent.ACTION_MAIN);
            callback.invoke(intent);
        }
    }

    @Override
    public Drawable getIcon() {
        return mContext.getDrawable(R.drawable.ic_qsb_logo);
    }

    @Override
    public Drawable getVoiceIcon() {
        return mContext.getDrawable(R.drawable.ic_qsb_mic);
    }

    @Override
    public Drawable getAssistantIcon() {
        return mContext.getDrawable(R.drawable.ic_qsb_assist);
    }
}

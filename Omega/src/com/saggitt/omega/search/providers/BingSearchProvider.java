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
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.util.PackageManagerHelper;
import com.saggitt.omega.search.SearchProvider;

public class BingSearchProvider extends SearchProvider {
    private static String PACKAGE = "com.microsoft.bing";
    private static String PACKAGE_CORTANA = "com.microsoft.cortana";
    private static String PACKAGE_ALEXA = "com.amazon.dee.app";
    private Context mContex;

    public BingSearchProvider(Context context) {
        super(context);
        mContex = context;
    }

    @Override
    public Drawable getIcon() {
        return mContex.getDrawable(R.drawable.ic_bing);
    }

    @Override
    public Drawable getVoiceIcon() {
        Drawable voiceIcon = mContex.getDrawable(R.drawable.ic_qsb_mic);
        voiceIcon.mutate().setTint(Color.parseColor("#00897B"));
        return voiceIcon;
    }

    private Boolean isCortanaInstalled() {
        return PackageManagerHelper.isAppEnabled(mContex.getPackageManager(), PACKAGE_CORTANA, 0);
    }

    @Override
    public Drawable getAssistantIcon() {
        return (isCortanaInstalled() ?
                mContex.getDrawable(R.drawable.ic_cortana) : mContex.getDrawable(R.drawable.ic_alexa));
    }
}

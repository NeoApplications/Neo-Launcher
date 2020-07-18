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
import android.graphics.drawable.Drawable;

abstract public class SearchProvider {
    protected Boolean supportsVoiceSearch;
    protected Boolean supportsAssistant;
    protected Boolean supportsFeed;
    protected Context mContext;
    private Boolean isAvailable = false;

    public SearchProvider(Context context) {
        mContext = context;
    }

    public abstract Drawable getIcon();

    public abstract Drawable getVoiceIcon();

    public abstract Drawable getAssistantIcon();
}

/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.search;

import com.android.launcher3.allapps.search.AllAppsSearchBarController.Callbacks;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;

public class SearchResult {

    public final ArrayList<ComponentKey> mApps = new ArrayList();
    public final Callbacks mCallbacks;
    public final String mQuery;

    public SearchResult(String query, Callbacks callbacks) {
        mQuery = query;
        mCallbacks = callbacks;
    }
}

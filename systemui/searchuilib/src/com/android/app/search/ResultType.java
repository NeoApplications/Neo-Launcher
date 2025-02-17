/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.app.search;

/**
 * Constants to be used with {@link android.app.search.SearchContext} and
 * {@link android.app.search.SearchTarget}.
 * <p>
 * Note, a result type could be a of two types.
 * For example, unpublished settings result type could be in slices:
 * <code> resultType = SETTING | SLICE </code>
 */
public class ResultType {

    // published corpus by 3rd party app, supported by SystemService
    public static final int APPLICATION = 1 << 0;
    public static final int SHORTCUT = 1 << 1;
    public static final int SLICE = 1 << 6;
    public static final int WIDGETS = 1 << 7;

    // Not extracted from any of the SystemService
    public static final int PEOPLE = 1 << 2;
    public static final int ACTION = 1 << 3;
    public static final int SETTING = 1 << 4;
    public static final int SCREENSHOT = 1 << 5;
    public static final int PLAY = 1 << 8;
    public static final int SUGGEST = 1 << 9;
    public static final int ASSISTANT = 1 << 10;
    public static final int CHROMETAB = 1 << 11;
    public static final int NAVVYSITE = 1 << 12;
    public static final int TIPS = 1 << 13;
    public static final int PEOPLE_TILE = 1 << 14;
    public static final int LEGACY_SHORTCUT = 1 << 15;
    public static final int MEMORY = 1 << 16;
}

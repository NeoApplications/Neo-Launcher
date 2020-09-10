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

package com.saggitt.omega.allapps;

import android.content.Context;
import android.os.Process;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.util.ItemInfoMatcher;

public class AllAppsPages {
    private final ItemInfoMatcher allAppMatcher;
    private final Context mContext;
    private int mNumRows = 0;
    private int mNumCols = 0;
    private int mAppsPerPage = 0;
    private int mPagesCount = 0;

    public AllAppsPages(Context context) {
        mContext = context;
        allAppMatcher = ItemInfoMatcher.ofUser(Process.myUserHandle());
        DeviceProfile grid = Launcher.getLauncher(context).getDeviceProfile();
        mNumCols = grid.inv.numColumns;
        mNumRows = grid.inv.numRows;
        mAppsPerPage = mNumCols * mNumRows;
    }

    public ItemInfoMatcher getMatcher() {
        return allAppMatcher;
    }

    public int getPageCount() {
        return mPagesCount;
    }
}

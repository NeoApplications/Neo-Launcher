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

import android.view.View;

import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsPagedView;
import com.android.launcher3.allapps.AllAppsStore;

import java.util.ArrayList;

public class PagedAppsController {

    private ArrayList holders = new ArrayList<AllAppsContainerView.AdapterHolder>();
    private AllAppsContainerView mContainer;
    private int horizontalPadding = 0;
    private int bottomPadding = 0;

    public PagedAppsController(AllAppsPages pages, AllAppsContainerView container) {
        mContainer = container;
    }

    public AllAppsContainerView.AdapterHolder[] createHolders() {
        while (holders.size() < getPagesCount()) {
            AllAppsContainerView.AdapterHolder holder = mContainer.createHolder(false);
            holder.padding.bottom = bottomPadding;
            holder.padding.left = horizontalPadding;
            holder.padding.right = horizontalPadding;

            holders.add(holder);
        }
        Object[] ret = holders.toArray(new Object[0]);
        return (AllAppsContainerView.AdapterHolder[]) ret;

    }

    public void setPadding(int leftRightPadding, int bottom) {
    }

    public void reloadPages() {
    }

    public boolean getShouldShowTabs() {
        return false;
    }

    public void registerIconContainers(AllAppsStore mAllAppsStore) {
    }

    public void unregisterIconContainers(AllAppsStore mAllAppsStore) {
    }

    public void setup(View viewById) {
    }

    public void setup(AllAppsPagedView mViewPager) {
    }

    public int getPagesCount() {
        return 1;
    }
}

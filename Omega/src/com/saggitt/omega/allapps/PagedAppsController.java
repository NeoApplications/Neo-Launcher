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
    private ArrayList<AllAppsContainerView.AdapterHolder> holders = new ArrayList<>();
    private AllAppsContainerView mContainer;
    private AllAppsPages mPages;
    private int horizontalPadding = 0;
    private int bottomPadding = 0;

    public PagedAppsController(AllAppsPages pages, AllAppsContainerView container) {
        mContainer = container;
        mPages = pages;
    }

    public AllAppsContainerView.AdapterHolder[] createHolders() {
        while (holders.size() < getPagesCount()) {
            AllAppsContainerView.AdapterHolder holder = mContainer.createHolder(false);
            holder.padding.bottom = bottomPadding;
            holder.padding.left = horizontalPadding;
            holder.padding.right = horizontalPadding;

            holders.add(holder);
        }
        return holders.toArray(new AllAppsContainerView.AdapterHolder[0]);

    }

    public void setPadding(int leftRightPadding, int bottom) {
        horizontalPadding = leftRightPadding;
        bottomPadding = bottom;
        for (AllAppsContainerView.AdapterHolder holder : holders) {
            holder.padding.bottom = bottomPadding;
            holder.padding.left = horizontalPadding;
            holder.padding.right = horizontalPadding;
            holder.applyPadding();
        }
    }

    public void reloadPages() {
    }

    public boolean getShouldShowTabs() {
        return false;
    }

    public void registerIconContainers(AllAppsStore mAllAppsStore) {
        for (AllAppsContainerView.AdapterHolder holder : holders) {
            mAllAppsStore.registerIconContainer(holder.recyclerView);
        }
    }

    public void unregisterIconContainers(AllAppsStore mAllAppsStore) {
        for (AllAppsContainerView.AdapterHolder holder : holders) {
            mAllAppsStore.unregisterIconContainer(holder.recyclerView);
        }
    }

    public void setup(View view) {
        for (AllAppsContainerView.AdapterHolder holder : holders) {
            holder.recyclerView = null;
        }
        holders.get(0).setup(view, null);
    }

    public void setup(AllAppsPagedView pagedView) {
        for (AllAppsContainerView.AdapterHolder holder : holders) {
            holder.setIsWork(false);
            holder.setup(pagedView.getChildAt(holders.lastIndexOf(holder)), mPages.getMatcher());
        }
    }

    public int getPagesCount() {
        return mPages.getPageCount();
    }
}

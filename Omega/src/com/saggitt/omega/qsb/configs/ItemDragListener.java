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
package com.saggitt.omega.qsb.configs;

import android.content.pm.LauncherActivityInfo;
import android.graphics.Rect;
import android.view.View;

import com.android.launcher3.InstallShortcutReceiver;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.WorkspaceItemInfo;
import com.android.launcher3.compat.ShortcutConfigActivityInfo.ShortcutConfigActivityInfoVO;
import com.android.launcher3.dragndrop.BaseItemDragListener;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingItemDragHelper;

public class ItemDragListener extends BaseItemDragListener {

    public LauncherActivityInfo mActivityInfo;

    public ItemDragListener(LauncherActivityInfo activityInfo, Rect rect) {
        super(rect, rect.width(), rect.width());
        mActivityInfo = activityInfo;
    }

    public PendingItemDragHelper createDragHelper() {
        PendingAddShortcutInfo tag = new PendingAddShortcutInfo(new ShortcutConfigActivityInfoVO(mActivityInfo) {
            public WorkspaceItemInfo createShortcutInfo() {
                return InstallShortcutReceiver.fromActivityInfo(mActivityInfo, mLauncher);
            }
        });
        View view = new View(mLauncher);
        view.setTag(tag);
        return new PendingItemDragHelper(view);
    }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, Target target, Target targetParent) {
    }
}

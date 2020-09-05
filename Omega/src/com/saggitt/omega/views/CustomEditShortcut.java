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

package com.saggitt.omega.views;

import android.content.Context;
import android.view.View;

import androidx.annotation.Keep;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.popup.SystemShortcut;
import com.saggitt.omega.override.CustomInfoProvider;

@Keep
public class CustomEditShortcut extends SystemShortcut.Custom {
    public CustomEditShortcut(Context context) {
        super();
    }

    @Override
    public View.OnClickListener getOnClickListener(final Launcher launcher, final ItemInfo itemInfo) {
        boolean enabled = CustomInfoProvider.Companion.isEditable(itemInfo);
        return enabled ? new View.OnClickListener() {
            private boolean mOpened = false;

            @Override
            public void onClick(View view) {
                if (!mOpened) {
                    mOpened = true;
                    AbstractFloatingView.closeAllOpenViews(launcher);
                    CustomBottomSheet.show(launcher, itemInfo);
                }
            }
        } : null;
    }
}

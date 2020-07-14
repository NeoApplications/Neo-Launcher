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

package com.saggitt.omega.icons;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.util.ComponentKey;
import com.saggitt.omega.icons.calendar.DateChangeReceiver;
import com.saggitt.omega.icons.calendar.DynamicCalendar;
import com.saggitt.omega.icons.clock.CustomClock;
import com.saggitt.omega.icons.clock.DynamicClock;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;

public class DynamicDrawableFactory extends DrawableFactory {
    private final DynamicClock mDynamicClockDrawer;
    private final CustomClock mCustomClockDrawer;
    private final DateChangeReceiver mCalendars;
    private Context mContext;

    public DynamicDrawableFactory(Context context) {
        mContext = context;
        if (Utilities.ATLEAST_OREO) {
            mDynamicClockDrawer = new DynamicClock(context);
            mCustomClockDrawer = new CustomClock(context);
        } else {
            mDynamicClockDrawer = null;
            mCustomClockDrawer = null;
        }
        mCalendars = new DateChangeReceiver(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public FastBitmapDrawable newIcon(Context context, ItemInfoWithIcon info) {
        if (info != null && info.getTargetComponent() != null && info.itemType == ITEM_TYPE_APPLICATION) {
            ComponentKey key = new ComponentKey(info.getTargetComponent(), info.user);
            mCalendars.setIsDynamic(key, info.getTargetComponent().getPackageName().equals(DynamicCalendar.CALENDAR));
            if (Utilities.ATLEAST_OREO) {
                if (info.getTargetComponent().equals(DynamicClock.DESK_CLOCK)) {
                    return mDynamicClockDrawer.drawIcon(info);
                }
            }
        }
        return super.newIcon(context, info);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public FastBitmapDrawable newIcon(BitmapInfo icon, ActivityInfo info) {
        return super.newIcon(icon, info);
    }
}

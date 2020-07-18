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

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.Bundle;

import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.util.ComponentKey;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.search.AppSearchProvider;

import java.lang.ref.WeakReference;

public class LongClickReceiver extends BroadcastReceiver {

    public static WeakReference<OmegaLauncher> mWeakReference = new WeakReference(null);

    public static void getWeakReference(OmegaLauncher launcher) {
        mWeakReference = new WeakReference(launcher);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        OmegaLauncher launcher = (OmegaLauncher) mWeakReference.get();
        if (launcher != null) {
            ComponentKey searchUri = AppSearchProvider.uriToComponent(intent.getData(), context);
            LauncherActivityInfo resolveActivity = LauncherAppsCompat.getInstance(context)
                    .resolveActivity(new Intent("android.intent.action.MAIN")
                            .setComponent(searchUri.componentName), searchUri.user);
            if (resolveActivity != null) {
                ItemDragListener onDragListener = new ItemDragListener(resolveActivity, intent.getSourceBounds());
                onDragListener.init(launcher, false);
                launcher.getDragLayer().setOnDragListener(onDragListener);
                ClipData clipData = new ClipData(new ClipDescription("", new String[]{onDragListener.getMimeType()}), new Item(""));
                Bundle bundle = new Bundle();
                bundle.putParcelable("clip_data", clipData);
                setResult(-1, null, bundle);
            }
        }
    }
}

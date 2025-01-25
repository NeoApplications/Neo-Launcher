/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.launcher3.icons.cache

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.os.Build.VERSION
import android.os.UserHandle
import android.util.Log
import com.android.launcher3.Flags.useNewIconForArchivedApps
import com.android.launcher3.icons.BaseIconFactory.IconOptions
import com.android.launcher3.icons.BitmapInfo

object LauncherActivityCachingLogic : CachingLogic<LauncherActivityInfo> {
    const val TAG = "LauncherActivityCachingLogic"

    override fun getComponent(info: LauncherActivityInfo): ComponentName = info.componentName

    override fun getUser(info: LauncherActivityInfo): UserHandle = info.user

    override fun getLabel(info: LauncherActivityInfo): CharSequence? = info.label

    override fun getDescription(info: LauncherActivityInfo, fallback: CharSequence) = fallback

    override fun loadIcon(
        context: Context,
        cache: BaseIconCache,
        info: LauncherActivityInfo,
    ): BitmapInfo {
        cache.iconFactory.use { li ->
            val iconOptions: IconOptions = IconOptions().setUser(info.user)
            iconOptions.setIsArchived(
                useNewIconForArchivedApps() && VERSION.SDK_INT >= 35 && info.activityInfo.isArchived
            )
            val iconDrawable = cache.iconProvider.getIcon(info, li.fullResIconDpi)
            if (
                VERSION.SDK_INT >= 30 &&
                    context.packageManager.isDefaultApplicationIcon(iconDrawable)
            ) {
                Log.w(
                    TAG,
                    "loadIcon: Default app icon returned from PackageManager." +
                        " component=${info.componentName}, user=${info.user}",
                    Exception(),
                )
                // Make sure this default icon always matches BaseIconCache#getDefaultIcon
                return cache.getDefaultIcon(info.user)
            }
            return li.createBadgedIconBitmap(iconDrawable, iconOptions)
        }
    }
}

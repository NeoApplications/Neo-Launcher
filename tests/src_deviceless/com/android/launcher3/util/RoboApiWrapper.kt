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

package com.android.launcher3.util

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Looper
import android.os.Process
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream
import java.util.function.Supplier
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

object RoboApiWrapper {

    fun initialize() {
        Shadows.shadowOf(
                RuntimeEnvironment.getApplication().getSystemService(LauncherApps::class.java)
            )
            .addEnabledPackage(
                Process.myUserHandle(),
                InstrumentationRegistry.getInstrumentation().context.packageName
            )
        LauncherModelHelper.ACTIVITY_LIST.forEach {
            installApp(ComponentName(InstrumentationRegistry.getInstrumentation().context, it))
        }
    }

    private fun installApp(componentName: ComponentName) {
        val app = RuntimeEnvironment.getApplication()
        val user = Process.myUserHandle()

        val pm = Shadows.shadowOf(app.packageManager)
        val ai = pm.addActivityIfNotPresent(componentName)
        pm.addIntentFilterForActivity(
            componentName,
            IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        )

        val li = Mockito.mock(LauncherActivityInfo::class.java)
        val appInfo = ApplicationInfo().apply { flags = 0 }
        Mockito.doReturn(ai).whenever(li).activityInfo
        Mockito.doReturn(appInfo).whenever(li).applicationInfo
        Mockito.doReturn(user).whenever(li).user
        Mockito.doReturn(1f).whenever(li).loadingProgress
        Mockito.doReturn(componentName).whenever(li).componentName

        Shadows.shadowOf(app.getSystemService(LauncherApps::class.java)).apply {
            addActivity(user, li)
            addEnabledPackage(user, componentName.packageName)
            setActivityEnabled(user, componentName)
            addApplicationInfo(user, componentName.packageName, ai.applicationInfo)
        }
    }

    fun registerInputStream(
        contentResolver: ContentResolver,
        uri: Uri,
        inputStreamSupplier: Supplier<InputStream>
    ) {
        Shadows.shadowOf(contentResolver).registerInputStreamSupplier(uri, inputStreamSupplier)
    }

    fun waitForLooperSync(looper: Looper) {
        Shadows.shadowOf(looper).runToEndOfTasks()
    }
}

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

package com.saggitt.omega.states

import com.android.launcher3.LauncherAnimUtils
import com.android.launcher3.LauncherState
import com.android.launcher3.userevent.nano.LauncherLogProto

class DashState(id: Int) : LauncherState(id, LauncherLogProto.ContainerType.OVERVIEW,
        LauncherAnimUtils.SPRING_LOADED_TRANSITION_MS, STATE_FLAGS) {

    companion object {
        private const val STATE_FLAGS = FLAG_MULTI_PAGE or FLAG_DISABLE_RESTORE or
                FLAG_DISABLE_PAGE_CLIPPING or FLAG_PAGE_BACKGROUNDS
    }
}
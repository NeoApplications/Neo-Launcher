/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.launcher3.util;

import static com.android.launcher3.icons.BitmapInfo.FLAG_CLONE;
import static com.android.launcher3.icons.BitmapInfo.FLAG_PRIVATE;
import static com.android.launcher3.icons.BitmapInfo.FLAG_WORK;

import android.os.UserHandle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

/**
 * Data class which stores various properties of a {@link android.os.UserHandle}
 * which affects rendering
 */
public class UserIconInfo {

    public static final int TYPE_MAIN = 0;
    public static final int TYPE_WORK = 1;
    public static final int TYPE_CLONED = 2;

    public static final int TYPE_PRIVATE = 3;

    @IntDef({TYPE_MAIN, TYPE_WORK, TYPE_CLONED, TYPE_PRIVATE})
    public @interface UserType { }

    public final UserHandle user;
    @UserType
    public final int type;

    public final long userSerial;

    public UserIconInfo(UserHandle user, @UserType int type) {
        this(user, type, user != null ? user.hashCode() : 0);
    }

    public UserIconInfo(UserHandle user, @UserType int type, long userSerial) {
        this.user = user;
        this.type = type;
        this.userSerial = userSerial;
    }

    public boolean isMain() {
        return type == TYPE_MAIN;
    }

    public boolean isWork() {
        return type == TYPE_WORK;
    }

    public boolean isCloned() {
        return type == TYPE_CLONED;
    }

    public boolean isPrivate() {
        return type == TYPE_PRIVATE;
    }

    @NonNull
    public FlagOp applyBitmapInfoFlags(@NonNull FlagOp op) {
        return op.setFlag(FLAG_WORK, isWork())
                .setFlag(FLAG_CLONE, isCloned())
                .setFlag(FLAG_PRIVATE, isPrivate());
    }
}

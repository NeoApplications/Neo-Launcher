/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 */

package android.app;

public class ActivityThread {
    Application mInitialApplication;
    private static volatile ActivityThread sCurrentActivityThread;

    public static Application currentApplication() {
        ActivityThread am = currentActivityThread();
        return am != null ? am.mInitialApplication : null;
    }

    public static ActivityThread currentActivityThread() {
        return sCurrentActivityThread;
    }
}

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

package com.neoapps.launcher.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
open class NavRoute : Parcelable {
    @Serializable
    open class Main : NavRoute()

    @Serializable
    open class Profile : NavRoute() {
        @Serializable
        class IconShape : Profile()

        @Serializable
        class AccentColor : Profile()
    }

    @Serializable
    open class Desktop : NavRoute() {
        @Serializable
        class FolderBG : Desktop()

        @Serializable
        class FolderStroke : Desktop()
    }

    @Serializable
    open class Dock : NavRoute() {
        @Serializable
        class BG : Dock()
    }

    @Serializable
    open class Drawer : NavRoute() {
        @Serializable
        class HiddenApps : Drawer()

        @Serializable
        class ProtectedApps : Drawer()

        @Serializable
        class ProtectedAppsView : Drawer()

        @Serializable
        class Categorize : Drawer()

        @Serializable
        class BG : Drawer()
    }

    @Serializable
    open class Widgets : NavRoute() {
        @Serializable
        class NotificationDots : Widgets()
    }

    @Serializable
    open class Gestures : NavRoute() {
        @Serializable
        data class Gesture(val key: String) : Gestures()

        @Serializable
        class EditDash : Gestures()
    }

    @Serializable
    open class Search : NavRoute() {
        @Serializable
        class SearchProviders : Search()
    }

    @Serializable
    class Backup : NavRoute()

    @Serializable
    open class Dev : NavRoute() {
        @Serializable
        class Categorize : Dev()
    }

    @Serializable
    open class About : NavRoute() {
        @Serializable
        class License : About()

        @Serializable
        class Translators : About()

        @Serializable
        class Changelog : About()
    }

    @Serializable
    data class EditIcon(val packageName: String, val name: String, val user: Int) : NavRoute()
}
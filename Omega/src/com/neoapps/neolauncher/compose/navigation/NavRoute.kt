package com.neoapps.neolauncher.compose.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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
        class Acknowledgement : About()
        @Serializable
        class Changelog : About()
    }

    @Serializable
    data class EditIcon(val packageName: String, val name: String, val user: Int) : NavRoute()
}
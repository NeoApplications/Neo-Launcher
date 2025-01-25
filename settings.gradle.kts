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

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}
include(":iconloaderlib")
project(":iconloaderlib").projectDir = File(rootDir, "libs_systemui/iconloaderlib")

include(":animationlib")
project(":animationlib").projectDir = File(rootDir, "libs_systemui/animationlib")

rootProject.name = "Neo Launcher"
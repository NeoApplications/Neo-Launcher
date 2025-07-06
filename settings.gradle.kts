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

include(":flags")
project(":flags").projectDir = File(rootDir, "flags")

include(":hidden-api")
project(":hidden-api").projectDir = File(rootDir, "hidden-api")

include(":smartspace")
project(":smartspace").projectDir = File(rootDir, "libs_systemui/smartspace")
rootProject.name = "Neo Launcher"
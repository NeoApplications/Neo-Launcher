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
include(":msdllib")
project(":msdllib").projectDir = File(rootDir, "libs_systemui/msdllib")
include(":contextualeducationlib")
project(":contextualeducationlib").projectDir =
    File(rootDir, "libs_systemui/contextualeducationlib")
include(":flags")
project(":flags").projectDir = File(rootDir, "flags")

include(":hidden-api")
project(":hidden-api").projectDir = File(rootDir, "hidden-api")

include(":plugincore")
project(":plugincore").projectDir = File(rootDir, "libs_systemui/plugin_core")

include(":log")
project(":log").projectDir = File(rootDir, "libs_systemui/log")

include(":smartspace")
project(":smartspace").projectDir = File(rootDir, "libs_systemui/smartspace")
include(":common")
project(":common").projectDir = File(rootDir, "libs_systemui/common")

include(":utils")
project(":utils").projectDir = File(rootDir, "libs_systemui/utils")
include(":shared")
project(":shared").projectDir = File(rootDir, "shared")

include(":concurrent")
project(":concurrent").projectDir = File(rootDir, "modules/concurrent")
include(":widgetpicker")
project(":widgetpicker").projectDir = File(rootDir, "modules/widgetpicker")

include(":dagger")
project(":dagger").projectDir = File(rootDir, "dagger")

include (":wmshell")
include (":compatLib")
include (":compatLib:compatLibVQ")
include (":compatLib:compatLibVR")
include (":compatLib:compatLibVS")
include (":compatLib:compatLibVT")
include (":compatLib:compatLibVU")
include (":compatLib:compatLibVV")
rootProject.name = "Neo Launcher"
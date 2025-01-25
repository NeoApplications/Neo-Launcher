plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.android.launcher3.icons"
    compileSdk = 35
    sourceSets {
        named("main") {
            java.setSrcDirs(listOf("src", "src_full_lib"))
            manifest.srcFile("AndroidManifest.xml")
            res.setSrcDirs(listOf("res"))
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
}

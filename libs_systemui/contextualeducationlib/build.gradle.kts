plugins {
    alias(libs.plugins.android.library)
}
android {
    compileSdk = 36
    namespace = "com.android.systemui.contextualeducation"

    sourceSets {
        getByName("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
            res.directories.add("res")
            manifest.srcFile("AndroidManifest.xml")
        }
    }
}

plugins {
    alias(libs.plugins.android.library)
}

android {
    compileSdk = 36
    namespace = "com.android.launcher3.icons"

    defaultConfig {
        minSdk = 30
    }

    sourceSets {
        named("main") {
            java.directories.addAll(listOf("src","src_full_lib"))
            kotlin.directories.addAll(listOf("src","src_full_lib"))
            manifest.srcFile("AndroidManifest.xml")
            res.directories.add("res")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":flags"))
    implementation(libs.core.ktx)
    implementation(libs.core.animation)
    implementation(libs.koin.android)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.rules)
}

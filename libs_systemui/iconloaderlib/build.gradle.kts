plugins {
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(21)
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
    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        register("neo") {
        }
        release {
        }
    }
}

dependencies {
    implementation(project(":flags"))
    implementation(libs.core.ktx)
    implementation(libs.core.animation)
    implementation(libs.koin.android)
    implementation(libs.palette.ktx)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.rules)
}

plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.android.launcher3.icons"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testOptions.targetSdk = 35
    }
    sourceSets {
        named("main") {
            java.setSrcDirs(listOf("src", "src_full_lib"))
            manifest.srcFile("AndroidManifest.xml")
            res.setSrcDirs(listOf("res"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(project(":flags"))
}

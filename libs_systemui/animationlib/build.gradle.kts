plugins{
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(21)
}

android {
    compileSdk = 36
    namespace = "com.android.systemui.animationlib"
    testNamespace = "com.android.systemui.animationlib.test"

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
            res.directories.add("res")
            manifest.srcFile("AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.directories.addAll(listOf("test/src", "test/robolectric/src"))
            manifest.srcFile("test/AndroidManifest.xml")
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
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies{
    implementation(project(":flags"))
    implementation(libs.core.animation)
    implementation(libs.core.ktx)
    implementation(libs.kotlin.stdlib.jdk7)
    androidTestImplementation (libs.junit)
    androidTestImplementation (libs.rules)
}
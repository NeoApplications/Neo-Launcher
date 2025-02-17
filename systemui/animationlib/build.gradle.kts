plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.android.launcher3.animation"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        create("neo") {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        named("main") {
            java.srcDirs(listOf("src", "src_full_lib"))
            manifest.srcFile("AndroidManifest.xml")
            res.srcDirs(listOf("res"))
        }
    }

    lint {
        abortOnError = false
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.animation)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.rules)
}

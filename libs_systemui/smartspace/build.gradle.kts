plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.saulhdev.smartspace"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    buildTypes {
        create("neo") {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        named("main") {
            java.srcDirs(listOf("src"))
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.palette)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.annotation)
}

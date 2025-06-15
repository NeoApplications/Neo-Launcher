plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.saulhdev.smartspace"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.palette)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.annotation)
}

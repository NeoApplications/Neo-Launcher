plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "android.app.smartspace"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    sourceSets {
        named("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
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
    implementation(libs.core.ktx)
    implementation(libs.palette.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.annotation)
}

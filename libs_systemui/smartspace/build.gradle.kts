plugins {
    alias(libs.plugins.android.library)
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
            kotlin.srcDirs(listOf("src"))
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
    implementation(libs.core.ktx)
    implementation(libs.palette.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.annotation)
}

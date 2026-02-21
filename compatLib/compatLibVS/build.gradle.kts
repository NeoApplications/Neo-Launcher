import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(21)
}

android {
    compileSdk = 36
    namespace = "app.lawnchair.compatlib.twelve"
    buildFeatures {
        aidl = true
    }

    sourceSets {
        getByName("main") {
            kotlin.directories.add("src/main/java")
            java.directories.add("src/main/java")
            aidl.directories.add("src/main/java")
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

val FRAMEWORK_PREBUILTS_DIR = "$rootDir/prebuilt/libs"
val addFrameworkJar = { name: String ->
    val frameworkJar = File(FRAMEWORK_PREBUILTS_DIR, name)
    if (!frameworkJar.exists()) {
        throw IllegalArgumentException("Framework jar path ${frameworkJar.path} doesn't exist")
    }
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile>().configureEach {
            classpath = files(frameworkJar, classpath)
        }
        tasks.withType<KotlinCompile>().configureEach {
            libraries.setFrom(files(frameworkJar, libraries))
        }
    }
    dependencies {
        compileOnly(files(frameworkJar))
    }
}
addFrameworkJar("framework-12.jar")

dependencies {
    api(project(":compatLib:compatLibVR"))
}

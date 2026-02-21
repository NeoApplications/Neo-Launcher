import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
}


android {
    compileSdk = 36
    namespace = "com.google.android.msdl"
    testNamespace = "com.google.android.msdl.tests"
    defaultConfig {
        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
            manifest.srcFile("AndroidManifest.xml")
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
addFrameworkJar("framework-16.jar")

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.core.animation)
    implementation(libs.core.ktx)
}

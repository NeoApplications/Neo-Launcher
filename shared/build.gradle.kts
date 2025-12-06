import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
}
android {
    namespace = "com.neoapps.shared"
    compileSdk = 36
    buildFeatures {
        aidl = true
    }

    sourceSets {
        getByName("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
            aidl.directories.add("src")
            res.directories.add("res")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
    annotationProcessor("dev.rikka.tools.refine:annotation-processor:4.4.0")
    compileOnly("dev.rikka.tools.refine:annotation:4.4.0")
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/framework-16.jar"))
}

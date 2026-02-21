import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.android.wm.shell"
    compileSdk = 36
    defaultConfig {
        minSdk = 30
    }
    sourceSets {
        named("main") {
            java.directories.add("shared/src")
            kotlin.directories.add("shared/src")
            aidl.directories.addAll(listOf("shared/src", "aidl"))
            manifest.srcFile("AndroidManifest.xml")
            res.directories.addAll(listOf("shared/res", "shared"))
        }

        protobuf {
            protoc {
                artifact = "com.google.protobuf:protoc:3.25.3"
            }
            generateProtoTasks {
                all().forEach { task ->
                    task.builtins {
                        create("java") {
                            option("lite")
                        }
                    }
                }
            }
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

    packaging {
        resources.excludes += "META-INF/gradle/incremental.annotation.processors"
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        aidl = true
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

dependencies{
    implementation(libs.compose.material3)
    implementation(libs.constraint.layout)
    implementation(libs.core.animation)
    implementation(libs.dynamic.animation)
    implementation(libs.protobuf.javalite)
    implementation(libs.material)
    implementation(libs.recyclerview)

    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.hilt.compiler)
    protobuf(files("proto/"))

    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/SystemUI-statsd-16.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/SystemUI-core.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/WindowManager-Shell-16.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/wmshell-aidls.jar"))

    compileOnly(project(":hidden-api"))
    compileOnly(project(":flags"))
    compileOnly(project(":iconloaderlib"))
    compileOnly(project(":compatLib"))
    compileOnly(project(":compatLib:compatLibVQ"))
    compileOnly(project(":compatLib:compatLibVR"))
    compileOnly(project(":compatLib:compatLibVS"))
    compileOnly(project(":compatLib:compatLibVT"))
    compileOnly(project(":compatLib:compatLibVU"))
    compileOnly(project(":compatLib:compatLibVV"))
}
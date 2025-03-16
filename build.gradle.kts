/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 */

import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

val vProtobuf = "3.25.3"

buildscript {
    dependencies {
        classpath(libs.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
            )
        }
    }
}

android {
    namespace = "com.android.launcher3"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        applicationId = "com.neoapps.neolauncher"

        versionName = "2.0.0-alpha01"
        versionCode = 2001

        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")
        buildConfigField("boolean", "IS_DEBUG_DEVICE", "false")
        buildConfigField("boolean", "IS_STUDIO_BUILD", "false")
        buildConfigField("boolean", "QSB_ON_FIRST_SCREEN", "true")
        buildConfigField("boolean", "WIDGET_ON_FIRST_SCREEN", "true")
        buildConfigField("boolean", "WIDGETS_ENABLED", "true")
        buildConfigField("boolean", "NOTIFICATION_DOTS_ENABLED", "true")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                }
            }
        }
        gradle.projectsEvaluated {
            val frameworkJar = File("prebuilts/libs/", "framework-15.jar")
            tasks.withType<JavaCompile>().configureEach {
                classpath = files(frameworkJar, classpath)
            }
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Launcher_${variant.versionName}_${variant.buildType.name}.apk"
        }
        variant.resValue(
            "string",
            "launcher_component",
            "${variant.applicationId}/com.neoapps.neolauncher.NeoLauncher"
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".alpha"
            signingConfig = signingConfigs.getByName("debug")
        }
        register("neo") {
            isMinifyEnabled = false
            applicationIdSuffix = ".neo"
        }

        release {
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard.flags"))
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("primary") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }

    plugins.withType<JavaBasePlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    packaging {
        jniLibs {
            pickFirsts += listOf("**/libeasyBypass.so")
        }
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin") // TODO remove when issue is fixed (https://github.com/Kotlin/kotlinx.coroutines/issues/3668)
    }

    flavorDimensionList.clear()
    flavorDimensionList.addAll(listOf("app", "custom"))

    productFlavors {
        create("aosp") {
            dimension = "app"
            applicationId = "com.neoapps.neolauncher"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            res.srcDirs(listOf("res"))
            java.srcDirs(listOf("src", "src_plugins", "src_no_quickstep"))
            assets.srcDirs(listOf("assets"))
            manifest.srcFile("AndroidManifest-common.xml")
        }

        named("aosp") {
            java.srcDirs(listOf("src_flags", "src_shortcuts_overrides"))
        }

        named("omega") {
            res.srcDirs(listOf("neo_launcher/res"))
            java.srcDirs(listOf("neo_launcher/src"))
            aidl.srcDirs(listOf("neo_launcher/aidl"))
            manifest.srcFile("neo_launcher/AndroidManifest.xml")
        }

        protobuf {
            // Configure the protoc executable
            protoc {
                artifact = "com.google.protobuf:protoc:$vProtobuf"
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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {
    implementation(project(":animationlib"))
    implementation(project(":iconloaderlib"))
    implementation(project(":plugincore"))
    implementation(project(":flags"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp)
    implementation(libs.collections.immutable)

    //UI
    implementation(libs.androidx.core)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.coordinator.layout)
    implementation(libs.androidx.dynamic.animation)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.recyclerview)

    implementation(libs.material)
    implementation(libs.material.kolor)

    implementation(libs.preference)
    implementation(libs.datastore.preferences)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.extensions)

    implementation(libs.slice)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    //Libs
    implementation(libs.protobuf.javalite)
    implementation(libs.restriction.bypass)
    implementation(libs.coroutines.android)

    api(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.androidx.startup)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.koin.androidx.workmanager)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.persian.date)
    implementation(libs.owm) {
        exclude("com.android.support", "support-compat")
        exclude("com.android.support", "appcompat-v7")
    }
    implementation(libs.alwan)
    implementation(libs.hoko.blur)
    implementation(libs.fuzzywuzzy)

    //Compose
    api(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.adaptive.layout)
    implementation(libs.compose.adaptive.navigation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.drawablepainter)

    //Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Jars
    compileOnly(fileTree(baseDir = "libs").include("SystemUI-core.jar"))
    compileOnly(fileTree(baseDir = "libs").include("SystemUI-statsd-15.jar"))
    compileOnly(fileTree(baseDir = "libs").include("WindowManager-Shell-15.jar"))
    compileOnly(fileTree(baseDir = "libs").include("framework-15.jar"))

    protobuf(files("protos/"))
    protobuf(files("protos_overrides/"))

    //Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit5)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockito)
    androidTestImplementation(libs.dexmaker.lib)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.androidx.annotation)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.support.runner)
    androidTestImplementation(libs.support.rules)
    androidTestImplementation(libs.support.uiautomator)
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile?.name?.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            if (languageCode != null) {
                langList.add(languageCode)
            }
        }
    }
    val langListString = "{${langList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField(
        "String[]",
        "DETECTED_ANDROID_LOCALES",
        langListString
    )
}
tasks.preBuild.dependsOn("detectAndroidLocals")

// Returns the build date in a RFC3339 compatible format. TZ is always converted to UTC
fun getBuildDate(): String {
    val RFC3339_LIKE = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    RFC3339_LIKE.timeZone = TimeZone.getTimeZone("UTC")
    return RFC3339_LIKE.format(Date())
}
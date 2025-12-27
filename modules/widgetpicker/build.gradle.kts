/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}
android.buildFeatures.compose = true

android {
    compileSdk = 36
    namespace = "com.android.launcher3.widgetpicker"
    testNamespace = "com.android.launcher3.widgetpicker.tests"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testApplicationId = "com.android.launcher3.widgetpicker.tests"
    }
    sourceSets {
        getByName("main") {
            java.directories.addAll(listOf("src"))
            kotlin.directories.addAll(listOf("src"))
            manifest.srcFile("AndroidManifest.xml")
            res.directories.addAll(listOf("res"))
        }
        /*
        getByName("androidTest") {
            java.directories.addAll(
                listOf(
                    "tests/multivalentScreenshotTests/src",
                    "tests/multivalentTestsForDevice/src",
                )
            )
            kotlin.directories.addAll(
                    listOf(
                        "tests/multivalentScreenshotTests/src",
                        "tests/multivalentTestsForDevice/src",
                    )
                    )
            res.directories.addAll(
                listOf(
                    "tests/multivalentScreenshotTests/res",
                    "res"
                )
            )
            manifest.srcFile("tests/AndroidManifest.xml")
        }
        getByName("test") {
            java.directories.addAll(listOf("tests/multivalentTests/src"))
            kotlin.directories.addAll(listOf("tests/multivalentTests/src"))
            resources.directories.addAll(listOf("tests/config"))
            manifest.srcFile("tests/AndroidManifest.xml")
            res.directories.addAll(listOf("tests/multivalentScreenshotTests/res"))
        }
        */
    }
    /*
    signingConfigs {
        getByName("debug") {
            // This is necessary or the private APIs from the studiow-generate SDK won't work.
            // Without the platform keystore, it will crash with:
            // "java.lang.NoSuchMethodError: No static method asyncTraceForTrackBegin"
            storeFile = file("$androidTop/vendor/google/certs/devkeys/platform.keystore")
        }
    }*/

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    // Exclude META-INF for running test with android studio
    packagingOptions.resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.hilt.compiler)

    // Compose UI dependencies
    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)

    // Other UI dependencies
    implementation(libs.compose.material3.window)
    implementation(libs.window)

    // Compose android studio preview support
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    // this needs to be modern to support JDK-17 + asm byte code.
    /*testImplementation(libs.mockito.robolectric.bytebuddy.agent)
    testImplementation(libs.mockito.robolectric.bytebuddy)
    testImplementation(libs.mockito.robolectric)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    testImplementation(libs.test.runner)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.kotlinx.coroutines.test)*/

    // Compose UI Tests
    //testApi(libs.compose.ui.test.junit4)
    //androidTestApi(libs.compose.ui.test.junit4)
    debugApi(libs.compose.ui.test.manifest)

    // Shared testing libs
    /*testImplementation(project(":RobolectricLib"))
    testImplementation(project(":SharedTestLib"))
    androidTestImplementation(project(":SharedTestLib"))
    androidTestImplementation(project(":PlatformParameterizedLib"))
    androidTestImplementation(project(":ScreenshotLib"))
    androidTestImplementation(project(":ScreenshotComposeLib"))*/

    implementation(project(":concurrent"))
    implementation(project(":dagger"))
}

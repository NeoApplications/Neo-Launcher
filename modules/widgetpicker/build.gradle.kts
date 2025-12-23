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
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.compose.compiler.get().pluginId)
}

// For the screenshot testing lib dependencies
apply<ResourceFixerPlugin>()

val androidTop = extra["ANDROID_TOP"].toString()
val robolibBuildDir = project(":RobolectricLib").layout.buildDirectory.toString()
val widgetPickerDir = "$androidTop/packages/apps/Launcher3/modules/widgetpicker"

android.buildFeatures.compose = true

android {
    namespace = "com.android.launcher3.widgetpicker"
    testNamespace = "com.android.launcher3.widgetpicker.tests"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testApplicationId = "com.android.launcher3.widgetpicker.tests"
    }
    sourceSets {
        named("main") {
            java.setSrcDirs(listOf("$widgetPickerDir/src"))
            manifest.srcFile("$widgetPickerDir/AndroidManifest.xml")
            res.setSrcDirs(listOf("$widgetPickerDir/res"))
        }
        named("androidTest") {
            java.setSrcDirs(
                listOf(
                    "$widgetPickerDir/tests/multivalentScreenshotTests/src",
                    "$widgetPickerDir/tests/multivalentTestsForDevice/src",
                )
            )
            res.setSrcDirs(
                listOf(
                    "$widgetPickerDir/tests/multivalentScreenshotTests/res",
                    "$widgetPickerDir/res"
                )
            )
            manifest.srcFile("$widgetPickerDir/tests/AndroidManifest.xml")
        }
        named("test") {
            java.setSrcDirs(listOf("$widgetPickerDir/tests/multivalentTests/src"))
            resources.setSrcDirs(listOf("$widgetPickerDir/tests/config"))
            manifest.srcFile("$widgetPickerDir/tests/AndroidManifest.xml")
            res.setSrcDirs(listOf("$widgetPickerDir/tests/multivalentScreenshotTests/res"))
        }
    }
    signingConfigs {
        getByName("debug") {
            // This is necessary or the private APIs from the studiow-generate SDK won't work.
            // Without the platform keystore, it will crash with:
            // "java.lang.NoSuchMethodError: No static method asyncTraceForTrackBegin"
            storeFile = file("$androidTop/vendor/google/certs/devkeys/platform.keystore")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    // Exclude META-INF for running test with android studio
    packagingOptions.resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.android.processor)
    kaptAndroidTest(libs.dagger.compiler)
    kaptAndroidTest(libs.dagger.android.processor)

    // Compose UI dependencies
    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)

    // Other UI dependencies
    implementation(libs.androidx.material3.window.size.cls)
    implementation(libs.androidx.window)

    // Compose android studio preview support
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    // this needs to be modern to support JDK-17 + asm byte code.
    testImplementation(libs.mockito.robolectric.bytebuddy.agent)
    testImplementation(libs.mockito.robolectric.bytebuddy)
    testImplementation(libs.mockito.robolectric)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Compose UI Tests
    testApi(libs.compose.ui.test.junit4)
    androidTestApi(libs.compose.ui.test.junit4)
    debugApi(libs.compose.ui.test.manifest)

    // Shared testing libs
    testImplementation(project(":RobolectricLib"))
    testImplementation(project(":SharedTestLib"))
    androidTestImplementation(project(":SharedTestLib"))
    androidTestImplementation(project(":PlatformParameterizedLib"))
    androidTestImplementation(project(":ScreenshotLib"))
    androidTestImplementation(project(":ScreenshotComposeLib"))
}

// Work around for kotlin bug with symlinked source: http://b/316363701
tasks.matching { it.name.matches(Regex("widgetpicker.*compile.*TestKotlin")) }.configureEach {
    inputs.dir("$widgetPickerDir/tests/multivalentTests/src")
}

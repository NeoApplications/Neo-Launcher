import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.api.AndroidBasePlugin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.gradle.toolchains) apply false
}
val vProtobuf = "3.25.3"
val frameworkPrebuiltsDir = "$rootDir/prebuilt/libs"


allprojects {
    plugins.withType<AndroidBasePlugin>().configureEach {
        extensions.findByType<ApplicationExtension>()?.apply {
            buildToolsVersion = "36.1.0"
            compileSdk = 37

            defaultConfig {
                minSdk = 26
                targetSdk = 37
                multiDexEnabled = true
                vectorDrawables.useSupportLibrary = true
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        extensions.findByType<LibraryExtension>()?.apply {
            buildToolsVersion = "36.1.0"
            compileSdk = 37

            defaultConfig {
                minSdk = 26
                multiDexEnabled = true
                vectorDrawables.useSupportLibrary = true
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        dependencies {
            add("implementation", libs.core.ktx)
            add("implementation", platform(libs.compose.bom))
        }
    }
}

configurations.all {
    resolutionStrategy {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<ApplicationExtension> {
    namespace = "com.android.launcher3"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
        targetSdk = 37
        applicationId = "com.saggitt.omega"
        javaCompileOptions.annotationProcessorOptions.arguments["dagger.hilt.disableModulesHaveInstallInCheck"] =
            "true"
        versionName = "1.0.2"
        versionCode = 1008
        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")
        buildConfigField("boolean", "IS_DEBUG_DEVICE", "false")
        buildConfigField("boolean", "IS_STUDIO_BUILD", "false")
        buildConfigField("boolean", "WIDGETS_ENABLED", "true")
        buildConfigField("boolean", "NOTIFICATION_DOTS_ENABLED", "true")
        buildConfigField("boolean", "WIDGET_ON_FIRST_SCREEN", "true")

        val langsList =
            file("res").listFiles { dir -> dir.isDirectory && dir.name.startsWith("values") }
                //noinspection WrongGradleMethod
                ?.map { it.name.removePrefix("values-").ifEmpty { "en" }.replace("values", "en") }
                ?.distinct()
                ?.sorted()
                //noinspection WrongGradleMethod
                ?.joinToString(",") { "\"$it\"" } ?: ""

        buildConfigField("String[]", "DETECTED_ANDROID_LOCALES", "{$langsList}")
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
            isMinifyEnabled = true
            isShrinkResources = true
        }
        all {
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard.flags"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        aidl = true
    }

    packaging {
        jniLibs {
            pickFirsts += listOf("**/libeasyBypass.so")
        }
        resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin") // TODO remove when issue is fixed (https://github.com/Kotlin/kotlinx.coroutines/issues/3668)
    }


    flavorDimensions += listOf("app", "custom")

    productFlavors {
        create("aosp") {
            dimension = "app"
            applicationId = "com.saggitt.omega"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            java.directories.addAll(
                listOf(
                    "src",
                    "src_plugins",
                    "shared/src",
                    "src_no_quickstep",
                    "compose"
                )
            )
            kotlin.directories.addAll(
                listOf(
                    "src",
                    "src_plugins",
                    "shared/src",
                    "src_no_quickstep",
                    "compose"
                )
            )
            res.directories.add("res")
            assets.directories.add("assets")
            manifest.srcFile("AndroidManifest-common.xml")

            extensions.findByName("proto")?.let {
                (it as SourceDirectorySet).srcDirs("protos", "protos_overrides")
            }
        }
        named("aosp") {
            java.directories.addAll(listOf("src_flags"))
            kotlin.directories.addAll(listOf("src_flags"))
        }
        named("omega") {
            java.directories.addAll(listOf("Omega/src"))
            kotlin.directories.addAll(listOf("Omega/src"))
            res.directories.add("Omega/res")
            aidl.directories.add("Omega/aidl")
            manifest.srcFile("Omega/AndroidManifest.xml")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}
protobuf {
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

androidComponents {
    onVariants { variant ->
        variant.resValues.put(
            variant.makeResValueKey("string", "launcher_component"),
            com.android.build.api.variant.ResValue(
                "${variant.applicationId.get()}/com.neoapps.neolauncher.NeoLauncher"
            )
        )

        variant.outputs.forEach { output ->
            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                val buildTypeName = variant.buildType ?: "release"
                val versionName = output.versionName.getOrElse("unknown")
                output.outputFileName.set("Neo_Launcher_${versionName}_${buildTypeName}.apk")
            }
        }
    }
}

dependencies {

    implementation(project(":animationlib"))
    implementation(project(":concurrent"))
    implementation(project(":iconloaderlib"))
    implementation(project(":flags"))
    implementation(project(":msdllib"))
    implementation(project(":plugincore"))
    implementation(project(":shared"))
    implementation(project(":smartspace"))
    implementation(project(":widgetpicker"))
    implementation(project(":wmshell"))
    compileOnly(files("$frameworkPrebuiltsDir/framework-16.jar"))
    compileOnly(files("$frameworkPrebuiltsDir/SystemUI-core-16.jar"))
    compileOnly(files("$frameworkPrebuiltsDir/SystemUI-statsd-16.jar"))
    compileOnly(files("$frameworkPrebuiltsDir/WindowManager-Shell-16.jar"))

    implementation(libs.accompanist.drawablepainter)
    implementation(libs.alwan)
    implementation(libs.annotation)
    implementation(libs.coil.compose)
    implementation(libs.collections.immutable)
    implementation(libs.compose.activity)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.adaptive.layout)
    implementation(libs.compose.adaptive.navigation)
    implementation(libs.compose.material3.navigationsuite)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.navigation)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.constraint.layout)
    implementation(libs.coordinator.layout)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.dynamic.animation)
    implementation(libs.fuzzywuzzy)
    implementation(libs.graphics.shapes)
    implementation(libs.guava)
    implementation(libs.hilt.compiler)
    ksp(libs.hilt.android)
    implementation(libs.hokofly.hokoblur)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.kotlin.stdlib) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    implementation(libs.koin.workmanager)
    implementation(libs.jakarta.inject)
    implementation(libs.java.inject)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.material)
    implementation(libs.material.kolor)
    implementation(libs.okhttp)
    implementation(libs.owm)
    implementation(libs.palette.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.protobuf.javalite)
    //implementation(libs.restriction.bypass)
    implementation(libs.recyclerview)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.serialization.json)
    implementation(libs.slice.core)

    api(platform(libs.compose.bom))
    protobuf(files("protos/"))
    protobuf(files("protos_overrides/"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.uiautomator.v18)

    androidTestImplementation(libs.dexmaker.lib)
}

fun getBuildDate(): String {
    val rfc3339 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    rfc3339.timeZone = TimeZone.getTimeZone("UTC")
    return rfc3339.format(Date())
}
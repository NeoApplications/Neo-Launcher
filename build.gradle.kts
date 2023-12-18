import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
    }
}

val vAccompanist = "0.32.0"
val vActivity = "1.8.1"
val vAlwan = "1.0.1"
val vCoil = "2.5.0"
val vComposeBOM = "2023.10.01"
val vComposeCompiler = "1.5.6"
val vConstraintLayout = "2.1.4"
val vCoordinatorLayout = "1.2.0"
val vCoroutines = "1.7.3"
val vDynamicanimation = "1.1.0-alpha03"
val vDSP = "1.0.0"
val vHokoBlur = "1.5.2"
val vKotlin = "1.9.21"
val vLifecycle = "2.6.2"
val vLifecycleExt = "2.2.0"
val vMaterial = "1.10.0"
val vNavigation = "2.7.5"
val vOkhttp = "5.0.0-alpha.12"
val vOWM = "2.1.0"
val vPersianDate = "1.7.1"
val vPalette = "1.0.0"
val vPrefs = "1.2.1"
val vProtobuf = "3.24.4"
val vReorderable = "0.9.6"
val vRoom = "2.6.1"
val vRV = "1.3.2"
val vResBypass = "2.2"
val vSerialization = "1.6.2"
val vSlice = "1.1.0-alpha02"

plugins {
    id("com.android.application").version("8.2.0")
    kotlin("android").version("1.9.21")
    kotlin("plugin.parcelize").version("1.9.21")
    kotlin("plugin.serialization").version("1.9.21")
    id("com.google.devtools.ksp").version("1.9.21-1.0.15")
    id("com.google.protobuf").version("0.9.4")
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

val prebuiltsDir: String = "prebuilts/"
android {
    namespace = "com.android.launcher3"
    compileSdk = 34

    val name = "1.0"
    val code = 950

    defaultConfig {
        minSdk = 26
        targetSdk = 33
        applicationId = "com.saggitt.omega"

        versionName = name
        versionCode = code

        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                }
            }
        }
    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "NeoLauncher_v${variant.versionName}_build_${variant.versionCode}.apk"
        }
        variant.resValue(
            "string",
            "launcher_component",
            "${variant.applicationId}/com.saggitt.omega.OmegaLauncher"
        )
        true
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".alpha"
            manifestPlaceholders["appIcon"] = "@drawable/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@drawable/ic_launcher_round_debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        create("neo") {
            isMinifyEnabled = false
            applicationIdSuffix = ".neo"
            manifestPlaceholders["appIcon"] = "@drawable/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@drawable/ic_launcher_round_debug"
        }

        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard.flags"))
            manifestPlaceholders["appIcon"] = "@drawable/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@drawable/ic_launcher_round"
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

    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = compileOptions.sourceCompatibility.toString()
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
            applicationId = "com.saggitt.omega"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            res.srcDirs(listOf("res"))
            java.srcDirs(listOf("src", "src_plugins", "src_ui_overrides"))
            assets.srcDirs(listOf("assets"))
            manifest.srcFile("AndroidManifest-common.xml")
        }

        /*named("androidTest") {
            res.srcDirs(listOf("tests/res"))
            java.srcDirs(listOf("tests/src", "tests/tapl"))
            manifest.srcFile("tests/AndroidManifest-common.xml")
        }

        named("androidTestDebug") {
            java.srcDirs("tests/src_common")
            manifest.srcFile("tests/AndroidManifest.xml")
        }*/

        named("aosp") {
            java.srcDirs(listOf("src_flags", "src_shortcuts_overrides"))
        }

        named("omega") {
            res.srcDirs(listOf("Omega/res"))
            java.srcDirs(listOf("Omega/src", "Omega/src_overrides"))
            aidl.srcDirs(listOf("Omega/aidl"))
            manifest.srcFile("Omega/AndroidManifest.xml")
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
    implementation(project(":iconloaderlib"))
    implementation(project(":animationlib"))
    implementation(project(":smartspace"))
    implementation(kotlin("stdlib", vKotlin))

    //UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:$vConstraintLayout")
    implementation("androidx.coordinatorlayout:coordinatorlayout:$vCoordinatorLayout")
    implementation("androidx.dynamicanimation:dynamicanimation:$vDynamicanimation")
    implementation("androidx.palette:palette-ktx:$vPalette")
    implementation("androidx.preference:preference-ktx:$vPrefs")
    implementation("androidx.recyclerview:recyclerview:$vRV")

    implementation("com.google.android.material:material:$vMaterial")

    implementation("androidx.datastore:datastore-preferences:$vDSP")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$vLifecycle")
    implementation("androidx.lifecycle:lifecycle-extensions:$vLifecycleExt")
    implementation("androidx.lifecycle:lifecycle-common-java8:$vLifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$vLifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$vLifecycle")
    implementation("androidx.slice:slice-core:$vSlice")

    //Libs
    implementation("com.google.protobuf:protobuf-javalite:$vProtobuf")
    implementation("com.github.ChickenHook:RestrictionBypass:$vResBypass")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$vCoroutines")
    implementation("com.squareup.okhttp3:okhttp:$vOkhttp")
    implementation("com.github.samanzamani:PersianDate:$vPersianDate")
    implementation("com.github.KwabenBerko:OpenWeatherMap-Android-Library:$vOWM") {
        exclude("com.android.support", "support-compat")
        exclude("com.android.support", "appcompat-v7")
    }
    implementation("com.raedapps:alwan:$vAlwan")
    implementation("io.github.hokofly:hoko-blur:$vHokoBlur")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$vSerialization")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")

    //Compose
    implementation("androidx.activity:activity-compose:$vActivity")
    implementation("androidx.compose.compiler:compiler:$vComposeCompiler")
    api(platform("androidx.compose:compose-bom:$vComposeBOM"))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.accompanist:accompanist-insets-ui:$vAccompanist")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$vAccompanist")
    implementation("com.google.accompanist:accompanist-drawablepainter:$vAccompanist")
    implementation("androidx.navigation:navigation-compose:$vNavigation")
    implementation("io.coil-kt:coil-compose:$vCoil")
    implementation("org.burnoutcrew.composereorderable:reorderable:$vReorderable")

    //Room
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    ksp("androidx.room:room-compiler:$vRoom")

    // Jars
    implementation(fileTree(baseDir = "${prebuiltsDir}/libs").include("SystemUI-statsd.jar"))
    implementation(fileTree(baseDir = "${prebuiltsDir}/libs").include("WindowManager-Shell.jar"))

    protobuf(files("protos/"))
    protobuf(files("protos_overrides/"))

    //Test
    testImplementation("junit:junit:4.13.2")
    implementation("junit:junit:4.13.2")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("org.mockito:mockito-core:5.0.0")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")
    androidTestImplementation("androidx.annotation:annotation:1.7.0")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("com.android.support.test.uiautomator:uiautomator-v18:2.1.3")
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField(
        "String[]",
        "DETECTED_ANDROID_LOCALES",
        langsListString
    )
}
tasks.preBuild.dependsOn("detectAndroidLocals")

@SuppressWarnings(
    "UnnecessaryQualifiedReference",
    "SpellCheckingInspection",
    "GroovyUnusedDeclaration"
)
// Returns the build date in a RFC3339 compatible format. TZ is always converted to UTC
fun getBuildDate(): String {
    val RFC3339_LIKE = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    RFC3339_LIKE.timeZone = TimeZone.getTimeZone("UTC")
    return RFC3339_LIKE.format(Date())
}
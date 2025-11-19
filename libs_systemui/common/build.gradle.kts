import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins{
    alias(libs.plugins.android.library)
}
android {
    compileSdk = 36
    namespace = "com.android.systemui.common"

    defaultConfig {
        minSdk = 30
    }

    sourceSets {
        getByName("main") {
            java.directories.add("src")
            kotlin.directories.add("src")
            res.directories.add("res")
            manifest.srcFile("AndroidManifest.xml")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    buildFeatures {
        viewBinding = true
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
addFrameworkJar("framework-15.jar")

dependencies{
    implementation(libs.core.ktx)
    implementation(project(":utils"))
    implementation(project(":plugincore"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/SystemUI-statsd-15.jar"))
    compileOnly(files("$FRAMEWORK_PREBUILTS_DIR/WindowManager-Shell-15.jar"))
}
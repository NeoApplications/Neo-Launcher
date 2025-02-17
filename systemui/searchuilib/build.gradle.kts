plugins {
    id(libs.plugins.android.library.get().pluginId)
}
android {
    namespace = "com.android.app.search"
    compileSdk = 35
    sourceSets {
        named("main") {
            java.setSrcDirs(listOf("src"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.annotation)
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun readBackendBaseUrl(): String {
    val sources = listOf(
        providers.gradleProperty("BACKEND_BASE_URL").orNull,
        localProperties.getProperty("BACKEND_BASE_URL"),
        System.getenv("BACKEND_BASE_URL")
    )

    return sources.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
}

fun readBackendApiKey(): String {
    val sources = listOf(
        providers.gradleProperty("BACKEND_API_KEY").orNull,
        localProperties.getProperty("BACKEND_API_KEY"),
        System.getenv("BACKEND_API_KEY")
    )

    return sources.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
}

android {
    namespace = "com.qcp.aioverlay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qcp.aioverlay"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Backend config - set in local.properties
        val backendUrl = project.findProperty("BACKEND_BASE_URL")?.toString() ?: "http://10.0.2.2:8080"
        val backendKey = project.findProperty("BACKEND_API_KEY")?.toString() ?: "android-dev-key-001"
        buildConfigField("String", "BACKEND_BASE_URL", "\"http://$backendUrl:8080\"")
        buildConfigField("String", "BACKEND_API_KEY", "\"$backendKey\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Lifecycle + ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Gemini AI SDK
    implementation(libs.generativeai)

    // DataStore (settings/prefs)
    implementation(libs.androidx.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
}

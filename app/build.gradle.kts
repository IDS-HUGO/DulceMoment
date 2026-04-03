import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun localOrProjectProperty(name: String, default: String = ""): String {
    val projectValue = project.findProperty(name) as String?
    return projectValue ?: localProps.getProperty(name, default)
}

fun normalizedBaseUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return trimmed
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}

android {
    namespace = "com.example.dulcemoment"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.dulcemoment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val apiBaseUrl = normalizedBaseUrl(localOrProjectProperty("API_BASE_URL", "https://apidulcemoment.ferluna.online/"))
            val paymentProvider = localOrProjectProperty("PAYMENT_PROVIDER", "stripe")
            val stripePublishableKey = localOrProjectProperty("STRIPE_PUBLISHABLE_KEY", "pk_test_51THZfYRY7KewwenDlZH97Z6WCOrWElZSr1oGrVxwRR4CrUIorUX2FiElUf6mwG5DdWJEvyWvUCDIZZih6LpNkvac00ZfRY0czV")
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            buildConfigField("String", "PAYMENT_PROVIDER", "\"stripe\"")
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableKey\"")
        }
        release {
            isMinifyEnabled = false
            val apiBaseUrl = normalizedBaseUrl(localOrProjectProperty("API_BASE_URL", "https://apidulcemoment.ferluna.online/"))
            val paymentProvider = localOrProjectProperty("PAYMENT_PROVIDER", "stripe")
            val stripePublishableKey = localOrProjectProperty("STRIPE_PUBLISHABLE_KEY", "pk_test_51THZfYRY7KewwenDlZH97Z6WCOrWElZSr1oGrVxwRR4CrUIorUX2FiElUf6mwG5DdWJEvyWvUCDIZZih6LpNkvac00ZfRY0czV")
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            buildConfigField("String", "PAYMENT_PROVIDER", "\"stripe\"")
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableKey\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.androidx.security.crypto)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.stripe.android)
    add("ksp", libs.androidx.room.compiler)
    add("ksp", libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
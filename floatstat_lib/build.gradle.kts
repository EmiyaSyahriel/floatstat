plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "id.psw.floatstat.plugins"
    compileSdk = 31

    defaultConfig {
        minSdk = 19
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.parceler:parceler-api:1.1.12")
    kapt("org.parceler:parceler:1.1.12")

    api("com.josesamuel:remoter-annotations:2.0.0")
    implementation("com.josesamuel:remoter-builder:2.0.0")
    kapt("com.josesamuel:remoter:2.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
}
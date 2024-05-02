plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "id.psw.floatstat.plugin_example"
    compileSdk = 31

    defaultConfig {
        applicationId = "id.psw.floatstat.plugin_example"
        minSdk = 19
        targetSdk = 31
        versionCode = 1
        versionName ="1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro");
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
    implementation (":floatstat_lib")
    implementation ("androidx.core:core:1.7.0")
}
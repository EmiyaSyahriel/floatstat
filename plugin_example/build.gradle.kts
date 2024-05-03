plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "id.psw.floatstat.plugin_example"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.psw.floatstat.plugin_example"
        minSdk = 19
        targetSdk = 34
        versionCode = 2
        versionName ="1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(project(mapOf("path" to ":floatstat_lib")))
}
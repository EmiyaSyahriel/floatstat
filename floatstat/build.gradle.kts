plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "id.psw.floatstat"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.psw.floatstat"
        minSdk = 19
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.4"

        testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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

    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(project(mapOf("path" to ":floatstat_lib")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
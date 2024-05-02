plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "id.psw.floatstat"
    compileSdk = 33

    defaultConfig {
        applicationId = "id.psw.floatstat"
        minSdk = 19
        targetSdk = 33
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
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
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation(project(mapOf("path" to ":floatstat_lib")))
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
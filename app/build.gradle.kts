plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.lineageos.aperture" // Tetap gunakan ini agar terdeteksi whitelist ROM
    compileSdk = 36 // Android 16 (Baklava)

    defaultConfig {
        applicationId = "org.lineageos.aperture.standalone"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0-A16-Sapphiren"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
        contextualAds = false
    }
}

dependencies {
    // CameraX Core
    val cameraxVersion = "1.4.0-rc01"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // UI & Material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    
    // LineageOS Platform (Shim)
    // Karena build standalone, kita pakai compileOnly agar tidak error saat mengakses API Lineage
    compileOnly("org.lineageos:platform:16.0") 
}

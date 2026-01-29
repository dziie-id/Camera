plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ngetes.kamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ngetes.kamera"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    val camVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$camVersion")
    implementation("androidx.camera:camera-camera2:$camVersion")
    implementation("androidx.camera:camera-lifecycle:$camVersion")
    implementation("androidx.camera:camera-view:$camVersion")
    implementation("androidx.camera:camera-extensions:$camVersion")
}

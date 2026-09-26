plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.swapbaju"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.swapbaju"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val hasSigningEnv =
                !System.getenv("ANDROID_KEYSTORE_PATH").isNullOrBlank() &&
                !System.getenv("ANDROID_KEYSTORE_PASSWORD").isNullOrBlank() &&
                !System.getenv("ANDROID_KEY_ALIAS").isNullOrBlank() &&
                !System.getenv("ANDROID_KEY_PASSWORD").isNullOrBlank()
            if (hasSigningEnv) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.exifinterface:exifinterface:1.4.2")

    val cameraxVersion = "1.6.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.google.mediapipe:tasks-vision:0.10.27")
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.jay.josaeworld"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.jay.josaeworld"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            manifestPlaceholders["appLabel"] = "조새크루"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appLabel"] = "조새크루 Debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    
    // Kotlin & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)

    // UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.lottie)
    implementation(libs.github.clans.fab)

    // Images
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Ads
    implementation(libs.play.services.ads)

    // DI (Hilt)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}


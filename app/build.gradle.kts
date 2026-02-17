plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
}
apply {
    plugin("kotlin-android")
}

android {
    namespace = "com.jay.josaeworld"
    compileSdk = Android.COMPILE_SDK
    defaultConfig {
        applicationId = "com.jay.josaeworld"
        minSdk = Android.MIN_SDK
        targetSdk = Android.TARGET_SDK
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            manifestPlaceholders["appLabel"] = "조새크루"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libs.KOTLIN)
    implementation(Libs.CORE_KTX)
    implementation(Libs.APPCOMPAT)
    implementation(Libs.ANDROIDX_CONSTRAINTLAYOUT)
    implementation(Libs.ANDROIDX_RECYCLERVIEW)
    testImplementation(TestLibs.JUNIT)
    androidTestImplementation(TestLibs.ANDROIDX_JUNIT)

    // Lottie for Android
    implementation(Libs.LOTTIE)

    // Glide
    implementation(Libs.GLIDE)
    kapt(Libs.GLIDE_COMPILER)

    // firebase
    implementation(platform(Libs.FIREBASE))
    implementation(Libs.FIREBASE_ANALYTICS)
    implementation(Libs.FIREBASE_CRASHLYTICS)
    implementation(Libs.FIREBASE_DATABASE)

    // swiperefreshlayout
    implementation(Libs.SWRFLAYOUT)

    implementation(Libs.CARDVIEW)
    implementation(Libs.GITHUB_CLANS_FAB)

    // Retrofit
    implementation(Libs.OKHTTP)
    implementation(Libs.RETROFIT)
    implementation(Libs.RETROFIT_CONVERTER)

    implementation(Libs.PLAY_SERVICES_ADS)

    implementation(Libs.HILT)
    kapt(Libs.HILT_COMPILER)

    implementation(Libs.DATA_STORE)
    implementation(Libs.COROUTINES)
}
repositories {
    mavenCentral()
}

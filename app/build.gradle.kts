import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

// local.properties에서 값을 가져오는 헬퍼 함수
fun getSecret(key: String, default: String = ""): String =
    localProperties.getProperty(key) ?: default

android {
    namespace = "com.jay.josaeworld"
    compileSdk = libs.versions.compileSdk.get().toInt()

    signingConfigs {
        create("release") {
            storeFile = file(getSecret("KEYSTORE_PATH"))
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias = getSecret("KEY_ALIAS")
            keyPassword = getSecret("KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.jay.josaeworld"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API & URL Configs
        buildConfigField("String", "BASE_URL", "\"${getSecret("BASE_URL")}\"")
        buildConfigField("String", "SEARCH_BASE_URL", "\"${getSecret("SEARCH_BASE_URL")}\"")
        buildConfigField("String", "REQUEST_HEADER", "\"${getSecret("REQUEST_HEADER")}\"")
        buildConfigField("String", "GO_LIVE_URL_APP", "\"${getSecret("GO_LIVE_URL_APP")}\"")
        buildConfigField("String", "GO_LIVE_URL_WEB", "\"${getSecret("GO_LIVE_URL_WEB")}\"")
        buildConfigField("String", "DEFAULT_LOGO_IMG", "\"${getSecret("DEFAULT_LOGO_IMG")}\"")
        buildConfigField("String", "LIVE_IMG_URL", "\"${getSecret("LIVE_IMG_URL")}\"")
    }

    buildTypes {
        getByName("release") {
            manifestPlaceholders["appLabel"] = "조새크루"

            // AdMob Configs
            resValue("string", "adMobId", getSecret("ADMOB_APP_ID"))
            resValue("string", "bannerId", getSecret("ADMOB_BANNER_ID"))
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${getSecret("ADMOB_BANNER_ID")}\"")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appLabel"] = "조새크루 Debug"
            
            // AdMob Configs (Debug)
            resValue("string", "adMobId", getSecret("ADMOB_APP_ID_DEBUG"))
            resValue("string", "bannerId", getSecret("ADMOB_BANNER_ID_DEBUG"))
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${getSecret("ADMOB_BANNER_ID_DEBUG")}\"")

            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
        resValues = true
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
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.google.material)
    implementation(libs.kotlinx.coroutines.android)

    // UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    implementation(libs.github.clans.fab)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime.livedata)

    // Images
    implementation(libs.glide)
    implementation(libs.glide.compose)
    ksp(libs.glide.compiler)

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
    ksp(libs.hilt.compiler)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}

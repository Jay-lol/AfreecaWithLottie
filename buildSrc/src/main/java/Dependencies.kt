object Android {
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 30
    const val MIN_SDK = 21
}

object Libs {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.KOTLIN}"
    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.ANDROIDX_APPCOMPAT}"
    const val ANDROIDX_CONSTRAINTLAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.ANDROIDX_CONSTRAINTLAYOUT}"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:${Versions.ANDROIDX_RECYCLERVIEW}"
    const val GITHUB_CLANS_FAB = "com.github.clans:fab:${Versions.GITHUB_CLANS_FAB}"
    const val RXANDROID3 = "io.reactivex.rxjava3:rxandroid:${Versions.RXANDROID3}"
    const val RXANDROID = "io.reactivex.rxjava2:rxandroid:${Versions.RXANDROID}"
    const val RXJAVA3 = "io.reactivex.rxjava3:rxjava:${Versions.RXJAVA3}"
    const val RXBINDING4 = "com.jakewharton.rxbinding4:rxbinding:${Versions.RXBINDING4}"
    const val LOTTIE = "com.airbnb.android:lottie:${Versions.LOTTIE}"
    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${Versions.GLIDE}"
    const val FIREBASE = "com.google.firebase:firebase-bom:${Versions.FIREBASE}"
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics-ktx"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
    const val FIREBASE_DATABASE = "com.google.firebase:firebase-database-ktx"
    const val SWRFLAYOUT = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.SWRFLAYOUT}"
    const val CARDVIEW = "androidx.cardview:cardview:${Versions.CARDVIEW}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_CONVERTER = "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}"
    const val RETROFIT_ADAPTER = "com.squareup.retrofit2:adapter-rxjava2:${Versions.RETROFIT}"
    const val PLAY_SERVICES_ADS =
        "com.google.android.gms:play-services-ads:${Versions.PLAY_SERVICES_ADS}"
    const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
}

object TestLibs {
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ANDROIDX_JUNIT = "androidx.test.ext:junit:${Versions.ANDROIDX_JUNIT}"
}

object Versions {
    const val versionName = "2.2.0"
    const val versionCode = 39

    const val ANDROID_GRADLE_PLUGIN = "7.1.2"
    const val ANDROIDX_RECYCLERVIEW = "1.2.1"
    const val FIREBASE_CRASHLYTICS = "2.8.1"
    const val CORE_KTX = "1.7.0"
    const val GOOGLE_SERVICES = "4.3.10"
    const val KOTLIN = "1.6.10"
    const val GITHUB_CLANS_FAB = "1.6.4"
    const val RXANDROID3 = "3.0.0"
    const val RXJAVA3 = "3.0.3"
    const val RXBINDING4 = "4.0.0"
    const val FIREBASE = "29.1.0"
    const val PLAY_SERVICES_ADS = "20.6.0"

    const val HILT = "2.41"

    // androidx
    const val ANDROIDX_CONSTRAINTLAYOUT = "2.1.3"
    const val ANDROIDX_APPCOMPAT = "1.4.1"
    const val ANDROIDX_JUNIT = "1.1.3"

    // library
    const val OKHTTP = "4.9.3"
    const val RETROFIT = "2.9.0"
    const val RXANDROID = "2.1.1"
    const val GLIDE = "4.13.0"
    const val LOTTIE = "5.0.3"
    const val SWRFLAYOUT = "1.1.0"
    const val CARDVIEW = "1.0.0"
    const val JUNIT = "4.13.2"
}

package com.jay.josaeworld.di

import com.jay.josaeworld.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object UrlModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BASE_URL

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SEARCH_BASE_URL

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class REQUEST_HEADER

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GO_LIVE_URL_APP

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GO_LIVE_URL_WEB

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DEFAULT_LOGO_IMG

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LIVE_IMG_URL

    @Provides
    @BASE_URL
    fun providesBaseUrl() = BuildConfig.BASE_URL

    @Provides
    @SEARCH_BASE_URL
    fun providesSearchBaseUrl() = BuildConfig.SEARCH_BASE_URL

    @Provides
    @REQUEST_HEADER
    fun providesRequestHeader() = BuildConfig.REQUEST_HEADER

    @Provides
    @GO_LIVE_URL_APP
    fun providesGoLiveUrlApp() = BuildConfig.GO_LIVE_URL_APP

    @Provides
    @GO_LIVE_URL_WEB
    fun providesGoLiveUrlWeb() = BuildConfig.GO_LIVE_URL_WEB

    @Provides
    @DEFAULT_LOGO_IMG
    fun providesDefaultLogoImg() = BuildConfig.DEFAULT_LOGO_IMG

    @Provides
    @LIVE_IMG_URL
    fun providesLiveImgUrl() = BuildConfig.LIVE_IMG_URL
}

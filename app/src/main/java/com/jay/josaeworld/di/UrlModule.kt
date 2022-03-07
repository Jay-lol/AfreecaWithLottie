package com.jay.josaeworld.di

import android.content.Context
import com.jay.josaeworld.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun prodvidesBaseUrl(@ApplicationContext context: Context) =
        context.getString(R.string.BASE_URL)

    @Provides
    @SEARCH_BASE_URL
    fun prodvidesSearchBaseUrl(@ApplicationContext context: Context) =
        context.getString(R.string.SEARCH_BASE_URL)

    @Provides
    @REQUEST_HEADER
    fun prodvidesRequestHeader(@ApplicationContext context: Context) =
        context.getString(R.string.REQUEST)

    @Provides
    @GO_LIVE_URL_APP
    fun providesGoLiveUrlApp(@ApplicationContext context: Context) =
        context.getString(R.string.GO_LIVE_URL_APP)

    @Provides
    @GO_LIVE_URL_WEB
    fun providesGoLiveUrlWeb(@ApplicationContext context: Context) =
        context.getString(R.string.GO_LIVE_URL_WEB)

    @Provides
    @DEFAULT_LOGO_IMG
    fun providesDefaultLogoImg(@ApplicationContext context: Context) =
        context.getString(R.string.DEFAULT_LOGO_IMG)

    @Provides
    @LIVE_IMG_URL
    fun providesLiveImgUrl(@ApplicationContext context: Context) =
        context.getString(R.string.LIVE_IMG_URL)
}

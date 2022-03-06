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
}

package com.jay.josaeworld.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.data.repository.impl.DataRepositoryImpl
import com.jay.josaeworld.data.service.ApiCall
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun providesMemberRestrofit(
        @UrlModule.BASE_URL baseUrl: String,
        okHttpClient: OkHttpClient
    ): ApiCall.Member {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall.Member::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun providesSearchRestrofit(
        @UrlModule.SEARCH_BASE_URL searchBaseUrl: String,
        okHttpClient: OkHttpClient,
    ): ApiCall.Search {
        return Retrofit.Builder()
            .baseUrl(searchBaseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall.Search::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @ActivityRetainedScoped
    fun providesFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Provides
    @ActivityRetainedScoped
    fun provideRandom(): Random = Random()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class RepositoryModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindsDataRepository(dataRepository: DataRepositoryImpl): DataRepository
}

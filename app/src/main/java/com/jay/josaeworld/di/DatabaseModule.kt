package com.jay.josaeworld.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.android.gms.ads.AdRequest
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesMemberRestrofit(
        @UrlModule.BASE_URL baseUrl: String,
        okHttpClient: OkHttpClient
    ): ApiCall.Member {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall.Member::class.java)
    }

    @Provides
    @Singleton
    fun providesSearchRestrofit(
        @UrlModule.SEARCH_BASE_URL searchBaseUrl: String,
        okHttpClient: OkHttpClient,
    ): ApiCall.Search {
        return Retrofit.Builder()
            .baseUrl(searchBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall.Search::class.java)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    fun providesFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Provides
    fun providesAdRequest(): AdRequest = AdRequest.Builder().build()

    @Provides
    @Singleton
    fun provideRandom(): Random = Random()

    @Provides
    @Singleton
    fun providesPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile("user_preferences") }
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindsDataRepository(dataRepository: DataRepositoryImpl): DataRepository
}

package com.jay.josaeworld.di

import android.content.Context
import com.jay.josaeworld.R
import com.jay.josaeworld.domain.GetMemberUsecase
import com.jay.josaeworld.domain.SearchKeywordUsecase
import com.jay.josaeworld.model.ApiCall
import com.jay.josaeworld.model.GetData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun providesDataRepo(
        getMemberUsecase: GetMemberUsecase,
        searchKeywordUsecase: SearchKeywordUsecase
    ): GetData {
        return GetData(getMemberUsecase, searchKeywordUsecase)
    }

    @Provides
    @ActivityRetainedScoped
    fun providesMemberRestrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ApiCall.Member {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.BASE_URL))
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall.Member::class.java)
    }

    @Provides
    @ActivityRetainedScoped
    fun providesSearchRestrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ApiCall.Search {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.SEARCH_BASE_URL))
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
    fun provideRandom(): Random = Random()
}

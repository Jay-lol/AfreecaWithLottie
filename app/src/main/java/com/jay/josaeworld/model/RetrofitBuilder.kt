package com.jay.josaeworld.model

import com.jay.josaeworld.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    lateinit var BASE_URL : String
    lateinit var SEARCH_BASE_URL : String
    lateinit var REQUEST : String

    fun setBaseURL(x: String ,y : String, z : String){
        BASE_URL = x
        SEARCH_BASE_URL = y
        REQUEST = z
    }

    const val x = R.string.BASE_URL
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitSearch: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SEARCH_BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }
}
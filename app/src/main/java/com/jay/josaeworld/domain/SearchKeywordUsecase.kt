package com.jay.josaeworld.domain

import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.model.ApiCall
import com.jay.josaeworld.model.response.gsonParse.RealBroad
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchKeywordUsecase @Inject constructor(
    @UrlModule.REQUEST_HEADER private val requestHeader: String,
    private val searchRetrofit: ApiCall.Search
) {
    fun searchJosae(): Single<RealBroad?> {
        return searchRetrofit
            .getSearchInfo(requestHeader)
            .timeout(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                null
            }
    }
}

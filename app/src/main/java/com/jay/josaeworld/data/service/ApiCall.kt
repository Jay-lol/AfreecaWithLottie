package com.jay.josaeworld.data.service

import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiCall {
    /**
     * BJ 정보
     */
    interface Member {
        @GET("api/{user}/station")
        fun getBjInfo(
            @Header("User-Agent") head: String,
            @Path("user") user: String
        ): Single<AfSearchResponse>
    }

    /**
     * 시조새 키워드로 검색한 결과
     */
    interface Search {
        @GET("api.php?m=liveSearch&v=1.0&szOrder=&c=UTF-8&szKeyword=%25EC%258B%259C%25EC%25A1%25B0%25EC%2583%2588&nPageNo=&nListCnt=20&hl=1&onlyParent=1")
        fun getSearchInfo(@Header("User-Agent") head: String): Single<RealBroad>
    }
    // &_=
}

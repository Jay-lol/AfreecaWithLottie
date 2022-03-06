package com.jay.josaeworld.domain

import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.model.ApiCall
import com.jay.josaeworld.model.UtilFnc.getBroadInfo
import com.jay.josaeworld.model.response.BallonInfo
import com.jay.josaeworld.model.response.BroadInfo
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetMemberUsecase @Inject constructor(
    @UrlModule.REQUEST_HEADER private val requestHeader: String,
    private val memberRetrofit: ApiCall.Member
) {
    fun doSearch(teamCode: Int, bid: String): Single<BroadInfo> {
        return memberRetrofit.getBjInfo(requestHeader, bid) // bj의 고유 id
            .map { getBroadInfo(it, teamCode) }
            .timeout(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                BroadInfo(
                    403, 1, bid, "", "",
                    "", "", "", "error", "", "",
                    BallonInfo("0", "0", "0", "0분", "0", "0")
                )
            }
    }
}

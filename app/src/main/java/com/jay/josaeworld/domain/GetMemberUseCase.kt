package com.jay.josaeworld.domain

import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.domain.model.response.BroadInfo
import io.reactivex.Single
import javax.inject.Inject

class GetMemberUseCase @Inject constructor(
    private val repository: DataRepository
) {
    data class Params(
        val teamCode: Int,
        val bid: String,
        val defaultLogoImgUrl: String,
        val liveImgUrl: String
    )

    operator fun invoke(params: Params): Single<BroadInfo> =
        repository.getBjInfo(params.teamCode, params.bid)
            .toBroadInfo(params)
}

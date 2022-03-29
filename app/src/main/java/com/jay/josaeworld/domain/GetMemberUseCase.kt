package com.jay.josaeworld.domain

import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.domain.model.response.AfSearchResponse
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

    suspend operator fun invoke(params: Params): AfSearchResponse =
        repository.getBjInfoWithCoroutines(params.bid)
}

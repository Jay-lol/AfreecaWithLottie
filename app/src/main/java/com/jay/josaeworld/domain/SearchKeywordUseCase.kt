package com.jay.josaeworld.domain

import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad
import io.reactivex.Single
import javax.inject.Inject

class SearchKeywordUseCase @Inject constructor(
    private val repository: DataRepository
) {
    operator fun invoke(): Single<RealBroad?> {
        return repository.searchJosae()
            .onErrorReturn {
                null
            }
    }
}

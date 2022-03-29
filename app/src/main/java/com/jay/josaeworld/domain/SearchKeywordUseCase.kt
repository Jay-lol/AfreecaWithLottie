package com.jay.josaeworld.domain

import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad
import javax.inject.Inject

class SearchKeywordUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(): RealBroad {
        return repository.searchJosaeWithCoroutines()
    }
}

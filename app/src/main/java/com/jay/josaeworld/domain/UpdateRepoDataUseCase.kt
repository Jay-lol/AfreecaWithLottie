package com.jay.josaeworld.domain

import com.jay.josaeworld.data.repository.DataRepository
import javax.inject.Inject

class UpdateRepoDataUseCase @Inject constructor(
    private val repository: DataRepository
) {
    data class Params(
        val updateData: HashMap<String, Any>,
        val onSuccessListener: () -> Unit,
        val onFailureListener: (e: Exception) -> Unit
    )

    operator fun invoke(params: Params) {
        repository.updateRepoData(params.updateData)
            .addOnSuccessListener {
                params.onSuccessListener
            }
            .addOnFailureListener {
                params.onFailureListener(it)
            }
    }
}

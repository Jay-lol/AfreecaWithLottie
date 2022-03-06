package com.jay.josaeworld.domain

import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.data.repository.DataRepository
import javax.inject.Inject

class ListenBJUpToDateUseCase @Inject constructor(
    private val repository: DataRepository
) {
    operator fun invoke(valueEventListener: ValueEventListener) =
        repository.listenBJUpToDate(valueEventListener)

    fun removeListener(valueEventListener: ValueEventListener) {
        repository.removeListener(valueEventListener)
    }
}

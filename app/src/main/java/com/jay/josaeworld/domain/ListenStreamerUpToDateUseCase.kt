package com.jay.josaeworld.domain

import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.data.repository.DataRepository
import javax.inject.Inject

class ListenStreamerUpToDateUseCase @Inject constructor(
    private val repository: DataRepository
) {
    operator fun invoke(valueEventListener: ValueEventListener) =
        repository.listenStreamerUpToDate(valueEventListener)

    fun removeListener(valueEventListener: ValueEventListener) {
        repository.removeListener(valueEventListener)
    }
}

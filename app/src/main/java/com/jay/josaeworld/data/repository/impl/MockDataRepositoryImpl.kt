package com.jay.josaeworld.data.repository.impl

import com.google.android.gms.tasks.Task
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad

class MockDataRepositoryImpl : DataRepository {

    override suspend fun getBjInfoWithCoroutines(bid: String): AfSearchResponse {
        TODO("Not yet implemented")
    }

    override suspend fun searchJosaeWithCoroutines(): RealBroad {
        TODO("Not yet implemented")
    }

    override fun updateRepoData(updateData: HashMap<String, Any>): Task<Void> {
        TODO("Not yet implemented")
    }

    override fun getInitTeamData(valueEventListener: ValueEventListener) {
        TODO("Not yet implemented")
    }

    override fun getUnderBoss(valueEventListener: ValueEventListener) {
        TODO("Not yet implemented")
    }

    override fun getBallonData(valueEventListener: ValueEventListener) {
        TODO("Not yet implemented")
    }

    override fun listenBJUpToDate(valueEventListener: ValueEventListener): ValueEventListener {
        TODO("Not yet implemented")
    }

    override fun removeListener(valueEventListener: ValueEventListener) {
        TODO("Not yet implemented")
    }
}

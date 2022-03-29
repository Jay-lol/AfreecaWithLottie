package com.jay.josaeworld.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad

interface DataRepository {
    suspend fun getBjInfoWithCoroutines(bid: String): AfSearchResponse
    suspend fun searchJosaeWithCoroutines(): RealBroad
    fun updateRepoData(updateData: HashMap<String, Any>): Task<Void>
    fun getInitTeamData(valueEventListener: ValueEventListener)
    fun getUnderBoss(valueEventListener: ValueEventListener)
    fun getBallonData(valueEventListener: ValueEventListener)
    fun listenBJUpToDate(valueEventListener: ValueEventListener): ValueEventListener
    fun removeListener(valueEventListener: ValueEventListener)
}

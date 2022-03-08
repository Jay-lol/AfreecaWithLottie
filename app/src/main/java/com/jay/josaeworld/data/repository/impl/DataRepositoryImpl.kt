package com.jay.josaeworld.data.repository.impl

import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.data.repository.DataRepository
import com.jay.josaeworld.data.service.ApiCall
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    @UrlModule.REQUEST_HEADER private val requestHeader: String,
    private val memberRetrofit: ApiCall.Member,
    private val searchRetrofit: ApiCall.Search,
    private val database: FirebaseDatabase,
) : DataRepository {
    override fun getBjInfo(teamCode: Int, bid: String): Single<AfSearchResponse?> =
        memberRetrofit.getBjInfo(requestHeader, bid) // bj의 고유 id
            .timeout(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())

    override fun searchJosae(): Single<RealBroad?> =
        searchRetrofit
            .getSearchInfo(requestHeader)
            .timeout(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())

    override fun updateRepoData(updateData: HashMap<String, Any>): Task<Void> =
        database.reference.updateChildren(updateData)

    override fun getInitTeamData(valueEventListener: ValueEventListener) {
        database.getReference("LoadingInfo").addListenerForSingleValueEvent(valueEventListener)
    }

    override fun getSecondSujang(valueEventListener: ValueEventListener) {
        database.getReference("SecondSujang").addListenerForSingleValueEvent(valueEventListener)
    }

    override fun getBallonData(valueEventListener: ValueEventListener) {
        database.getReference("Ballon").addListenerForSingleValueEvent(valueEventListener)
    }

    override fun listenBJUpToDate(valueEventListener: ValueEventListener): ValueEventListener =
        database.getReference("BjStatus").addValueEventListener(valueEventListener)

    override fun removeListener(valueEventListener: ValueEventListener) {
        database.getReference("BjStatus").removeEventListener(valueEventListener)
    }
}

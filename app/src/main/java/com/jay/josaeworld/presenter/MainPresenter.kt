package com.jay.josaeworld.presenter

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.*
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo
import kotlinx.coroutines.*
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private var searchView: MainContract.View?,
    private val memberUseCase: GetMemberUseCase,
    private val updateRepoDataUseCase: UpdateRepoDataUseCase,
    private val getUnderBossUseCase: GetUnderBossUseCase,
    private val getBallonDataUseCase: GetBallonDataUseCase,
    private val listenBJUpToDateUseCase: ListenBJUpToDateUseCase,
    @UrlModule.DEFAULT_LOGO_IMG private val defaultLogoImgUrl: String,
    @UrlModule.LIVE_IMG_URL private val liveImgUrl: String
) : MainContract.Presenter {

    private val TAG: String = "로그 ${this.javaClass.simpleName}"

    private val job = Job()
    private var bjStatusListener: ValueEventListener? = null

    /**
     비제이들 고유 아이디 가져와서 데이터 요청
     */
    override fun getRecentBJData(
        bjLists: Array<ArrayList<BroadInfo>>,
        bjDataList: Array<ArrayList<BroadInfo>>?
    ) = CoroutineScope(Dispatchers.Main.immediate + job).launch {
        val bidList = arrayListOf<Pair<Int, String>>()

        for (team in bjLists) {
            val index = team[0].teamCode
            for (member in team) bidList.add(Pair(index, member.bid))
        }

        val bjdata = ArrayList<BroadInfo>()

        val jobs = List(bidList.size) { index ->
            launch(Dispatchers.IO) {
                val params = GetMemberUseCase.Params(
                    bidList[index].first,
                    bidList[index].second,
                    defaultLogoImgUrl,
                    liveImgUrl
                )
                runCatching {
                    memberUseCase(params)
                }.onSuccess {
                    bjdata.add(it.toBroadInfo(params))
                }.onFailure {
                    bjdata.add(
                        BroadInfo(
                            teamCode = 403, onOff = 1,
                            bid = params.bid, balloninfo = BallonInfo()
                        )
                    )
                }
            }
        }

        runCatching {
            jobs.joinAll()
        }.onSuccess {
            searchView?.stopLoadingAnimation()
            searchView?.makeRefreshstate(false)

            val errorCnt: Int = bjdata.count { Bj -> Bj.teamCode == 403 }

            if (errorCnt == bjdata.size) {
                // 전부 에러일 경우
                searchView?.showError(4)
                searchView?.changeIsCrawlingForFirebaseState(false)
            } else {
                // 일부 비제이 정보만 누락된 경우
                sendUpdateData(bjdata.filter { bj -> bj.teamCode != 403 }) { result: Boolean ->
                    val name: String = bjdata.find { it.teamCode == 403 }?.bid ?: ""
                    if (result) {
                        if (errorCnt != 0) {
                            var n = 0
                            bjLists.forEach { n += it.size }
                            var bjname = "unknown"
                            try {
                                var find = false
                                for (i in bjDataList!!) {
                                    if (find) break
                                    for (member in i)
                                        if (member.bid == name) {
                                            find = true
                                            bjname = member.bjname
                                            break
                                        }
                                }
                            } catch (e: Exception) {
                                searchView?.showError(1)
                                Log.e(TAG, "finishSendBjStatus: $e")
                            }

                            if (errorCnt == 1)
                                searchView?.showToast("${n}명 중 아프리카 에러로\n  $bjname 정보 누락")
                            else
                                searchView?.showToast("      ${n}명 중 응답 에러로\n $bjname 외 ${errorCnt - 1}명의 정보 누락")
                        }
                    }
                    searchView?.changeIsCrawlingForFirebaseState(false)
                }
            }
        }.onFailure {
            searchView?.run {
                stopLoadingAnimation()
                makeRefreshstate(false)
                showError(4)
                changeIsCrawlingForFirebaseState(false)
            }
        }
    }

    // 데이터를 업데이트하는 함수
    private fun sendUpdateData(slist: List<BroadInfo>, callback: (Boolean) -> Unit) {
        try {
            Log.d(TAG, "GetData ~ sendUpdateData() called $slist")

            val childUpdates = hashMapOf<String, Any>()
            // 업데이트 시간
            childUpdates["/LoadingInfo/LastUpDateTime"] = System.currentTimeMillis()
            // BJ 정보
            slist.forEach { bj ->
                childUpdates["/BjStatus/${bj.teamCode}/${bj.bid}"] = bj.toMap()
            }

            updateRepoDataUseCase(
                UpdateRepoDataUseCase.Params(
                    childUpdates,
                    onSuccessListener = {
                        callback(true)
                    },
                    onFailureListener = {
                        Log.e(TAG, "GetData ~ addonFailureListener() called $it")
                        searchView?.showError(4)
                    }
                )
            )
        } catch (e: Exception) {
            searchView?.showError(4)
            Log.e(TAG, "sendUpdateData: $e")
        }
    }

    /**
     * 파이어베이스 서버에 리스너를 설정해서 최신 정보를 계속 업데이트
     * newBJDataList : Array<ArrayList<BroadInfo>>?
     */
    override fun createBJDataListener(teamSize: Int) {
        // 이미 등록 되어있으면 처리안되게 설정
        if (bjStatusListener != null) return

        var recentBJList: Array<java.util.ArrayList<BroadInfo>>
        bjStatusListener = listenBJUpToDateUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recentBJList = Array(teamSize) { arrayListOf() }
                    ballonData { ballonMap ->
                        try {
                            for ((index, team) in snapshot.children.withIndex()) {
                                val teamCode = team.key as String
                                for (member in team.value as HashMap<*, *>) {
                                    val v = member.value as HashMap<*, *>
                                    val b =
                                        if (ballonMap.containsKey(member.key as String)) ballonMap[member.key as String]
                                        else null
                                    recentBJList[index].add(
                                        v.goodBjData(
                                            teamCode,
                                            member.key as String,
                                            b,
                                            defaultLogoImgUrl
                                        )
                                    )
                                }
                                if (index == teamSize - 2) {
                                    recentBJList = recentBJList.sortedBJList()
                                }
                            }
                            // 업데이트
                            Log.d(TAG, "MainPresenter ~ bjDataListener() 갱신 성공")
                            searchView?.changeMainBJData(recentBJList)
                        } catch (e: Exception) {
                            searchView?.stopLoadingAnimation()
                            searchView?.showToast("$e")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.stopLoadingAnimation()
                    searchView?.showToast("$error")
                }
            }
        )
    }

    private fun ballonData(callback: (HashMap<String, BallonInfo>) -> Unit) {
        getBallonDataUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val ballonMap = HashMap<String, BallonInfo>()
                        for (x in snapshot.children) {
                            val v = x.value as HashMap<*, *>
                            ballonMap[x.key as String] = v.goodBallonData()
                        }
                        Log.d(TAG, "GetData ~ ballonData() called $ballonMap")
                        callback(ballonMap)
                    } catch (e: Exception) {
                        searchView?.stopLoadingAnimation()
                        searchView?.showToast("$e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.stopLoadingAnimation()
                    searchView?.showToast("$error")
                }
            }
        )
    }

    override fun getUnderBoss() {
        getUnderBossUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val secondMap = HashMap<String, String>()
                        for (x in snapshot.children) {
                            secondMap[x.key as String] = x.value as String
                        }
                        searchView?.initUnderBoss(secondMap)
                    } catch (e: Exception) {
                        searchView?.initUnderBoss(hashMapOf())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.initUnderBoss(hashMapOf())
                }
            }
        )
    }

    // 건의 사항
    override fun sendReport(reportList: List<String>, function: () -> Unit) {
        try {
            val updateData = hashMapOf<String, Any>()

            updateData["/Report/${System.currentTimeMillis()}"] =
                reportList[0] + ": " + reportList[1]

            updateRepoDataUseCase(
                UpdateRepoDataUseCase.Params(
                    updateData,
                    onSuccessListener = {
                        searchView?.showToast("전송 완료\n검토 후 반영하겠습니다")
                        function()
                    },
                    onFailureListener = {
                        Log.e(TAG, "sendReport ~ addonFailureListener() called $it")
                        searchView?.showToast("$it")
                    }
                )
            )
        } catch (e: Exception) {
            searchView?.showToast("$e")
        }
    }

    override fun removeBJDataListener() {
        bjStatusListener?.let {
            listenBJUpToDateUseCase.removeListener(it)
            bjStatusListener = null
        }
    }

    override fun dropView() {
        searchView = null
        if (job.isActive) {
            job.cancel()
        }
    }
}

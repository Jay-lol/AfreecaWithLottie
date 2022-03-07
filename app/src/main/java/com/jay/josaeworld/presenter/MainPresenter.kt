package com.jay.josaeworld.presenter

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.*
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private var searchView: MainContract.View?,
    private val memberUseCase: GetMemberUseCase,
    private val updateRepoDataUseCase: UpdateRepoDataUseCase,
    private val getInitTeamDataUseCase: GetInitTeamDataUseCase,
    private val getSecondSujangUseCase: GetSecondSujangUseCase,
    private val getBallonDataUseCase: GetBallonDataUseCase,
    private val listenBJUpToDateUseCase: ListenBJUpToDateUseCase,
    @UrlModule.DEFAULT_LOGO_IMG private val defaultLogoImgUrl: String,
    @UrlModule.LIVE_IMG_URL private val liveImgUrl: String
) : MainContract.Presenter {

    private val TAG: String = "로그 ${this.javaClass.simpleName}"

    private var disposable: Disposable? = null
    private var bjStatusListener: ValueEventListener? = null

    /**
     비제이들 고유 아이디 가져와서 데이터 요청
     */
    override fun getRecentBJData(
        bjLists: Array<ArrayList<BroadInfo>>,
        bjDataList: Array<ArrayList<BroadInfo>>?
    ) {
        val bidList = arrayListOf<Pair<Int, String>>()

        for (team in bjLists) {
            val index = team[0].teamCode
            for (member in team) bidList.add(Pair(index, member.bid))
        }

        val singles = (bidList).map { targetBJ ->
            memberUseCase(
                GetMemberUseCase.Params(
                    targetBJ.first,
                    targetBJ.second,
                    defaultLogoImgUrl,
                    liveImgUrl
                )
            )
        }.toList()

        disposable = Single
            .zip(singles) { array ->
                array.map {
                    it as BroadInfo
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bjdata ->
                searchView?.stopLoadingAnimation()
                searchView?.makeRefreshstate(false)

                val errorCnt: Int = bjdata.count { Bj -> Bj.teamCode == 403 }

                if (errorCnt == bjdata.size) {
                    // 전부 에러일 경우
                    searchView?.showError(4)
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
                    }
                }
                searchView?.changeIsLoadingState(false)
            }, {
                searchView?.run {
                    stopLoadingAnimation()
                    makeRefreshstate(false)
                    showError(4)
                    changeIsLoadingState(false)
                }
            })
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
                        Log.d(TAG, "GetData ~ addSuccessListener() called")
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

    override fun getSecondSujang() {
        getSecondSujangUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val secondMap = HashMap<String, String>()
                        for (x in snapshot.children) {
                            secondMap[x.key as String] = x.value as String
                        }
                        searchView?.initSecondSujang(secondMap)
                    } catch (e: Exception) {
                        searchView?.initSecondSujang(hashMapOf())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.initSecondSujang(hashMapOf())
                }
            }
        )
    }

    // 팀명 가져오기
    override fun getTeamData() {
        getInitTeamDataUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val value = snapshot.children
                        val list = arrayListOf<String>()
                        var time = 0L
                        var isForce = false
                        var minversionCode = 123456789
                        var currentversionCode = 123456789
                        for (x in value) {
                            when (x.key) {
                                "LastUpDateTime" -> time = x.value as Long
                                "isForce" -> isForce = x.value as Boolean
                                "minversionCode" -> minversionCode = (x.value as Long).toInt()
                                "currentversionCode" ->
                                    currentversionCode =
                                        (x.value as Long).toInt()
                                else -> list.add(x.value as String)
                            }
                        }
                        Log.d(TAG, "GetData ~ onDataChange() called $list")
                        initTeamData(list, time, isForce, minversionCode, currentversionCode)
                    } catch (e: Exception) {
                        searchView?.showToast("$e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.showToast("$error")
                }
            }
        )
    }

    private fun initTeamData(
        newList: List<String>,
        time: Long,
        isForce: Boolean,
        minversionCode: Int,
        currentversionCode: Int
    ) {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        val vc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            pi.versionCode.toLong()
        }
        if (isForce && vc < minversionCode) {
            Handler(Looper.getMainLooper()).postDelayed({
                searchView?.showCustomDialog(3)
            }, 2000)
        } else {
            if (vc < currentversionCode) {
                Handler(Looper.getMainLooper()).postDelayed({
                    searchView?.showCustomDialog(2)
                }, 2000)
            }
        }
        searchView?.initTeamData(newList, time)
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
        disposable?.dispose()
    }
}

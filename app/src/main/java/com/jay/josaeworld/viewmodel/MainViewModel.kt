package com.jay.josaeworld.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.*
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val memberUseCase: GetMemberUseCase,
    private val updateRepoDataUseCase: UpdateRepoDataUseCase,
    private val getUnderBossUseCase: GetUnderBossUseCase,
    private val getBallonDataUseCase: GetBallonDataUseCase,
    private val listenBJUpToDateUseCase: ListenBJUpToDateUseCase,
    @UrlModule.DEFAULT_LOGO_IMG private val defaultLogoImgUrl: String,
    @UrlModule.LIVE_IMG_URL private val liveImgUrl: String
) : ViewModel() {

    private val TAG: String = "로그 MainViewModel"

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<MainSideEffect>()
    val sideEffect: SharedFlow<MainSideEffect> = _sideEffect.asSharedFlow()

    private var bjStatusListener: ValueEventListener? = null

    /**
     비제이들 고유 아이디 가져와서 데이터 요청
     */
    fun getRecentBJData(
        bjLists: Array<ArrayList<BroadInfo>>,
        bjDataList: Array<ArrayList<BroadInfo>>?
    ) {
        viewModelScope.launch {
            val bidList = arrayListOf<Pair<Int, String>>()

            for (team in bjLists) {
                if (team.isEmpty()) continue
                val index = team[0].teamCode
                for (member in team) bidList.add(Pair(index, member.bid))
            }

            val bjdata = java.util.Collections.synchronizedList(ArrayList<BroadInfo>())

            val jobs = bidList.map { item ->
                async(Dispatchers.IO) {
                    val params = GetMemberUseCase.Params(
                        item.first,
                        item.second,
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
                jobs.awaitAll()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }

                val errorCnt: Int = bjdata.count { Bj -> Bj.teamCode == 403 }

                if (errorCnt == bjdata.size) {
                    _sideEffect.emit(MainSideEffect.ShowError(4))
                    _uiState.update { it.copy(isCrawlingForFirebase = false) }
                } else {
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
                                    viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowError(1)) }
                                    Log.e(TAG, "finishSendBjStatus: $e")
                                }

                                viewModelScope.launch {
                                    if (errorCnt == 1)
                                        _sideEffect.emit(MainSideEffect.ShowToast("${n}명 중 아프리카 에러로\n  $bjname 정보 누락"))
                                    else
                                        _sideEffect.emit(MainSideEffect.ShowToast("      ${n}명 중 응답 에러로\n $bjname 외 ${errorCnt - 1}명의 정보 누락"))
                                }
                            }
                        }
                        _uiState.update { it.copy(isCrawlingForFirebase = false) }
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, isCrawlingForFirebase = false) }
                _sideEffect.emit(MainSideEffect.ShowError(4))
            }
        }
    }

    private fun sendUpdateData(slist: List<BroadInfo>, callback: (Boolean) -> Unit) {
        try {
            Log.d(TAG, "GetData ~ sendUpdateData() called $slist")

            val childUpdates = hashMapOf<String, Any>()
            childUpdates["/LoadingInfo/LastUpDateTime"] = System.currentTimeMillis()
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
                        viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowError(4)) }
                    }
                )
            )
        } catch (e: Exception) {
            viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowError(4)) }
            Log.e(TAG, "sendUpdateData: $e")
        }
    }

    fun createBJDataListener(teamSize: Int) {
        if (bjStatusListener != null) return

        bjStatusListener = listenBJUpToDateUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var recentBJList = Array(teamSize) { arrayListOf<BroadInfo>() }
                    ballonData { ballonMap ->
                        try {
                            for ((index, team) in snapshot.children.withIndex()) {
                                if (index >= teamSize) break
                                val teamCode = team.key as String
                                for (member in team.value as HashMap<*, *>) {
                                    val v = member.value as HashMap<*, *>
                                    val b = ballonMap[member.key as String]
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
                            Log.d(TAG, "MainViewModel ~ bjDataListener() 갱신 성공")
                            _uiState.update { it.copy(mainBJDataList = recentBJList) }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(isLoading = false) }
                            viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$e")) }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update { it.copy(isLoading = false) }
                    viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$error")) }
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
                        _uiState.update { it.copy(isLoading = false) }
                        viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$e")) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update { it.copy(isLoading = false) }
                    viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$error")) }
                }
            }
        )
    }

    fun getUnderBoss() {
        getUnderBossUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val secondMap = HashMap<String, String>()
                        for (x in snapshot.children) {
                            secondMap[x.key as String] = x.value as String
                        }
                        _uiState.update { it.copy(underBossList = secondMap) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(underBossList = hashMapOf()) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.update { it.copy(underBossList = hashMapOf()) }
                }
            }
        )
    }

    fun sendReport(reportList: List<String>, onDone: () -> Unit) {
        try {
            val updateData = hashMapOf<String, Any>()
            updateData["/Report/${System.currentTimeMillis()}"] =
                reportList[0] + ": " + reportList[1]

            updateRepoDataUseCase(
                UpdateRepoDataUseCase.Params(
                    updateData,
                    onSuccessListener = {
                        viewModelScope.launch {
                            _sideEffect.emit(MainSideEffect.ShowToast("전송 완료\n검토 후 반영하겠습니다"))
                            onDone()
                        }
                    },
                    onFailureListener = {
                        Log.e(TAG, "sendReport ~ addonFailureListener() called $it")
                        viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$it")) }
                    }
                )
            )
        } catch (e: Exception) {
            viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$e")) }
        }
    }

    fun removeBJDataListener() {
        bjStatusListener?.let {
            listenBJUpToDateUseCase.removeListener(it)
            bjStatusListener = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeBJDataListener()
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun setRefreshing(isRefreshing: Boolean) {
        _uiState.update { it.copy(isRefreshing = isRefreshing) }
    }

    fun setIsCrawlingForFirebase(isCrawling: Boolean) {
        _uiState.update { it.copy(isCrawlingForFirebase = isCrawling) }
    }
}

data class MainUiState(
    val mainBJDataList: Array<ArrayList<BroadInfo>>? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCrawlingForFirebase: Boolean = false,
    val underBossList: HashMap<String, String> = hashMapOf()
)

sealed class MainSideEffect {
    data class ShowError(val code: Int) : MainSideEffect()
    data class ShowToast(val message: String) : MainSideEffect()
    data class ShowCustomDialog(val code: Int) : MainSideEffect()
}

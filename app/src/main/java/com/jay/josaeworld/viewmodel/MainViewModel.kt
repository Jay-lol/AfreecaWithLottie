package com.jay.josaeworld.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.GetBallonDataUseCase
import com.jay.josaeworld.domain.GetMemberUseCase
import com.jay.josaeworld.domain.GetUnderBossUseCase
import com.jay.josaeworld.domain.ListenStreamerUpToDateUseCase
import com.jay.josaeworld.domain.UpdateRepoDataUseCase
import com.jay.josaeworld.domain.goodBallonData
import com.jay.josaeworld.domain.goodStreamerData
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.domain.sortedStreamerList
import com.jay.josaeworld.domain.toBroadInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val memberUseCase: GetMemberUseCase,
        private val updateRepoDataUseCase: UpdateRepoDataUseCase,
        private val getUnderBossUseCase: GetUnderBossUseCase,
        private val getBallonDataUseCase: GetBallonDataUseCase,
        private val listenStreamerUpToDateUseCase: ListenStreamerUpToDateUseCase,
        private val dataStore: com.jay.josaeworld.data.UserPreferencesRepository,
        @UrlModule.DEFAULT_LOGO_IMG private val defaultLogoImgUrl: String,
        @UrlModule.LIVE_IMG_URL private val liveImgUrl: String,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MainUiState())
        val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

        private val _sideEffect = MutableSharedFlow<MainSideEffect>()
        val sideEffect: SharedFlow<MainSideEffect> = _sideEffect.asSharedFlow()

        private var streamerStatusListener: ValueEventListener? = null

        init {
            observeCoachMark()
        }

        private fun observeCoachMark() {
            viewModelScope.launch {
                dataStore.coachMarkCount.collect { count ->
                    _uiState.update { it.copy(isCoachMarkVisible = count <= 1) }
                }
            }
        }

        fun incrementCoachMark() {
            viewModelScope.launch {
                dataStore.incrementCoachMarkCount()
            }
        }

        /**
         스트리머들 고유 아이디 가져와서 데이터 요청
         */
        fun getRecentStreamerData(
            streamerLists: Array<ArrayList<BroadInfo>>,
            streamerDataList: Array<ArrayList<BroadInfo>>?,
        ) {
            _uiState.update { it.copy(isCrawlingForFirebase = true) }
            viewModelScope.launch {
                val streamerIdList = arrayListOf<Pair<Int, String>>()

                for (team in streamerLists) {
                    if (team.isEmpty()) continue
                    val index = team[0].teamCode
                    for (member in team) streamerIdList.add(Pair(index, member.streamerId))
                }

                val streamerData = java.util.Collections.synchronizedList(ArrayList<BroadInfo>())

                val jobs =
                    streamerIdList.map { item ->
                        async(Dispatchers.IO) {
                            val params =
                                GetMemberUseCase.Params(
                                    item.first,
                                    item.second,
                                    defaultLogoImgUrl,
                                    liveImgUrl,
                                )
                            runCatching {
                                memberUseCase(params)
                            }.onSuccess {
                                streamerData.add(it.toBroadInfo(params))
                            }.onFailure {
                                streamerData.add(
                                    BroadInfo(
                                        teamCode = 403,
                                        onOff = 1,
                                        streamerId = params.streamerId,
                                        balloninfo = BallonInfo(),
                                    ),
                                )
                            }
                        }
                    }

                runCatching {
                    jobs.awaitAll()
                }.onSuccess {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false) }

                    val errorCnt: Int = streamerData.count { streamer -> streamer.teamCode == 403 }

                    if (errorCnt == streamerData.size) {
                        Log.e(TAG, " 여기")
                        _sideEffect.emit(MainSideEffect.ShowError(4))
                        _uiState.update { it.copy(isCrawlingForFirebase = false, isRefreshing = false) }
                    } else {
                        sendUpdateData(streamerData.filter { streamer -> streamer.teamCode != 403 }) { result: Boolean ->
                            val name: String =
                                streamerData.find { it.teamCode == 403 }?.streamerId ?: ""
                            if (result) {
                                if (errorCnt != 0) {
                                    var n = 0
                                    streamerLists.forEach { n += it.size }
                                    var streamerName = "unknown"
                                    try {
                                        var find = false
                                        for (i in streamerDataList!!) {
                                            if (find) break
                                            for (member in i) {
                                                if (member.streamerId == name) {
                                                    find = true
                                                    streamerName = member.streamerName
                                                    break
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        viewModelScope.launch {
                                            _sideEffect.emit(
                                                MainSideEffect.ShowError(
                                                    1,
                                                ),
                                            )
                                        }
                                        Log.e(TAG, "finishSendBjStatus: $e")
                                    }

                                    viewModelScope.launch {
                                        if (errorCnt == 1) {
                                            _sideEffect.emit(MainSideEffect.ShowToast("${n}명 중 SOOP 에러로\n  $streamerName 정보 누락"))
                                        } else {
                                            _sideEffect.emit(
                                                MainSideEffect.ShowToast("      ${n}명 중 응답 에러로\n $streamerName 외 ${errorCnt - 1}명의 정보 누락"),
                                            )
                                        }
                                    }
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    isCrawlingForFirebase = false,
                                    isRefreshing = false,
                                )
                            }
                        }
                    }
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isCrawlingForFirebase = false,
                        )
                    }
                    _sideEffect.emit(MainSideEffect.ShowError(4))
                }
            }
        }

        private fun sendUpdateData(
            slist: List<BroadInfo>,
            callback: (Boolean) -> Unit,
        ) {
            try {
                Log.d(TAG, "GetData ~ sendUpdateData() called $slist")

                val childUpdates = hashMapOf<String, Any>()
                childUpdates["/LoadingInfo/LastUpDateTime"] = System.currentTimeMillis()
                slist.forEach { streamer ->
                    childUpdates["/BjStatus/${streamer.teamCode}/${streamer.streamerId}"] =
                        streamer.toMap()
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
                        },
                    ),
                )
            } catch (e: Exception) {
                viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowError(4)) }
                Log.e(TAG, "sendUpdateData: $e")
            }
        }

        fun createStreamerDataListener(teamSize: Int) {
            if (streamerStatusListener != null) return

            if (_uiState.value.mainStreamerDataList == null) {
                _uiState.update { it.copy(isLoading = true) }
            }

            streamerStatusListener =
                listenStreamerUpToDateUseCase(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var recentStreamerList = Array(teamSize) { arrayListOf<BroadInfo>() }
                            ballonData { ballonMap ->
                                try {
                                    for ((index, team) in snapshot.children.withIndex()) {
                                        if (index >= teamSize) break
                                        val teamCode = team.key as String
                                        for (member in team.value as HashMap<*, *>) {
                                            val v = member.value as HashMap<*, *>
                                            val b = ballonMap[member.key as String]
                                            recentStreamerList[index].add(
                                                v.goodStreamerData(
                                                    teamCode,
                                                    member.key as String,
                                                    b,
                                                    defaultLogoImgUrl,
                                                ),
                                            )
                                        }
                                        if (index == teamSize - 2) {
                                            recentStreamerList = recentStreamerList.sortedStreamerList()
                                        }
                                    }
                                    Log.d(TAG, "MainViewModel ~ streamerDataListener() 갱신 성공")

                                    var nAllviewers = 0
                                    var nAllballon = 0

                                    for (team in recentStreamerList) {
                                        if (team.isEmpty()) continue
                                        for (member in team) {
                                            if (member.onOff == 1) {
                                                nAllviewers += member.viewCnt
                                                    .filter { it.isDigit() }
                                                    .toIntOrNull() ?: 0
                                            }
                                            nAllballon += member.balloninfo
                                                ?.monthballon
                                                ?.filter { c -> c.isDigit() }
                                                ?.toIntOrNull() ?: 0
                                        }
                                    }

                                    _uiState.update {
                                        it.copy(
                                            mainStreamerDataList = recentStreamerList,
                                            allViewers = nAllviewers,
                                            allBallons = nAllballon,
                                            isLoading = false,
                                        )
                                    }
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
                    },
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
                },
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
                        } catch (_: Exception) {
                            _uiState.update { it.copy(underBossList = hashMapOf()) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _uiState.update { it.copy(underBossList = hashMapOf()) }
                    }
                },
            )
        }

        fun sendReport(
            reportList: List<String>,
            onDone: () -> Unit,
        ) {
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
                        },
                    ),
                )
            } catch (e: Exception) {
                viewModelScope.launch { _sideEffect.emit(MainSideEffect.ShowToast("$e")) }
            }
        }

        fun removeStreamerDataListener() {
            streamerStatusListener?.let {
                listenStreamerUpToDateUseCase.removeListener(it)
                streamerStatusListener = null
            }
        }

        override fun onCleared() {
            super.onCleared()
            removeStreamerDataListener()
        }

        fun setRefreshing(isRefreshing: Boolean) {
            _uiState.update { it.copy(isRefreshing = isRefreshing) }
        }

        fun showDialog(dialogType: MainDialogType?) {
            _uiState.update { it.copy(dialogType = dialogType) }
        }

        companion object {
            private const val TAG: String = "로그 MainViewModel"
        }
    }

data class MainUiState(
    val mainStreamerDataList: Array<ArrayList<BroadInfo>>? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCrawlingForFirebase: Boolean = false,
    val underBossList: HashMap<String, String> = hashMapOf(),
    val allViewers: Int = 0,
    val allBallons: Int = 0,
    val isCoachMarkVisible: Boolean = false,
    val dialogType: MainDialogType? = null,
)

sealed class MainDialogType {
    object Exit : MainDialogType()
    object Update : MainDialogType()
    object Report : MainDialogType()
    data class Info(val broadInfo: BroadInfo) : MainDialogType()
}

sealed class MainSideEffect {
    data class ShowError(
        val code: Int,
    ) : MainSideEffect()

    data class ShowToast(
        val message: String,
    ) : MainSideEffect()

    data class ShowCustomDialog(
        val code: Int,
    ) : MainSideEffect()
}

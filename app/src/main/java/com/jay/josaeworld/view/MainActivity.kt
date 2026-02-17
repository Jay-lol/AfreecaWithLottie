package com.jay.josaeworld.view

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.jay.josaeworld.BuildConfig
import com.jay.josaeworld.R
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.extension.showErrorToast
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.ui.component.JosaeCustomDialog
import com.jay.josaeworld.ui.component.JosaeInfoDialog
import com.jay.josaeworld.ui.component.JosaeMoveDialog
import com.jay.josaeworld.ui.component.JosaeReportDialog
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_TEAM_DATA_LIST
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_TEAM_NAME
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_UNDER_BOSS_NAME
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_LAST_UPDATE_TIME
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_NEW_LIST
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_UPDATE_CODE
import com.jay.josaeworld.viewmodel.MainDialogType
import com.jay.josaeworld.viewmodel.MainSideEffect
import com.jay.josaeworld.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private lateinit var teamInfo: List<String>

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var adRequest: AdRequest

    @Inject
    @UrlModule.GO_LIVE_URL_APP
    lateinit var goLiveUrlApp: String

    @Inject
    @UrlModule.GO_LIVE_URL_WEB
    lateinit var goLiveUrlWeb: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            toast("디버그 모드")
        }

        intent.extras?.run {
            if (getInt(KEY_UPDATE_CODE) == 2) {
                viewModel.showDialog(MainDialogType.Update)
            }
            val newList = getStringArrayList(KEY_NEW_LIST) as? List<String>
            newList?.let {
                initTeamData(
                    it,
                    getLong(KEY_LAST_UPDATE_TIME),
                )
            }
        }

        MobileAds.initialize(this) {}

        setContent {
            JosaeWorldTheme {
                val state by viewModel.uiState.collectAsState()
                var moveDialogInfo by remember { mutableStateOf<Triple<String, String, Int>?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.sideEffect.collect { effect ->
                        when (effect) {
                            is MainSideEffect.ShowError -> {
                                showErrorToast(effect.code)
                            }

                            is MainSideEffect.ShowToast -> {
                                toast(effect.message)
                            }

                            is MainSideEffect.ShowCustomDialog -> {
                                when (effect.code) {
                                    1 -> viewModel.showDialog(MainDialogType.Exit)
                                    2 -> viewModel.showDialog(MainDialogType.Update)
                                }
                            }
                        }
                    }
                }

                MainScreen(
                    state = state,
                    teamNames = if (::teamInfo.isInitialized) teamInfo else emptyList(),
                    onRefresh = { refreshAct() },
                    onBossClick = { bossInfo ->
                        if (bossInfo.onOff == 1) {
                            moveDialogInfo = Triple(bossInfo.streamerId, "${bossInfo.viewCnt} 명이 시청중입니다!\n이동할까요?", 1)
                        } else {
                            moveDialogInfo = Triple(bossInfo.streamerId, "시조새는 방송중이 아닙니다\nSOOP 으로 이동하시겠습니까?", 0)
                        }
                        firebaseAnalytics.logEvent("click_boss", bundleOf("click" to true))
                    },
                    onBossMoreInfoClick = { bossInfo -> handleBossMoreInfoClick(bossInfo) },
                    onTeamClick = { teamName, teamList, underBoss -> moveTeamList(teamName, teamList, underBoss) },
                    onSearchClick = { handleSearchClick() },
                    onFabChatClick = { handleChatClick() },
                    onFabRoungeClick = { handleRoungeClick() },
                    onFabReportClick = { handleReportClick() },
                    adRequest = adRequest,
                    isCoachMarkVisible = state.isCoachMarkVisible,
                )

                // Dialogs
                state.dialogType?.let { type ->
                    when (type) {
                        is MainDialogType.Exit -> {
                            JosaeCustomDialog(
                                question = stringResource(id = R.string.exit),
                                warning = "너무 짧은 간격의 새로고침은 핸드폰에 무리를 줄 수 있습니다 :)",
                                enableMarquee = true,
                                okText = stringResource(id = R.string.exit_title),
                                cancelText = stringResource(id = R.string.exit_cancel),
                                onConfirm = { finish() },
                                onDismiss = { viewModel.showDialog(null) },
                            )
                        }

                        is MainDialogType.Update -> {
                            JosaeCustomDialog(
                                question = "새로운 버전이 출시되었습니다\n지금 업데이트 하시겠습니까?",
                                okText = stringResource(id = R.string.update),
                                cancelText = stringResource(id = R.string.later),
                                onConfirm = {
                                    firebaseAnalytics.logEvent("click_update", bundleOf("click" to true))
                                    if (gotoMarket()) viewModel.showDialog(null)
                                },
                                onDismiss = {
                                    firebaseAnalytics.logEvent("click_close_update", bundleOf("click" to true))
                                    viewModel.showDialog(null)
                                },
                            )
                        }

                        is MainDialogType.Report -> {
                            JosaeReportDialog(
                                onDismiss = { viewModel.showDialog(null) },
                                onSubmit = { streamer, content ->
                                    if (streamer.isNotEmpty() && content.isNotEmpty()) {
                                        viewModel.sendReport(listOf(streamer, content)) {
                                            viewModel.showDialog(null)
                                        }
                                    } else {
                                        toast("Streamer명과 건의사항을 확인해주세요")
                                    }
                                },
                            )
                        }

                        is MainDialogType.Info -> {
                            JosaeInfoDialog(
                                streamerName = type.broadInfo.streamerName,
                                ballonInfo = type.broadInfo.balloninfo,
                                onDismiss = { viewModel.showDialog(null) },
                            )
                        }
                    }
                }

                moveDialogInfo?.let { (streamerId, question, code) ->
                    JosaeMoveDialog(
                        question = question,
                        okText = if (code == 0) stringResource(id = R.string.move_text) else stringResource(id = R.string.app_kor),
                        cancelText = if (code == 0) stringResource(id = R.string.no_text) else stringResource(id = R.string.web_kor),
                        isBroadOff = code == 0,
                        onConfirm = {
                            handleMoveConfirm(streamerId, code)
                            moveDialogInfo = null
                        },
                        onCancel = {
                            if (code == 1) {
                                handleMoveWeb(streamerId)
                            }
                            moveDialogInfo = null
                        },
                        onDismiss = {
                            moveDialogInfo = null
                        },
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.showDialog(MainDialogType.Exit)
                }
            },
        )

        viewModel.getUnderBoss()
    }

    private fun initTeamData(
        teamInfo: List<String>,
        time: Long,
    ) {
        this.teamInfo = teamInfo
        val now = System.currentTimeMillis()
        if (now - time > 30000) {
            refreshAct()
        }
        viewModel.createStreamerDataListener(teamInfo.size + 1)
    }

    private fun refreshAct() {
        val state = viewModel.uiState.value
        if (state.mainStreamerDataList != null && !state.isCrawlingForFirebase) {
            viewModel.setRefreshing(true)
            viewModel.getRecentStreamerData(state.mainStreamerDataList, state.mainStreamerDataList)
            firebaseAnalytics.logEvent("request_refresh", bundleOf("scroll_down" to true))
        }
    }

    private fun handleBossMoreInfoClick(v: BroadInfo) {
        viewModel.showDialog(MainDialogType.Info(v))
        viewModel.incrementCoachMark()
        firebaseAnalytics.logEvent("click_boss_moreInfo", bundleOf("click" to true))
    }

    private fun handleSearchClick() {
        val intent = Intent(this, SearchActivity::class.java)
        firebaseAnalytics.logEvent("click_searchKeyword", bundleOf("click" to true))
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    private fun handleChatClick() {
        firebaseAnalytics.logEvent("click_chat_button", bundleOf("click" to true))
        toast("서버 비용 문제로 준비중입니다\n빠른 시일내에 업데이트 하겠습니다")
    }

    private fun handleRoungeClick() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://namu.wiki/w/%EC%8B%9C%EC%A1%B0%EC%83%88(%EC%9D%B8%ED%84%B0%EB%84%B7%20%EB%B0%A9%EC%86%A1%EC%9D%B8)"),
            ),
        )
    }

    private fun handleReportClick() {
        viewModel.showDialog(MainDialogType.Report)
    }

    private fun moveTeamList(
        teamName: String,
        teamList: ArrayList<BroadInfo>?,
        underBoss: String,
    ) {
        val intent = Intent(this, BroadListActivity::class.java)
        if (!teamList.isNullOrEmpty()) {
            intent.putExtra(KEY_TEAM_NAME, teamName)
            intent.putExtra(KEY_TEAM_DATA_LIST, teamList)
            intent.putExtra(KEY_UNDER_BOSS_NAME, underBoss)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        } else {
            showErrorToast(3)
        }
    }

    private fun handleMoveConfirm(
        streamerId: String,
        code: Int,
    ) {
        var intent = Intent(Intent.ACTION_VIEW)
        if (code == 0) {
            intent.data = Uri.parse("afreeca://")
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            firebaseAnalytics.logEvent("move_app", bundleOf("click_boss" to true, "status" to "broad_off"))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "http://www.afreecatv.com/total_search.html?szSearchType=total&szStype=di&szKeyword=%EC%8B%9C%EC%A1%B0%EC%83%88",
                        ),
                    )
                startActivity(intent)
            }
        } else {
            intent.data = Uri.parse(goLiveUrlApp + streamerId)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            firebaseAnalytics.logEvent("move_app", bundleOf("click_boss" to true, "status" to "broad_on"))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kr.co.nowcom.mobile.afreeca"))
                startActivity(intent)
            }
        }
    }

    private fun handleMoveWeb(streamerId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(goLiveUrlWeb + streamerId))
        firebaseAnalytics.logEvent("move_web", bundleOf("move" to true))
        startActivity(intent)
    }

    private fun gotoMarket(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            toast("플레이스토어 연결 불가")
            false
        }
    }

    override fun onStart() {
        super.onStart()
        if (::teamInfo.isInitialized) {
            viewModel.createStreamerDataListener(teamInfo.size + 1)
        }
    }

    override fun onStop() {
        viewModel.removeStreamerDataListener()
        super.onStop()
    }
}

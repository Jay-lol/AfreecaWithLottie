package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.jay.josaeworld.BuildConfig
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.CustomDialog2Binding
import com.jay.josaeworld.databinding.CustomDialogBinding
import com.jay.josaeworld.databinding.DialogMemberChangeReportBinding
import com.jay.josaeworld.databinding.InfoDialogBinding
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.extension.showErrorToast
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_TEAM_DATA_LIST
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_TEAM_NAME
import com.jay.josaeworld.view.BroadListActivity.Companion.KEY_UNDER_BOSS_NAME
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_LAST_UPDATE_TIME
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_NEW_LIST
import com.jay.josaeworld.view.InitialActivity.Companion.KEY_UPDATE_CODE
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
                showCustomDialog(2)
            }
            initTeamData(
                getStringArrayList(KEY_NEW_LIST) as List<String>,
                getLong(KEY_LAST_UPDATE_TIME),
            )
        }

        MobileAds.initialize(this) {}

        setContent {
            JosaeWorldTheme {
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.sideEffect.collect { effect ->
                        when (effect) {
                            is MainSideEffect.ShowError -> showErrorToast(effect.code)
                            is MainSideEffect.ShowToast -> toast(effect.message)
                            is MainSideEffect.ShowCustomDialog -> showCustomDialog(effect.code)
                        }
                    }
                }

                MainScreen(
                    state = state,
                    teamNames = if (::teamInfo.isInitialized) teamInfo else emptyList(),
                    onRefresh = { refreshAct() },
                    onBossClick = { bossInfo -> handleBossClick(bossInfo) },
                    onBossMoreInfoClick = { bossInfo -> handleBossMoreInfoClick(bossInfo) },
                    onTeamClick = { teamName, teamList, underBoss -> moveTeamList(teamName, teamList, underBoss) },
                    onSearchClick = { handleSearchClick() },
                    onFabChatClick = { handleChatClick() },
                    onFabRoungeClick = { handleRoungeClick() },
                    onFabReportClick = { handleReportClick() },
                    adRequest = adRequest,
                    isCoachMarkVisible = state.isCoachMarkVisible,
                )
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showCustomDialog(1)
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

    private fun handleBossClick(v: BroadInfo) {
        if (v.onOff == 1) {
            val question = "${v.viewCnt} 명이 시청중입니다!\n이동할까요?"
            popDialog(v.streamerId, question, 1)
        } else {
            val question = "시조새는 방송중이 아닙니다\nSOOP 으로 이동하시겠습니까?"
            popDialog(v.streamerId, question, 0)
        }
        firebaseAnalytics.logEvent("click_boss", bundleOf("click" to true))
    }

    private fun handleBossMoreInfoClick(v: BroadInfo) {
        val dlg = Dialog(this)
        val dlgBinding = InfoDialogBinding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)
        dlgBinding.infoStreamerName.text = v.streamerName

        v.balloninfo?.let {
            dlgBinding.monthview.text = v.balloninfo!!.monthview
            dlgBinding.monthmaxview.text = v.balloninfo!!.monthmaxview
            dlgBinding.monthtime.text = v.balloninfo!!.monthtime
            dlgBinding.monthpay.text = v.balloninfo!!.monthpay
        }
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val dlg = Dialog(this, R.style.DialogStyle)
        val dlgBinding = DialogMemberChangeReportBinding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.reportSubmit.setOnClickListener {
            val streamer =
                dlgBinding.reportStreamer.text
                    .trim()
                    .toString()
            val content =
                dlgBinding.suggest.text
                    .trim()
                    .toString()
            if (streamer.isNotEmpty() && content.isNotEmpty()) {
                viewModel.sendReport(listOf(streamer, content)) { dlg.dismiss() }
            } else {
                toast("Streamer명과 건의사항을 확인해주세요")
            }
        }
        dlgBinding.reportClose.setOnClickListener { dlg.dismiss() }
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

    private fun popDialog(
        streamerId: String,
        question: String,
        code: Int,
    ) {
        var intent = Intent(Intent.ACTION_VIEW)
        val dlg = Dialog(this)
        val dlgBinding = CustomDialog2Binding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)
        dlgBinding.moveQuestion.text = question
        if (code == 0) {
            dlgBinding.moveApp.text = "이동"
            dlgBinding.moveWeb.text = "아니요"
            dlgBinding.moveWeb.setTextColor(Color.parseColor("#99FFFFFF"))
        }
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.moveApp.setOnClickListener {
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
                dlg.dismiss()
            } else {
                intent.data = Uri.parse(goLiveUrlApp + streamerId)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                firebaseAnalytics.logEvent("move_app", bundleOf("click_boss" to true, "status" to "broad_on"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kr.co.nowcom.mobile.afreeca"))
                    startActivity(intent)
                }
                dlg.dismiss()
            }
        }

        dlgBinding.moveWeb.setOnClickListener {
            if (code == 1) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(goLiveUrlWeb + streamerId))
                firebaseAnalytics.logEvent("move_web", bundleOf("move" to true))
                startActivity(intent)
            }
            dlg.dismiss()
        }
    }

    private fun showCustomDialog(code: Int) {
        val dlg = Dialog(this)
        val dlgBinding = CustomDialogBinding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)

        when (code) {
            1 -> {
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlgBinding.warning.isSelected = true
                dlgBinding.closeOkButton.setOnClickListener {
                    finish()
                    dlg.dismiss()
                }
                dlgBinding.closeNotOk.setOnClickListener { dlg.dismiss() }
            }

            2 -> {
                dlgBinding.question.text = "새로운 버전이 출시되었습니다\n지금 업데이트 하시겠습니까?"
                dlgBinding.warning.text = ""
                dlgBinding.closeOkButton.text = "업데이트"
                dlgBinding.closeNotOk.text = "나중에"
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlgBinding.closeOkButton.setOnClickListener {
                    firebaseAnalytics.logEvent("click_update", bundleOf("click" to true))
                    if (gotoMarket()) dlg.dismiss()
                }
                dlgBinding.closeNotOk.setOnClickListener {
                    firebaseAnalytics.logEvent("click_close_update", bundleOf("click" to true))
                    dlg.dismiss()
                }
            }
        }
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

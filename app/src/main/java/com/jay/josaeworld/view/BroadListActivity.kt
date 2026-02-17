package com.jay.josaeworld.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.jay.josaeworld.R
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.ui.component.JosaeInfoDialog
import com.jay.josaeworld.ui.component.JosaeMoveDialog
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.viewmodel.BroadListDialogType
import com.jay.josaeworld.viewmodel.BroadListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BroadListActivity : ComponentActivity() {
    private val viewModel: BroadListViewModel by viewModels()

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
        setContent {
            JosaeWorldTheme {
                val state by viewModel.uiState.collectAsState()

                BroadListScreen(
                    state = state,
                    adRequest = adRequest,
                    onStreamerClick = { streamerInfo ->
                        firebaseAnalytics.logEvent(
                            "click_member",
                            bundleOf(
                                "member_name" to streamerInfo.streamerName,
                                "member_viewCnt" to streamerInfo.viewCnt,
                            ),
                        )
                        viewModel.showDialog(BroadListDialogType.Move(streamerInfo))
                    },
                    onMoreInfoClick = { streamerInfo ->
                        firebaseAnalytics.logEvent(
                            "click_member_moreInfo",
                            bundleOf("member_name" to streamerInfo.streamerName),
                        )
                        viewModel.showDialog(BroadListDialogType.Info(streamerInfo))
                    },
                )

                // Dialogs
                state.dialogType?.let { type ->
                    when (type) {
                        is BroadListDialogType.Move -> {
                            val streamerName = type.broadInfo.streamerName
                            val viewCnt = type.broadInfo.viewCnt
                            JosaeMoveDialog(
                                question = "$viewCnt 명이 시청중입니다!\n$streamerName 방송으로 이동할까요?",
                                okText = stringResource(id = R.string.move_app),
                                cancelText = stringResource(id = R.string.move_web),
                                onConfirm = {
                                    moveToLive(type.broadInfo, true)
                                    viewModel.showDialog(null)
                                },
                                onCancel = {
                                    moveToLive(type.broadInfo, false)
                                    viewModel.showDialog(null)
                                },
                                onDismiss = {
                                    viewModel.showDialog(null)
                                },
                            )
                        }

                        is BroadListDialogType.Info -> {
                            JosaeInfoDialog(
                                streamerName = type.broadInfo.streamerName,
                                ballonInfo = type.broadInfo.balloninfo,
                                onDismiss = { viewModel.showDialog(null) },
                            )
                        }
                    }
                }
            }
        }
        firebaseAnalytics.logEvent("view_team", bundleOf("teamName" to intent.getStringExtra(KEY_TEAM_NAME)))
    }

    private fun moveToLive(
        v: BroadInfo,
        isApp: Boolean,
    ) {
        val streamerId = v.streamerId
        var intent = Intent(Intent.ACTION_VIEW)

        if (isApp) {
            intent.data = Uri.parse(goLiveUrlApp + streamerId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=kr.co.nowcom.mobile.afreeca"),
                    )
                startActivity(intent)
            }
        } else {
            intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(goLiveUrlWeb + streamerId),
                )
            startActivity(intent)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    companion object {
        const val KEY_TEAM_DATA_LIST = "key_team_data_list"
        const val KEY_UNDER_BOSS_NAME = "key_under_boss"
        const val KEY_TEAM_NAME = "key_team_name"
    }
}

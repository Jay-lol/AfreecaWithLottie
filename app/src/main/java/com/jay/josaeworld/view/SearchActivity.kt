package com.jay.josaeworld.view

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.android.gms.ads.AdRequest
import com.jay.josaeworld.R
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.SearchStreamerInfo
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.ui.component.JosaeMoveDialog
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.viewmodel.SearchSideEffect
import com.jay.josaeworld.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels()

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
                var moveDialogInfo by remember { mutableStateOf<SearchStreamerInfo?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.sideEffect.collect { effect ->
                        when (effect) {
                            is SearchSideEffect.ShowToast -> toast(effect.message)
                        }
                    }
                }

                SearchScreen(
                    state = state,
                    adRequest = adRequest,
                    onItemClick = { item -> moveDialogInfo = item },
                )

                moveDialogInfo?.let { info ->
                    JosaeMoveDialog(
                        question = "${info.total_view_cnt} 명이 시청중입니다!\n${info.user_nick} 방송으로 이동할까요?",
                        okText = stringResource(id = R.string.move_app),
                        cancelText = stringResource(id = R.string.move_web),
                        onConfirm = {
                            moveToLive(info, true)
                            moveDialogInfo = null
                        },
                        onCancel = {
                            moveToLive(info, false)
                            moveDialogInfo = null
                        },
                        onDismiss = {
                            moveDialogInfo = null
                        }
                    )
                }
            }
        }
    }

    private fun moveToLive(item: SearchStreamerInfo, isApp: Boolean) {
        val streamerId = item.user_id
        var intent = Intent(Intent.ACTION_VIEW)

        if (isApp) {
            intent.data = Uri.parse(goLiveUrlApp + streamerId)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
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
}

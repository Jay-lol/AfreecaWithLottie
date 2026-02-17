package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.gms.ads.AdRequest
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.CustomDialog2Binding
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.SearchStreamerInfo
import com.jay.josaeworld.extension.toast
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
                    onItemClick = { item -> moveToLive(item) },
                )
            }
        }
    }

    private fun moveToLive(item: SearchStreamerInfo) {
        val streamerId = item.user_id
        val streamerName = item.user_nick
        val viewCnt = item.total_view_cnt

        var intent = Intent(Intent.ACTION_VIEW)
        val dlg = Dialog(this)
        val dlgBinding = CustomDialog2Binding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)

        dlgBinding.moveQuestion.text = "$viewCnt 명이 시청중입니다!\n$streamerName 방송으로 이동할까요?"

        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.moveApp.setOnClickListener {
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
            dlg.dismiss()
        }

        dlgBinding.moveWeb.setOnClickListener {
            intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(goLiveUrlWeb + streamerId),
                )
            startActivity(intent)
            dlg.dismiss()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }
}

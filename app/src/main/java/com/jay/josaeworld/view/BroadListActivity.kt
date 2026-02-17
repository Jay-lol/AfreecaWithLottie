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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.CustomDialog2Binding
import com.jay.josaeworld.databinding.InfoDialogBinding
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
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
                    onBJClick = { bjInfo ->
                        firebaseAnalytics.logEvent(
                            "click_member",
                            bundleOf(
                                "member_name" to bjInfo.bjname,
                                "member_viewCnt" to bjInfo.viewCnt
                            )
                        )
                        moveToLive(bjInfo)
                    },
                    onMoreInfoClick = { bjInfo ->
                        firebaseAnalytics.logEvent(
                            "click_member_moreInfo",
                            bundleOf("member_name" to bjInfo.bjname)
                        )
                        showMoreInfo(bjInfo)
                    }
                )
            }
        }
        firebaseAnalytics.logEvent("view_team", bundleOf("teamName" to intent.getStringExtra(KEY_TEAM_NAME)))
    }

    private fun moveToLive(v: BroadInfo) {
        val bid = v.bid
        val bjname = v.bjname
        val viewCnt = v.viewCnt

        var intent = Intent(Intent.ACTION_VIEW)
        val dlg = Dialog(this)
        val dlgBinding = CustomDialog2Binding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)

        dlgBinding.moveQuestion.text = "$viewCnt 명이 시청중입니다!\n$bjname 방송으로 이동할까요?"

        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.moveApp.setOnClickListener {
            intent.data = Uri.parse(goLiveUrlApp + bid)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=kr.co.nowcom.mobile.afreeca")
                )
                startActivity(intent)
            }
            dlg.dismiss()
        }

        dlgBinding.moveWeb.setOnClickListener {
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(goLiveUrlWeb + bid)
            )
            startActivity(intent)
            dlg.dismiss()
        }
    }

    private fun showMoreInfo(v: BroadInfo) {
        val dlg = Dialog(this)
        val dlgBinding = InfoDialogBinding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)
        dlgBinding.infoBjname.text = v.bjname

        v.balloninfo?.run {
            dlgBinding.monthview.text = monthview
            dlgBinding.monthmaxview.text = monthmaxview
            dlgBinding.monthtime.text = monthtime
            dlgBinding.monthpay.text = monthpay
        }
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

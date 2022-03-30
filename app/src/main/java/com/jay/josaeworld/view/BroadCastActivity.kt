package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.jay.josaeworld.adapter.RecyclerBroadListAdapter
import com.jay.josaeworld.adapter.RecyclerSearchListAdapter
import com.jay.josaeworld.base.BaseViewBindingActivity
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.databinding.ActivityBroadCastBinding
import com.jay.josaeworld.databinding.CustomDialog2Binding
import com.jay.josaeworld.databinding.InfoDialogBinding
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.domain.model.response.SearchBJInfo
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad
import com.jay.josaeworld.extension.showErrorToast
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.presenter.BroadPresenter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BroadCastActivity :
    BaseViewBindingActivity<ActivityBroadCastBinding, BroadPresenter>({
        ActivityBroadCastBinding.inflate(
            it
        )
    }),
    BroadContract.View {

    private lateinit var mAdapter: RecyclerBroadListAdapter
    private lateinit var sAdapter: RecyclerSearchListAdapter
    private lateinit var underBoss: String
    lateinit var mAdView: AdView

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

    @Inject
    lateinit var random: Random

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAdmob()

        underBoss = intent.getStringExtra(KEY_UNDER_BOSS_NAME) ?: "1"

        val list = intent.getSerializableExtra(KEY_TEAM_DATA_LIST) as ArrayList<BroadInfo>?

        binding.broadRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        if (!list.isNullOrEmpty()) {
            setBroadView(list)
        } else {
            setSearchView()
        }

        binding.teamName.text = intent.getStringExtra(KEY_TEAM_NAME)
        firebaseAnalytics.logEvent("view_team", bundleOf("teamName" to binding.teamName.text))
    }

    /**
     * 팀원 목록 보여주기
     */
    private fun setBroadView(list: ArrayList<BroadInfo>) {
        mAdapter = RecyclerBroadListAdapter(
            Glide.with(this),
            list.sortedWith(
                compareBy(
                    { -it.viewCnt.filter { c -> c.isDigit() }.toInt() }, // 시청자순
                    { -it.onOff }, // 방송켜져있는지, 비번방 처리
                    { it.bid != underBoss },
                    { it.balloninfo?.dayballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                    { it.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                    { -it.fanCnt.filter { c -> c.isDigit() }.toInt() }
                ) // 즐찾 순
            ),
            underBoss,
            memberClick,
            random
        )

        binding.broadRecyclerView.adapter = mAdapter
    }

    /**
     * 검색 목록 보여주기
     */
    private fun setSearchView() {
        binding.searchLoading.visibility = View.VISIBLE
        binding.searchLoading.playAnimation()
        presenter.searchJosae()
    }

    override fun showSearchResult(searchBJInfo: RealBroad?) {
        binding.searchLoading.pauseAnimation()
        binding.searchLoading.visibility = View.GONE

        searchBJInfo?.REAL_BROAD?.let { searchList ->
            if (searchList.isEmpty()) {
                binding.searchNoResult.visibility = View.VISIBLE
                return@let
            }

            sAdapter =
                RecyclerSearchListAdapter(
                    Glide.with(this),
                    searchList,
                    searchMemberClick,
                    random
                )

            binding.broadRecyclerView.adapter = sAdapter
        }
    }

    private val memberClick: (BroadInfo, Int) -> Unit = { v, code ->
        if (code == 0) {
            firebaseAnalytics.logEvent(
                "click_member",
                bundleOf("member_name" to v.bjname, "member_viewCnt" to v.viewCnt)
            )
            moveToLive(v.bid, v.bjname, v.viewCnt)
        } else {
            firebaseAnalytics.logEvent("click_member_moreInfo", bundleOf("member_name" to v.bjname))
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
    }

    private val searchMemberClick: (SearchBJInfo) -> Unit = { v ->
        moveToLive(v.user_id, v.user_nick, v.total_view_cnt)
    }

    private fun moveToLive(bid: String, bjname: String, viewCnt: String) {
        var intent = Intent(Intent.ACTION_VIEW)
        val dlg = Dialog(this)
        val dlgBinding = CustomDialog2Binding.inflate(layoutInflater)
        dlg.setContentView(dlgBinding.root)

        dlgBinding.moveQuestion.text = "$viewCnt 명이 시청중입니다!\n$bjname 방송으로 이동할까요?"

        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlgBinding.moveApp.setOnClickListener {
            intent.data = Uri.parse(goLiveUrlApp + bid)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            try {
                firebaseAnalytics.logEvent("move_app", bundleOf("move" to true))
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
            firebaseAnalytics.logEvent("move_web", bundleOf("move" to true))
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(goLiveUrlWeb + bid)
            )
            startActivity(intent)
            dlg.dismiss()
        }
    }

    override fun showError(code: Int) {
        showErrorToast(code)
    }

    override fun showToast(msg: String, isCenter: Boolean) {
        toast(msg, isCenter)
    }

    private fun createAdmob() {
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        mAdView.loadAd(adRequest)
    }

    companion object {
        const val KEY_TEAM_DATA_LIST = "key_team_data_list"
        const val KEY_UNDER_BOSS_NAME = "key_under_boss"
        const val KEY_TEAM_NAME = "key_team_name"
    }
}

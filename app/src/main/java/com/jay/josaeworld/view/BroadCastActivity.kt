package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.adapter.RecyclerBroadListAdapter
import com.jay.josaeworld.adapter.RecyclerSearchListAdapter
import com.jay.josaeworld.base.BaseViewBindingActivity
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.databinding.ActivityBroadCastBinding
import com.jay.josaeworld.databinding.CustomDialog2Binding
import com.jay.josaeworld.databinding.InfoDialogBinding
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
    private lateinit var secondSujang: String
    lateinit var mAdView: AdView

    @Inject
    lateinit var random: Random

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()

        val list = intent.getSerializableExtra("teamInfo") as ArrayList<BroadInfo>?
        secondSujang = intent.getStringExtra("secondSujang") ?: "1"

        binding.broadRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        if (!list.isNullOrEmpty()) {
            setBroadView(list)
        } else {
            setSearchView()
        }

        binding.teamName.text = intent.getStringExtra("teamName") ?: "시조새 검색 결과"
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
                    { it.bid != secondSujang },
                    { it.balloninfo?.dayballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                    { it.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                    { -it.fanCnt.filter { c -> c.isDigit() }.toInt() }
                ) // 즐찾 순
            ),
            secondSujang,
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
            moveToLive(v.bid, v.bjname, v.viewCnt)
        } else {
            val dlg = Dialog(this)
            val dlgBinding = InfoDialogBinding.inflate(layoutInflater)
            dlg.setContentView(dlgBinding.root)
            dlgBinding.infoBjname.text = v.bjname

            v.balloninfo?.let {
                dlgBinding.monthview.text = v.balloninfo!!.monthview
                dlgBinding.monthmaxview.text = v.balloninfo!!.monthmaxview
                dlgBinding.monthtime.text = v.balloninfo!!.monthtime
                dlgBinding.monthpay.text = v.balloninfo!!.monthpay
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
            intent.data = Uri.parse("afreeca://player/live?user_id=$bid")
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
                Uri.parse("http://m.afreecatv.com/#/player/$bid")
            )
            startActivity(intent)
            dlg.dismiss()
        }
    }

    override fun showError(code: Int) {
        showErrorToast(code)
    }

    override fun showToast(msg: String) {
        toast(msg)
    }

    private fun loadAd() {
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}

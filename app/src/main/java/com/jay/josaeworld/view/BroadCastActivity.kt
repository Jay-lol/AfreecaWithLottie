package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.R
import com.jay.josaeworld.adapter.RecyclerBroadListAdapter
import com.jay.josaeworld.adapter.RecyclerSearchListAdapter
import com.jay.josaeworld.databinding.ActivityBroadCastBinding
import com.jay.josaeworld.model.GetData
import com.jay.josaeworld.model.response.BroadInfo
import com.jay.josaeworld.model.response.SearchBJInfo
import com.jay.josaeworld.model.response.gsonParse.RealBroad
import kotlinx.android.synthetic.main.custom_dialog2.*
import kotlinx.android.synthetic.main.info_dialog.*

class BroadCastActivity : AppCompatActivity(), BaseBroadCastActivity {
    private lateinit var binding: ActivityBroadCastBinding
    private lateinit var mAdapter: RecyclerBroadListAdapter
    private lateinit var sAdapter: RecyclerSearchListAdapter
    private lateinit var secondSujang: String
    lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadCastBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadAd()

        val list = intent.getSerializableExtra("teamINfo") as ArrayList<BroadInfo>?
        secondSujang = intent.getStringExtra("secondSujang") ?: "1"

        binding.broadRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        if (!list.isNullOrEmpty())
            setBroadView(list)
        else
            setSearchView()

        binding.teamName.text = intent.getStringExtra("teamName") ?: "시조새 검색 결과"
    }

    /**
     * 팀원 목록 보여주기
     */
    override fun setBroadView(list: ArrayList<BroadInfo>) {
        mAdapter = RecyclerBroadListAdapter(Glide.with(this), list.sortedWith(
            compareBy({ -it.viewCnt.filter { c -> c.isDigit() }.toInt() },   // 시청자순
                { -it.onOff }, // 방송켜져있는지, 비번방 처리
                { it.bid != secondSujang },
                { it.balloninfo?.dayballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                { it.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt()?.times(-1) },
                { -it.fanCnt.filter { c -> c.isDigit() }.toInt() })  // 즐찾 순
        ), secondSujang, memberClick)

        binding.broadRecyclerView.adapter = mAdapter
    }

    /**
     * 검색 목록 보여주기
     */
    override fun setSearchView() {
        binding.searchLoading.visibility = View.VISIBLE
        binding.searchLoading.playAnimation()

        GetData.searchJosae(complete = {
            binding.searchLoading.pauseAnimation()
            binding.searchLoading.visibility = View.GONE

            it?.REAL_BROAD?.let { SearchList ->
                sAdapter = RecyclerSearchListAdapter(Glide.with(this), SearchList, searchMemberClick)

                binding.broadRecyclerView.adapter = sAdapter
            }

            if (it==null)
                Toast.makeText(this, "검색 실패!", Toast.LENGTH_SHORT).show()
        })
    }

    private val memberClick: (BroadInfo, Int) -> Unit = { v, code ->
        val dlg = Dialog(this)

        if (code == 0) {
            moveToLive(dlg, v.bid, v.bjname, v.viewCnt)
        } else {
            dlg.setContentView(R.layout.info_dialog)
            dlg.infoBjname.text = v.bjname

            v.balloninfo?.let {
                dlg.monthview.text = v.balloninfo!!.monthview
                dlg.monthmaxview.text = v.balloninfo!!.monthmaxview
                dlg.monthtime.text = v.balloninfo!!.monthtime
                dlg.monthpay.text = v.balloninfo!!.monthpay
            }
            dlg.show()
            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private val searchMemberClick: (SearchBJInfo) -> Unit = { v ->
        val dlg = Dialog(this)
        moveToLive(dlg, v.user_id, v.user_nick, v.total_view_cnt)
    }

    private fun moveToLive(dlg: Dialog, bid: String, bjname: String, viewCnt: String) {
        var intent = Intent(Intent.ACTION_VIEW)

        dlg.setContentView(R.layout.custom_dialog2)

        dlg.move_question.text = "$viewCnt 명이 시청중입니다!\n${bjname} 방송으로 이동할까요?"

        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlg.moveApp.setOnClickListener {
            intent.data = Uri.parse("afreeca://player/live?user_id=${bid}")
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

        dlg.moveWeb.setOnClickListener {
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://m.afreecatv.com/#/player/${bid}")
            )
            startActivity(intent)
            dlg.dismiss()
        }
    }

    override fun finish() {
        super.finish()
        // 새로운 액티비티, 기존 액티비티
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun loadAd() {
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}
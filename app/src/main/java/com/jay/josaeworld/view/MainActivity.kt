package com.jay.josaeworld.view

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.ActivityMainBinding
import com.jay.josaeworld.model.BroadInfo
import com.jay.josaeworld.model.DataCallback
import com.jay.josaeworld.model.GetData
import kotlinx.android.synthetic.main.custom_dialog.*

class MainActivity : AppCompatActivity() {
    private val TAG: String = "로그"
    private lateinit var binding: ActivityMainBinding
    private lateinit var teamInfo: List<String>
    private var bjDataList: Array<ArrayList<BroadInfo>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        // View 생성
        setContentView(view)
        loadInitData()
        buttonListener()
        refreshListener()
    }

    override fun onBackPressed() {
        val dlg = Dialog(this)
        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.custom_dialog)

        // 커스텀 다이얼로그를 노출한다.
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        dlg.closeOkButton.setOnClickListener {
            super.onBackPressed()
            dlg.dismiss()
        }

        dlg.closeNotOk.setOnClickListener{
            dlg.dismiss()
        }
    }

    private fun refreshListener() {
        binding.refreshLayout.setOnRefreshListener {
            Log.d(TAG, "MainActivity ~ refreshListener() called")
            binding.refreshLayout.isRefreshing = false
            binding.mainLoadingbar.visibility = View.VISIBLE
            binding.mainLoadingbar.playAnimation()
            loadInitData()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun loadInitData() {
        GetData().teamData(object : DataCallback {
            override fun finishLoading(list: List<String>) {
                teamInfo = list
                Log.d(TAG, "MainActivity ~ finishLoading() called $teamInfo")
                binding.teamOne.text = teamInfo[0]
                binding.teamTwo.text = teamInfo[1]
                binding.teamThree.text = teamInfo[2]
            }
        })

        GetData().liveOnData(object : DataCallback {
            override fun finishBjDataLoading(bjlist: Array<ArrayList<BroadInfo>>) {
                Log.d(TAG + "y", "MainActivity ~ finishOnOffLoading() called ${bjlist.contentDeepToString()}")

                if (bjlist[bjlist.size - 1].size == 1) {
                    bjDataList = null
                    Toast.makeText(
                        baseContext, "정보를 완벽히 불러오지 못했습니다\n" +
                                "화면을 당겨서 새로고침을 해주세염보성", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.loadingbar.pauseAnimation()
                    binding.loadingbar.visibility = View.GONE

                    val nextOff = listOf(
                        binding.teamOnelotti, binding.teamTwolotti, binding.teamThreelotti
                    )
                    val nextOn = listOf(
                        binding.teamOnelottiOn, binding.teamTwolottiOn, binding.teamThreelottiOn
                    )

                    for ((index, team) in bjlist.withIndex()) {
                        // 수장 처리는 따로
                        if (index == 3) {
                            val v = team[0]
                            if (v.onOff == 1) {
                                binding.sujangRest.pauseAnimation()
                                binding.sujangRest.visibility = View.INVISIBLE
                                binding.sujanLive.visibility = View.VISIBLE
                                binding.sujanLive.playAnimation()

                                Glide.with(baseContext).load(v.imgurl)
                                    .centerCrop()
                                    .into(binding.sujangThumbnail)
                            } else {
                                binding.sujanLive.pauseAnimation()
                                binding.sujanLive.visibility = View.INVISIBLE
                                binding.sujangThumbnail.visibility = View.INVISIBLE
                                binding.sujangRest.visibility = View.VISIBLE
                                binding.sujangRest.playAnimation()
                            }

                            binding.sujangname.text = v.bjname
                            binding.sujangTitle.text = v.title
                            binding.sujangname.isSelected = true
                            binding.sujangTitle.isSelected = true
                            break
                        }

                        var onOff = false
                        for (member in team) {
                            if (member.onOff == 1) {
                                onOff = true
                                break
                            }
                        }
                        if (onOff) {
                            nextOff[index].visibility = View.GONE
                            nextOff[index].pauseAnimation()
                            nextOn[index].visibility = View.VISIBLE
                            nextOn[index].playAnimation()
                        } else {
                            nextOn[index].visibility = View.GONE
                            nextOn[index].pauseAnimation()
                            nextOff[index].visibility = View.VISIBLE
                            nextOff[index].playAnimation()
                        }
                    }
                    bjDataList = Array(bjlist.size - 1) { arrayListOf() }
                    repeat(4) {
                        bjDataList!![it] = bjlist[it]
                    }
                }
                binding.mainLoadingbar.pauseAnimation()
                binding.mainLoadingbar.visibility = View.GONE
            }
        })
    }


    private fun buttonListener() {
        Log.d(TAG, "MainActivity ~ buttonListener() called")

        binding.teamFirst.setOnClickListener {
            try {
                bjDataList?.let {
                    val intent = Intent(this, BroadCastActivity::class.java)
                    intent.putExtra("teamName", teamInfo[0])
                    intent.putExtra("teamINfo", bjDataList!![0])
                    val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        Pair.create(binding.teamOne, "changeAct"),
                        Pair.create(binding.teamOneImg, "changeActImg")
                    )
                    startActivity(intent, options.toBundle())
                }
            } catch (e: Exception) {
                Log.d(TAG, "MainActivity ~ buttonListener() called $e")
                Toast.makeText(baseContext, "당겨서 새로고침을 해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        binding.teamSecond.setOnClickListener {
            try {
                bjDataList?.let {
                    val intent = Intent(this, BroadCastActivity::class.java)
                    intent.putExtra("teamName", teamInfo[1])
                    intent.putExtra("teamINfo", bjDataList!![1])
                    val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        Pair.create(binding.teamTwo, "changeAct"),
                        Pair.create(binding.teamTwoImg, "changeActImg")
                    )
                    startActivity(intent, options.toBundle())
                }
            } catch (e: Exception) {
                Toast.makeText(baseContext, "당겨서 새로고침을 해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.teamThird.setOnClickListener {
            try {
                bjDataList?.let {
                    val intent = Intent(this, BroadCastActivity::class.java)
                    intent.putExtra("teamName", teamInfo[2])
                    intent.putExtra("teamINfo", bjDataList!![2])
                    val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        Pair.create(binding.teamThree, "changeAct"),
                        Pair.create(binding.teamThreeImg, "changeActImg")
                    )
                    startActivity(intent, options.toBundle())
                }
            } catch (e: Exception) {
                Toast.makeText(baseContext, "당겨서 새로고침을 해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sujang.setOnClickListener {
            bjDataList?.let {
                try {
                    if (bjDataList!![3][0].onOff==1) {
                        var intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("afreeca://player/live?user_id=superbsw123")
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://m.afreecatv.com/#/player/superbsw123")
                            )
                            startActivity(intent)
                        }
                    }
                } catch (e : Exception){
                    Toast.makeText(baseContext, "당겨서 새로고침을 해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val nextOff = listOf(
            binding.teamOnelotti, binding.teamTwolotti, binding.teamThreelotti
        )
        val nextOn = listOf(
            binding.teamOnelottiOn, binding.teamTwolottiOn, binding.teamThreelottiOn
        )

        repeat(3){
            nextOff[it].setOnClickListener { _->
                nextOff[it].playAnimation()
            }
            nextOn[it].setOnClickListener { _->
                nextOn[it].playAnimation()
            }
        }

    }
}
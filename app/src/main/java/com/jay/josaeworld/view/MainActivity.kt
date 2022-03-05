package com.jay.josaeworld.view

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.MainApp.Companion.disposable
import com.jay.josaeworld.R
import com.jay.josaeworld.base.BaseMainActivity
import com.jay.josaeworld.databinding.ActivityMainBinding
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.model.GetData
import com.jay.josaeworld.model.UtilFnc
import com.jay.josaeworld.model.response.BroadInfo
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.android.synthetic.main.custom_dialog2.*
import kotlinx.android.synthetic.main.info_dialog.*
import kotlinx.android.synthetic.main.memberchangereport_dialog.*
import java.util.*


class MainActivity : BaseMainActivity(), View.OnClickListener {
    private val TAG: String = "로그 ${this.javaClass.simpleName}"
    private lateinit var binding: ActivityMainBinding
    private lateinit var teamInfo: List<String>
    private var mainBJDataList: Array<ArrayList<BroadInfo>>? = null
    private var isLoading = false
    private var allViewers = 0
    private var allBallon = 0
    private var isFirst: Int = 0   // 0 아무것도안함, 1 최초로딩 상태만 변경 ,2 이미 데이터가 로딩된상태
    private var random = Random()
    lateinit var mAdView: AdView
    private var isRecentData = false
    private var secondSujangList: HashMap<String, String> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        val intent = Intent(this, SplashActivity::class.java)
        isSplash = true
        startActivity(intent)
        setContentView(view)
        initWithFirebaseServerData()
        buttonListener()
        refreshListener()
        fabButtonListener()
        createAdmob()
    }

    /**
     * 파이어베이스 정보로 팀데이터 정보 초기화
     */
    private fun initWithFirebaseServerData() {
        presenter.getSecondSujang()
        presenter.getTeamData(baseContext)
    }

    override fun initSecondSujang(newList: HashMap<String, String>) {
        secondSujangList = newList
    }

    override fun initTeamData(teamInfo: List<String>, time : Long) {
        this.teamInfo = teamInfo
        val now = System.currentTimeMillis()
        // 30초 보다 크면 백그라운드에서 업데이트 함수 돌려놈
        if (now - time > 30000 && isFirst == 0) {
            isFirst = 1
        }
        setDataListener()  // 팀 데이터 오면 리스너 등록
        splashException = true
        if (!isLoading) {
            binding.teamOne.text = this.teamInfo[0]
            binding.teamTwo.text = this.teamInfo[1]
            binding.teamThree.text = this.teamInfo[2]
        }
    }

    private fun createAdmob() {
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun fabButtonListener() {
        binding.fabChat.setOnClickListener {
            if (true) {
                Toast.makeText(baseContext, "서버 비용 문제로 준비중입니다\n빠른 시일내에 업데이트 하겠습니다", Toast.LENGTH_SHORT).show()
            } else
                if (!isLoading) {
                    val intent = Intent(this, ChatActivity::class.java)
                    val androidId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
                    val nickname = UtilFnc.makeCuteNickName(androidId) + androidId.slice(0..1)
                    intent.putExtra("userId", nickname)
                    intent.putExtra("uid", androidId)
                    startActivity(intent)
                }
        }
        binding.fabRounge.setOnClickListener {
            if (!isLoading) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://namu.wiki/w/%EC%8B%9C%EC%A1%B0%EC%83%88(%EC%9D%B8%ED%84%B0%EB%84%B7%20%EB%B0%A9%EC%86%A1%EC%9D%B8)"
                        )
                    )
                )
            }
        }
        binding.fabReport.setOnClickListener {
            if (!isLoading) {
                val dlg = Dialog(this, R.style.DialogStyle)

                // 커스텀 다이얼로그의 레이아웃을 설정한다.
                dlg.setContentView(R.layout.memberchangereport_dialog)
                dlg.show()

                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dlg.reportSubmit.setOnClickListener {
                    val bj = dlg.reportBj.text.trim().toString()
                    val content = dlg.suggest.text.trim().toString()
                    if (bj != "" && content != "") {
                        GetData.sendReport(listOf(bj, content), completion = {
                            if (it) {
                                Toast.makeText(baseContext, "전송 완료\n검토 후 반영하겠습니다", Toast.LENGTH_LONG).show()
                                dlg.dismiss()
                            } else
                                Toast.makeText(baseContext, "전송 불가", Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        Toast.makeText(baseContext, "BJ명과 건의사항을 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                }

                dlg.reportClose.setOnClickListener {
                    dlg.dismiss()
                }
            }
        }
    }

    private fun refreshAct() {
        if (mainBJDataList != null && !isLoading) {
            Log.d(TAG, "MainActivity ~ refreshListener() called")
            isLoading = true
            getNewBjData(mainBJDataList!!)
        }
    }

    private fun refreshListener() {
        binding.refreshLayout.setOnRefreshListener {
            if (mainBJDataList != null && isRecentData)
                refreshAct()
            else {
                binding.refreshLayout.isRefreshing = false
                this.showError(5)
            }
        }
    }

    /**
     * #1 파이어베이스 BJStatus 리스너 설정
     */
    override fun setDataListener() {
        presenter.createBJDataListener(teamInfo.size + 1)
    }

    /**
     * #2 리스너를 통해 계속 정보 업데이트
     */
    override fun changeMainBJData(newBJDataList: Array<ArrayList<BroadInfo>>?) {
        newBJDataList ?: return
        mainBJDataList = newBJDataList
        isRecentData = true
        upDateUi(mainBJDataList!!)
        if (isFirst == 1) {
            refreshAct()
            isFirst = 2
        }
    }

    override fun removeDataListener() {
        isRecentData = false
        presenter.removeBJDataListener()
    }

    /**
     * BJStatus를 아프리카 서버 기준으로 갱신시켜줍니다
     */
    private fun getNewBjData(bjlists: Array<ArrayList<BroadInfo>>) {
        Log.d(TAG, "MainActivity ~ getNewBjData() called")
        binding.loadingbar.visibility = View.VISIBLE
        binding.loadingbar.playAnimation()

        presenter.getRecentBJData(bjlists, mainBJDataList)
    }

    private fun buttonListener() {
        Log.d(TAG, "MainActivity ~ buttonListener() called")
        binding.teamFirst.setOnClickListener(this)
        binding.teamSecond.setOnClickListener(this)
        binding.teamThird.setOnClickListener(this)
        binding.teamFourth.setOnClickListener(this)
        binding.sujang.setOnClickListener(this)
        binding.moreInfo.setOnClickListener(this)
        binding.searchSijosae.setOnClickListener(this)
    }


    private fun upDateUi(bjlist: Array<ArrayList<BroadInfo>>) {

        if (!isLoading) {
            stopLoadingAnimation()
        }

        try {
            var nAllviewers = 0
            var nAllballon = 0

            val nextOff = listOf(
                binding.teamOnelotti, binding.teamTwolotti, binding.teamThreelotti, binding.teamFourlotti
            )
            val nextOn = listOf(
                binding.teamOnelottiOn, binding.teamTwolottiOn, binding.teamThreelottiOn, binding.teamFourlottiOn
            )
            val nextViewCnt = listOf(
                binding.viewCntTeam1, binding.viewCntTeam2, binding.viewCntTeam3, binding.viewCntTeam4
            )

            val teamName = listOf(
                binding.teamOne, binding.teamTwo, binding.teamThree, binding.teamFour
            )

            val teamCardViewList = listOf(binding.teamFirst, binding.teamSecond, binding.teamThird, binding.teamFourth)

            for ((index, team) in bjlist.withIndex()) {
                // 수장 처리는 따로
                if (team[0].bid == "superbsw123") {
                    nAllviewers += makeSujangView(team[0])
                    nAllballon += team[0].balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt() ?: 0
                    break
                }

                // 팀 처리
                var onOff = false
                var viewCnt = 0

                val teamNum = bjlist[index][0].teamCode
                try {
                    teamName[index].text = teamInfo[teamNum]
                } catch (e: Exception) {
                    val l = listOf("시", "조", "새", "!")
                    teamName[index].text = l[teamNum]
                }

                if (teamName[index].text == "X") {
                    teamCardViewList[index].visibility = View.GONE
                } else {
                    teamCardViewList[index].visibility = View.VISIBLE
                }


                for (member in team) {
                    if (member.onOff == 1) {
                        onOff = true
                        viewCnt += member.viewCnt.filter { it.isDigit() }.toInt()
                    }
                    nAllballon += member.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt() ?: 0
                }

                nAllviewers += viewCnt

                if (onOff) {
                    val viewer = viewCnt.toString()
                    nextViewCnt[index].text = if (viewer.length > 3)
                        viewer.slice(0 until viewer.length - 3) + "," + viewer.slice(viewer.length - 3 until viewer.length)
                    else
                        viewer

                    nextOff[index].visibility = View.GONE
                    nextOff[index].pauseAnimation()
                    nextOn[index].visibility = View.VISIBLE
                    nextOn[index].playAnimation()
                } else {
                    nextViewCnt[index].text = "0"
                    nextOn[index].visibility = View.GONE
                    nextOn[index].pauseAnimation()
                    nextOff[index].visibility = View.VISIBLE
                    nextOff[index].playAnimation()
                }
            }

            mainBJDataList = Array(bjlist.size) { arrayListOf() }

            val tNSize = teamName.size

            repeat(tNSize) {
                mainBJDataList!![it] = bjlist[it]
            }

            mainBJDataList!![tNSize] = bjlist[tNSize]

            if (allViewers != nAllviewers) {
                startCountAnimation(nAllviewers, 0)
                allViewers = nAllviewers
            }

            if (allBallon != nAllballon) {
                startCountAnimation(nAllballon, 1)
                allBallon = nAllballon
            }

        } catch (e: Exception) {
            Log.e(TAG, "upDateUi: $e")
            this.showError(1)
        }
    }

    private fun makeSujangView(broadInfo: BroadInfo): Int {
        val v = broadInfo
        var view = 0
        if (v.onOff == 1) {
            binding.sujangRest.pauseAnimation()
            binding.sujangRest.visibility = View.INVISIBLE
            binding.viewCnt.text = v.viewCnt
            binding.sujangThumbnail.visibility = View.VISIBLE

            Glide.with(baseContext).load(v.imgurl + "${random.nextInt(123456789)}")
                .override(480, 270)
                .placeholder(R.drawable.placeholder)
                .fitCenter()
                .into(binding.sujangThumbnail)

            view += v.viewCnt.filter { it.isDigit() }.toInt()

        } else {
            binding.viewCnt.text = ""
            binding.sujangThumbnail.visibility = View.INVISIBLE
            binding.sujangRest.visibility = View.VISIBLE
            binding.sujangRest.playAnimation()
        }

        Log.d(TAG, "MainActivity ~ upDateUi() called $v")
        if (v.incFanCnt.filter { c -> (c.isDigit() || c == '-') }.toInt() < 0) {
            binding.incFanCnt.setTextColor(Color.parseColor("#FF4A4A"))
        } else {
            binding.incFanCnt.setTextColor(Color.parseColor("#FFFFFF"))
        }
        binding.incFanCnt.text = v.incFanCnt
        binding.fanCnt.text = v.fanCnt
        binding.okCnt.text = v.okCnt
        binding.sujangname.text = v.bjname
        binding.sujangTitle.text = v.title
        binding.sujangname.isSelected = true
        binding.sujangTitle.isSelected = true
        binding.mballon.text = v.balloninfo?.monthballon
        binding.dballon.text = v.balloninfo?.dayballon
        return view
    }

    private fun popDialog(bid: String, question: String, code: Int) {
        var intent = Intent(Intent.ACTION_VIEW)
        val dlg = Dialog(this)

        dlg.setContentView(R.layout.custom_dialog2)

        dlg.move_question.text = question
        if (code == 0) {
            dlg.moveApp.text = "이동"
            dlg.moveWeb.text = "아니요"
            dlg.moveWeb.setTextColor(Color.parseColor("#99FFFFFF"))
        }
        dlg.show()
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dlg.moveApp.setOnClickListener {
            if (code == 0) {
                intent.data = Uri.parse("afreeca://")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.afreecatv.com/total_search.html?szSearchType=total&szStype=di&szKeyword=%EC%8B%9C%EC%A1%B0%EC%83%88")
                    )
                    startActivity(intent)
                }
                dlg.dismiss()
            } else {
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
        }

        dlg.moveWeb.setOnClickListener {
            if (code == 1) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://m.afreecatv.com/#/player/$bid")
                )
                startActivity(intent)
            }
            dlg.dismiss()
        }

    }

    private fun startCountAnimation(num: Int, code: Int) {
        val animator = ValueAnimator.ofInt(0, num)
        animator.duration = 1500
        if (code == 0) {
            try {
                animator.addUpdateListener { animation ->
                    val v = animation.animatedValue.toString()
                    binding.nAllViewer.text = UtilFnc.goodString(v)
                }
                animator.start()
            } catch (e: Exception) {
                binding.nAllViewer.text = num.toString()
            }
        } else {
            try {
                animator.addUpdateListener { animation ->
                    val v = animation.animatedValue.toString()
                    binding.nAllBallon.text = UtilFnc.goodString(v)
                }
                animator.start()
            } catch (e: Exception) {
                binding.nAllBallon.text = num.toString()
            }
        }
    }

    override fun showCustomDialog(code: Int) {
        val dlg = Dialog(this)
        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.custom_dialog)

        when (code) {
            1 -> {
                Log.d(TAG, "1 ~ showCustomDialog() called")
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.warning.isSelected = true
                // 커스텀 다이얼로그의 각 위젯들을 정의한다.
                dlg.closeOkButton.setOnClickListener {
                    super.onBackPressed()
                    dlg.dismiss()
                }

                dlg.closeNotOk.setOnClickListener {
                    dlg.dismiss()
                }
            }
            2 -> {
                dlg.question.text = "새로운 버전이 출시되었습니다\n지금 업데이트 하시겠습니까?"
                dlg.warning.text = ""
                dlg.closeOkButton.text = "업데이트"
                dlg.closeNotOk.text = "나중에"
                Log.d(TAG, "2 ~ showCustomDialog() called")
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // 커스텀 다이얼로그의 각 위젯들을 정의한다.
                dlg.closeOkButton.setOnClickListener {
                    if (gotoMarket())
                        dlg.dismiss()
                }

                dlg.closeNotOk.setOnClickListener {
                    dlg.dismiss()
                }
            }
            3 -> {
                Log.d(TAG, "3 ~ showCustomDialog() called")
                dlg.question.text = "업데이트를 필수로 진행해야 합니다!"
                dlg.warning.text = ""
                dlg.closeOkButton.text = "업데이트"
                dlg.closeNotOk.text = ""
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.setCancelable(false)
                dlg.setCanceledOnTouchOutside(false)
                // 커스텀 다이얼로그의 각 위젯들을 정의한다.
                dlg.closeOkButton.setOnClickListener {
                    if (gotoMarket())
                        finish()
                }
            }
        }
    }

    fun gotoMarket(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        var result: Boolean
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
        try {
            startActivity(intent)
            result = true
        } catch (e: Exception) {
            Toast.makeText(baseContext, "플레이스토어 연결 불가", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "gotoMarket: $e")
            result = false
        }
        return result
    }

    override fun stopLoadingAnimation() {
        binding.loadingbar.pauseAnimation()
        binding.loadingbar.visibility = View.GONE
    }

    override fun makeRefreshstate(state: Boolean) {
//        disposable.dispose()
        binding.refreshLayout.isRefreshing = state
    }

    override fun changeIsLoadingState(state: Boolean) {
        isLoading = state
    }

    override fun onBackPressed() {
        showCustomDialog(1)
    }

    override fun showError(code: Int) {
        this@MainActivity.showError(code)
    }

    override fun showToast(msg: String) {
        this@MainActivity.toast(msg)
    }

    override fun onClick(view: View?) {
        var toast: Toast

        when (view) {
            binding.teamFirst -> {
                moveTeamList(
                    binding.teamOne.text as String, mainBJDataList?.get(0),
                    secondSujangList[binding.teamOne.text as String] ?: "1"
                )
            }
            binding.teamSecond -> {
                moveTeamList(
                    binding.teamTwo.text as String, mainBJDataList?.get(1),
                    secondSujangList[binding.teamTwo.text as String] ?: "2"
                )
            }
            binding.teamThird -> {
                moveTeamList(
                    binding.teamThree.text as String, mainBJDataList?.get(2),
                    secondSujangList[binding.teamThree.text as String] ?: "3"
                )
            }
            binding.teamFourth -> {
                moveTeamList(
                    binding.teamFour.text as String, mainBJDataList?.get(3),
                    secondSujangList[binding.teamFour.text as String] ?: "3"
                )
            }

            binding.sujang -> {
                mainBJDataList?.let {
                    try {
                        val n = mainBJDataList!!.size - 1
                        val v = mainBJDataList!![n].first()

                        if (mainBJDataList!![n][0].onOff == 1 && !isLoading) {
                            val question = "${v.viewCnt} 명이 시청중입니다!\n이동할까요?"
                            popDialog(v.bid, question, 1)
                        } else if (mainBJDataList!![n][0].onOff == 0 && !isLoading) {
                            val question = "시조새는 방송중이 아닙니다\n아프리카로 이동하시겠습니까?"
                            popDialog(v.bid, question, 0)
                        } else if (isLoading)
                            this.showError(3)

                    } catch (e: Exception) {
                        Log.e(TAG, "buttonListener: $e")
                        toast = Toast.makeText(
                            baseContext, "밑으로 내려서 다시 로딩해 주세요", Toast.LENGTH_LONG
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
            }
            binding.moreInfo -> {
                mainBJDataList?.let {
                    try {
                        val n = mainBJDataList!!.size - 1
                        val v = mainBJDataList!![n].first()

                        if (!isLoading) {
                            var intent = Intent(Intent.ACTION_VIEW)
                            val dlg = Dialog(this)
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

                        } else
                            this.showError(3)

                    } catch (e: Exception) {
                        Log.e(TAG, "buttonListener: $e")
                        toast = Toast.makeText(
                            baseContext, "밑으로 내려서 다시 로딩해 주세요", Toast.LENGTH_LONG
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
            }

            binding.searchSijosae -> {
                val intent = Intent(this, BroadCastActivity::class.java)
                if (!isLoading)
                    mainBJDataList?.let {
                        intent.putExtra("teamName", "시조새 검색 결과")
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                    }
                else
                    this.showError(3)
            }
        }
    }

    /**
     * 새로운 액티비티에서 해당 팀 보여주기
     */
    private fun moveTeamList(teamName: String, teamList: ArrayList<BroadInfo>?, secondSujang: String) {
        val toast: Toast
        val intent = Intent(this, BroadCastActivity::class.java)
        try {
            if (!isLoading && !teamList.isNullOrEmpty()) {
                intent.putExtra("teamName", teamName)
                intent.putExtra("teamINfo", teamList)
                intent.putExtra(
                    "secondSujang",
                    secondSujang
                )
                startActivity(intent)
                // 새로운 액티비티ani, 기존 액티비티ani
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            } else
                this.showError(3)
        } catch (e: Exception) {
            Log.d(TAG, "MainActivity ~ $e() called")
            toast = Toast.makeText(
                baseContext, "밑으로 내려서 다시 로딩해 주세요", Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }
}
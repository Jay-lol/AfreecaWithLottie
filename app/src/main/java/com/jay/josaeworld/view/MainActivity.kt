package com.jay.josaeworld.view

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.BuildConfig
import com.jay.josaeworld.R
import com.jay.josaeworld.base.BaseViewBindingActivity
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.data.UserPreferencesRepository
import com.jay.josaeworld.databinding.*
import com.jay.josaeworld.di.UrlModule
import com.jay.josaeworld.domain.goodString
import com.jay.josaeworld.domain.makeCuteNickName
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.extension.showErrorToast
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.presenter.MainPresenter
import com.jay.josaeworld.view.BroadCastActivity.Companion.KEY_TEAM_NAME
import com.jay.josaeworld.view.SplashActivity.Companion.KEY_LAST_UPDATE_TIME
import com.jay.josaeworld.view.SplashActivity.Companion.KEY_NEW_LIST
import com.jay.josaeworld.view.SplashActivity.Companion.KEY_UPDATE_CODE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    BaseViewBindingActivity<ActivityMainBinding, MainPresenter>({ ActivityMainBinding.inflate(it) }),
    MainContract.View {

    private val TAG: String = "로그 ${this.javaClass.simpleName}"
    private lateinit var teamInfo: List<String>
    private var mainBJDataList: Array<ArrayList<BroadInfo>>? = null
    private var isCrawlingForFirebase = false
    private var allViewers = 0
    private var allBallon = 0
    private var isDataUpdateNeeded = false

    @Inject
    lateinit var dataStore: UserPreferencesRepository

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
    lateinit var mAdView: AdView
    private var isRecentData = false
    private var underBossList: HashMap<String, String> = hashMapOf()

    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            showToast("디버그 모드")
        }
        intent.extras?.run {
            if (getInt(KEY_UPDATE_CODE) == 2) {
                showCustomDialog(2)
            }
            initTeamData(
                getStringArrayList(KEY_NEW_LIST) as List<String>,
                getLong(KEY_LAST_UPDATE_TIME)
            )
        }
        showCoachMark()
        getUnderBossFromFirebase()
        refreshListener()
        initButtonListener()
        createAdmob()
    }

    /**
     * 파이어베이스 정보로 second 정보 초기화
     */
    private fun getUnderBossFromFirebase() {
        presenter.getUnderBoss()
    }

    override fun initUnderBoss(newList: HashMap<String, String>) {
        underBossList = newList
    }

    private fun initTeamData(teamInfo: List<String>, time: Long) {
        this.teamInfo = teamInfo
        val now = System.currentTimeMillis()
        // 30초 보다 크면 백그라운드에서 업데이트 함수 돌려놈
        if (now - time > 30000) {
            isDataUpdateNeeded = true
        }
        setDataListener() // 팀 데이터 오면 리스너 등록
    }

    private fun refreshAct() {
        if (mainBJDataList != null && !isCrawlingForFirebase) {
            Log.d(TAG, "MainActivity ~ refreshListener() called")
            isCrawlingForFirebase = true
            getNewBjData(mainBJDataList!!)
        }
    }

    private fun refreshListener() {
        binding.refreshLayout.setOnRefreshListener {
            if (isRecentData) {
                refreshAct()
            } else {
                binding.refreshLayout.isRefreshing = false
                this.showError(5)
            }
        }
    }

    /**
     * #1 파이어베이스 BJStatus 리스너 설정
     */
    private fun setDataListener() {
        presenter.createBJDataListener(teamInfo.size + 1)
    }

    /**
     * #2 리스너를 통해 계속 정보 업데이트
     */
    override fun changeMainBJData(newBJDataList: Array<ArrayList<BroadInfo>>?) {
        newBJDataList ?: return
        mainBJDataList = newBJDataList.clone()
        isRecentData = true
        mainBJDataList?.let {
            updateUIwithRecentList(it)
        }
        if (isDataUpdateNeeded) {
            refreshAct()
            isDataUpdateNeeded = false
        }
    }

    private fun removeDataListener() {
        isRecentData = false
        presenter.removeBJDataListener()
    }

    /**
     * BJStatus를 아프리카 서버 기준으로 갱신시켜줍니다
     */
    private fun getNewBjData(bjlists: Array<ArrayList<BroadInfo>>) {
        Log.d(TAG, "MainActivity ~ getNewBjData() called")
        binding.mainLoadingLottie.visibility = View.VISIBLE
        binding.mainLoadingLottie.playAnimation()

        presenter.getRecentBJData(bjlists, mainBJDataList)
    }

    private fun updateUIwithRecentList(bjlist: Array<ArrayList<BroadInfo>>) {

        if (!isCrawlingForFirebase) {
            stopLoadingAnimation()
        }

        try {
            var nAllviewers = 0
            var nAllballon = 0

            for (team in bjlist) {
                // 수장 처리는 따로
                if (team[0].bid == "superbsw123") {
                    nAllviewers += makeBossView(team[0])
                    nAllballon += team[0].balloninfo?.monthballon?.filter { c -> c.isDigit() }
                        ?.toInt() ?: 0
                    break
                }

                // 팀 시청자, 별풍선 카운트
                var viewCnt = 0

                for (member in team) {
                    if (member.onOff == 1) {
                        viewCnt += member.viewCnt.filter { it.isDigit() }.toInt()
                    }
                    nAllballon += member.balloninfo?.monthballon?.filter { c -> c.isDigit() }?.toInt() ?: 0
                }

                nAllviewers += viewCnt
            }

            if (allViewers != nAllviewers) {
                startCountAnimation(nAllviewers, 0)
                allViewers = nAllviewers
            }

            if (allBallon != nAllballon) {
                startCountAnimation(nAllballon, 1)
                allBallon = nAllballon
            }

            if (fragment == null) {
                fragment = TeamListFragment.newInstance(teamInfo, bjlist, underBossList)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_team_list, fragment as TeamListFragment)
                    .commit()
            } else {
                (fragment as? TeamListFragment)?.updateTeamListFragmentUi(mainBJDataList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "upDateUi: $e")
            this.showError(1)
        }
    }

    private fun makeBossView(broadInfo: BroadInfo): Int {
        val v = broadInfo
        var viewCount = 0
        if (v.onOff == 1) {
            binding.bossView.run {
                braodRestLottieV2.pauseAnimation()
                braodRestLottieV2.visibility = View.INVISIBLE
                viewCnt.text = v.viewCnt
                thumbnail.visibility = View.VISIBLE
                Glide.with(baseContext).load(v.imgurl + "${random.nextInt(123456789)}")
                    .override(480, 270)
                    .fitCenter()
                    .into(thumbnail)

                viewCount += v.viewCnt.filter { it.isDigit() }.toInt()

                if (viewCount >= 10_000) {
                    highlightCardViewLottie.visibility = View.VISIBLE
                    highlightCardViewLottie.playAnimation()
                } else {
                    highlightCardViewLottie.visibility = View.INVISIBLE
                    highlightCardViewLottie.pauseAnimation()
                }
            }
        } else {
            binding.bossView.run {
                viewCnt.text = ""
                thumbnail.visibility = View.INVISIBLE
                braodRestLottieV2.visibility = View.VISIBLE
                braodRestLottieV2.playAnimation()
                highlightCardViewLottie.visibility = View.INVISIBLE
                highlightCardViewLottie.pauseAnimation()
            }
        }

        Log.d(TAG, "MainActivity ~ upDateUi() called $v")
        binding.bossView.run {
            if (v.incFanCnt.filter { c -> (c.isDigit() || c == '-') }.toInt() < 0) {
                incFanCnt.setTextColor(Color.parseColor("#FF4A4A"))
            } else {
                incFanCnt.setTextColor(Color.parseColor("#FFFFFF"))
            }
            incFanCnt.text = v.incFanCnt
            fanCnt.text = v.fanCnt
            okCnt.text = v.okCnt
            bjname.text = v.bjname
            title.text = v.title
            bjname.isSelected = true
            title.isSelected = true
            mballon.text = v.balloninfo?.monthballon
            dballon.text = v.balloninfo?.dayballon
        }
        return viewCount
    }

    private fun popDialog(bid: String, question: String, code: Int) {
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
                intent.data = Uri.parse(goLiveUrlApp + bid)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
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

        dlgBinding.moveWeb.setOnClickListener {
            if (code == 1) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(goLiveUrlWeb + bid)
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
                    binding.viewMainInfoData.nAllViewer.text = v.goodString()
                }
                animator.start()
            } catch (e: Exception) {
                binding.viewMainInfoData.nAllViewer.text = num.toString()
            }
        } else {
            try {
                animator.addUpdateListener { animation ->
                    val v = animation.animatedValue.toString()
                    binding.viewMainInfoData.nAllBallon.text = v.goodString()
                }
                animator.start()
            } catch (e: Exception) {
                binding.viewMainInfoData.nAllBallon.text = num.toString()
            }
        }
    }

    override fun showCustomDialog(code: Int) {
        val dlg = Dialog(this)
        val dlgBinding = CustomDialogBinding.inflate(layoutInflater)
        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(dlgBinding.root)

        when (code) {
            1 -> {
                Log.d(TAG, "1 ~ showCustomDialog() called")
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlgBinding.warning.isSelected = true
                // 커스텀 다이얼로그의 각 위젯들을 정의한다.
                dlgBinding.closeOkButton.setOnClickListener {
                    finish()
                    dlg.dismiss()
                }

                dlgBinding.closeNotOk.setOnClickListener {
                    dlg.dismiss()
                }
            }
            2 -> {
                dlgBinding.question.text = "새로운 버전이 출시되었습니다\n지금 업데이트 하시겠습니까?"
                dlgBinding.warning.text = ""
                dlgBinding.closeOkButton.text = "업데이트"
                dlgBinding.closeNotOk.text = "나중에"
                Log.d(TAG, "2 ~ showCustomDialog() called")
                dlg.show()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // 커스텀 다이얼로그의 각 위젯들을 정의한다.
                dlgBinding.closeOkButton.setOnClickListener {
                    if (gotoMarket())
                        dlg.dismiss()
                }

                dlgBinding.closeNotOk.setOnClickListener {
                    dlg.dismiss()
                }
            }
        }
    }

    private fun gotoMarket(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        var result: Boolean
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
        try {
            startActivity(intent)
            result = true
        } catch (e: Exception) {
            showToast("플레이스토어 연결 불가")
            result = false
        }
        return result
    }

    override fun stopLoadingAnimation() {
        binding.mainLoadingLottie.pauseAnimation()
        binding.mainLoadingLottie.visibility = View.GONE
    }

    override fun makeRefreshstate(state: Boolean) {
        binding.refreshLayout.isRefreshing = state
    }

    override fun changeIsCrawlingForFirebaseState(state: Boolean) {
        isCrawlingForFirebase = state
    }

    override fun onBackPressed() {
        showCustomDialog(1)
    }

    override fun showError(code: Int) {
        showErrorToast(code)
    }

    override fun showToast(msg: String, isCenter: Boolean) {
        toast(msg, isCenter)
    }

    private fun initButtonListener() {
        binding.bossView.root.setOnClickListener {
            mainBJDataList?.let {
                try {
                    val n = mainBJDataList!!.size - 1
                    val v = mainBJDataList!![n].first()

                    if (mainBJDataList!![n][0].onOff == 1) {
                        val question = "${v.viewCnt} 명이 시청중입니다!\n이동할까요?"
                        popDialog(v.bid, question, 1)
                    } else {
                        val question = "시조새는 방송중이 아닙니다\n아프리카로 이동하시겠습니까?"
                        popDialog(v.bid, question, 0)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "buttonListener: $e")
                    showToast("밑으로 내려서 다시 로딩해 주세요", true)
                }
            }
        }
        binding.bossView.moreInfo.setOnClickListener {
            mainBJDataList?.let {
                try {
                    val n = mainBJDataList!!.size - 1
                    val v = mainBJDataList!![n].first()
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
                    CoroutineScope(Dispatchers.Main).launch {
                        dataStore.incrementCoachMarkCount()
                        binding.bossView.coachClick.visibility = View.GONE
                        cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "buttonListener: $e")
                    showToast("밑으로 내려서 다시 로딩해 주세요", true)
                }
            }
        }

        binding.searchSijosae.setOnClickListener {
            mainBJDataList?.let {
                val intent = Intent(this, BroadCastActivity::class.java)
                intent.putExtra(KEY_TEAM_NAME, "시조새 검색 결과")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            }
        }

        binding.buttonFloatingMenu.fabChat.setOnClickListener {
            if (true) {
                showToast(
                    "서버 비용 문제로 준비중입니다\n" +
                        "빠른 시일내에 업데이트 하겠습니다"
                )
            } else {
                val intent = Intent(this, ChatActivity::class.java)
                val androidId =
                    Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
                val nickname = androidId.makeCuteNickName() + androidId.slice(0..1)
                intent.putExtra("userId", nickname)
                intent.putExtra("uid", androidId)
                startActivity(intent)
            }
        }
        binding.buttonFloatingMenu.fabRounge.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://namu.wiki/w/%EC%8B%9C%EC%A1%B0%EC%83%88(%EC%9D%B8%ED%84%B0%EB%84%B7%20%EB%B0%A9%EC%86%A1%EC%9D%B8)"
                    )
                )
            )
        }
        binding.buttonFloatingMenu.fabReport.setOnClickListener {
            val dlg = Dialog(this, R.style.DialogStyle)
            val dlgBinding = DialogMemberChangeReportBinding.inflate(layoutInflater)
            // 커스텀 다이얼로그의 레이아웃을 설정한다.
            dlg.setContentView(dlgBinding.root)
            dlg.show()

            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dlgBinding.reportSubmit.setOnClickListener {
                val bj = dlgBinding.reportBj.text.trim().toString()
                val content = dlgBinding.suggest.text.trim().toString()
                if (bj.isNotEmpty() && content.isNotEmpty()) {
                    presenter.sendReport(listOf(bj, content)) { dlg.dismiss() }
                } else {
                    showToast("BJ명과 건의사항을 확인해주세요")
                }
            }

            dlgBinding.reportClose.setOnClickListener {
                dlg.dismiss()
            }
        }
    }

    private fun showCoachMark() {
        if (dataStore.coachMarkCount <= 1) {
            binding.bossView.coachClick.visibility = View.VISIBLE
        }
    }

    private fun createAdmob() {
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        mAdView.loadAd(adRequest)
    }

    // 라이프사이클에 맞춰 리스너 설정
    override fun onStart() {
        setDataListener()
        super.onStart()
    }

    override fun onStop() {
        removeDataListener()
        super.onStop()
    }
}

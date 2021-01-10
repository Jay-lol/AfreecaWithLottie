package com.jay.josaeworld.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.ActivityChatBinding


class ChatActivity : AppCompatActivity() {
    lateinit var binding : ActivityChatBinding
    lateinit var mAdView : AdView
    lateinit var nickname : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        nickname = intent.getStringExtra("userId") ?: "익명의 사용자"
        val uid = intent.getStringExtra("uid") ?: "123456789"
        Toast.makeText(baseContext, "${nickname}으로 접속", Toast.LENGTH_SHORT).show()

        MobileAds.initialize(this){}
        mAdView = binding.adView

//        val lyparams = mAdView.layoutParams as ConstraintLayout.LayoutParams
//        lyparams.topMargin = getStatusBarHeight()
//        mAdView.layoutParams = lyparams

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }
    override fun onResume() {

        super.onResume()
    }

    override fun onStop() {

        super.onStop()
    }

    //status bar의 높이 계산
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }
}
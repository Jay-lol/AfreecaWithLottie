package com.jay.josaeworld.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.jay.josaeworld.R
import com.jay.josaeworld.contract.BroadContract
import com.jay.josaeworld.presenter.BroadPresenter

abstract class BaseBroadActivity
    : AppCompatActivity(), BroadContract.View {

    lateinit var presenter : BroadPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BroadPresenter()
        initPresenter()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }
    private fun initPresenter(){
        presenter.takeView(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }
    override fun finish() {
        super.finish()
        // 새로운 액티비티, 기존 액티비티
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }
}
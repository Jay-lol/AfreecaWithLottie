package com.jay.josaeworld.base

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.jay.josaeworld.R
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.model.RetrofitBuilder
import com.jay.josaeworld.presenter.MainPresenter

abstract class BaseActivity<T : MainContract.Presenter> : AppCompatActivity(), MainContract.View {

    private val TAG: String = "로그 ${this.javaClass.simpleName}"
    lateinit var presenter : T
    var splashException = false
    companion object{
        var isSplash = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitBuilder.setBaseURL(
            getString(R.string.BASE_URL),
            getString(R.string.SEARCH_BASE_URL),
            getString(R.string.REQUEST)
        )
        initPresenter()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }

    open fun initPresenter(){
        presenter.takeView(this)
    }
    abstract fun setDataListener()
    abstract fun removeDataListener()

    // 라이프사이클에 맞춰 리스너 설정
    override fun onStart() {
        if (splashException) setDataListener()
        super.onStart()
    }

    override fun onResume() {
        if (splashException) setDataListener()
        super.onResume()
    }

    override fun onPause() {
        if (!isSplash) {
            Log.d(TAG, "MainActivity ~ onPause() remove called")
            removeDataListener()
        }
        super.onPause()
    }

    override fun onStop() {
        if (!isSplash) {
            Log.d(TAG, "MainActivity ~ onStop() remove called")
            removeDataListener()
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }

}
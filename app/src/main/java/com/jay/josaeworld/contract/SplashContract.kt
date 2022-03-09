package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView

interface SplashContract {
    interface View : BaseView {
        fun startMainActivity(newList: List<String>, time: Long, code: Int = 0)
    }

    interface Presenter : BasePresenter {
        fun getInitTeamData()
    }
}

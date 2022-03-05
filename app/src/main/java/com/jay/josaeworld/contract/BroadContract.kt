package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView

interface BroadContract {
    interface View : BaseView
    interface Presenter : BasePresenter<View>
}

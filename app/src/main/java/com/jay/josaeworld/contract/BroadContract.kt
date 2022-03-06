package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView
import com.jay.josaeworld.domain.model.response.gsonParse.RealBroad

interface BroadContract {
    interface View : BaseView {
        fun showSearchResult(searchBJInfo: RealBroad?)
    }

    interface Presenter : BasePresenter {
        fun searchJosae()
    }
}

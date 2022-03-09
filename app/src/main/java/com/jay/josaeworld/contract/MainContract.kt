package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView
import com.jay.josaeworld.domain.model.response.BroadInfo

interface MainContract {

    interface View : BaseView {
        fun changeMainBJData(newBJDataList: Array<ArrayList<BroadInfo>>?)
        fun stopLoadingAnimation()
        fun makeRefreshstate(state: Boolean)
        fun changeIsCrawlingForFirebaseState(state: Boolean)
        fun initSecondSujang(newList: HashMap<String, String>)
        fun showCustomDialog(code: Int)
    }

    interface Presenter : BasePresenter {
        fun getRecentBJData(
            bjLists: Array<ArrayList<BroadInfo>>,
            bjDataList: Array<ArrayList<BroadInfo>>?
        )

        fun createBJDataListener(teamSize: Int)
        fun removeBJDataListener()
        fun getSecondSujang()
        fun sendReport(reportList: List<String>, function: () -> Unit)
    }
}

package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView
import com.jay.josaeworld.domain.model.response.BroadInfo
import kotlinx.coroutines.Job

interface MainContract {

    interface View : BaseView {
        fun changeMainBJData(newBJDataList: Array<ArrayList<BroadInfo>>?)
        fun stopLoadingAnimation()
        fun makeRefreshstate(state: Boolean)
        fun changeIsCrawlingForFirebaseState(state: Boolean)
        fun initUnderBoss(newList: HashMap<String, String>)
        fun showCustomDialog(code: Int)
    }

    interface Presenter : BasePresenter {
        fun getRecentBJData(
            bjLists: Array<ArrayList<BroadInfo>>,
            bjDataList: Array<ArrayList<BroadInfo>>?
        ): Job

        fun createBJDataListener(teamSize: Int)
        fun removeBJDataListener()
        fun getUnderBoss()
        fun sendReport(reportList: List<String>, function: () -> Unit)
    }
}

package com.jay.josaeworld.contract

import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView
import com.jay.josaeworld.domain.model.response.BroadInfo
import kotlinx.coroutines.Job

interface MainContract {

    interface View : BaseView {
        fun changeMainStreamerData(newStreamerDataList: Array<ArrayList<BroadInfo>>?)
        fun stopLoadingAnimation()
        fun makeRefreshstate(state: Boolean)
        fun changeIsCrawlingForFirebaseState(state: Boolean)
        fun initUnderBoss(newList: HashMap<String, String>)
        fun showCustomDialog(code: Int)
    }

    interface Presenter : BasePresenter {
        fun getRecentStreamerData(
            streamerLists: Array<ArrayList<BroadInfo>>,
            streamerDataList: Array<ArrayList<BroadInfo>>?
        ): Job

        fun createStreamerDataListener(teamSize: Int)
        fun removeStreamerDataListener()
        fun getUnderBoss()
        fun sendReport(reportList: List<String>, function: () -> Unit)
    }
}

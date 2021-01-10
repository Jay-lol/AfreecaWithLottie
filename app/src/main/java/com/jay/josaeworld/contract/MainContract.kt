package com.jay.josaeworld.contract

import android.content.Context
import com.jay.josaeworld.base.BasePresenter
import com.jay.josaeworld.base.BaseView
import com.jay.josaeworld.model.response.BroadInfo
import java.util.ArrayList
import java.util.HashMap

interface MainContract {

    interface View : BaseView {
        fun changeMainBJData(newBJDataList : Array<ArrayList<BroadInfo>>?)
        fun stopLoadingAnimation()
        fun makeRefreshstate(state : Boolean)
        fun changeIsLoadingState(state : Boolean)
        fun initSecondSujang(newList : HashMap<String, String>)
        fun initTeamData(teamInfo : List<String>, time :Long)
        fun showCustomDialog(code : Int)
    }

    interface Presenter : BasePresenter<View>{
        fun getRecentBJData(bjLists: Array<ArrayList<BroadInfo>>, bjDataList : Array<ArrayList<BroadInfo>>?)
        fun createBJDataListener(teamSize: Int)
        fun removeBJDataListener()
        fun getSecondSujang()
        fun getTeamData(context : Context)
    }
}
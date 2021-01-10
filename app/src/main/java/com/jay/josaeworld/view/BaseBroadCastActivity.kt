package com.jay.josaeworld.view

import com.jay.josaeworld.model.response.BroadInfo

interface BaseBroadCastActivity {
    fun loadAd()
    fun setBroadView(list : ArrayList<BroadInfo>)
    fun setSearchView()
}

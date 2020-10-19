package com.jay.josaeworld.model

interface DataCallback {
    fun finishLoading(list : List<String>){}
    fun finishBroadDataLoading(list : List<BroadInfo>){}
    fun finishBjDataLoading(bjlist : Array<ArrayList<BroadInfo>>){}
}
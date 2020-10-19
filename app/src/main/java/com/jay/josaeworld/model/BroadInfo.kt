package com.jay.josaeworld.model

import java.io.Serializable

data class BroadInfo(
    var onOff : Int,
    var bid : String,
    var title : String,
    var bjname : String,
    var imgurl : String
) : Serializable
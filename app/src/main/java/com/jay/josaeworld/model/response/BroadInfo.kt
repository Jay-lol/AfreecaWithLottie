package com.jay.josaeworld.model.response

import java.io.Serializable

data class BroadInfo(
    var teamCode : Int,
    var onOff : Int,
    var bid : String,
    var title : String,
    var bjname : String,
    var imgurl : String,
    var viewCnt : String,
    var fanCnt : String,
    var okCnt : String,
    var incFanCnt : String,
    var profilePhoto : String,
    var balloninfo : BallonInfo?
) : Serializable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "teamCode" to teamCode,
            "onOff" to onOff,
            "title" to title,
            "bjname" to bjname,
            "imgUrl" to imgurl,
            "viewCnt" to viewCnt,
            "fanCnt" to fanCnt,
            "okCnt" to okCnt,
            "incFanCnt" to incFanCnt,
            "profilePhoto" to profilePhoto
        )
    }
}
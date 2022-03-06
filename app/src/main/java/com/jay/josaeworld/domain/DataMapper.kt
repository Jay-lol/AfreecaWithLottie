package com.jay.josaeworld.domain

import android.util.Log
import com.jay.josaeworld.UtilFnc
import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo
import io.reactivex.Single

fun Single<AfSearchResponse?>.toBroadInfo(params: GetMemberUseCase.Params): Single<BroadInfo> =
    map { searchResponse ->
        val CLIENT_ID: String = searchResponse.station!!.user_id

        val onOff: Int = searchResponse.broad?.let { 1 } ?: 0
        val bjname: String = searchResponse.station.user_nick
        val title: String = searchResponse.broad?.broad_title?.replace("   ", "")
            ?: "방송 중이지 않습니다"
        val allviewers: String = searchResponse.broad?.let {
            UtilFnc.goodString(it.current_sum_viewer.toString())
        } ?: "0" // 전체 시청자
        val imgurl: String = searchResponse.broad?.let {
            "http://liveimg.afreecatv.com/${it.broad_no}_480x270.jpg?dummy="
        } ?: "http://res.afreecatv.com/images/default_logo_300x300.jpg"

        // 0아니면 1 today0, today1
        val activeNo = searchResponse.station.active_no
        val profile = "http:" + searchResponse.profile
        // 팬 숫자
        val fanCnt: String = UtilFnc.goodString(searchResponse.station.upd.fan_cnt.toString())
        // 추천 숫자
        val okCnt: String = if (activeNo == 0)
            UtilFnc.goodString(searchResponse.station.upd.today0_ok_cnt.toString())
        else
            UtilFnc.goodString(searchResponse.station.upd.today1_ok_cnt.toString())

        // 오늘 추가된 즐겨찾기 수
        var incFanCnt: String = if (activeNo == 0)
            searchResponse.station.upd.today0_fav_cnt.toString()
        else
            searchResponse.station.upd.today1_fav_cnt.toString()

        incFanCnt = if (incFanCnt.toInt() < 0)
            "-" + UtilFnc.goodString(incFanCnt.slice(1 until incFanCnt.length))
        else UtilFnc.goodString(incFanCnt)

        Log.d(
            "DataMapper",
            "GetJson() $bjname $title $allviewers $imgurl $fanCnt $okCnt $incFanCnt"
        )

        BroadInfo(
            params.teamCode,
            onOff,
            CLIENT_ID,
            title,
            bjname,
            imgurl,
            allviewers,
            fanCnt,
            okCnt,
            incFanCnt,
            profile,
            null
        )
    }.onErrorReturn {
        BroadInfo(
            403, 1, params.bid, "", "",
            "", "", "", "error", "", "",
            BallonInfo("0", "0", "0", "0분", "0", "0")
        )
    }

fun HashMap<*, *>.goodBallonData(): BallonInfo {
    return BallonInfo(
        get("dayballon") as? String ?: "0",
        get("monthballon") as? String ?: "0",
        get("monthmaxview") as? String ?: "0",
        get("monthtime") as? String ?: "0분",
        get("monthview") as? String ?: "0",
        get("monthpay") as? String ?: "0"
    )
}

package com.jay.josaeworld.domain

import android.util.Log
import com.jay.josaeworld.domain.model.response.AfSearchResponse
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.domain.model.response.BroadInfo

fun AfSearchResponse.toBroadInfo(params: GetMemberUseCase.Params): BroadInfo =
    run {
        station ?: return BroadInfo(
            teamCode = 403,
            onOff = 1,
            bid = params.bid,
            balloninfo = BallonInfo()
        )

        val CLIENT_ID = station.user_id

        val onOff: Int = broad?.let { 1 } ?: 0
        val bjname = station.user_nick
        val title = broad?.broad_title?.replace("   ", "")
            ?: "방송 중이지 않습니다"
        val allviewers =
            broad?.current_sum_viewer?.toString()?.goodString() ?: "0" // 전체 시청자
        val imgurl: String = broad?.let {
            "${params.liveImgUrl}${it.broad_no}_480x270.jpg?dummy="
        } ?: params.defaultLogoImgUrl

        // 0아니면 1 today0, today1
        val activeNo = station.active_no
        val profile = "http:$profile"
        // 팬 숫자
        val fanCnt = station.upd.fan_cnt.toString().goodString()
        // 추천 숫자
        val okCnt = if (activeNo == 0)
            station.upd.today0_ok_cnt.toString().goodString()
        else
            station.upd.today1_ok_cnt.toString().goodString()

        // 오늘 추가된 즐겨찾기 수
        var incFanCnt = if (activeNo == 0)
            station.upd.today0_fav_cnt.toString()
        else
            station.upd.today1_fav_cnt.toString()

        incFanCnt = if (incFanCnt.toInt() < 0)
            "-" + incFanCnt.slice(1 until incFanCnt.length).goodString()
        else incFanCnt.goodString()

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

fun HashMap<*, *>.goodBjData(
    teamCode: String,
    bid: String,
    b: BallonInfo?,
    defaultLogoImgUrl: String
): BroadInfo {
    return BroadInfo(
        teamCode.toInt(),
        (get("onOff") as? Long)?.toInt() ?: 0,
        bid,
        get("title") as? String ?: "정보 갱신을 해주세요",
        get("bjname") as? String ?: "새로운 멤버",
        get("imgUrl") as? String ?: defaultLogoImgUrl,
        get("viewCnt") as? String ?: "0",
        get("fanCnt") as? String ?: "0",
        get("okCnt") as? String ?: "0",
        get("incFanCnt") as? String ?: "0",
        (get("profilePhoto") as? String)
            ?: defaultLogoImgUrl,
        b ?: BallonInfo()
    )
}

fun Array<ArrayList<BroadInfo>>.sortedBJList(): Array<ArrayList<BroadInfo>> {
    return sortedWith(
        compareBy({ teamData ->
            -teamData.sumOf { view ->
                view.viewCnt.filter { c -> c.isDigit() }.toInt()
            }
        }, { teamData ->
            -teamData.sumOf { i ->
                i.onOff
            }
        }, { teamData ->
            -teamData.sumOf { view ->
                view.okCnt.filter { c -> c.isDigit() }.toInt()
            }
        })
    ).toTypedArray()
}

fun String.goodString(): String {
    return try {
        if (length > 3) {
            slice(0 until length - 3).goodString() +
                "," + slice(length - 3 until length)
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}

fun String.makeCuteNickName(): String {
    var nickname: String = ""

    val a = listOf(
        "점잖은", "귀여운", "힘내라", "대견한", "우람한", "보송보송", "뽀송뽀송", "초코", "구수한", "엄근진",
        "불쌍한", "마라톤을 잘하는", "먹성이 좋은", "새파란", "젠틀", "아메리칸", "나이스한", "경치좋은", "타로잘하는", "매니저",
        "콜라매니아", "세수한", "시원한", "답답한", "멋진", "아름다운", "괴씸한", "사쿠란보"
    )
    val b = listOf("애기", "왕관", "호우", "")

    val c = listOf(
        "사자", "호랑이", "새우", "낙타", "곰", "고등어", "독수리", "벌잡새", "수달",
        "불가사리", "하늘소", "거북이", "염소", "치타",
        "참새", "까치", "박쥐", "해파리", "캥거루", "토끼", "강아지", "고양이", "코알라", "고릴라", "원숭이", "고래", "뱀",
        "물개", "쥐", "소", "말", "돼지", "악어", "표범",
        "늑대", "여우", "스컹크", "두더지", "돌고래", "도마뱀", "독소리", "바다표범", "가재",
        "랍스타", "원앙", "까마귀", "오리", "앵무새", "부엉이", "참새", "꾀꼬리", "나비",
        "잠자리", "이구아나", "카멜레온", "개미핥기", "거미", "잉어", "펭귄", "거위",
        "병아리", "닭", "멧돼지", "갈매기", "코뿔소", "사슴", "코끼리", "하마",
        "다람쥐", "가오리", "미어캣", "코브라", "자라", "두꺼비", "복어", "문어",
        "오징어", "쭈꾸미", "오리너구리", "너구리", "양", "꿩", "개구리",
        "메추라기", "살모사", "매미", "매", "망둥어", "말똥구리", "벌",
        "비둘기", "풍뎅이", "산양", "딱따구리", "등에", "두루미", "달팽이", "다슬기",
        "노루", "오솔개", "누에", "아나콘다", "남생이", "뻐꾸기", "낙지", "제비", "검은꼬리잭토끼"
    )

    val len = length
    var start = 0
    var last = len / 3
    try {
        for (i in 0 until 3) {
            val t: String
            if (i != 2) {
                t = slice(start until last)
                start = last
                last += len / 3
            } else
                t = slice(last until len)

            var sum = 0
            for (j in t) {
                sum += j.code
            }

            if (i == 0)
                nickname += a[sum % a.size] + " "
            else if (i == 1)
                nickname += b[sum % b.size]
            else
                nickname += c[sum % c.size]
        }

        return nickname
    } catch (e: Exception) {
        return "익명"
    }
}

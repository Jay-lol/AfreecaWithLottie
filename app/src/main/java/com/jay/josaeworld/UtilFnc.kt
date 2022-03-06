package com.jay.josaeworld

import android.util.Log
import com.jay.josaeworld.model.response.AfSearchResponse
import com.jay.josaeworld.model.response.BallonInfo
import com.jay.josaeworld.model.response.BroadInfo

object UtilFnc {
    private val TAG: String = "로그"

    fun goodString(st: String): String {
        return try {
            if (st.length > 3) {
                (
                    goodString(st.slice(0 until st.length - 3)) +
                        "," + st.slice(st.length - 3 until st.length)
                    )
            } else
                st
        } catch (e: Exception) {
            Log.e(TAG, "goodString: $e")
            st
        }
    }

    fun goodBallonData(v: HashMap<*, *>): BallonInfo {
        return BallonInfo(
            if (v["dayballon"] == null) "0" else v["dayballon"] as String,
            if (v["monthballon"] == null) "0" else v["monthballon"] as String,
            if (v["monthmaxview"] == null) "0" else v["monthmaxview"] as String,
            if (v["monthtime"] == null) "0분" else v["monthtime"] as String,
            if (v["monthview"] == null) "0" else v["monthview"] as String,
            if (v["monthpay"] == null) "0" else v["monthpay"] as String,
        )
    }

    fun goodBjData(v: HashMap<*, *>, teamCode: String, bid: String, b: BallonInfo?): BroadInfo {
        return BroadInfo(
            teamCode.toInt(),
            if (v["onOff"] == null) 0 else (v["onOff"] as Long).toInt(),
            bid,
            if (v["title"] == null) "정보 갱신을 해주세요" else v["title"] as String,
            if (v["bjname"] == null) "새로운 멤버" else v["bjname"] as String,
            if (v["imgUrl"] == null) "http://res.afreecatv.com/images/default_logo_300x300.jpg"
            else v["imgUrl"] as String,
            if (v["viewCnt"] == null) "0" else v["viewCnt"] as String,
            if (v["fanCnt"] == null) "0" else v["fanCnt"] as String,
            if (v["okCnt"] == null) "0" else v["okCnt"] as String,
            if (v["incFanCnt"] == null) "0" else v["incFanCnt"] as String,
            v["profilePhoto"]?.let { it as String } ?: "http://res.afreecatv.com/images/default_logo_300x300.jpg",
            b ?: BallonInfo("0", "0", "0", "0분", "0", "0")
        )
    }

    fun sortedBJlist(bjlist: Array<ArrayList<BroadInfo>>): Array<ArrayList<BroadInfo>> {
        return bjlist.sortedWith(
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

    fun makeCuteNickName(st: String): String {
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

        val len = st.length
        var start = 0
        var last = len / 3
        try {
            for (i in 0 until 3) {
                val t: String
                if (i != 2) {
                    t = st.slice(start until last)
                    start = last
                    last += len / 3
                } else
                    t = st.slice(last until len)

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

    fun getBroadInfo(searchResponse: AfSearchResponse, teamCode: Int): BroadInfo {
        val CLIENT_ID: String = searchResponse.station!!.user_id
        Log.d(TAG, "$CLIENT_ID $searchResponse onResponse() called")

        val onOff: Int = searchResponse.broad?.let { 1 } ?: 0
        val bjname: String = searchResponse.station.user_nick
        val title: String = searchResponse.broad?.broad_title?.replace("   ", "")
            ?: "방송 중이지 않습니다"
        val allviewers: String = searchResponse.broad?.let {
            goodString(it.current_sum_viewer.toString())
        } ?: "0" // 전체 시청자
        val imgurl: String = searchResponse.broad?.let {
            "http://liveimg.afreecatv.com/${it.broad_no}_480x270.jpg?dummy="
        } ?: "http://res.afreecatv.com/images/default_logo_300x300.jpg"

        // 0아니면 1 today0, today1
        val activeNo = searchResponse.station.active_no
        val profile = "http:" + searchResponse.profile
        // 팬 숫자
        val fanCnt: String = goodString(searchResponse.station.upd.fan_cnt.toString())
        // 추천 숫자
        val okCnt: String = if (activeNo == 0)
            goodString(searchResponse.station.upd.today0_ok_cnt.toString())
        else
            goodString(searchResponse.station.upd.today1_ok_cnt.toString())

        // 오늘 추가된 즐겨찾기 수
        var incFanCnt: String = if (activeNo == 0)
            searchResponse.station.upd.today0_fav_cnt.toString()
        else
            searchResponse.station.upd.today1_fav_cnt.toString()

        incFanCnt = if (incFanCnt.toInt() < 0)
            "-" + goodString(incFanCnt.slice(1 until incFanCnt.length))
        else goodString(incFanCnt)

        Log.d(
            TAG + "test",
            "GetJson() $bjname $title $allviewers $imgurl $fanCnt $okCnt $incFanCnt"
        )

        return BroadInfo(
            teamCode,
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
}

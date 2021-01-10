package com.jay.josaeworld.model

import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jay.josaeworld.MainApp.Companion.disposable
import com.jay.josaeworld.model.response.BallonInfo
import com.jay.josaeworld.model.response.BroadInfo
import com.jay.josaeworld.model.response.gsonParse.RealBroad
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

object GetData {
    private val TAG: String = "로그"
    private val database = Firebase.database
    private val databaseReference = database.reference
    private val refBJStatus = database.getReference("BjStatus")
    private var bjStatusListener: ValueEventListener? = null

    fun secondSujang(complete: (HashMap<String, String>) -> Unit) {
        val myRef = database.getReference("SecondSujang")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val secondMap = HashMap<String, String>()

                    for (x in snapshot.children) {
                        secondMap[x.key as String] = x.value as String
                    }

                    Log.d(TAG, "GetData ~ secondSujang() called $secondMap")
                    complete(secondMap)
                } catch (e: Exception) {
                    Log.e(TAG, "secondSujang: $e")
                    complete(hashMapOf())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                complete(hashMapOf())
                Log.d(TAG, "GetData ~ onCancelled() called")
            }
        })
    }

    fun ballonData(complete: (HashMap<String, BallonInfo>) -> Unit) {
        val myRef = database.getReference("Ballon")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val ballonMap = HashMap<String, BallonInfo>()

                    for (x in snapshot.children) {
                        val v = x.value as HashMap<*, *>
                        ballonMap[x.key as String] = UtilFnc.goodBallonData(v)
                    }
                    Log.d(TAG, "GetData ~ ballonData() called $ballonMap")
                    complete(ballonMap)
                } catch (e: Exception) {
                    Log.e(TAG, "ballonDataa: $e")
                    complete(hashMapOf())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                complete(hashMapOf())
                Log.d(TAG, "GetData ~ onCancelled() called")
            }
        })
    }

    // 팀명 가져오기
    fun teamData(complete: (List<String>, Long, Boolean, Int, Int) -> Unit) {
        val myRef = database.getReference("LoadingInfo")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val value = snapshot.children
                    val list = arrayListOf<String>()
                    var time = 0L
                    var isForce = false
                    var minversionCode = 123456789
                    var currentversionCode = 123456789
                    for (x in value) {
                        if (x.key == "LastUpDateTime")
                            time = x.value as Long
                        else if (x.key == "isForce")
                            isForce = x.value as Boolean
                        else if (x.key == "minversionCode")
                            minversionCode = (x.value as Long).toInt()
                        else if (x.key == "currentversionCode")
                            currentversionCode = (x.value as Long).toInt()
                        else
                            list.add(x.value as String)
                    }
                    Log.d(TAG, "GetData ~ onDataChange() called $list")
                    complete(list, time, isForce, minversionCode, currentversionCode)
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChange: $e")
                    complete(listOf("시", "조", "새", "!"), 0, false, 0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                complete(listOf("시", "조", "새", "!"), 0, false, 0, 0)
                Log.d(TAG, "GetData ~ onCancelled() called")
            }
        })
    }

    // 데이터를 업데이트하는 함수
    private fun sendUpdateData(slist: List<BroadInfo>, completion: (Boolean) -> Unit) {
        try {
            Log.d(TAG, "GetData ~ sendUpdateData() called $slist")

            val childUpdates = hashMapOf<String, Any>()
            // 업데이트 시간
            childUpdates["/LoadingInfo/LastUpDateTime"] = System.currentTimeMillis()
            // BJ 정보
            slist.forEach { bj ->
                childUpdates["/BjStatus/${bj.teamCode}/${bj.bid}"] = bj.toMap()
            }

            databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener {
                    Log.d(TAG, "GetData ~ addSuccessListener() called")
                    completion(true)
                }
                .addOnFailureListener {
                    Log.e(TAG, "GetData ~ addonFailureListener() called $it")
                    completion(false)
                }

        } catch (e: Exception) {
            completion(false)
            Log.e(TAG, "sendUpdateData: $e")
        }
    }

    /**
    비제이 고유 아이디 가져와서 데이터 요청
     */
    fun liveOnData(bjlist: Array<ArrayList<BroadInfo>>, complete: (Boolean, Int, String) -> Unit) {

        val bidList = arrayListOf<Pair<Int, String>>()

        for (team in bjlist) {
            val index = team[0].teamCode
            for (member in team) bidList.add(Pair(index, member.bid))
        }

        val searchBJ = SearchBJ()
        val singles = (bidList).map { targetBJ ->
            searchBJ.doSearch(targetBJ.first, targetBJ.second)
        }.toList()

        disposable =
            Single.zip(singles) { array ->
                array.map { it as BroadInfo }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bjdata ->
                    val errorCnt: Int = bjdata.count { Bj -> Bj.teamCode == 403 }
                    // 전부 에러일 경우
                    if (errorCnt == bjdata.size) {
                        complete(false, 0, "")
                    }
                    // 일부 비제이 정보만 누락된 경우
                    else {
                        sendUpdateData(bjdata.filter { bj -> bj.teamCode != 403 }, completion = { result ->
                            val name: String = bjdata.find { it.teamCode == 403 }?.bid ?: ""
                            complete(result, errorCnt, name)
                        })
                    }
                }, {
                    Log.e(TAG, "liveOnData: $it")
                    complete(false, 0, "")
                })
//        getLiveOnData(bidlist, bjlist)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ bjdata ->
//                // 함수 전체가 오류라면
//                if (bjdata[bjdata.size - 1].size == 1 && bjdata[bjdata.size - 1][0].teamCode == 404) {
//                    complete(false, 0, "")
//                }
//                // 일부 비제이 정보만 누락된 경우
//                else {
//                    sendUpdateData(bjdata.dropLast(1).toTypedArray(), completion = { result ->
//                        Log.d(TAG, "GetData ~ finishSendBjStatus() called")
//                        val errorCnt: Int
//                        val name: String
//                        if (bjdata[bjdata.size - 1].size == 1) {
//                            errorCnt = bjdata[bjdata.size - 1][0].teamCode
//                            name = bjdata[bjdata.size - 1][0].bid
//                        } else {
//                            errorCnt = 0
//                            name = ""
//                        }
//                        complete(result, errorCnt, name)
//                    })
//                }
//            }, { error ->
//                Log.e(TAG, "liveOnData: $error")
//                complete(false, 0, "")
//            })
    }

    // 실시간 정보 함수
//    private fun getLiveOnData(
//        bid: Array<ArrayList<String>>, bjlist: Array<ArrayList<BroadInfo>>
//    ): Single<Array<ArrayList<BroadInfo>>> {
//        try {
//            val factory = Factory()
//            return Single.fromObservable(
//                Observable
//                    .create {
//                        val bjDataList = Array(bid.size + 1) { arrayListOf<BroadInfo>() }
//
//                        var errorCnt = 0
//
//                        var teamSize = 0
//                        for (team in bjlist) {
//                            teamSize += team.size
//                        }
//
//                        var finishCount = 0
//
//                        for (team in bjlist) {
//                            for (member in team) {
//                                factory.doc(member.bid, member.teamCode, complete = { bj ->
//                                    Log.d(TAG + "test", "$finishCount ${member.bjname} ~ finishLoding() called")
//                                    if (bj == null) {
//                                        if (errorCnt + 1 == teamSize)
//                                            errorCnt = 403
//                                        bjDataList[bid.size] =
//                                            arrayListOf(
//                                                BroadInfo(
//                                                    ++errorCnt, 1, member.bid, "", "",
//                                                    "", "", "", "error", "",
//                                                    BallonInfo("0", "0", "0", "0분", "0", "0")
//                                                )
//                                            )
//                                    } else {
//                                        bjDataList[member.teamCode].add(bj)
//                                    }
//
//                                    if (++finishCount == teamSize) {
//                                        Log.d(TAG + "test", "GetData ~ bjFactory end() called")
//                                        it.onNext(bjDataList)
//                                        it.onComplete()
//                                    }
//                                })
//                            }
//                        }
////                    for ((index, team) in bid.withIndex()) {
////                        for (member in team) {
////                            try {
////
////                                val doc = Jsoup.connect("http://bjapi.afreecatv.com/api/$member/station")
////                                    .timeout(2000)
////                                    .ignoreContentType(true)
////                                    .execute().body()
////
////                                val broadInfo = JSONObject(doc)
////
////                                var onOff1 = 0
////                                var title = "방송 중이지 않습니다"
////                                val bjname = broadInfo.getJSONObject("station")["user_nick"] as String
////                                var imgurl = "http://res.afreecatv.com/images/default_logo_300x300.jpg"
////                                var allviewers = "0"    // 전체 시청자
////                                var fanCnt: String // 팬 숫자
////                                var okCnt: String  // 추천 숫자
////                                var incFanCnt: String  // 오늘 추가된 즐겨찾기 수
////
////                                // 방송중이면 제목,시청자수,썸네일 로딩
////                                if (!broadInfo["broad"].equals(null)) {
////                                    onOff1 = 1
////                                    title =
////                                        (broadInfo.getJSONObject("broad")["broad_title"] as String).replace("   ", "")
////                                    allviewers =
////                                        (broadInfo.getJSONObject("broad")["current_sum_viewer"] as Int).toString()
////                                    allviewers = UtilFnc.goodString(allviewers)
////
////                                    val broadNum = broadInfo.getJSONObject("broad")["broad_no"] as Int
////                                    imgurl = "http://liveimg.afreecatv.com/${broadNum}_480x270.jpg?dummy="
////                                }
////
////                                // 0아니면 1 today0, today1
////                                val activeNo = broadInfo.getJSONObject("station")["active_no"] as Int
////
////                                fanCnt = (broadInfo.getJSONObject("station")
////                                    .getJSONObject("upd")["fan_cnt"] as Int).toString()
////                                fanCnt = UtilFnc.goodString(fanCnt)
////
////                                okCnt = UtilFnc.goodString(
////                                    (broadInfo.getJSONObject("station")
////                                        .getJSONObject("upd")["today${activeNo}_ok_cnt"] as Int)
////                                        .toString()
////                                )
////
////                                incFanCnt = (broadInfo.getJSONObject("station")
////                                    .getJSONObject("upd")["today${activeNo}_fav_cnt"] as Int)
////                                    .toString()
////
////                                incFanCnt = if (incFanCnt.toInt() < 0)
////                                    "-" + UtilFnc.goodString(incFanCnt.slice(1 until incFanCnt.length))
////                                else UtilFnc.goodString(incFanCnt)
////
////                                bjDataList[index].add(
////                                    BroadInfo(
////                                        index,
////                                        onOff1,
////                                        member,
////                                        title,
////                                        bjname,
////                                        imgurl,
////                                        allviewers,
////                                        fanCnt,
////                                        okCnt,
////                                        incFanCnt
////                                    )
////                                )
////
////                                Log.d(
////                                    TAG,
////                                    "GetJson() $bjname $title $allviewers $imgurl $fanCnt $okCnt $incFanCnt"
////                                )
////                            } catch (e: Exception) {
////                                bjDataList[bid.size] =
////                                    arrayListOf(BroadInfo(++errorCnt, 1, member, "", "", "", "", "", "error", ""))
////                                Log.e(TAG, "GetData ~ getLiveOnData() called3 $member $e")
////                            }
////                        }
////                    }
////
////                    it.onNext(bjDataList)
////                    it.onComplete()
//                    }
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "getLiveOnData: $e")
//            return Single.fromObservable(
//                Observable.create {
//                    it.onNext(Array(1) {
//                        arrayListOf(
//                            BroadInfo(
//                                404, 1, "", "", "", "", "",
//                                "", "error", "",
//                                BallonInfo("0", "0", "0", "0분", "0", "0")
//                            )
//                        )
//                    })
//                }
//            )
//        }
//    }
    fun sendReport(reportlist: List<String>, completion: (Boolean) -> Unit) {
        try {
            val childUpdates = hashMapOf<String, Any>()
            // 건의 사항

            childUpdates["/Report/${System.currentTimeMillis()}"] = reportlist[0] + ": " + reportlist[1]

            databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener {
                    Log.d(TAG, "sendReport ~ addSuccessListener() called")
                    completion(true)
                }
                .addOnFailureListener {
                    Log.e(TAG, "sendReport ~ addonFailureListener() called $it")
                    completion(false)
                }

        } catch (e: Exception) {
            completion(false)
            Log.e(TAG, "sendReport: $e")
        }
    }

    fun searchJosae(complete: (RealBroad?) -> Unit) {
        disposable = SearchBJ().searchJosae()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) {
                    for (x in it.REAL_BROAD)
                        Log.d(TAG, "GetData ~ searchJosae() called $x")
                    complete(it)
                } else {
                    complete(null)
                }
            }, {
                Log.e(TAG, "searchJosae: $it")
                complete(null)
            }) ?: disposable
    }

    /**
     * 파이어베이스 서버에 리스너를 설정해서 최신 정보를 계속 업데이트
     */
    fun setBjDataListener(teamSize: Int,
        callback: (Boolean, Array<java.util.ArrayList<BroadInfo>>?) -> Unit
    ) {
        // 이미 등록 되어있으면 처리안되게 설정
        if (bjStatusListener!=null) return
        
        var recentBJList: Array<java.util.ArrayList<BroadInfo>>
        bjStatusListener = refBJStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    recentBJList = Array(teamSize) { arrayListOf() }
                    ballonData { map ->
                        // 팀 숫자만큼 배열 할당
                        try {
                            for ((index, team) in snapshot.children.withIndex()) {
                                val teamCode = team.key as String
                                for (member in team.value as HashMap<*, *>) {
                                    val v = member.value as HashMap<*, *>
                                    val b = if (map.containsKey(member.key as String)) map[member.key as String]
                                    else null
                                    recentBJList[index].add(UtilFnc.goodBjData(v, teamCode, member.key as String, b))
                                }
                                if (index == teamSize - 2) recentBJList = UtilFnc.sortedBJlist(recentBJList)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChange: $e")
                        }
                        Log.d(TAG, "datalisnter ~ onDataChange() called")

                        // 업데이트
                        callback(true, recentBJList)
                    }
                } catch (e: Exception) {
                    callback(false, null)
                    Log.e(TAG, "onDataChange: $e")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: $error")
                callback(false, null)
            }
        })
    }

    fun removeBjDataListener(){
        bjStatusListener?:return
        refBJStatus.removeEventListener(bjStatusListener!!)
        bjStatusListener = null
    }
}
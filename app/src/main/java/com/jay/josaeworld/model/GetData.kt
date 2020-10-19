package com.jay.josaeworld.model

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jay.josaeworld.model.Static.Companion.allMemberName
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GetData {
    private val TAG: String = "로그"

    // 팀명 가져오기
    fun teamData(callback: DataCallback) {
        val database = Firebase.database
        val myRef = database.getReference("Team")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.children
                val list = arrayListOf<String>()
                for (x in value)
                    list.add(x.value as String)

                Log.d(TAG, "GetData ~ onDataChange() called $list")
                callback.finishLoading(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "GetData ~ onCancelled() called")
            }
        })
    }

    // 비제이 고유 아이디 가져와서 크롤링 함수 실행
    fun liveOnData(callback: DataCallback) {
        val database = Firebase.database
        val myRef = database.getReference("Member").parent

        myRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.children
                val bid = Array(4) { arrayListOf<String>() }

                for ((s, x) in value.withIndex()) {
                    if (s == 1)
                        break
                    for ((index, y) in x.children.withIndex()) {
                        y.key?.let {
                            Log.d(TAG + "x", "GetData ~ onDataChange() called ${y.value}")
                            for (k in y.value as HashMap<*, *>) {
                                bid[index].add(k.key as String)
                                allMemberName[k.key as String] = k.value as String
                            }
                        }
                    }
                }

                getLiveOnData(bid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { bjdata ->
                        callback.finishBjDataLoading(bjdata)
                        Log.d(TAG, "GetData ~ onDataChange() called $bjdata")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "GetData ~ onCancelled() called")
            }
        })

    }

    // 크롤링 함수
    fun getLiveOnData(bid: Array<ArrayList<String>>): Single<Array<ArrayList<BroadInfo>>> {
        return Single.fromObservable(
            Observable.create {
                val bjDataList = Array(bid.size + 1) { arrayListOf<BroadInfo>() }

                for ((index, team) in bid.withIndex()) {
                    for (member in team) {
                        try {
                            val doc: Document =
                                Jsoup.connect("http://play.afreecatv.com/$member")
                                    .get()

                            var title = doc.select("meta[property=og:title]")
                                .first().attr("content") as String
                            val description = doc.select("meta[property=og:description]")
                                .first().attr("content") as String
                            val image_url = doc.select("meta[property=og:image]")
                                .first().attr("content") as String
                            val update_time = doc.select("meta[property=og:updated_time]")
                                .first().attr("content") as String

                            val isOnOff = if (update_time == "") 0 else 1

                            var bjname = description.slice(description.indexOf('|') + 1 until description.length)

                            if (bjname.contains("방송중이지 않습니다"))
                                allMemberName[member]?.let {
                                    bjname = allMemberName[member]!!
                                }
                            else
                                title = title.replace("   ", " ")

                            bjDataList[index].add(
                                BroadInfo(
                                    isOnOff,
                                    member,
                                    title,
                                    bjname,
                                    "http:$image_url"
                                )
                            )

                            Log.d(TAG + "d", "GetData ~ getLiveOnData() called $isOnOff")
                        } catch (e: Exception) {
                            bjDataList[bid.size].add(BroadInfo(1, "", "", "", ""))
                            Log.e(TAG, "GetData ~ getLiveOnData() called")
                            break
                        }
                    }
                }
                it.onNext(bjDataList)
                it.onComplete()
            }
        )
    }

//    // 팀클릭시 그팀정보만 다시 업데이트하는함수
//    fun broadList(rid: Int, callback: DataCallback) {
//
//        val database = Firebase.database
//        val myRef = database.getReference("Member/$rid")
//        val bid = arrayListOf<String>()
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = snapshot.children
//                for (x in value)
//                    bid.add(x.key as String)
//
//                requestNotice(bid, rid)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe { list ->
//                        callback.finishBroadDataLoading(list)
//                    }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d(TAG, "GetData ~ onCancelled() called")
//            }
//        })
//    }

//    fun requestNotice(bid: ArrayList<String>, rid: Int): Single<List<BroadInfo>> {
//        return Single.fromObservable(
//            Observable.create {
//                val bjStatusDataList = arrayListOf<BroadInfo>()
//
//                for ((index, x) in bid.withIndex()) {
//                    val doc: Document =
//                        Jsoup.connect("http://play.afreecatv.com/$x")
//                            .get()
//
//                    val ogTags = doc.select("meta[property^=og:]")
//
//                    val meta_title = ogTags.select("meta[property=og:title]").first()
//                    val meta_description = ogTags.select("meta[property=og:description]").first()
//                    val meta_image_url = ogTags.select("meta[property=og:image]").first()
//                    val meta_updated_time = ogTags.select("meta[property=og:updated_time]").first()
//
//                    val title = meta_title.attr("content") as String
//                    val description = meta_description.attr("content") as String
//                    val image_url = meta_image_url.attr("content") as String
//
//                    val updated_time = meta_updated_time.attr("content") as String
//                    val onOff = if (updated_time == "") 0 else 1
//                    var bjname = description.slice(description.indexOf('|') + 1 until description.length)
//                    if (bjname.contains("방송중이지 않습니다")) {
//                        allMemberName[x]?.let {
//                            bjname = allMemberName[x]!!
//                        }
//                    }
//                    bjStatusDataList.add(
//                        BroadInfo(
//                            onOff,
//                            x,
//                            title,
//                            bjname,
//                            "http:$image_url"
//                        )
//                    )
//                }
//
//                it.onNext(bjStatusDataList.sortedByDescending { c -> c.onOff })
//                it.onComplete()
//            }
//        )
//    }
}
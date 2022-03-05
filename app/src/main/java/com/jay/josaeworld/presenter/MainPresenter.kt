package com.jay.josaeworld.presenter

import android.content.Context
import android.os.Handler
import android.util.Log
import com.jay.josaeworld.contract.MainContract
import com.jay.josaeworld.model.GetData
import com.jay.josaeworld.model.response.BroadInfo
import java.util.*

class MainPresenter : MainContract.Presenter {

    private val TAG: String = "로그 ${this.javaClass.simpleName}"
    private var searchView: MainContract.View? = null

    override fun getRecentBJData(bjLists: Array<ArrayList<BroadInfo>>, bjDataList: Array<ArrayList<BroadInfo>>?) {
        GetData.liveOnData(bjLists) { clear, errorCnt, name ->
            searchView?.stopLoadingAnimation()
            searchView?.makeRefreshstate(false)
            if (!clear) searchView?.showError(4)
            else {
                if (errorCnt != 0) {
                    var n = 0
                    bjLists.forEach { n += it.size }
                    var bjname = "unknown"
                    try {
                        var find = false
                        for (i in bjDataList!!) {
                            if (find) break
                            for (member in i)
                                if (member.bid == name) {
                                    find = true
                                    bjname = member.bjname
                                    break
                                }
                        }
                    } catch (e: Exception) {
                        searchView?.showError(1)
                        Log.e(TAG, "finishSendBjStatus: a")
                    }

                    if (errorCnt == 1)
                        searchView?.showToast("${n}명 중 아프리카 에러로\n  $bjname 정보 누락")
                    else
                        searchView?.showToast("      ${n}명 중 응답 에러로\n $bjname 외 ${errorCnt - 1}명의 정보 누락")
                }
            }

            searchView?.changeIsLoadingState(false)
            // 모든 상황 처리 후 로딩 끝
        }
    }

    // newBJDataList : Array<ArrayList<BroadInfo>>?
    override fun createBJDataListener(teamSize: Int) {
        GetData.setBjDataListener(teamSize) { success, newDataList ->
            if (success) {
                Log.d(TAG, "MainPresenter ~ bjDataListener() 갱신 성공")
                searchView?.changeMainBJData(newDataList)
            } else {
                Log.d(TAG, "MainPresenter ~ bjDataListener() 갱신 실패")
                searchView?.stopLoadingAnimation()
                searchView?.showError(2)
            }
        }
    }

    override fun removeBJDataListener() {
        GetData.removeBjDataListener()
    }

    override fun getSecondSujang() {
        GetData.secondSujang { newList ->
            searchView?.initSecondSujang(newList)
        }
    }

    override fun getTeamData(context: Context) {
        GetData.teamData { newList, time, isForce, minversionCode, currentversionCode ->
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            val vc = pi.versionCode
            if (isForce && vc < minversionCode) {
                Handler().postDelayed({
                    searchView?.showCustomDialog(3)
                }, 2000)
            } else {
                if (vc < currentversionCode) {
                    Handler().postDelayed({
                        searchView?.showCustomDialog(2)
                    }, 2000)
                }
            }
            searchView?.initTeamData(newList, time)
        }
    }

    override fun takeView(view: MainContract.View) {
        searchView = view
    }

    override fun dropView() {
        searchView = null
    }
}

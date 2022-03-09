package com.jay.josaeworld.presenter

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.josaeworld.contract.SplashContract
import com.jay.josaeworld.domain.GetInitTeamDataUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SplashPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private var searchView: SplashContract.View?,
    private val getInitTeamDataUseCase: GetInitTeamDataUseCase
) : SplashContract.Presenter {
    override fun getInitTeamData() {
        getInitTeamDataUseCase(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val value = snapshot.children
                        val list = arrayListOf<String>()
                        var time = 0L
                        var minversionCode = 123456789
                        var currentversionCode = 123456789
                        for (x in value) {
                            when (x.key) {
                                "LastUpDateTime" -> time = x.value as Long
                                "minversionCode" -> minversionCode = (x.value as Long).toInt()
                                "currentversionCode" ->
                                    currentversionCode =
                                        (x.value as Long).toInt()
                                else -> list.add(x.value as String)
                            }
                        }
                        Log.d(TAG, "SplashPresenter ~ onDataChange() called $list")
                        initTeamData(list, time, minversionCode, currentversionCode)
                    } catch (e: Exception) {
                        searchView?.showToast("$e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    searchView?.showToast("$error")
                }
            }
        )
    }

    private fun initTeamData(
        newList: List<String>,
        time: Long,
        minversionCode: Int,
        currentversionCode: Int
    ) {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        val vc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi.longVersionCode
        } else {
            pi.versionCode.toLong()
        }

        when {
            vc < minversionCode -> {
                searchView?.startMainActivity(newList, time, 3)
            }
            vc < currentversionCode -> {
                searchView?.startMainActivity(newList, time, 2)
            }
            else -> {
                searchView?.startMainActivity(newList, time)
            }
        }
    }

    override fun dropView() {
        searchView = null
    }

    companion object {
        private val TAG: String = "로그 SplashPresenter"
    }
}

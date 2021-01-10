package com.jay.josaeworld.extension

import android.app.Activity
import android.view.Gravity
import android.widget.Toast

internal fun Activity.showToast(code: Int) {
    var msg: String = "Error"
    when (code) {
        1 -> {
            msg = "        아프리카 서버 오류\n3초 후 밑으로 내려서 다시 로딩해 주세요"
        }
        2 -> {
            msg = "인터넷 연결을 확인해주세요"
        }
        3 -> {
            msg = "먼저 정보를 업데이트 해주세요"
        }
        4 -> {
            msg = "서버 연결 시간 초과로\n   일부 데이터 누락"
        }
        5 -> {
            msg = "최신정보를 받아오는 중\n잠시 후 다시 로딩해주세요"
        }
    }
    val toast = Toast.makeText(
        baseContext, msg, Toast.LENGTH_SHORT
    )
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

internal fun Activity.toast(msg : String){
    val toast = Toast.makeText(
        baseContext, msg, Toast.LENGTH_SHORT
    )
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}
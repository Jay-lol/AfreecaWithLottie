package com.jay.josaeworld.extension

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.Toast

internal fun Activity.showErrorToast(code: Int) {
    var msg = "Error"
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
    toast(msg)
}

internal fun Activity.toast(msg: String, isCenter: Boolean = false) {
    val toast = Toast.makeText(
        applicationContext, msg, Toast.LENGTH_SHORT
    )
    if (isCenter) {
        toast.setGravity(Gravity.CENTER, 0, 0)
    }
    toast.show()
}

internal fun Number.pxToDp(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).toInt()
package com.jay.josaeworld.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import com.jay.josaeworld.base.BaseViewBindingActivity
import com.jay.josaeworld.contract.SplashContract
import com.jay.josaeworld.databinding.ActivitySplashBinding
import com.jay.josaeworld.databinding.CustomDialogBinding
import com.jay.josaeworld.extension.showErrorToast
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.presenter.SplashPresenter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity :
    BaseViewBindingActivity<ActivitySplashBinding, SplashPresenter>({
        ActivitySplashBinding.inflate(
            it
        )
    }),
    SplashContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.getInitTeamData()
    }

    override fun startMainActivity(newList: List<String>, time: Long, code: Int) {
        if (code == 3) {
            val dlg = Dialog(this)
            val dlgBinding = CustomDialogBinding.inflate(layoutInflater)
            // 커스텀 다이얼로그의 레이아웃을 설정한다.
            dlg.setContentView(dlgBinding.root)

            dlgBinding.question.text = "업데이트를 필수로 진행해야 합니다!"
            dlgBinding.warning.text = ""
            dlgBinding.closeOkButton.text = "업데이트"
            dlgBinding.closeNotOk.text = ""
            dlg.show()
            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dlg.setCancelable(false)
            dlg.setCanceledOnTouchOutside(false)
            dlgBinding.closeOkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
                try {
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    showToast("플레이스토어 연결 불가")
                }
            }
        } else {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra(KEY_NEW_LIST, newList as ArrayList)
                    putExtra(KEY_LAST_UPDATE_TIME, time)
                    putExtra(KEY_UPDATE_CODE, code)
                }
            )
            finish()
        }
    }

    override fun showError(code: Int) {
        showErrorToast(code)
    }

    override fun showToast(msg: String, isCenter: Boolean) {
        toast(msg, isCenter)
    }

    override fun onBackPressed() {}

    companion object {
        const val KEY_NEW_LIST = "key_new_list"
        const val KEY_LAST_UPDATE_TIME = "key_last_update_time"
        const val KEY_UPDATE_CODE = "key_update_code"
    }
}

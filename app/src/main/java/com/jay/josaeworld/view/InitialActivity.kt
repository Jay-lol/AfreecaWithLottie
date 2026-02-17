package com.jay.josaeworld.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.compose.*
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.CustomDialogBinding
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.ui.theme.MapleStory
import com.jay.josaeworld.viewmodel.InitialSideEffect
import com.jay.josaeworld.viewmodel.InitialViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InitialActivity : ComponentActivity() {

    private val viewModel: InitialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 시스템 스플래시 화면을 데이터 로딩이 완료될 때까지 유지
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }

        setContent {
            JosaeWorldTheme {
                InitialScreenContent()
            }
        }

        observeViewModel()
        viewModel.getInitTeamData()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sideEffect.collect { effect ->
                    when (effect) {
                        is InitialSideEffect.NavigateToMain -> {
                            handleNavigation(effect.newList, effect.time, effect.code)
                        }
                        is InitialSideEffect.ShowToast -> {
                            toast(effect.message)
                        }
                    }
                }
            }
        }
    }

    private fun handleNavigation(newList: List<String>, time: Long, code: Int) {
        if (code == 3) {
            showUpdateDialog()
        } else {
            val intent = Intent(this, MainActivity::class.java).apply {
                putStringArrayListExtra(KEY_NEW_LIST, ArrayList(newList))
                putExtra(KEY_LAST_UPDATE_TIME, time)
                putExtra(KEY_UPDATE_CODE, code)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showUpdateDialog() {
        val dlg = Dialog(this)
        val dlgBinding = CustomDialogBinding.inflate(layoutInflater)
        dlgBinding.run {
            question.text = "업데이트를 필수로 진행해야 합니다!"
            warning.text = ""
            closeOkButton.text = "업데이트"
            closeNotOk.text = ""
            closeOkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
                }
                try {
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    toast("플레이스토어 연결 불가")
                }
            }
        }

        dlg.setContentView(dlgBinding.root)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.setCancelable(false)
        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
    }

    companion object {
        const val KEY_NEW_LIST = "key_new_list"
        const val KEY_LAST_UPDATE_TIME = "key_last_update_time"
        const val KEY_UPDATE_CODE = "key_update_code"
    }
}

@Composable
private fun InitialScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF282828))
    ) {
        // 배경 애니메이션
        val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("434-gradient-animated-background.json"))
        LottieAnimation(
            composition = bgComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 상단 히어로 애니메이션
            val heroComposition by rememberLottieComposition(LottieCompositionSpec.Asset("34763-hero-on-its-way.json"))
            LottieAnimation(
                composition = heroComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // 중앙 로고
            Image(
                painter = painterResource(id = R.drawable.splash_logo3),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            )
        }

        // 하단 텍스트
        Text(
            text = "만든이 : °へ°",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 14.sp,
            fontFamily = MapleStory,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialActivityPreview() {
    JosaeWorldTheme {
        InitialScreenContent()
    }
}

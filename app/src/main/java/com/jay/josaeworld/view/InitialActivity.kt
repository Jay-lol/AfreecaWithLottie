package com.jay.josaeworld.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jay.josaeworld.R
import com.jay.josaeworld.extension.toast
import com.jay.josaeworld.ui.component.JosaeCustomDialog
import com.jay.josaeworld.ui.theme.JosaeWorldTheme
import com.jay.josaeworld.ui.theme.MapleStory
import com.jay.josaeworld.viewmodel.InitialSideEffect
import com.jay.josaeworld.viewmodel.InitialViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InitialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            JosaeWorldTheme {
                InitialScreenContent(
                    onNavigate = { newList, time, code ->
                        val intent =
                            Intent(this, MainActivity::class.java).apply {
                                putStringArrayListExtra(KEY_NEW_LIST, ArrayList(newList))
                                putExtra(KEY_LAST_UPDATE_TIME, time)
                                putExtra(KEY_UPDATE_CODE, code)
                            }
                        startActivity(intent)
                        finish()
                    },
                    onToast = { message ->
                        toast(message)
                    },
                    onForceUpdate = {
                        val intent =
                            Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/apps/details?id=com.jay.josaeworld")
                            }
                        try {
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            toast("플레이스토어 연결 불가")
                        }
                    },
                )
            }
        }
    }

    companion object {
        const val KEY_NEW_LIST = "key_new_list"
        const val KEY_LAST_UPDATE_TIME = "key_last_update_time"
        const val KEY_UPDATE_CODE = "key_update_code"
    }
}

@Composable
private fun InitialScreenContent(
    viewModel: InitialViewModel = hiltViewModel(),
    onNavigate: (List<String>, Long, Int) -> Unit,
    onToast: (String) -> Unit,
    onForceUpdate: () -> Unit,
) {
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is InitialSideEffect.NavigateToMain -> {
                    if (effect.code == 3) {
                        showUpdateDialog = true
                    } else {
                        onNavigate(effect.newList, effect.time, effect.code)
                    }
                }

                is InitialSideEffect.ShowToast -> {
                    onToast(effect.message)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getInitTeamData()
    }

    if (showUpdateDialog) {
        JosaeCustomDialog(
            question = "업데이트를 필수로 진행해야 합니다!",
            okText = "업데이트",
            cancelText = "",
            properties =
                androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            onConfirm = onForceUpdate,
            onDismiss = {}, // Prevent dismissal
        )
    }

    InitialScreenContentInner()
}

@Composable
private fun InitialScreenContentInner() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics
                        .Color(0xFF282828),
                ),
    ) {
        // 배경 애니메이션
        val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("intro_gradient_animated_background.json"))
        LottieAnimation(
            composition = bgComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 상단 히어로 애니메이션
            val heroComposition by rememberLottieComposition(LottieCompositionSpec.Asset("hero_on_its_way.json"))
            LottieAnimation(
                composition = heroComposition,
                iterations = LottieConstants.IterateForever,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            )

            // 중앙 로고
            Image(
                painter = painterResource(id = R.drawable.splash_logo3),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(210.dp),
            )
        }

        // 하단 텍스트
        Text(
            text = "만든이 : °へ°",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 14.sp,
            fontFamily = MapleStory,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialActivityPreview() {
    JosaeWorldTheme {
        InitialScreenContentInner()
    }
}

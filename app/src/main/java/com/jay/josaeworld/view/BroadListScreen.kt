package com.jay.josaeworld.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.ads.AdRequest
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.ui.component.AdBanner
import com.jay.josaeworld.ui.component.StreamerCard
import com.jay.josaeworld.viewmodel.BroadListUiState

@Composable
fun BroadListScreen(
    state: BroadListUiState,
    adRequest: AdRequest,
    onStreamerClick: (BroadInfo) -> Unit,
    onMoreInfoClick: (BroadInfo) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 Lottie
        val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("10201-background-full-screen-night.json"))
        LottieAnimation(
            composition = bgComposition,
            iterations = 1,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Scaffold(
            topBar = {
                SearchTopBar(title = state.teamName)
            },
            containerColor = Color.Transparent,
            bottomBar = {
                AdBanner(
                    adRequest = adRequest,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(state.items, key = { it.streamerId }) { streamerInfo ->
                    StreamerCard(
                        streamerInfo = streamerInfo,
                        onClick = { onStreamerClick(streamerInfo) },
                        onMoreInfoClick = { onMoreInfoClick(streamerInfo) },
                        isUnderBoss = streamerInfo.streamerId == state.underBoss,
                        useProfileImage = true,
                        useThumbnailPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

package com.jay.josaeworld.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

private val allAssets = listOf(
    "rest_sloth.json",
    "hero_on_its_way.json",
    "more_info.json",
    "mr_cookie_drink.json",
    "rest_green_sloth.json",
    "bg_night.json",
    "streamer_highlight.json",
    "click_coach_mark.json",
    "dance_monkey.json",
    "intro_gradient_animated_background.json",
)

@Composable
private fun LottieAssetItem(fileName: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(fileName))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = fileName,
            fontSize = 9.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(2.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, widthDp = 400, heightDp = 800)
@Composable
fun LottieAssetPreview() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Color(0xFF111111))
            .padding(8.dp)
    ) {
        items(allAssets) { fileName ->
            LottieAssetItem(fileName)
        }
    }
}

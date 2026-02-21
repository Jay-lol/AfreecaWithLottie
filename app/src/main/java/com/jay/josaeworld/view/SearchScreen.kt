package com.jay.josaeworld.view

import android.os.Build
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.android.gms.ads.AdRequest
import com.jay.josaeworld.R
import com.jay.josaeworld.domain.model.response.SearchStreamerInfo
import com.jay.josaeworld.ui.component.AdBanner
import com.jay.josaeworld.ui.component.LoadingOverlay
import com.jay.josaeworld.ui.theme.MapleStory
import com.jay.josaeworld.ui.theme.tightTextStyle
import com.jay.josaeworld.viewmodel.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    adRequest: AdRequest,
    onItemClick: (SearchStreamerInfo) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 Lottie
        val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("bg_night.json"))
        LottieAnimation(
            composition = bgComposition,
            iterations = 1,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Scaffold(
            topBar = {
                SearchTopBar(
                    title = "시조새 검색 결과"
                )
            },
            containerColor = Color.Transparent,
            bottomBar = {
                AdBanner(
                    adRequest = adRequest,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.items != null) {
                    if (state.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "검색 결과가 없습니다",
                                color = Color.White,
                                style = tightTextStyle
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.items) { item ->
                                SearchStreamerItem(
                                    item = item,
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LoadingOverlay()
                }
            }
        }
    }
}

@Composable
fun SearchTopBar(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            style = tightTextStyle
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchStreamerItem(
    item: SearchStreamerInfo,
    onClick: () -> Unit
) {
    val random = remember { java.util.Random() }
    val broadTitle = remember(item.broad_title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(item.broad_title, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(item.broad_title).toString()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(16f / 9f)
        ) {
            GlideImage(
                model = item.broad_img + "?dummy=${random.nextInt(123456789)}",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = broadTitle,
                color = Color(0xF2F0F0F0),
                fontSize = 13.sp,
                lineHeight = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                style = tightTextStyle
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.user_nick,
                color = Color(0xFF8587FE),
                fontSize = 11.sp,
                lineHeight = 11.sp,
                style = tightTextStyle
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_people_alt_24),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = item.total_view_cnt,
                    color = Color(0xFFA6A6A6),
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    modifier = Modifier.padding(start = 4.dp),
                    style = tightTextStyle
                )
            }
        }
    }
}

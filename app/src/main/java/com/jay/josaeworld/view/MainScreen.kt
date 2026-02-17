package com.jay.josaeworld.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.ButtonFloatingMenuBinding
import com.jay.josaeworld.domain.goodString
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.ui.component.MainInfoSection
import com.jay.josaeworld.ui.theme.MapleStory
import com.jay.josaeworld.viewmodel.MainUiState

import androidx.compose.ui.tooling.preview.Preview
import com.jay.josaeworld.domain.model.response.BallonInfo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    state: MainUiState,
    teamNames: List<String>,
    onRefresh: () -> Unit,
    onBossClick: (BroadInfo) -> Unit,
    onBossMoreInfoClick: (BroadInfo) -> Unit,
    onTeamClick: (String, ArrayList<BroadInfo>, String) -> Unit,
    onSearchClick: () -> Unit,
    onFabChatClick: () -> Unit,
    onFabRoungeClick: () -> Unit,
    onFabReportClick: () -> Unit,
    adRequest: AdRequest,
    isCoachMarkVisible: Boolean
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pullRefresh(pullRefreshState)
    ) {
        // 배경 Lottie
        val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("10201-background-full-screen-night.json"))
        LottieAnimation(
            composition = bgComposition,
            iterations = 1,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 전체 수직 구조
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. 상단 컨텐츠 영역 (스크롤 가능)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                MainInfoSection(
                    allViewers = state.allViewers,
                    allBallons = state.allBallons
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        state.mainBJDataList?.lastOrNull()?.firstOrNull()?.let { bossInfo ->
                            BossSection(
                                bossInfo = bossInfo,
                                onClick = { onBossClick(bossInfo) },
                                onMoreInfoClick = { onBossMoreInfoClick(bossInfo) },
                                isCoachMarkVisible = isCoachMarkVisible
                            )
                        }
                    }

                    state.mainBJDataList?.let { allData ->
                        val teamsData = allData.dropLast(1)
                        val filteredTeams = teamsData.mapIndexedNotNull { index, teamList ->
                            val teamName = teamNames.getOrNull(index) ?: ""
                            if (teamName != "X" && teamList.isNotEmpty()) {
                                Triple(teamName, teamList, index)
                            } else null
                        }

                        val rows = filteredTeams.chunked(2)
                        items(rows.size) { rowIndex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rows[rowIndex].forEach { (teamName, teamList, index) ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        TeamItem(
                                            teamName = teamName,
                                            teamList = teamList,
                                            onClick = {
                                                onTeamClick(
                                                    teamName,
                                                    teamList,
                                                    state.underBossList[teamName]
                                                        ?: (index + 1).toString()
                                                )
                                            }
                                        )
                                    }
                                }
                                if (rows[rowIndex].size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 2. 하단 고정 영역 (검색 바 + 광고)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar(
                    modifier = Modifier.padding(bottom = 4.dp),
                    onClick = onSearchClick
                )
                AdBanner(adRequest = adRequest)
            }
        }

        // 3. 플로팅 메뉴 (광고 바로 위 우측에 고정)
        AndroidView(
            factory = { context ->
                val binding =
                    ButtonFloatingMenuBinding.inflate(android.view.LayoutInflater.from(context))
                binding.fabChat.setOnClickListener { onFabChatClick() }
                binding.fabRounge.setOnClickListener { onFabRoungeClick() }
                binding.fabReport.setOnClickListener { onFabReportClick() }
                binding.root
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 50.dp) // AdBanner 높이(50dp) 만큼 띄움
        )

        // 로딩 및 새로고침 UI
        AnimatedVisibility(
            visible = state.isLoading || state.isCrawlingForFirebase,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingOverlay()
        }
        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.DarkGray,
            contentColor = Color.White
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BossSection(
    bossInfo: BroadInfo,
    onClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    isCoachMarkVisible: Boolean
) {
    val random = remember { java.util.Random() }
    val isOn = bossInfo.onOff == 1
    val viewCountInt = bossInfo.viewCnt.filter { it.isDigit() }.toIntOrNull() ?: 0
    val isHighViewCount = isOn && viewCountInt >= 10000

    val textStyleNoPadding = LocalTextStyle.current.copy(
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isHighViewCount) {
            val highlightComposition by rememberLottieComposition(LottieCompositionSpec.Asset("lf30_editor_cecsqjtv.json"))
            LottieAnimation(
                composition = highlightComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .scale(2.5f),
                contentScale = ContentScale.Inside
            )
        }

        Card(
            modifier = Modifier
                .width(295.dp)
                .wrapContentHeight()
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 썸네일 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    if (isOn) {
                        GlideImage(
                            model = bossInfo.imgurl + "${random.nextInt(123456789)}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val restComposition by rememberLottieComposition(
                            LottieCompositionSpec.Asset(
                                "8266-rest-sloth.json"
                            )
                        )
                        LottieAnimation(
                            composition = restComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    val infoComposition by rememberLottieComposition(LottieCompositionSpec.Asset("12246-info.json"))
                    LottieAnimation(
                        composition = infoComposition,
                        iterations = 1,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(40.dp, 45.dp)
                            .clickable { onMoreInfoClick() }
                    )

                    if (isCoachMarkVisible) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 35.dp, end = 15.dp)
                        ) {
                            val clickComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset(
                                    "clickmark.json"
                                )
                            )
                            LottieAnimation(
                                composition = clickComposition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(100.dp)
                            )
                            Text(
                                text = "느낌표를\n클릭해보세요",
                                color = Color(0xC9FFFFFF),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    thickness = 2.dp,
                    color = Color(0xFFA6A6A6)
                )

                // 정보 영역
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bossInfo.title,
                        color = Color(0xE6FFFFFF),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontFamily = MapleStory,
                        style = textStyleNoPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 9.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_star_border_24),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = bossInfo.fanCnt,
                                color = Color.White,
                                fontSize = 9.sp,
                                style = textStyleNoPadding,
                                modifier = Modifier.padding(start = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_outline_thumb_up_alt_24),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = bossInfo.okCnt,
                                color = Color.White,
                                fontSize = 9.sp,
                                style = textStyleNoPadding,
                                modifier = Modifier.padding(start = 1.dp)
                            )
                        }
                        Text(
                            text = bossInfo.bjname,
                            color = Color(0xFF8587FE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MapleStory,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 80.dp),
                            style = textStyleNoPadding
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_people_alt_24),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = bossInfo.viewCnt,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                style = textStyleNoPadding,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }

                    // 풍력 정보 (촘촘하게 배치)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 2.dp),
                        verticalArrangement = Arrangement.spacedBy((-4).dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "오늘의 즐겨찾기 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                style = textStyleNoPadding
                            )
                            val fanCntVal = bossInfo.incFanCnt.filter { it.isDigit() || it == '-' }
                                .toIntOrNull() ?: 0
                            Text(
                                text = bossInfo.incFanCnt,
                                color = if (fanCntVal < 0) Color(0xFFFF4A4A) else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                style = textStyleNoPadding
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "일일 풍력 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                style = textStyleNoPadding
                            )
                            Text(
                                text = bossInfo.balloninfo?.dayballon ?: "-",
                                color = Color(0xFF46E9FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                style = textStyleNoPadding
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "월간 풍력 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                style = textStyleNoPadding
                            )
                            Text(
                                text = bossInfo.balloninfo?.monthballon ?: "-",
                                color = Color(0xFF46E9FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                style = textStyleNoPadding
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color(0xFF3A3A3A), shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00CCFF))
    ) {
        Text(
            text = stringResource(id = R.string.search_as_josae),
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun TeamItem(teamName: String, teamList: List<BroadInfo>, onClick: () -> Unit) {
    val isOn = teamList.any { it.onOff == 1 }
    val totalViewers = teamList.filter { it.onOff == 1 }
        .sumOf { it.viewCnt.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF444444))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(65.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = teamName,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = MapleStory
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_people_alt_24),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = totalViewers.toString().goodString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            VerticalDivider(color = Color(0xFFA6A6A6), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val lottieFile =
                    if (isOn) "35627-weather-day-clear-sky.json" else "8438-mr-cookie-drink.json"
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieFile))
                LottieAnimation(
                    composition = composition,
                    iterations = if (isOn) LottieConstants.IterateForever else 1,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun AdBanner(adRequest: AdRequest) {
    val adUnitId = stringResource(id = R.string.bannerId)
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(adRequest)
            }
        },
        update = { it.loadAd(adRequest) }
    )
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x4D000000))
            .clickable(enabled = false) {}, contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("8438-mr-cookie-drink2.json"))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun BossSectionPreview() {
    val mockBoss = BroadInfo(
        teamCode = 0,
        onOff = 1,
        bid = "test",
        title = "JosaeWorld에 오신 것을 환영합니다!",
        bjname = "시조새",
        viewCnt = "12,345",
        fanCnt = "50,000",
        okCnt = "1,234",
        incFanCnt = "123",
        balloninfo = BallonInfo(
            dayballon = "5,000",
            monthballon = "150,000"
        )
    )
    BossSection(
        bossInfo = mockBoss,
        onClick = {},
        onMoreInfoClick = {},
        isCoachMarkVisible = true
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val mockBoss = BroadInfo(
        teamCode = 0,
        onOff = 1,
        bid = "test",
        title = "방송 중입니다!",
        bjname = "시조새",
        viewCnt = "15,000",
        fanCnt = "100,000",
        okCnt = "1,000",
        incFanCnt = "150",
        balloninfo = BallonInfo(
            dayballon = "10,000",
            monthballon = "300,000"
        )
    )

    val state = MainUiState(
        mainBJDataList = arrayOf(arrayListOf(mockBoss)),
        allViewers = 25000,
        allBallons = 500000,
        isCoachMarkVisible = true
    )

    MainScreen(
        state = state,
        teamNames = listOf("X"),
        onRefresh = {},
        onBossClick = {},
        onBossMoreInfoClick = {},
        onTeamClick = { _, _, _ -> },
        onSearchClick = {},
        onFabChatClick = {},
        onFabRoungeClick = {},
        onFabReportClick = {},
        adRequest = AdRequest.Builder().build(),
        isCoachMarkVisible = true
    )
}


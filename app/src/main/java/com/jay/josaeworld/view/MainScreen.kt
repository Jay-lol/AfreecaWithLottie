package com.jay.josaeworld.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.airbnb.lottie.compose.*
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
                                            onClick = { onTeamClick(teamName, teamList, state.underBossList[teamName] ?: (index + 1).toString()) }
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
                val binding = ButtonFloatingMenuBinding.inflate(android.view.LayoutInflater.from(context))
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
        AnimatedVisibility(visible = state.isLoading || state.isCrawlingForFirebase, enter = fadeIn(), exit = fadeOut()) {
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
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (bossInfo.onOff == 1 && (bossInfo.viewCnt.filter { it.isDigit() }.toIntOrNull() ?: 0) >= 10000) {
            val highlightComposition by rememberLottieComposition(LottieCompositionSpec.Asset("lf30_editor_cecsqjtv.json"))
            LottieAnimation(
                composition = highlightComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(380.dp, 260.dp).scale(2.2f)
            )
        }

        Card(
            modifier = Modifier.width(275.dp).wrapContentHeight().clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
        ) {
            Column {
                Box(modifier = Modifier.size(275.dp, 130.dp)) {
                    if (bossInfo.onOff == 1) {
                        GlideImage(
                            model = bossInfo.imgurl + System.currentTimeMillis(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val restComposition by rememberLottieComposition(LottieCompositionSpec.Asset("8266-rest-sloth.json"))
                        LottieAnimation(composition = restComposition, iterations = LottieConstants.IterateForever, modifier = Modifier.fillMaxSize())
                    }
                    val infoComposition by rememberLottieComposition(LottieCompositionSpec.Asset("12246-info.json"))
                    LottieAnimation(
                        composition = infoComposition,
                        iterations = 1,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(35.dp, 40.dp).clickable { onMoreInfoClick() }
                    )
                    if (isCoachMarkVisible) {
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 35.dp, end = 15.dp)) {
                            val clickComposition by rememberLottieComposition(LottieCompositionSpec.Asset("clickmark.json"))
                            LottieAnimation(composition = clickComposition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(70.dp))
                            Text(text = "느낌표를\n클릭해보세요", color = Color(0xC9FFFFFF), fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter))
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFA6A6A6), thickness = 1.dp)
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = bossInfo.title, color = Color(0xE6FFFFFF), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = MapleStory, modifier = Modifier.padding(bottom = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                            Image(painter = painterResource(id = R.drawable.ic_baseline_star_border_24), contentDescription = null, modifier = Modifier.size(10.dp))
                            Text(text = bossInfo.fanCnt, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(start = 2.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Image(painter = painterResource(id = R.drawable.ic_outline_thumb_up_alt_24), contentDescription = null, modifier = Modifier.size(10.dp))
                            Text(text = bossInfo.okCnt, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(start = 2.dp))
                        }
                        Text(text = bossInfo.bjname, color = Color(0xFF8587FE), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = MapleStory, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                            Image(painter = painterResource(id = R.drawable.ic_baseline_people_alt_24), contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(text = bossInfo.viewCnt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "오늘 즐겨찾기: ", color = Color(0xE4D5D5D5), fontSize = 8.sp)
                            Text(text = bossInfo.incFanCnt, color = if ((bossInfo.incFanCnt.filter { it.isDigit() || it == '-' }.toIntOrNull() ?: 0) < 0) Color(0xFFFF4A4A) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "일일 풍력: ", color = Color(0xE4D5D5D5), fontSize = 8.sp)
                                Text(text = bossInfo.balloninfo?.dayballon ?: "-", color = Color(0xFF46E9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "월간 풍력: ", color = Color(0xE4D5D5D5), fontSize = 8.sp)
                                Text(text = bossInfo.balloninfo?.monthballon ?: "-", color = Color(0xFF46E9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
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
        Text(text = stringResource(id = R.string.search_as_josae), color = Color(0xCCFFFFFF), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp)
    }
}

@Composable
fun TeamItem(teamName: String, teamList: List<BroadInfo>, onClick: () -> Unit) {
    val isOn = teamList.any { it.onOff == 1 }
    val totalViewers = teamList.filter { it.onOff == 1 }.sumOf { it.viewCnt.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF444444))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(65.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = teamName, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, fontFamily = MapleStory)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_baseline_people_alt_24), contentDescription = null, modifier = Modifier.size(12.dp))
                    Text(text = totalViewers.toString().goodString(), color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(start = 2.dp))
                }
            }
            VerticalDivider(color = Color(0xFFA6A6A6), thickness = 1.dp)
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                val lottieFile = if (isOn) "35627-weather-day-clear-sky.json" else "8438-mr-cookie-drink.json"
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieFile))
                LottieAnimation(composition = composition, iterations = if (isOn) LottieConstants.IterateForever else 1, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AdBanner(adRequest: AdRequest) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { AdView(it).apply { setAdSize(AdSize.BANNER); adUnitId = context.getString(R.string.bannerId); loadAd(adRequest) } },
        update = { it.loadAd(adRequest) }
    )
}

@Composable
fun LoadingOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0x4D000000)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("8438-mr-cookie-drink2.json"))
        LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever, modifier = Modifier.fillMaxSize())
    }
}

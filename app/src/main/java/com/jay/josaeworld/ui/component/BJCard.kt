package com.jay.josaeworld.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.jay.josaeworld.R
import com.jay.josaeworld.domain.model.response.BroadInfo
import com.jay.josaeworld.ui.theme.MapleStory

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BJCard(
    bjInfo: BroadInfo,
    onClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCoachMarkVisible: Boolean = false,
    isUnderBoss: Boolean = false,
    useProfileImage: Boolean = false, // 오프라인 시 Lottie 대신 프로필 이미지를 사용할지 여부
) {
    val random = remember { java.util.Random() }
    val isOn = bjInfo.onOff == 1
    val viewCountInt = bjInfo.viewCnt.filter { it.isDigit() }.toIntOrNull() ?: 0
    val isHighViewCount = isOn && viewCountInt >= 10000

    val tightTextStyle =
        LocalTextStyle.current.copy(
            platformStyle =
                androidx.compose.ui.text
                    .PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle =
                LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
        )

    Box(
        modifier =
            modifier
                .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 배경 효과 (고시청자 또는 부수장)
        if (isHighViewCount || isUnderBoss) {
            val lottieFile = if (isHighViewCount) "lf30_editor_cecsqjtv.json" else "34763-hero-on-its-way.json"
            val highlightComposition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieFile))
            LottieAnimation(
                composition = highlightComposition,
                iterations = LottieConstants.IterateForever,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .scale(2.5f),
                contentScale = ContentScale.Inside,
            )
        }

        Card(
            modifier =
                Modifier
                    .width(295.dp)
                    .wrapContentHeight()
                    .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 썸네일 영역
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                ) {
                    if (isOn) {
                        GlideImage(
                            model = bjInfo.imgurl + "${random.nextInt(123456789)}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!useProfileImage) {
                                // 보스 섹션 등: 방송 종료 시 나무늘보 Lottie 표시
                                val restComposition by rememberLottieComposition(
                                    LottieCompositionSpec.Asset(
                                        "8266-rest-sloth.json",
                                    ),
                                )
                                LottieAnimation(
                                    composition = restComposition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                // 일반 섹션 등: 방송 종료 시 프로필 이미지(원형) 표시
                                GlideImage(
                                    model = bjInfo.profilePhoto,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(100.dp)
                                            .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }

                    val infoComposition by rememberLottieComposition(LottieCompositionSpec.Asset("12246-info.json"))
                    LottieAnimation(
                        composition = infoComposition,
                        iterations = 1,
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(40.dp, 45.dp)
                                .clickable { onMoreInfoClick() },
                    )

                    if (isCoachMarkVisible) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 35.dp, end = 15.dp),
                        ) {
                            val clickComposition by rememberLottieComposition(
                                LottieCompositionSpec.Asset(
                                    "clickmark.json",
                                ),
                            )
                            LottieAnimation(
                                composition = clickComposition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(100.dp),
                            )
                            Text(
                                text = "느낌표를\n클릭해보세요",
                                color = Color(0xC9FFFFFF),
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                style = tightTextStyle,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                    thickness = 2.dp,
                    color = Color(0xFFA6A6A6),
                )

                // 정보 영역
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = bjInfo.title,
                        color = Color(0xE6FFFFFF),
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontFamily = MapleStory,
                        style = tightTextStyle,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 9.dp),
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_star_border_24),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = bjInfo.fanCnt,
                                color = Color.White,
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                style = tightTextStyle,
                                modifier = Modifier.padding(start = 1.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_outline_thumb_up_alt_24),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = bjInfo.okCnt,
                                color = Color.White,
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                style = tightTextStyle,
                                modifier = Modifier.padding(start = 1.dp),
                            )
                        }
                        Text(
                            text = bjInfo.bjname,
                            color = Color(0xFF8587FE),
                            fontSize = 13.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MapleStory,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 80.dp),
                            style = tightTextStyle,
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_people_alt_24),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (isOn) bjInfo.viewCnt else "",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Bold,
                                style = tightTextStyle,
                                modifier = Modifier.padding(start = 2.dp),
                            )
                        }
                    }

                    // 풍력 정보
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "오늘의 즐겨찾기 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                style = tightTextStyle,
                            )
                            val fanCntVal =
                                bjInfo.incFanCnt
                                    .filter { it.isDigit() || it == '-' }
                                    .toIntOrNull() ?: 0
                            Text(
                                text = bjInfo.incFanCnt,
                                color = if (fanCntVal < 0) Color(0xFFFF4A4A) else Color.White,
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                fontWeight = FontWeight.Bold,
                                style = tightTextStyle,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "일일 풍력 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                style = tightTextStyle,
                            )
                            Text(
                                text = bjInfo.balloninfo?.dayballon ?: "-",
                                color = Color(0xFF46E9FF),
                                fontSize = 11.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Bold,
                                style = tightTextStyle,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "월간 풍력 : ",
                                color = Color(0xE4D5D5D5),
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                style = tightTextStyle,
                            )
                            Text(
                                text = bjInfo.balloninfo?.monthballon ?: "-",
                                color = Color(0xFF46E9FF),
                                fontSize = 11.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Bold,
                                style = tightTextStyle,
                            )
                        }
                    }
                }
            }
        }
    }
}

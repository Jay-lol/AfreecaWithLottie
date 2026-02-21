package com.jay.josaeworld.ui.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jay.josaeworld.R
import com.jay.josaeworld.domain.goodString
import com.jay.josaeworld.ui.theme.MapleStory

@Composable
fun MainInfoSection(
    allViewers: Int,
    allBallons: Int,
    modifier: Modifier = Modifier,
) {
    val animatedViewers by animateIntAsState(
        targetValue = allViewers,
        animationSpec = tween(durationMillis = 1500),
        label = "viewers",
    )
    val animatedBallons by animateIntAsState(
        targetValue = allBallons,
        animationSpec = tween(durationMillis = 1500),
        label = "ballons",
    )

    val tightTextStyle = LocalTextStyle.current.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.weight(0.55f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "전체 시청자 : ",
                    style = tightTextStyle,
                    color = Color(0xFFBCBCBC),
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = MapleStory,
                )
            }
            Box(modifier = Modifier.weight(0.45f), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = animatedViewers.toString().goodString(),
                        style = tightTextStyle,
                        color = Color(0xFFEDFF49),
                        fontSize = 21.sp,
                        lineHeight = 21.sp,
                        fontFamily = MapleStory,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = " 명",
                        style = tightTextStyle,
                        color = Color(0xFFBCBCBC),
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = MapleStory,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.weight(0.55f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "이번 달 크루 풍력 : ",
                    style = tightTextStyle,
                    color = Color(0xFFBCBCBC),
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = MapleStory,
                )
            }
            Box(modifier = Modifier.weight(0.45f), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = animatedBallons.toString().goodString(),
                    style = tightTextStyle,
                    color = Color(0xFF46E9FF),
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = MapleStory,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.weight(0.55f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "●출처: 풍투데이 ",
                    style = tightTextStyle,
                    color = Color(0xB2FFFFFF),
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = MapleStory,
                )
            }
            Box(modifier = Modifier.weight(0.45f), contentAlignment = Alignment.CenterStart) {
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = null,
                    modifier = Modifier.size(width = 55.dp, height = 22.dp),
                )
            }
        }
    }
}

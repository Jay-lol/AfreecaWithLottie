package com.jay.josaeworld.ui.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jay.josaeworld.domain.model.response.BallonInfo
import com.jay.josaeworld.ui.theme.tightTextStyle

@Composable
fun JosaeCustomDialog(
    question: String,
    warning: String? = null,
    okText: String = "나감",
    cancelText: String = "안나감",
    enableMarquee: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                Modifier
                    .width(300.dp)
                    .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF282828),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 20.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = question,
                        style =
                            tightTextStyle.copy(
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            ),
                    )
                }

                if (!warning.isNullOrEmpty()) {
                    Text(
                        text = warning,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .padding(horizontal = 8.dp)
                                .then(
                                    if (enableMarquee) Modifier.basicMarquee() else Modifier,
                                ),
                        style =
                            tightTextStyle.copy(
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xA6FFFFFF),
                            ),
                        maxLines = 1,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 1.dp),
                    thickness = 1.dp,
                    color = Color.Black,
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelText,
                            style =
                                tightTextStyle.copy(
                                    fontSize = 14.sp,
                                    color = Color(0x99FFFFFF),
                                ),
                        )
                    }

                    VerticalDivider(
                        thickness = 1.dp,
                        color = Color.Black,
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { onConfirm() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = okText,
                            style =
                                tightTextStyle.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JosaeMoveDialog(
    question: String,
    okText: String = "이동",
    cancelText: String = "아니요",
    isBroadOff: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                Modifier
                    .width(300.dp)
                    .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF282828),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00CCFF)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 20.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = question,
                        style =
                            tightTextStyle.copy(
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            ),
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 1.dp),
                    thickness = 1.dp,
                    color = Color.Black,
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { onCancel() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelText,
                            style =
                                tightTextStyle.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBroadOff) Color(0x99FFFFFF) else Color.White,
                                ),
                        )
                    }

                    VerticalDivider(
                        thickness = 1.dp,
                        color = Color.Black,
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { onConfirm() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = okText,
                            style =
                                tightTextStyle.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JosaeReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var streamerName by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                decorFitsSystemWindows = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .wrapContentHeight()
                        .clickable(enabled = false) {},
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF282828),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00CCFF)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Streamer",
                            modifier = Modifier.width(100.dp),
                            style =
                                tightTextStyle.copy(
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFCCCCCC),
                                ),
                        )
                        OutlinedTextField(
                            value = streamerName,
                            onValueChange = { streamerName = it },
                            modifier = Modifier.weight(1f),
                            textStyle = tightTextStyle.copy(fontSize = 14.sp, color = Color.White),
                            placeholder = {
                                Text(
                                    "Streamer 이름을 적어주세요",
                                    style = tightTextStyle.copy(fontSize = 14.sp, color = Color(0x85FFFFFF)),
                                )
                            },
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = Color.White,
                                ),
                            singleLine = true,
                        )
                    }

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "건의 내용",
                            modifier = Modifier.width(100.dp),
                            style =
                                tightTextStyle.copy(
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFCCCCCC),
                                ),
                        )
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier.weight(1f),
                            textStyle = tightTextStyle.copy(fontSize = 12.sp, color = Color.White),
                            placeholder = {
                                Text(
                                    "건의 내용을 적어주세요(크루명, 가입, 탈퇴)",
                                    style = tightTextStyle.copy(fontSize = 12.sp, color = Color(0x85FFFFFF)),
                                )
                            },
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = Color.White,
                                ),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        thickness = 1.dp,
                        color = Color.Black,
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clickable { onDismiss() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "취소",
                                style =
                                    tightTextStyle.copy(
                                        fontSize = 14.sp,
                                        color = Color(0x8BFFFFFF),
                                    ),
                            )
                        }

                        VerticalDivider(thickness = 1.dp, color = Color.Black)

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clickable { onSubmit(streamerName.text, content.text) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "제출",
                                style =
                                    tightTextStyle.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xBFFFFFFF),
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JosaeInfoDialog(
    streamerName: String,
    ballonInfo: BallonInfo?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF282828),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00CCFF)),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = streamerName,
                    modifier = Modifier.padding(top = 12.dp),
                    style =
                        tightTextStyle.copy(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xE4D5D5D5),
                        ),
                )

                Spacer(modifier = Modifier.height(15.dp))

                InfoRow(label = "누적 시청자 ", value = ballonInfo?.monthview ?: "-")
                Spacer(modifier = Modifier.height(15.dp))
                InfoRow(label = "최대 시청자 ", value = ballonInfo?.monthmaxview ?: "-")
                Spacer(modifier = Modifier.height(15.dp))
                InfoRow(label = " 방송 시간 ", value = ballonInfo?.monthtime ?: "-")
                Spacer(modifier = Modifier.height(15.dp))
                InfoRow(label = "        시급 ", value = ballonInfo?.monthpay ?: "-")

                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    thickness = 1.dp,
                    color = Color(0xFC00CCFF),
                )

                Text(
                    text = "*풍투데이 월간 데이터 기준 (정각마다 갱신)",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                    style =
                        tightTextStyle.copy(
                            fontSize = 10.sp,
                            textAlign = TextAlign.End,
                            color = Color(0xADFFFFFF),
                        ),
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style =
                tightTextStyle.copy(
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xE4D5D5D5),
                ),
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style =
                tightTextStyle.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = Color(0xE6FFFFFF),
                ),
        )
    }
}

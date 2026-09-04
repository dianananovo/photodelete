package com.xiuxiu.photoclean.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiuxiu.photoclean.data.PhotoItem
import com.xiuxiu.photoclean.ui.components.SwipeCard
import com.xiuxiu.photoclean.ui.theme.BackgroundApp
import com.xiuxiu.photoclean.ui.theme.PastelBlue
import com.xiuxiu.photoclean.ui.theme.PastelBlueBorder
import com.xiuxiu.photoclean.ui.theme.PastelBlueLight
import com.xiuxiu.photoclean.ui.theme.PastelPink
import com.xiuxiu.photoclean.ui.theme.TextMuted
import com.xiuxiu.photoclean.ui.theme.TextPrimary
import com.xiuxiu.photoclean.ui.theme.TextSecondary

@Composable
fun CleanScreen(
    isLoading: Boolean,
    photoQueue: List<PhotoItem>,
    trashCount: Int,
    canUndo: Boolean,
    onBackToHome: () -> Unit,
    onSwipeUpDelete: (PhotoItem) -> Unit,
    onSwipeDownKeep: (PhotoItem) -> Unit,
    onSwipeSideMove: (PhotoItem) -> Unit = {},
    onUndo: () -> Unit,
    onFinishClean: () -> Unit,
    onPreviewPhoto: (PhotoItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部 Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 回到首页返回键 (纯白底带淡蓝微边框)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, PastelBlueBorder, CircleShape)
                            .clickable { onBackToHome() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "返回首页",
                            tint = PastelBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "咻咻相册",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "上滑删 · 下滑留 · 左右移动",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // 右上角【待确认】重新设计：纯白底协调微胶囊，黑字，浅粉小圆气泡
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, PastelBlueBorder, RoundedCornerShape(20.dp))
                        .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "待确认",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PastelPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$trashCount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 中间卡片展示区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PastelBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "正在打乱相册照片与视频...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    photoQueue.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(PastelBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "✓", color = PastelBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "未整理照片全部翻完啦",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "点击下方结束清理，二次审核待删照片",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        val current = photoQueue.first()
                        val next = photoQueue.getOrNull(1)

                        if (next != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(0.94f)
                            ) {
                                key(next.id) {
                                    SwipeCard(
                                        photo = next,
                                        modifier = Modifier.fillMaxSize(),
                                        onSwipeUpDelete = {},
                                        onSwipeDownKeep = {}
                                    )
                                }
                            }
                        }

                        key(current.id) {
                            SwipeCard(
                                photo = current,
                                modifier = Modifier.fillMaxSize(),
                                onSwipeUpDelete = { onSwipeUpDelete(it) },
                                onSwipeDownKeep = { onSwipeDownKeep(it) },
                                onSwipeSideMove = { onSwipeSideMove(it) },
                                onCardClick = { onPreviewPhoto(current) }
                            )
                        }
                    }
                }
            }

            // 底部操作栏：统一纯色浅粉蓝
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 撤回键 (低饱和马卡龙纯粉，无渐变，略微加长)
                Button(
                    onClick = onUndo,
                    enabled = canUndo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelPink,
                        disabledContainerColor = PastelPink.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = "撤回",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "撤回",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // 结束本次清理按钮 (统一纯色浅蓝 #7FAFF6，无渐变，白字加粗)
                Button(
                    onClick = onFinishClean,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelBlue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "结束本次清理",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

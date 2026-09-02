package com.xiuxiu.photoclean.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xiuxiu.photoclean.data.PhotoItem
import com.xiuxiu.photoclean.ui.theme.BabyBlue
import com.xiuxiu.photoclean.ui.theme.PastelPink
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun SwipeCard(
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    onSwipeUpDelete: (PhotoItem) -> Unit,
    onSwipeDownKeep: (PhotoItem) -> Unit,
    onSwipeSideMove: (PhotoItem) -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenHeightPx * 0.16f
    val sideThreshold = screenWidthPx * 0.28f

    val rotation = (offsetX.value / 25f) + (offsetY.value / 60f).coerceIn(-8f, 8f)

    val isHorizontal = abs(offsetX.value) > abs(offsetY.value) * 1.2f
    val moveAlpha = if (isHorizontal) (abs(offsetX.value) / (sideThreshold * 0.7f)).coerceIn(0f, 1f) else 0f
    val deleteAlpha = if (!isHorizontal) (-offsetY.value / (swipeThreshold * 0.8f)).coerceIn(0f, 1f) else 0f
    val keepAlpha = if (!isHorizontal) (offsetY.value / (swipeThreshold * 0.8f)).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotation)
            .pointerInput(photo.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            val absX = abs(offsetX.value)
                            val absY = abs(offsetY.value)
                            val totalDistance = hypot(offsetX.value, offsetY.value)

                            // 单击判定
                            if (totalDistance < 15f) {
                                onCardClick()
                                launch { offsetX.animateTo(0f) }
                                launch { offsetY.animateTo(0f) }
                                return@launch
                            }

                            when {
                                absX > sideThreshold && absX > absY * 1.2f -> {
                                    onSwipeSideMove(photo)
                                    launch { offsetX.animateTo(0f, spring()) }
                                    launch { offsetY.animateTo(0f, spring()) }
                                }
                                offsetY.value < -swipeThreshold -> {
                                    offsetY.animateTo(-screenHeightPx * 1.2f, tween(250))
                                    onSwipeUpDelete(photo)
                                }
                                offsetY.value > swipeThreshold -> {
                                    offsetY.animateTo(screenHeightPx * 1.2f, tween(250))
                                    onSwipeDownKeep(photo)
                                }
                                else -> {
                                    launch {
                                        offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                    launch {
                                        offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            }
                        }
                    }
                )
            }
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(28.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
            ) {
                // 统一浅色衬底背景容器 (Clean Light Backdrop)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF6F7FB))
                ) {
                    // 轻微淡雅浅色模糊
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(36.dp)
                            .alpha(0.18f)
                    )

                    // 完整前景（自适应长图、宽图）
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }

                // 顶部左侧：所在相册胶囊 (浅白底)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = "所在相册",
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = photo.albumName,
                                color = Color(0xFF2B2D42),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (photo.isVideo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF232533), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (photo.formattedDuration.isNotEmpty()) "视频 ${photo.formattedDuration}" else "视频",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else if (photo.isGif) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF232533), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GIF 动图",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // 顶部右侧：单击大图提示
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 16.dp)
                        .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.Black.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "放大",
                            tint = Color(0xFF757A95),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "单击大图",
                            color = Color(0xFF757A95),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 底部信息栏 (浅白渐变蒙层，深色文字)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.75f),
                                    Color.White.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = photo.formattedDate,
                                color = Color(0xFF2B2D42),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0F2F6), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = photo.formattedSize,
                                    color = Color(0xFF4A4E69),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = photo.displayName,
                            color = Color(0xFF757A95),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }

                // 上滑删除指示
                if (deleteAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp)
                            .alpha(deleteAlpha)
                            .background(PastelPink.copy(alpha = 0.94f), RoundedCornerShape(20.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "上滑 · 移入待删池",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 下滑保留指示
                if (keepAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 90.dp)
                            .alpha(keepAlpha)
                            .background(BabyBlue.copy(alpha = 0.94f), RoundedCornerShape(20.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "下滑 · 留下它",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 左右滑移动相册指示
                if (moveAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(moveAlpha)
                            .background(Color(0xFF9D4EDD).copy(alpha = 0.94f), RoundedCornerShape(22.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(22.dp))
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DriveFileMove,
                                contentDescription = "移动相册",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "左右拖动 · 移动所在相册",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

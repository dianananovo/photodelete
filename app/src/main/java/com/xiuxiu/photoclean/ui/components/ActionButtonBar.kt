package com.xiuxiu.photoclean.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiuxiu.photoclean.ui.theme.BabyBlue
import com.xiuxiu.photoclean.ui.theme.BabyBlueLight
import com.xiuxiu.photoclean.ui.theme.PastelPink
import com.xiuxiu.photoclean.ui.theme.PastelPinkLight
import com.xiuxiu.photoclean.ui.theme.TextMuted
import com.xiuxiu.photoclean.ui.theme.TextSecondary

@Composable
fun ActionButtonBar(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    onKeepClick: () -> Unit,
    onUndoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 保留按钮 (下滑对应操作，治愈蓝)
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(6.dp, CircleShape, spotColor = BabyBlue.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onKeepClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BabyBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "保留照片",
                    tint = BabyBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 撤销按钮 (Undo，灵巧小圆圈)
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(
                    elevation = if (canUndo) 4.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(if (canUndo) Color.White else Color(0xFFF0F1F5))
                .clickable(enabled = canUndo) { onUndoClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Undo,
                contentDescription = "撤销上一步",
                tint = if (canUndo) TextSecondary else TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }

        // 移入待删除池按钮 (上滑对应操作，初恋粉)
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(6.dp, CircleShape, spotColor = PastelPink.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onDeleteClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(PastelPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "移入待删池",
                    tint = PastelPink,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

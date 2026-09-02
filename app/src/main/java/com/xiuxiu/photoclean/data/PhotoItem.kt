package com.xiuxiu.photoclean.data

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 媒体实体模型（支持照片、GIF动图、视频）
 */
data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val albumName: String, // 所在相册文件夹名称 (如 Camera, Screenshots)
    val mimeType: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val dateAdded: Long,
    val durationMs: Long = 0L // 视频时长 (毫秒)
) {
    val isVideo: Boolean
        get() = mimeType.startsWith("video/")

    val isGif: Boolean
        get() = mimeType == "image/gif"

    val formattedSize: String
        get() {
            return when {
                size <= 0 -> "0 B"
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
                else -> String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0))
            }
        }

    val formattedDate: String
        get() {
            if (dateAdded <= 0) return "未知日期"
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            return sdf.format(Date(dateAdded * 1000L))
        }

    val formattedDuration: String
        get() {
            if (!isVideo || durationMs <= 0) return ""
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
}

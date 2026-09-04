package com.xiuxiu.photoclean

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.xiuxiu.photoclean.data.PhotoRepository
import com.xiuxiu.photoclean.ui.screens.CleanScreen
import com.xiuxiu.photoclean.ui.screens.HomeScreen
import com.xiuxiu.photoclean.ui.screens.PhotoPreviewDialog
import com.xiuxiu.photoclean.ui.screens.PhotoViewModel
import com.xiuxiu.photoclean.ui.screens.ResultDialog
import com.xiuxiu.photoclean.ui.screens.ReviewScreen
import com.xiuxiu.photoclean.ui.theme.BackgroundCream
import com.xiuxiu.photoclean.ui.theme.TextPrimary
import com.xiuxiu.photoclean.ui.theme.TextSecondary
import com.xiuxiu.photoclean.ui.theme.XiuXiuTheme

enum class AppScreen {
    HOME, CLEAN, REVIEW
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PhotoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PhotoRepository(applicationContext)
        viewModel = PhotoViewModel(repository)

        setContent {
            XiuXiuTheme {
                Box(modifier = Modifier.fillMaxSize().background(BackgroundCream).systemBarsPadding()) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: PhotoViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                requiredPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && viewModel.allPhotos.value.isEmpty()) {
            viewModel.scanPhotos()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.scanPhotos()
        }
    }

    // Android 11+ 系统相册安全删除回调
    val systemDeleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeleteConfirmed()
            currentScreen = AppScreen.HOME
        }
    }

    if (!hasPermission) {
        PermissionGuideScreen(
            onRequestPermission = { permissionLauncher.launch(requiredPermission) }
        )
    } else {
        val isLoading by viewModel.isLoading.collectAsState()
        val allPhotos by viewModel.allPhotos.collectAsState()
        val deletedIds by viewModel.deletedIds.collectAsState()
        val keptIds by viewModel.keptIds.collectAsState()
        val photoQueue by viewModel.photoQueue.collectAsState()
        val trashPool by viewModel.trashPool.collectAsState()
        val selectedForDelete by viewModel.selectedForDelete.collectAsState()
        val historyStack by viewModel.historyStack.collectAsState()
        val previewPhoto by viewModel.previewPhoto.collectAsState()
        val showResultDialog by viewModel.showResultDialog.collectAsState()
        val cleanedCount by viewModel.cleanedCount.collectAsState()
        val cleanedBytes by viewModel.cleanedBytes.collectAsState()

        val totalCount = allPhotos.size
        val processedCount = deletedIds.size + keptIds.size
        val pendingCount = (totalCount - processedCount).coerceAtLeast(0)

        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    totalCount = totalCount,
                    processedCount = processedCount,
                    pendingCount = pendingCount,
                    onStartClean = {
                        viewModel.refreshQueue()
                        currentScreen = AppScreen.CLEAN
                    },
                    onResetProgress = {
                        viewModel.resetProgress()
                    }
                )
            }
            AppScreen.CLEAN -> {
                CleanScreen(
                    isLoading = isLoading,
                    photoQueue = photoQueue,
                    trashCount = trashPool.size,
                    canUndo = historyStack.isNotEmpty(),
                    onBackToHome = { currentScreen = AppScreen.HOME },
                    onSwipeUpDelete = { viewModel.swipeUpDelete(it) },
                    onSwipeDownKeep = { viewModel.swipeDownKeep(it) },
                    onUndo = { viewModel.undo() },
                    onFinishClean = { currentScreen = AppScreen.REVIEW },
                    onPreviewPhoto = { viewModel.setPreviewPhoto(it) }
                )
            }
            AppScreen.REVIEW -> {
                ReviewScreen(
                    trashList = trashPool,
                    selectedItems = selectedForDelete,
                    onBack = { currentScreen = AppScreen.CLEAN },
                    onToggleSelect = { viewModel.toggleDeleteSelection(it) },
                    onToggleSelectAll = { viewModel.toggleSelectAll(it) },
                    onPreviewPhoto = { viewModel.setPreviewPhoto(it) },
                    onConfirmDelete = {
                        val pendingIntent = viewModel.createDeletePendingIntent(useTrash = true)
                        if (pendingIntent != null) {
                            val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                            systemDeleteLauncher.launch(request)
                        }
                    }
                )
            }
        }

        // 大图预览弹窗
        previewPhoto?.let { photo ->
            PhotoPreviewDialog(
                photo = photo,
                onDismiss = { viewModel.setPreviewPhoto(null) }
            )
        }

        // 清理完成弹窗
        if (showResultDialog) {
            ResultDialog(
                deletedCount = cleanedCount,
                deletedBytes = cleanedBytes,
                onDismiss = {
                    viewModel.dismissResultDialog()
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }
}

@Composable
fun PermissionGuideScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "咻咻相册",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.permission_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.permission_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232533)),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.grant_permission),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

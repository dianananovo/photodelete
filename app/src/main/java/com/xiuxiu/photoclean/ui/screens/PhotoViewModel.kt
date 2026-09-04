package com.xiuxiu.photoclean.ui.screens

import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiuxiu.photoclean.data.PhotoItem
import com.xiuxiu.photoclean.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SwipeAction {
    KEEP, DELETE, MOVE
}

data class ActionRecord(
    val photo: PhotoItem,
    val action: SwipeAction,
    val targetAlbum: String? = null
)

class PhotoViewModel(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 全量照片
    private val _allPhotos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val allPhotos: StateFlow<List<PhotoItem>> = _allPhotos.asStateFlow()

    // 1. 已删除集合（永久排除，绝不恢复）
    private val _deletedIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletedIds: StateFlow<Set<Long>> = _deletedIds.asStateFlow()

    // 2. 已整理且留下的集合（进度归零时仅重置这部分）
    private val _keptIds = MutableStateFlow<Set<Long>>(emptySet())
    val keptIds: StateFlow<Set<Long>> = _keptIds.asStateFlow()

    // 当前未整理的推送队列 (allPhotos 排除 deletedIds 和 keptIds)
    private val _photoQueue = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photoQueue: StateFlow<List<PhotoItem>> = _photoQueue.asStateFlow()

    // 当次待确认删除池
    private val _trashPool = MutableStateFlow<List<PhotoItem>>(emptyList())
    val trashPool: StateFlow<List<PhotoItem>> = _trashPool.asStateFlow()

    // 审核面板中被勾选的图片
    private val _selectedForDelete = MutableStateFlow<Set<PhotoItem>>(emptySet())
    val selectedForDelete: StateFlow<Set<PhotoItem>> = _selectedForDelete.asStateFlow()

    // 撤回栈
    private val _historyStack = MutableStateFlow<List<ActionRecord>>(emptyList())
    val historyStack: StateFlow<List<ActionRecord>> = _historyStack.asStateFlow()

    // 大图预览
    private val _previewPhoto = MutableStateFlow<PhotoItem?>(null)
    val previewPhoto: StateFlow<PhotoItem?> = _previewPhoto.asStateFlow()

    // 结算弹窗
    private val _showResultDialog = MutableStateFlow(false)
    val showResultDialog: StateFlow<Boolean> = _showResultDialog.asStateFlow()

    private val _cleanedCount = MutableStateFlow(0)
    val cleanedCount: StateFlow<Int> = _cleanedCount.asStateFlow()

    private val _cleanedBytes = MutableStateFlow(0L)
    val cleanedBytes: StateFlow<Long> = _cleanedBytes.asStateFlow()

    fun scanPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            val fullList = repository.fetchShuffledPhotos()
            _allPhotos.value = fullList
            refreshQueue()
            _isLoading.value = false
        }
    }

    /**
     * 刷新未整理推送队列：排除已彻底删除与已保留的照片
     */
    fun refreshQueue() {
        val deleted = _deletedIds.value
        val kept = _keptIds.value
        val unhandled = _allPhotos.value.filter { it.id !in deleted && it.id !in kept }
        _photoQueue.value = unhandled.shuffled()
    }

    /**
     * 核心规则：进度归零仅将【已经整理且留下的图片】重新放入未整理；【已删除的图片】永久排除！
     */
    fun resetProgress() {
        _keptIds.value = emptySet()
        _trashPool.value = emptyList()
        _selectedForDelete.value = emptySet()
        _historyStack.value = emptyList()
        refreshQueue()
    }

    fun swipeUpDelete(photo: PhotoItem) {
        val currentQueue = _photoQueue.value
        if (currentQueue.isNotEmpty() && currentQueue.first().id == photo.id) {
            _photoQueue.value = currentQueue.drop(1)
            _trashPool.value = _trashPool.value + photo
            _selectedForDelete.value = _selectedForDelete.value + photo
            _historyStack.value = _historyStack.value + ActionRecord(photo, SwipeAction.DELETE)
        }
    }

    fun swipeDownKeep(photo: PhotoItem) {
        val currentQueue = _photoQueue.value
        if (currentQueue.isNotEmpty() && currentQueue.first().id == photo.id) {
            _photoQueue.value = currentQueue.drop(1)
            _keptIds.value = _keptIds.value + photo.id
            _historyStack.value = _historyStack.value + ActionRecord(photo, SwipeAction.KEEP)
        }
    }

    fun swipeSideMove(photo: PhotoItem, targetAlbum: String) {
        val currentQueue = _photoQueue.value
        if (currentQueue.isNotEmpty() && currentQueue.first().id == photo.id) {
            _photoQueue.value = currentQueue.drop(1)
            _keptIds.value = _keptIds.value + photo.id
            _historyStack.value = _historyStack.value + ActionRecord(photo, SwipeAction.MOVE, targetAlbum)
        }
    }

    fun undo() {
        val history = _historyStack.value
        if (history.isEmpty()) return

        val lastRecord = history.last()
        _historyStack.value = history.dropLast(1)

        _photoQueue.value = listOf(lastRecord.photo) + _photoQueue.value

        when (lastRecord.action) {
            SwipeAction.KEEP, SwipeAction.MOVE -> {
                _keptIds.value = _keptIds.value - lastRecord.photo.id
            }
            SwipeAction.DELETE -> {
                _trashPool.value = _trashPool.value.filter { it.id != lastRecord.photo.id }
                _selectedForDelete.value = _selectedForDelete.value.filter { it.id != lastRecord.photo.id }.toSet()
            }
        }
    }

    fun toggleDeleteSelection(photo: PhotoItem) {
        val current = _selectedForDelete.value.toMutableSet()
        if (current.contains(photo)) {
            current.remove(photo)
        } else {
            current.add(photo)
        }
        _selectedForDelete.value = current
    }

    fun toggleSelectAll(selectAll: Boolean) {
        if (selectAll) {
            _selectedForDelete.value = _trashPool.value.toSet()
        } else {
            _selectedForDelete.value = emptySet()
        }
    }

    fun setPreviewPhoto(photo: PhotoItem?) {
        _previewPhoto.value = photo
    }

    fun createDeletePendingIntent(useTrash: Boolean = true): PendingIntent? {
        val targets = _selectedForDelete.value.toList()
        return repository.createDeleteOrTrashPendingIntent(
            uris = targets.map { it.uri },
            useTrash = useTrash
        )
    }

    /**
     * 系统确认删除成功：将删除照片 ID 加入永久排除集合 deletedIds
     */
    fun onDeleteConfirmed() {
        val deletedItems = _selectedForDelete.value
        val totalBytes = deletedItems.sumOf { it.size }
        val count = deletedItems.size

        val deletedIdList = deletedItems.map { it.id }.toSet()
        _deletedIds.value = _deletedIds.value + deletedIdList
        _keptIds.value = _keptIds.value - deletedIdList

        _trashPool.value = _trashPool.value.filter { it.id !in deletedIdList }
        _selectedForDelete.value = emptySet()

        _cleanedCount.value = count
        _cleanedBytes.value = totalBytes
        _showResultDialog.value = true
    }

    fun dismissResultDialog() {
        _showResultDialog.value = false
    }
}

package com.tanmay.buddie.presentation.screens.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseWithItems(
    val courseName: String,
    val items: List<FetchedMoodleItem>
)

data class SubjectSyncPreviewUiItem(
    val subjectId: String,
    val subjectName: String,
    val courses: List<CourseWithItems>,
    val totalCount: Int,
    val isExpanded: Boolean
)

data class SyncPreviewUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val subjects: List<SubjectSyncPreviewUiItem> = emptyList(),
    val totalCount: Int = 0
)

@HiltViewModel
class SyncPreviewViewModel @Inject constructor(
    private val syncEngine: MoodleContentSyncEngine
) : ViewModel() {

    private val _expandedSubjectIds = MutableStateFlow<Set<String>>(emptySet())
    private val _rawItems = MutableStateFlow<List<FetchedMoodleItem>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private var hasInitializedExpanded = false

    val uiState: StateFlow<SyncPreviewUiState> = combine(
        _rawItems,
        _expandedSubjectIds,
        _isLoading,
        _isRefreshing,
        _errorMessage
    ) { items, expandedIds, loading, refreshing, error ->
        val subjectsGrouped = items.groupBy { it.mappedSubjectId to it.mappedSubjectName }

        val subjectUiItems = subjectsGrouped.map { (key, subjectItems) ->
            val (subId, subName) = key
            val courses = subjectItems.groupBy { it.courseName }.map { (cName, cItems) ->
                CourseWithItems(courseName = cName, items = cItems)
            }
            SubjectSyncPreviewUiItem(
                subjectId = subId,
                subjectName = subName,
                courses = courses,
                totalCount = subjectItems.size,
                isExpanded = expandedIds.contains(subId)
            )
        }

        SyncPreviewUiState(
            isLoading = loading,
            isRefreshing = refreshing,
            errorMessage = error,
            subjects = subjectUiItems,
            totalCount = items.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncPreviewUiState())

    init {
        loadPreview(forceRefresh = false)
    }

    fun loadPreview(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
                _errorMessage.value = null
            } else {
                _isLoading.value = true
                _errorMessage.value = null
            }

            val result = syncEngine.fetchPreviewItems(forceRefreshCourseStructure = forceRefresh)
            if (result.isSuccess) {
                val items = result.getOrThrow()
                _rawItems.value = items

                // Expand first subject by default on initial load
                if (!hasInitializedExpanded && items.isNotEmpty()) {
                    val firstSubjectId = items.first().mappedSubjectId
                    _expandedSubjectIds.value = setOf(firstSubjectId)
                    hasInitializedExpanded = true
                }

                _isLoading.value = false
                _isRefreshing.value = false
                _errorMessage.value = null
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to fetch Moodle preview data"
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun toggleSubjectExpanded(subjectId: String) {
        val current = _expandedSubjectIds.value.toMutableSet()
        if (current.contains(subjectId)) {
            current.remove(subjectId)
        } else {
            current.add(subjectId)
        }
        _expandedSubjectIds.value = current
    }
}

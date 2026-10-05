package com.tanmay.buddie.presentation.screens.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleCourse
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoodleCourseMappingUiItem(
    val courseId: Long,
    val courseName: String,
    val shortName: String,
    val mappedSubjectId: String?,
    val isIgnored: Boolean
)

data class CourseMappingUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val courses: List<MoodleCourseMappingUiItem> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList()
)

@HiltViewModel
class CourseMappingViewModel @Inject constructor(
    private val moodleApiClient: MoodleApiClient,
    private val subjectRepository: SubjectRepository,
    private val mappingRepository: MoodleCourseMappingRepository,
    private val submissionRepository: SubmissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseMappingUiState())
    val uiState: StateFlow<CourseMappingUiState> = _uiState.asStateFlow()

    private val _rawCourses = MutableStateFlow<List<MoodleCourse>>(emptyList())

    init {
        observeData()
        loadCourses()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                _rawCourses,
                subjectRepository.getAllSubjects(),
                mappingRepository.getAllMappings()
            ) { rawCourses, subjects, mappings ->
                val mappingMap = mappings.associateBy { it.moodleCourseId }
                val uiItems = rawCourses.map { course ->
                    val mapping = mappingMap[course.id]
                    val isIgnored = mapping != null && mapping.subjectId == null
                    MoodleCourseMappingUiItem(
                        courseId = course.id,
                        courseName = course.fullName,
                        shortName = course.shortName,
                        mappedSubjectId = mapping?.subjectId,
                        isIgnored = isIgnored
                    )
                }
                _uiState.value.copy(
                    courses = uiItems,
                    subjects = subjects
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = moodleApiClient.getEnrolledCourses()
            if (result.isSuccess) {
                val courses = result.getOrNull().orEmpty()
                _rawCourses.value = courses
                _uiState.value = _uiState.value.copy(isLoading = false)
            } else {
                val error = result.exceptionOrNull()?.localizedMessage ?: "Failed to fetch enrolled courses"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }

    fun mapCourseToSubject(courseId: Long, courseName: String, subjectId: String) {
        viewModelScope.launch {
            val existing = mappingRepository.getMappingByCourseId(courseId)
            if (existing != null && existing.subjectId != null && existing.subjectId != subjectId) {
                // Course was previously mapped to a different subject, remove old assignments
                submissionRepository.deleteMoodleSubmissionsByCourseId(courseId)
            }
            mappingRepository.saveMapping(courseId, courseName, subjectId)
        }
    }

    fun ignoreCourse(courseId: Long, courseName: String) {
        viewModelScope.launch {
            // Delete all Moodle assignments for this de-mapped / ignored course
            submissionRepository.deleteMoodleSubmissionsByCourseId(courseId)
            mappingRepository.saveMapping(courseId, courseName, null)
        }
    }

    fun unmapCourse(courseId: Long) {
        viewModelScope.launch {
            // Delete all Moodle assignments for this de-mapped course
            submissionRepository.deleteMoodleSubmissionsByCourseId(courseId)
            mappingRepository.deleteMappingByCourseId(courseId)
        }
    }
}

package com.tanmay.buddie.presentation.screens.moodle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.FilterCode
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.repository.FilterCodeRepository
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.sync.CalibrationProposal
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class BaseFilterData(
    val subjects: List<SubjectEntity>,
    val mappings: List<MoodleCourseMappingEntity>,
    val filterCodes: List<FilterCode>
)

private data class CalibrationFlowState(
    val calibratingMap: Map<String, List<CalibrationPatternUi>>,
    val selMineMap: Map<String, Set<String>>,
    val selNotMineMap: Map<String, Set<String>>
)

data class CalibrationPatternUi(
    val id: String, // unique pattern key e.g. tokens.joinToString(",")
    val representativeTitle: String,
    val tokens: Set<String>,
    val userLabel: Boolean? = null // true = yes (mine), false = no (not mine), null = unset
)

data class SubjectFilterSectionUi(
    val subject: SubjectEntity,
    val mappedCourseNames: List<String>,
    val filterCodes: List<FilterCode>,
    val unresolvedCount: Int,
    val totalFetchedCount: Int,
    val hasWarningBadge: Boolean,
    val isExpanded: Boolean,
    val isCalibrating: Boolean = false,
    val calibrationPatterns: List<CalibrationPatternUi> = emptyList(),
    val calibrationProposal: CalibrationProposal? = null,
    val confirmedMineProposals: Set<String> = emptySet(),
    val confirmedNotMineProposals: Set<String> = emptySet()
)

data class FilterCodeUiState(
    val isLoading: Boolean = true,
    val subjects: List<SubjectFilterSectionUi> = emptyList(),
    val focusedSubjectId: String? = null,
    val totalUnresolvedCount: Int = 0
)

@HiltViewModel
class FilterCodeViewModel @Inject constructor(
    private val filterCodeRepository: FilterCodeRepository,
    private val subjectRepository: SubjectRepository,
    private val courseMappingRepository: MoodleCourseMappingRepository,
    private val syncEngine: MoodleContentSyncEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetSubjectId: String? = savedStateHandle["subjectId"]

    private val _expandedSubjectIds = MutableStateFlow<Set<String>?>(null)
    private val _isFetching = MutableStateFlow(false)
    private val _itemsVersion = MutableStateFlow(0)

    // SubjectId -> List of calibration patterns
    private val _calibratingSubjects = MutableStateFlow<Map<String, List<CalibrationPatternUi>>>(emptyMap())
    // SubjectId -> Set of selected mine proposals
    private val _selectedMineProposals = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    // SubjectId -> Set of selected not-mine proposals
    private val _selectedNotMineProposals = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    private val baseDataFlow = combine(
        subjectRepository.getAllSubjects(),
        courseMappingRepository.getAllMappings(),
        filterCodeRepository.getAllFilterCodes()
    ) { subjects, mappings, filterCodes ->
        BaseFilterData(subjects, mappings, filterCodes)
    }

    private val calibrationFlow = combine(
        _calibratingSubjects,
        _selectedMineProposals,
        _selectedNotMineProposals
    ) { calibrating, selMine, selNotMine ->
        CalibrationFlowState(calibrating, selMine, selNotMine)
    }

    val uiState: StateFlow<FilterCodeUiState> = combine(
        baseDataFlow,
        calibrationFlow,
        _expandedSubjectIds,
        _isFetching,
        _itemsVersion
    ) { baseData, calState, expandedIds, isFetching, _ ->
        val mappedSubjectIds = baseData.mappings.mapNotNull { it.subjectId }.toSet()
        val mappedSubjects = baseData.subjects.filter { it.id in mappedSubjectIds }

        val currentExpandedIds: Set<String> = if (expandedIds == null) {
            val initial = if (!targetSubjectId.isNullOrBlank()) {
                setOf(targetSubjectId)
            } else {
                mappedSubjects.firstOrNull()?.let { setOf(it.id) } ?: emptySet()
            }
            _expandedSubjectIds.value = initial
            initial
        } else {
            expandedIds
        }

        val fetchedItems = syncEngine.getLastFetchedItems()
        val codesBySubject = baseData.filterCodes.groupBy { it.subjectId }
        val mappingsBySubject = baseData.mappings.filter { it.subjectId != null }.groupBy { it.subjectId!! }

        var totalUnresolved = 0

        val subjectSections = mappedSubjects.map { subject ->
            val itemsForSubject = fetchedItems.filter { it.mappedSubjectId == subject.id }
            val courseNames = mappingsBySubject[subject.id]?.map { it.moodleCourseName }.orEmpty()
            val codesForSubject = codesBySubject[subject.id].orEmpty()

            val unresolvedCount = MoodleItemFilter.countUnresolvedItems(itemsForSubject, codesForSubject)
            totalUnresolved += unresolvedCount

            val isCalibrating = calState.calibratingMap.containsKey(subject.id)
            val patterns = calState.calibratingMap[subject.id].orEmpty()

            // Compute proposal from current user labels in patterns
            val proposal = if (isCalibrating && patterns.any { it.userLabel != null }) {
                val labeledPairs = patterns
                    .filter { it.userLabel != null }
                    .map { it.tokens to it.userLabel!! }
                MoodleItemFilter.computeCalibrationProposals(labeledPairs)
            } else null

            val confirmedMine = calState.selMineMap[subject.id] ?: proposal?.proposedMineCodes?.toSet().orEmpty()
            val confirmedNotMine = calState.selNotMineMap[subject.id] ?: proposal?.proposedNotMineCodes?.toSet().orEmpty()

            SubjectFilterSectionUi(
                subject = subject,
                mappedCourseNames = courseNames,
                filterCodes = codesForSubject,
                unresolvedCount = unresolvedCount,
                totalFetchedCount = itemsForSubject.size,
                hasWarningBadge = unresolvedCount > 0,
                isExpanded = currentExpandedIds.contains(subject.id),
                isCalibrating = isCalibrating,
                calibrationPatterns = patterns,
                calibrationProposal = proposal,
                confirmedMineProposals = confirmedMine,
                confirmedNotMineProposals = confirmedNotMine
            )
        }

        FilterCodeUiState(
            isLoading = isFetching,
            subjects = subjectSections,
            focusedSubjectId = targetSubjectId,
            totalUnresolvedCount = totalUnresolved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterCodeUiState())

    init {
        viewModelScope.launch {
            if (syncEngine.getLastFetchedItems().isEmpty()) {
                _isFetching.value = true
                syncEngine.fetchPreviewItems()
                _isFetching.value = false
                _itemsVersion.value++
            }
        }
    }

    fun toggleSubjectExpanded(subjectId: String) {
        val current = _expandedSubjectIds.value ?: emptySet()
        _expandedSubjectIds.value = if (current.contains(subjectId)) {
            current - subjectId
        } else {
            current + subjectId
        }
    }

    /**
     * Initializes the labeled-example calibration flow:
     * Deduplicates fetched items for this subject down to unique token-sets (capped at ~15).
     */
    fun startCalibration(subjectId: String) {
        val fetchedItems = syncEngine.getLastFetchedItems().filter { it.mappedSubjectId == subjectId }
        val seenSignatures = mutableSetOf<String>()
        val patterns = mutableListOf<CalibrationPatternUi>()

        for (item in fetchedItems) {
            val tokens = MoodleItemFilter.tokenizeItem(item)
            if (tokens.isNotEmpty()) {
                val sig = MoodleItemFilter.computeSignature(tokens)
                if (sig !in seenSignatures) {
                    seenSignatures.add(sig)
                    patterns.add(
                        CalibrationPatternUi(
                            id = sig,
                            representativeTitle = item.title,
                            tokens = tokens,
                            userLabel = null
                        )
                    )
                    if (patterns.size >= 15) break
                }
            }
        }

        val updatedMap = _calibratingSubjects.value.toMutableMap()
        updatedMap[subjectId] = patterns
        _calibratingSubjects.value = updatedMap

        // Clear previous proposal selections
        val mineMap = _selectedMineProposals.value.toMutableMap()
        mineMap.remove(subjectId)
        _selectedMineProposals.value = mineMap

        val notMineMap = _selectedNotMineProposals.value.toMutableMap()
        notMineMap.remove(subjectId)
        _selectedNotMineProposals.value = notMineMap
    }

    fun cancelCalibration(subjectId: String) {
        val updatedMap = _calibratingSubjects.value.toMutableMap()
        updatedMap.remove(subjectId)
        _calibratingSubjects.value = updatedMap
    }

    fun setCalibrationPatternLabel(subjectId: String, patternId: String, isMine: Boolean) {
        val currentList = _calibratingSubjects.value[subjectId].orEmpty()
        val updated = currentList.map { p ->
            if (p.id == patternId) {
                // If clicked again on the same value, toggle off (null), otherwise set to isMine
                val newLabel = if (p.userLabel == isMine) null else isMine
                p.copy(userLabel = newLabel)
            } else {
                p
            }
        }
        val map = _calibratingSubjects.value.toMutableMap()
        map[subjectId] = updated
        _calibratingSubjects.value = map

        // Recompute default selections when labels change
        val labeledPairs = updated.filter { it.userLabel != null }.map { it.tokens to it.userLabel!! }
        val proposal = MoodleItemFilter.computeCalibrationProposals(labeledPairs)

        val mineMap = _selectedMineProposals.value.toMutableMap()
        mineMap[subjectId] = proposal.proposedMineCodes.toSet()
        _selectedMineProposals.value = mineMap

        val notMineMap = _selectedNotMineProposals.value.toMutableMap()
        notMineMap[subjectId] = proposal.proposedNotMineCodes.toSet()
        _selectedNotMineProposals.value = notMineMap
    }

    fun toggleMineProposal(subjectId: String, code: String) {
        val current = _selectedMineProposals.value[subjectId].orEmpty()
        val updated = if (current.contains(code)) current - code else current + code
        val map = _selectedMineProposals.value.toMutableMap()
        map[subjectId] = updated
        _selectedMineProposals.value = map
    }

    fun toggleNotMineProposal(subjectId: String, code: String) {
        val current = _selectedNotMineProposals.value[subjectId].orEmpty()
        val updated = if (current.contains(code)) current - code else current + code
        val map = _selectedNotMineProposals.value.toMutableMap()
        map[subjectId] = updated
        _selectedNotMineProposals.value = map
    }

    fun confirmAndSaveCalibration(subjectId: String) {
        val mineCodes = _selectedMineProposals.value[subjectId].orEmpty()
        val notMineCodes = _selectedNotMineProposals.value[subjectId].orEmpty()

        viewModelScope.launch {
            for (code in mineCodes) {
                filterCodeRepository.saveFilterCode(subjectId, code, isMine = true)
            }
            for (code in notMineCodes) {
                filterCodeRepository.saveFilterCode(subjectId, code, isMine = false)
            }
            cancelCalibration(subjectId)
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun addFilterCode(subjectId: String, code: String, isMine: Boolean) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            filterCodeRepository.saveFilterCode(subjectId, trimmed, isMine)
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun flipFilterCode(filterCode: FilterCode) {
        viewModelScope.launch {
            filterCodeRepository.saveFilterCode(
                subjectId = filterCode.subjectId,
                code = filterCode.code,
                isMine = !filterCode.isMine
            )
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun deleteFilterCode(filterCodeId: Long) {
        viewModelScope.launch {
            filterCodeRepository.deleteFilterCodeById(filterCodeId)
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            _isFetching.value = true
            syncEngine.fetchPreviewItems(forceRefreshCourseStructure = true)
            _isFetching.value = false
            _itemsVersion.value++
        }
    }
}

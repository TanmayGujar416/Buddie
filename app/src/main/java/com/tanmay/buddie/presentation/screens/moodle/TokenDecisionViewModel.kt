package com.tanmay.buddie.presentation.screens.moodle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TokenSignatureRepository
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter
import com.tanmay.buddie.domain.sync.MoodleTokenScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnresolvedSignatureUi(
    val signature: String,
    val representativeTitle: String,
    val itemCount: Int
)

data class DecidedSignatureUi(
    val id: Long,
    val signature: String,
    val representativeTitle: String,
    val decision: SignatureDecision,
    val itemCount: Int
)

data class SubjectDecisionSectionUi(
    val subject: SubjectEntity,
    val mappedCourseNames: List<String>,
    val unresolvedSignatures: List<UnresolvedSignatureUi>,
    val decidedSignatures: List<DecidedSignatureUi>,
    val isExpanded: Boolean
)

data class TokenDecisionUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0 = Needs Review, 1 = Decided
    val subjects: List<SubjectDecisionSectionUi> = emptyList(),
    val focusedSubjectId: String? = null,
    val totalNeedsReviewCount: Int = 0
)

@HiltViewModel
class TokenDecisionViewModel @Inject constructor(
    private val tokenSignatureRepository: TokenSignatureRepository,
    private val subjectRepository: SubjectRepository,
    private val courseMappingRepository: MoodleCourseMappingRepository,
    private val syncEngine: MoodleContentSyncEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val targetSubjectId: String? = savedStateHandle["subjectId"]

    private val _expandedSubjectIds = MutableStateFlow<Set<String>?>(null)
    private val _selectedTab = MutableStateFlow(0)
    private val _isFetching = MutableStateFlow(false)
    private val _itemsVersion = MutableStateFlow(0)

    private val baseDataFlow = combine(
        subjectRepository.getAllSubjects(),
        courseMappingRepository.getAllMappings(),
        tokenSignatureRepository.getAllDecisions()
    ) { subjects, mappings, decisions ->
        Triple(subjects, mappings, decisions)
    }

    val uiState: StateFlow<TokenDecisionUiState> = combine(
        baseDataFlow,
        _expandedSubjectIds,
        _selectedTab,
        _isFetching,
        _itemsVersion
    ) { (subjects, mappings, decisions), expandedIds, selectedTab, isFetching, _ ->
        val mappedSubjectIds = mappings.mapNotNull { it.subjectId }.toSet()
        val mappedSubjects = subjects.filter { it.id in mappedSubjectIds }

        // Initialize expanded subject once
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
        val decisionsBySubject = decisions.groupBy { it.subjectId }
        val mappingsBySubject = mappings.filter { it.subjectId != null }.groupBy { it.subjectId!! }

        var totalUnresolved = 0

        val subjectSections = mappedSubjects.map { subject ->
            val itemsForSubject = fetchedItems.filter { it.mappedSubjectId == subject.id }
            val courseNames = mappingsBySubject[subject.id]?.map { it.moodleCourseName }.orEmpty()

            // Group fetched items by signature
            val itemsBySignature = mutableMapOf<String, MutableList<FetchedMoodleItem>>()
            for (item in itemsForSubject) {
                val tokens = MoodleTokenScanner.extractTokensForItem(item)
                if (tokens.isNotEmpty()) {
                    val sig = MoodleItemFilter.computeSignature(tokens)
                    itemsBySignature.getOrPut(sig) { mutableListOf() }.add(item)
                }
            }

            val subjectDecisions = decisionsBySubject[subject.id].orEmpty()
            val decisionMap = subjectDecisions.associateBy { it.signature }

            // 1. Unresolved: detected signatures with NO decision in Room
            val unresolved = itemsBySignature
                .filter { (sig, _) -> !decisionMap.containsKey(sig) }
                .map { (sig, itemList) ->
                    UnresolvedSignatureUi(
                        signature = sig,
                        representativeTitle = itemList.first().title,
                        itemCount = itemList.size
                    )
                }
                .sortedByDescending { it.itemCount }

            totalUnresolved += unresolved.size

            // 2. Decided: all signatures with a decision in Room for this subject
            val decided = subjectDecisions.map { d ->
                val matchingItems = itemsBySignature[d.signature].orEmpty()
                val repTitle = matchingItems.firstOrNull()?.title ?: d.signature
                DecidedSignatureUi(
                    id = d.id,
                    signature = d.signature,
                    representativeTitle = repTitle,
                    decision = d.decision,
                    itemCount = matchingItems.size
                )
            }.sortedBy { it.signature }

            SubjectDecisionSectionUi(
                subject = subject,
                mappedCourseNames = courseNames,
                unresolvedSignatures = unresolved,
                decidedSignatures = decided,
                isExpanded = currentExpandedIds.contains(subject.id)
            )
        }

        TokenDecisionUiState(
            isLoading = isFetching,
            selectedTab = selectedTab,
            subjects = subjectSections,
            focusedSubjectId = targetSubjectId,
            totalNeedsReviewCount = totalUnresolved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TokenDecisionUiState())

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

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleSubjectExpanded(subjectId: String) {
        val current = _expandedSubjectIds.value ?: emptySet()
        _expandedSubjectIds.value = if (current.contains(subjectId)) {
            current - subjectId
        } else {
            current + subjectId
        }
    }

    fun makeDecision(subjectId: String, signature: String, decision: SignatureDecision) {
        viewModelScope.launch {
            tokenSignatureRepository.saveDecision(
                subjectId = subjectId,
                signature = signature,
                decision = decision
            )
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun flipDecision(decidedItem: DecidedSignatureUi, subjectId: String) {
        viewModelScope.launch {
            val newDecision = if (decidedItem.decision == SignatureDecision.INCLUDE) {
                SignatureDecision.EXCLUDE
            } else {
                SignatureDecision.INCLUDE
            }
            tokenSignatureRepository.saveDecision(
                subjectId = subjectId,
                signature = decidedItem.signature,
                decision = newDecision
            )
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    fun resetDecision(decisionId: Long) {
        viewModelScope.launch {
            tokenSignatureRepository.deleteDecisionById(decisionId)
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

package com.tanmay.buddie.presentation.screens.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import javax.inject.Inject

data class SlotWithSubject(
    val slot: TimetableSlotEntity,
    val subject: SubjectEntity?
)

data class TimetableUiState(
    val slots: List<SlotWithSubject> = emptyList(),
    val activeDays: List<DayOfWeek> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val uiState: StateFlow<TimetableUiState> = combine(
        timetableRepository.getAllSlots(),
        subjectRepository.getAllSubjects()
    ) { slots, subjects ->
        val days = DayOfWeek.entries

        val enrichedSlots = slots.map { slot ->
            SlotWithSubject(
                slot = slot,
                subject = subjects.find { it.id == slot.subjectId }
            )
        }

        TimetableUiState(
            slots = enrichedSlots,
            activeDays = days,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimetableUiState())
}

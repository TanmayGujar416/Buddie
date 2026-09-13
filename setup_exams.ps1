$ErrorActionPreference = 'Stop'
$baseDir = "d:\Buddie"
Set-Location $baseDir

# ExamRepository.kt
@"
package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun getAllExams(): Flow<List<ExamEntity>>
    suspend fun getExamById(id: String): ExamEntity?
    suspend fun insertExam(exam: ExamEntity)
    suspend fun updateExam(exam: ExamEntity)
    suspend fun deleteExam(exam: ExamEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/domain/repository/ExamRepository.kt" -Encoding UTF8

# ExamRepositoryImpl.kt
@"
package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.ExamDao
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val dao: ExamDao
) : ExamRepository {
    override fun getAllExams(): Flow<List<ExamEntity>> = dao.getAllExams()
    override suspend fun getExamById(id: String): ExamEntity? = dao.getExamById(id)
    override suspend fun insertExam(exam: ExamEntity) = dao.insertExam(exam)
    override suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    override suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/repository/ExamRepositoryImpl.kt" -Encoding UTF8

# ExamsViewModel.kt
@"
package com.tanmay.buddie.presentation.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamWithSubject(
    val exam: ExamEntity,
    val subject: SubjectEntity?
)

data class ExamsUiState(
    val exams: List<ExamWithSubject> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val uiState: StateFlow<ExamsUiState> = combine(
        examRepository.getAllExams(),
        subjectRepository.getAllSubjects()
    ) { exams, subjects ->
        val mapped = exams
            .filter { it.datetime.isAfter(java.time.LocalDateTime.now().minusDays(1)) } // Filter out old exams
            .map { exam ->
                ExamWithSubject(
                    exam = exam,
                    subject = subjects.find { it.id == exam.subjectId }
                )
            }
            .sortedBy { it.exam.datetime } // Closest first
            
        ExamsUiState(
            exams = mapped,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExamsUiState())

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            examRepository.deleteExam(exam)
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/exams/ExamsViewModel.kt" -Encoding UTF8

# ExamsScreen.kt
@"
package com.tanmay.buddie.presentation.screens.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.ExamEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    viewModel: ExamsViewModel = hiltViewModel(),
    onAddExamClick: () -> Unit,
    onEditExamClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exams") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExamClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { padding ->
        if (uiState.exams.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (uiState.isLoading) "Loading..." else "No upcoming exams! Relax.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.exams, key = { it.exam.id }) { item ->
                    ExamCard(
                        item = item,
                        onClick = { onEditExamClick(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCard(
    item: ExamWithSubject,
    onClick: (ExamEntity) -> Unit
) {
    val exam = item.exam
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e:Exception) { Color.Gray }
    
    val daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), exam.datetime)
    val isUrgent = daysUntil in 0..3

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(exam) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUrgent) 6.dp else 2.dp)
    ) {
        Column {
            // Prominent Countdown Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUrgent) MaterialTheme.colorScheme.errorContainer else color.copy(alpha = 0.2f))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject?.name ?: "Unknown Subject",
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                    
                    val countdownText = when {
                        daysUntil < 0 -> "Done"
                        daysUntil == 0L -> "TODAY"
                        daysUntil == 1L -> "Tomorrow"
                        else -> "In ${"$"}{daysUntil} days"
                    }
                    
                    Text(
                        text = countdownText,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Exam Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = exam.datetime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"$"}{exam.durationMinutes} mins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location Details
                val location = exam.roomOverride ?: exam.hall
                if (location.isNotBlank()) {
                    Text(
                        text = "Location: ${"$"}{location}${if (!exam.seatNumber.isNullOrBlank()) " • Seat: ${"$"}{exam.seatNumber}" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (!exam.syllabusScope.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Syllabus: ${"$"}{exam.syllabusScope}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/exams/ExamsScreen.kt" -Encoding UTF8

Write-Host "Exams Screen Created"

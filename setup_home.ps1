$ErrorActionPreference = 'Stop'
$baseDir = "d:\Buddie"
Set-Location $baseDir

# SubmissionRepository.kt
@"
package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>
    suspend fun getSubmissionById(id: String): SubmissionEntity?
    suspend fun insertSubmission(submission: SubmissionEntity)
    suspend fun updateSubmission(submission: SubmissionEntity)
    suspend fun deleteSubmission(submission: SubmissionEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/domain/repository/SubmissionRepository.kt" -Encoding UTF8

# SubmissionRepositoryImpl.kt
@"
package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.SubmissionDao
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmissionRepositoryImpl @Inject constructor(
    private val dao: SubmissionDao
) : SubmissionRepository {
    override fun getAllSubmissions(): Flow<List<SubmissionEntity>> = dao.getAllSubmissions()
    override suspend fun getSubmissionById(id: String): SubmissionEntity? = dao.getSubmissionById(id)
    override suspend fun insertSubmission(submission: SubmissionEntity) = dao.insertSubmission(submission)
    override suspend fun updateSubmission(submission: SubmissionEntity) = dao.updateSubmission(submission)
    override suspend fun deleteSubmission(submission: SubmissionEntity) = dao.deleteSubmission(submission)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/repository/SubmissionRepositoryImpl.kt" -Encoding UTF8

# HomeViewModel.kt
@"
package com.tanmay.buddie.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val submissions: List<SubmissionWithSubject> = emptyList(),
    val isLoading: Boolean = true
)

data class SubmissionWithSubject(
    val submission: SubmissionEntity,
    val subject: SubjectEntity?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        submissionRepository.getAllSubmissions(),
        subjectRepository.getAllSubjects()
    ) { submissions, subjects ->
        // Map submissions to include their subject details
        val mapped = submissions
            .filter { it.status != SubmissionStatus.DONE } // Hide completed in MVP home
            .map { sub ->
                SubmissionWithSubject(
                    submission = sub,
                    subject = subjects.find { it.id == sub.subjectId }
                )
            }
            .sortedBy { it.submission.deadline } // Most urgent first
            
        HomeUiState(
            submissions = mapped,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
    
    fun markAsDone(submission: SubmissionEntity) {
        viewModelScope.launch {
            submissionRepository.updateSubmission(
                submission.copy(status = SubmissionStatus.DONE)
            )
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/home/HomeViewModel.kt" -Encoding UTF8

# HomeScreen.kt
@"
package com.tanmay.buddie.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddSubmissionClick: () -> Unit,
    onEditSubmissionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Deadlines") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubmissionClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { padding ->
        if (uiState.submissions.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (uiState.isLoading) "Loading..." else "No upcoming assignments! 🎉", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.submissions, key = { it.submission.id }) { item ->
                    SubmissionCard(
                        item = item,
                        onMarkDone = { viewModel.markAsDone(it) },
                        onClick = { onEditSubmissionClick(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(
    item: SubmissionWithSubject,
    onMarkDone: (SubmissionEntity) -> Unit,
    onClick: (SubmissionEntity) -> Unit
) {
    val sub = item.submission
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e:Exception) { Color.Gray }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(sub) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject color indicator
            Box(
                modifier = Modifier.size(16.dp).clip(CircleShape).background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${subject?.name ?: "Unknown Subject"} • ${sub.deadline.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Room info
                val roomDisplay = sub.roomOverride ?: "Auto-lookup room" // MVP simplified fallback message
                if (roomDisplay.isNotBlank()) {
                    Text(
                        text = "Room: $roomDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Mark Done button
            IconButton(onClick = { onMarkDone(sub) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Mark as Done",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/home/HomeScreen.kt" -Encoding UTF8

Write-Host "Home Screen Created"

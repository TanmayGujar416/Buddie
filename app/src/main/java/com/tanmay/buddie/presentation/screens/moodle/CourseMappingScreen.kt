package com.tanmay.buddie.presentation.screens.moodle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMappingScreen(
    onNavigateBack: () -> Unit,
    viewModel: CourseMappingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Map Your Courses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Connect Moodle courses to Buddie subjects",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            text = "Done",
                            color = AccentBlueLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.courses.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Fetching enrolled courses from Moodle...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                uiState.errorMessage != null && uiState.courses.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Failed to load courses",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusError,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadCourses() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                uiState.courses.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Enrolled Courses Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We couldn't find any active course enrollments for your Moodle account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.loadCourses() },
                            border = BorderStroke(1.dp, BorderOutline)
                        ) {
                            Text("Refresh Courses", color = AccentBlueLight)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        item {
                            // Guidance Info Banner
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                                border = BorderStroke(1.dp, BorderOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AccentBlueLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Flexible Subject Mapping",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Multiple Moodle courses (e.g. Theory and Lab) can be mapped to the same subject. Mark non-academic courses as 'Ignore' to skip them.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(uiState.courses, key = { it.courseId }) { courseItem ->
                            CourseMappingCard(
                                courseItem = courseItem,
                                subjects = uiState.subjects,
                                onSelectSubject = { subjectId ->
                                    viewModel.mapCourseToSubject(courseItem.courseId, courseItem.courseName, subjectId)
                                },
                                onIgnoreCourse = {
                                    viewModel.ignoreCourse(courseItem.courseId, courseItem.courseName)
                                },
                                onUnmapCourse = {
                                    viewModel.unmapCourse(courseItem.courseId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseMappingCard(
    courseItem: MoodleCourseMappingUiItem,
    subjects: List<SubjectEntity>,
    onSelectSubject: (String) -> Unit,
    onIgnoreCourse: () -> Unit,
    onUnmapCourse: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val mappedSubject = subjects.find { it.id == courseItem.mappedSubjectId }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(
            1.dp,
            when {
                courseItem.mappedSubjectId != null -> AccentBlue.copy(alpha = 0.5f)
                courseItem.isIgnored -> BorderOutline
                else -> StatusWarning.copy(alpha = 0.35f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Course Full Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = courseItem.courseName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                if (courseItem.shortName.isNotBlank() && courseItem.shortName != courseItem.courseName) {
                    Text(
                        text = courseItem.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Current Mapping Status Row & Dropdown Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                courseItem.mappedSubjectId != null -> AccentBlue.copy(alpha = 0.15f)
                                courseItem.isIgnored -> CharcoalSurfaceHighlight
                                else -> StatusWarning.copy(alpha = 0.12f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (courseItem.mappedSubjectId != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Text(
                                text = "Mapped: ${mappedSubject?.name ?: "Subject"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentBlueLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (courseItem.isIgnored) {
                            Text(
                                text = "Ignored",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Not mapped",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatusWarning,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Dropdown Action
                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderOutline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CharcoalSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                courseItem.mappedSubjectId != null -> "Change"
                                courseItem.isIgnored -> "Unignore"
                                else -> "Map to..."
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .background(CharcoalSurfaceElevated)
                            .widthIn(min = 200.dp, max = 280.dp)
                    ) {
                        // Section Header: Subjects
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "SELECT APP SUBJECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            },
                            onClick = {},
                            enabled = false
                        )

                        if (subjects.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "No subjects found. Create subjects in Timetable first.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                },
                                onClick = {}
                            )
                        } else {
                            subjects.forEach { subject ->
                                val isSelected = subject.id == courseItem.mappedSubjectId
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = subject.name,
                                                color = if (isSelected) AccentBlueLight else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.5.sp
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = AccentBlueLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectSubject(subject.id)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }

                        Divider(color = DividerFaint)

                        // Option: Ignore Course
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚫 Ignore this course",
                                        color = if (courseItem.isIgnored) StatusError else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (courseItem.isIgnored) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (courseItem.isIgnored) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Ignored",
                                            tint = StatusError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onIgnoreCourse()
                                dropdownExpanded = false
                            }
                        )

                        // Option: Clear / Unmap (if mapped or ignored)
                        if (courseItem.mappedSubjectId != null || courseItem.isIgnored) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Clear mapping",
                                        color = TextMuted,
                                        fontSize = 12.5.sp
                                    )
                                },
                                onClick = {
                                    onUnmapCourse()
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

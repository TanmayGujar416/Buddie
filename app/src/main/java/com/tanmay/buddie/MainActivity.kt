package com.tanmay.buddie

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import com.tanmay.buddie.presentation.screens.exams.ExamsScreen
import com.tanmay.buddie.presentation.screens.forms.AddEditExamScreen
import com.tanmay.buddie.presentation.screens.forms.AddEditSubmissionScreen
import com.tanmay.buddie.presentation.screens.home.HomeScreen
import com.tanmay.buddie.presentation.screens.onboarding.TimetableSetupScreen
import com.tanmay.buddie.presentation.screens.settings.SettingsScreen
import com.tanmay.buddie.presentation.screens.timetable.TimetableScreen
import com.tanmay.buddie.presentation.theme.AccentBlue
import com.tanmay.buddie.presentation.theme.AccentBlueLight
import com.tanmay.buddie.presentation.theme.BuddieTheme
import com.tanmay.buddie.presentation.theme.CharcoalBackground
import com.tanmay.buddie.presentation.theme.TextPrimary
import com.tanmay.buddie.presentation.theme.TextSecondary
import com.tanmay.buddie.service.BubbleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TARGET_TAB = "extra_target_tab"
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var subjectRepository: SubjectRepository

    @Inject
    lateinit var timetableRepository: TimetableRepository

    private val targetTabFlow = MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val target = intent.getStringExtra(EXTRA_TARGET_TAB)
        if (target != null) {
            targetTabFlow.value = target
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialTarget = intent?.getStringExtra(EXTRA_TARGET_TAB)
        if (initialTarget != null) {
            targetTabFlow.value = initialTarget
        }

        // Auto-migration: if existing user already has subjects or timetable slots, mark onboarding as completed
        lifecycleScope.launch {
            try {
                val completed = userPreferences.onboardingCompletedFlow.first()
                if (!completed) {
                    val hasSlots = timetableRepository.getAllSlots().first().isNotEmpty()
                    val hasSubjects = subjectRepository.getAllSubjects().first().isNotEmpty()
                    if (hasSlots || hasSubjects) {
                        userPreferences.setOnboardingCompleted(true)
                    }
                }
            } catch (_: Exception) {}
        }

        // Ensure BubbleService starts on launch if enabled and overlay permission is granted
        lifecycleScope.launch {
            userPreferences.floatingBubbleEnabledFlow.collect { enabled ->
                if (enabled && Settings.canDrawOverlays(this@MainActivity)) {
                    BubbleService.start(this@MainActivity)
                }
            }
        }

        setContent {
            BuddieTheme {
                val isOnboardingCompleted by userPreferences.onboardingCompletedFlow.collectAsState(initial = null)

                if (isOnboardingCompleted == null) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = CharcoalBackground
                    ) {}
                    return@BuddieTheme
                }

                val startDestination = if (isOnboardingCompleted == true) "home" else "onboarding"
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                fun navigateToTab(route: String) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                val targetTab by targetTabFlow.collectAsState()
                LaunchedEffect(targetTab) {
                    val target = targetTab ?: return@LaunchedEffect
                    if (target in listOf("home", "timetable", "exams", "settings")) {
                        navigateToTab(target)
                        targetTabFlow.value = null
                    }
                }

                Scaffold(
                    containerColor = CharcoalBackground,
                    bottomBar = {
                        val hideBottomBar = currentRoute == "onboarding" || currentRoute?.startsWith("add_edit") == true || currentRoute == "connect_moodle" || currentRoute == "map_courses" || currentRoute == "sync_preview" || currentRoute?.startsWith("filter_codes") == true
                        if (!hideBottomBar) {
                            NavigationBar(
                                containerColor = CharcoalBackground,
                                contentColor = TextSecondary
                            ) {
                                val navItemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextPrimary,
                                    selectedTextColor = AccentBlueLight,
                                    indicatorColor = AccentBlue.copy(alpha = 0.35f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )

                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentRoute == "home",
                                    onClick = { navigateToTab("home") },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Timetable") },
                                    label = { Text("Timetable") },
                                    selected = currentRoute == "timetable",
                                    onClick = { navigateToTab("timetable") },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Edit, contentDescription = "Exams") },
                                    label = { Text("Exams") },
                                    selected = currentRoute == "exams",
                                    onClick = { navigateToTab("exams") },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    selected = currentRoute == "settings",
                                    onClick = { navigateToTab("settings") },
                                    colors = navItemColors
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        color = CharcoalBackground
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            enterTransition = {
                                fadeIn(animationSpec = tween(240)) + slideInHorizontally(
                                    initialOffsetX = { 36 },
                                    animationSpec = tween(240)
                                )
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(180))
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(240)) + slideInHorizontally(
                                    initialOffsetX = { -36 },
                                    animationSpec = tween(240)
                                )
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(180))
                            }
                        ) {
                            composable("onboarding") {
                                TimetableSetupScreen(
                                    onSetupComplete = {
                                        lifecycleScope.launch {
                                            userPreferences.setOnboardingCompleted(true)
                                        }
                                        if (navController.previousBackStackEntry != null) {
                                            navController.popBackStack()
                                        } else {
                                            navController.navigate("home") {
                                                popUpTo("onboarding") { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }
                            composable("home") {
                                HomeScreen(
                                    onAddSubmissionClick = { navController.navigate("add_edit_submission") },
                                    onEditSubmissionClick = { id -> navController.navigate("add_edit_submission?submissionId=$id") }
                                )
                            }
                            composable("timetable") {
                                TimetableScreen()
                            }
                            composable("exams") {
                                ExamsScreen(
                                    onAddExamClick = { navController.navigate("add_edit_exam") },
                                    onEditExamClick = { id -> navController.navigate("add_edit_exam?examId=$id") }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onEditTimetableClick = { navController.navigate("onboarding") },
                                    onConnectMoodleClick = { navController.navigate("connect_moodle") },
                                    onMapCoursesClick = { navController.navigate("map_courses") },
                                    onFilterCodesClick = { navController.navigate("filter_codes") },
                                    onSyncPreviewClick = { navController.navigate("sync_preview") }
                                )
                            }
                            composable(
                                "connect_moodle",
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                com.tanmay.buddie.presentation.screens.moodle.ConnectMoodleScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("settings")
                                        }
                                    },
                                    onConnectSuccess = {
                                        navController.navigate("map_courses") {
                                            popUpTo("connect_moodle") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(
                                "map_courses",
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                com.tanmay.buddie.presentation.screens.moodle.CourseMappingScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("settings")
                                        }
                                    }
                                )
                            }
                            composable(
                                "sync_preview",
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                com.tanmay.buddie.presentation.screens.moodle.SyncPreviewScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("settings")
                                        }
                                    }
                                )
                            }
                            composable(
                                "filter_codes?subjectId={subjectId}",
                                arguments = listOf(androidx.navigation.navArgument("subjectId") {
                                    nullable = true
                                    defaultValue = null
                                }),
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                com.tanmay.buddie.presentation.screens.moodle.FilterCodeScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("settings")
                                        }
                                    }
                                )
                            }
                            composable(
                                "add_edit_submission?submissionId={submissionId}",
                                arguments = listOf(androidx.navigation.navArgument("submissionId") { nullable = true }),
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                AddEditSubmissionScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("home")
                                        }
                                    }
                                )
                            }
                            composable(
                                "add_edit_exam?examId={examId}",
                                arguments = listOf(androidx.navigation.navArgument("examId") { nullable = true }),
                                enterTransition = {
                                    slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(240)) + fadeIn(tween(240))
                                },
                                exitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                },
                                popEnterTransition = {
                                    fadeIn(tween(200))
                                },
                                popExitTransition = {
                                    slideOutVertically(targetOffsetY = { 80 }, animationSpec = tween(180)) + fadeOut(tween(180))
                                }
                            ) {
                                AddEditExamScreen(
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navigateToTab("exams")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

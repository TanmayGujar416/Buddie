package com.tanmay.buddie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tanmay.buddie.presentation.screens.onboarding.TimetableSetupScreen
import com.tanmay.buddie.presentation.theme.BuddieTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.NavigationBarItemDefaults
import com.tanmay.buddie.presentation.theme.CharcoalBackground
import com.tanmay.buddie.presentation.theme.AccentBlue
import com.tanmay.buddie.presentation.theme.AccentBlueLight
import com.tanmay.buddie.presentation.theme.TextPrimary
import com.tanmay.buddie.presentation.theme.TextSecondary

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuddieTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    containerColor = CharcoalBackground,
                    bottomBar = {
                        val hideBottomBar = currentRoute == "onboarding" || currentRoute?.startsWith("add_edit") == true
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
                                    label = { androidx.compose.material3.Text("Home") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") { launchSingleTop = true } },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Timetable") },
                                    label = { androidx.compose.material3.Text("Timetable") },
                                    selected = currentRoute == "timetable",
                                    onClick = { navController.navigate("timetable") { launchSingleTop = true } },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Edit, contentDescription = "Exams") },
                                    label = { androidx.compose.material3.Text("Exams") },
                                    selected = currentRoute == "exams",
                                    onClick = { navController.navigate("exams") { launchSingleTop = true } },
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { androidx.compose.material3.Text("Settings") },
                                    selected = currentRoute == "settings",
                                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
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
                            startDestination = "onboarding",
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
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("home") {
                                com.tanmay.buddie.presentation.screens.home.HomeScreen(
                                    onAddSubmissionClick = { navController.navigate("add_edit_submission") },
                                    onEditSubmissionClick = { id -> navController.navigate("add_edit_submission?submissionId=$id") }
                                )
                            }
                            composable("timetable") {
                                com.tanmay.buddie.presentation.screens.timetable.TimetableScreen()
                            }
                            composable("exams") {
                                com.tanmay.buddie.presentation.screens.exams.ExamsScreen(
                                    onAddExamClick = { navController.navigate("add_edit_exam") },
                                    onEditExamClick = { id -> navController.navigate("add_edit_exam?examId=$id") }
                                )
                            }
                            composable("settings") {
                                com.tanmay.buddie.presentation.screens.settings.SettingsScreen(
                                    onEditTimetableClick = { navController.navigate("onboarding") }
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
                                com.tanmay.buddie.presentation.screens.forms.AddEditSubmissionScreen(
                                    onNavigateBack = { navController.popBackStack() }
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
                                com.tanmay.buddie.presentation.screens.forms.AddEditExamScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.tanmay.buddie.presentation.components.mascot.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.screens.exams.ExamsViewModel
import com.tanmay.buddie.presentation.screens.home.HomeViewModel
import com.tanmay.buddie.presentation.screens.timetable.TimetableViewModel

@Composable
fun MascotCardContainer(
    cardStyle: String,
    mood: MascotMood,
    homeViewModel: HomeViewModel,
    timetableViewModel: TimetableViewModel,
    examsViewModel: ExamsViewModel,
    onCollapse: () -> Unit,
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    companionType: CompanionType = CompanionType.BUDDIE,
    animateIdle: Boolean = true
) {
    if (cardStyle == "COMPANION_HEADER") {
        CompanionHeaderShell(
            mood = mood,
            homeViewModel = homeViewModel,
            timetableViewModel = timetableViewModel,
            examsViewModel = examsViewModel,
            onCollapse = onCollapse,
            onOpenApp = onOpenApp,
            modifier = modifier,
            companionType = companionType,
            animateIdle = animateIdle
        )
    } else {
        SpeechBubbleShell(
            mood = mood,
            homeViewModel = homeViewModel,
            timetableViewModel = timetableViewModel,
            examsViewModel = examsViewModel,
            onCollapse = onCollapse,
            onOpenApp = onOpenApp,
            modifier = modifier,
            companionType = companionType,
            animateIdle = animateIdle
        )
    }
}

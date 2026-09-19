package com.tanmay.buddie

import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.screens.exams.ExamsUiState
import com.tanmay.buddie.presentation.screens.home.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionUiStateTest {

    @Test
    fun testCompanionTypeCatAlias() {
        assertEquals(CompanionType.MOMO, CompanionType.CAT)
    }

    @Test
    fun testCompanionTypeCloudAndGhostAliases() {
        assertEquals(CompanionType.WISP, CompanionType.CLOUD)
        assertEquals(CompanionType.WISP, CompanionType.GHOST)
    }

    @Test
    fun testHomeUiState_defaultCompanionIsBuddie() {
        val state = HomeUiState()
        assertEquals(CompanionType.BUDDIE, state.companionType)
    }

    @Test
    fun testHomeUiState_customCompanion() {
        val state = HomeUiState(companionType = CompanionType.MOMO)
        assertEquals(CompanionType.MOMO, state.companionType)
    }

    @Test
    fun testExamsUiState_defaultCompanionIsBuddie() {
        val state = ExamsUiState()
        assertEquals(CompanionType.BUDDIE, state.companionType)
    }

    @Test
    fun testExamsUiState_customCompanion() {
        val state = ExamsUiState(companionType = CompanionType.WISP)
        assertEquals(CompanionType.WISP, state.companionType)
    }
}

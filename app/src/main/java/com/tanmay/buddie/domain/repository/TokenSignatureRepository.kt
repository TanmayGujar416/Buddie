package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import kotlinx.coroutines.flow.Flow

interface TokenSignatureRepository {
    fun getDecisionsForSubject(subjectId: String): Flow<List<TokenSignatureDecision>>
    fun getAllDecisions(): Flow<List<TokenSignatureDecision>>
    suspend fun getAllDecisionsSync(): List<TokenSignatureDecision>
    suspend fun getDecision(subjectId: String, signature: String): TokenSignatureDecision?
    suspend fun saveDecision(subjectId: String, signature: String, decision: SignatureDecision): Long
    suspend fun deleteDecision(subjectId: String, signature: String): Int
    suspend fun deleteDecisionById(id: Long): Int
    suspend fun deleteAllForSubject(subjectId: String): Int
}

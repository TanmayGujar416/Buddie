package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import com.tanmay.buddie.domain.repository.TokenSignatureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenSignatureRepositoryImpl @Inject constructor(
    private val dao: TokenSignatureDecisionDao
) : TokenSignatureRepository {

    override fun getDecisionsForSubject(subjectId: String): Flow<List<TokenSignatureDecision>> =
        dao.getDecisionsForSubject(subjectId)

    override fun getAllDecisions(): Flow<List<TokenSignatureDecision>> =
        dao.getAllDecisions()

    override suspend fun getAllDecisionsSync(): List<TokenSignatureDecision> =
        dao.getAllDecisionsSync()

    override suspend fun getDecision(subjectId: String, signature: String): TokenSignatureDecision? =
        dao.getDecision(subjectId, signature)

    override suspend fun saveDecision(subjectId: String, signature: String, decision: SignatureDecision): Long {
        val entity = TokenSignatureDecision(
            subjectId = subjectId,
            signature = signature,
            decision = decision
        )
        return dao.saveDecision(entity)
    }

    override suspend fun deleteDecision(subjectId: String, signature: String): Int =
        dao.deleteDecision(subjectId, signature)

    override suspend fun deleteDecisionById(id: Long): Int =
        dao.deleteDecisionById(id)

    override suspend fun deleteAllForSubject(subjectId: String): Int =
        dao.deleteAllForSubject(subjectId)
}

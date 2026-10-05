package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao
import com.tanmay.buddie.data.local.entity.FilterCode
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import com.tanmay.buddie.domain.repository.FilterCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FilterCodeRepositoryImpl @Inject constructor(
    private val dao: TokenSignatureDecisionDao
) : FilterCodeRepository {

    override fun getFilterCodesForSubject(subjectId: String): Flow<List<FilterCode>> =
        dao.getDecisionsForSubject(subjectId).map { list ->
            list.map { it.toFilterCode() }
        }

    override fun getAllFilterCodes(): Flow<List<FilterCode>> =
        dao.getAllDecisions().map { list ->
            list.map { it.toFilterCode() }
        }

    override suspend fun getAllFilterCodesSync(): List<FilterCode> =
        dao.getAllDecisionsSync().map { it.toFilterCode() }

    override suspend fun getFilterCode(subjectId: String, code: String): FilterCode? =
        dao.getDecision(subjectId, code.trim().uppercase())?.toFilterCode()

    override suspend fun saveFilterCode(subjectId: String, code: String, isMine: Boolean): Long {
        val entity = TokenSignatureDecision(
            subjectId = subjectId,
            signature = code.trim().uppercase(),
            decision = if (isMine) SignatureDecision.INCLUDE else SignatureDecision.EXCLUDE
        )
        return dao.saveDecision(entity)
    }

    override suspend fun deleteFilterCode(subjectId: String, code: String): Int =
        dao.deleteDecision(subjectId, code.trim().uppercase())

    override suspend fun deleteFilterCodeById(id: Long): Int =
        dao.deleteDecisionById(id)

    override suspend fun deleteAllForSubject(subjectId: String): Int =
        dao.deleteAllForSubject(subjectId)
}

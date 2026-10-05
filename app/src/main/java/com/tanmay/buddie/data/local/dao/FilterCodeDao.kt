package com.tanmay.buddie.data.local.dao

import com.tanmay.buddie.data.local.entity.FilterCode
import kotlinx.coroutines.flow.Flow

/**
 * Data access abstraction for FilterCodes.
 * Can be used alongside or mapped from TokenSignatureDecisionDao.
 */
interface FilterCodeDao {
    fun getFilterCodesForSubject(subjectId: String): Flow<List<FilterCode>>
    fun getAllFilterCodes(): Flow<List<FilterCode>>
    suspend fun getAllFilterCodesSync(): List<FilterCode>
    suspend fun getFilterCode(subjectId: String, code: String): FilterCode?
    suspend fun saveFilterCode(filterCode: FilterCode): Long
    suspend fun deleteFilterCode(subjectId: String, code: String): Int
    suspend fun deleteFilterCodeById(id: Long): Int
    suspend fun deleteAllForSubject(subjectId: String): Int
}

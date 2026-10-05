package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.FilterCode
import kotlinx.coroutines.flow.Flow

interface FilterCodeRepository {
    fun getFilterCodesForSubject(subjectId: String): Flow<List<FilterCode>>
    fun getAllFilterCodes(): Flow<List<FilterCode>>
    suspend fun getAllFilterCodesSync(): List<FilterCode>
    suspend fun getFilterCode(subjectId: String, code: String): FilterCode?
    suspend fun saveFilterCode(subjectId: String, code: String, isMine: Boolean): Long
    suspend fun deleteFilterCode(subjectId: String, code: String): Int
    suspend fun deleteFilterCodeById(id: Long): Int
    suspend fun deleteAllForSubject(subjectId: String): Int
}

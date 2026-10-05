package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenSignatureDecisionDao {

    @Query("SELECT * FROM token_signature_decisions WHERE subjectId = :subjectId ORDER BY signature ASC")
    fun getDecisionsForSubject(subjectId: String): Flow<List<TokenSignatureDecision>>

    @Query("SELECT * FROM token_signature_decisions ORDER BY subjectId ASC, signature ASC")
    fun getAllDecisions(): Flow<List<TokenSignatureDecision>>

    @Query("SELECT * FROM token_signature_decisions")
    suspend fun getAllDecisionsSync(): List<TokenSignatureDecision>

    @Query("SELECT * FROM token_signature_decisions WHERE subjectId = :subjectId AND signature = :signature LIMIT 1")
    suspend fun getDecision(subjectId: String, signature: String): TokenSignatureDecision?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDecision(decision: TokenSignatureDecision): Long

    @Query("DELETE FROM token_signature_decisions WHERE subjectId = :subjectId AND signature = :signature")
    suspend fun deleteDecision(subjectId: String, signature: String): Int

    @Query("DELETE FROM token_signature_decisions WHERE id = :id")
    suspend fun deleteDecisionById(id: Long): Int

    @Query("DELETE FROM token_signature_decisions WHERE subjectId = :subjectId")
    suspend fun deleteAllForSubject(subjectId: String): Int
}

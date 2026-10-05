package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SignatureDecision {
    INCLUDE,
    EXCLUDE
}

@Entity(
    tableName = "token_signature_decisions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId", "signature"], unique = true),
        Index("subjectId")
    ]
)
data class TokenSignatureDecision(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val signature: String, // Normalized, sorted, comma-joined tokens (e.g. "D3,T1,W1")
    val decision: SignatureDecision
) {
    val code: String get() = signature
    val isMine: Boolean get() = decision == SignatureDecision.INCLUDE

    fun toFilterCode(): FilterCode = FilterCode(
        id = id,
        subjectId = subjectId,
        code = signature,
        isMine = isMine
    )
}

fun FilterCode.toTokenSignatureDecision(): TokenSignatureDecision = TokenSignatureDecision(
    id = id,
    subjectId = subjectId,
    signature = code.trim().uppercase(),
    decision = if (isMine) SignatureDecision.INCLUDE else SignatureDecision.EXCLUDE
)

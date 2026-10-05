package com.tanmay.buddie.domain.sync

import com.tanmay.buddie.data.local.entity.FilterCode
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.domain.model.FetchedMoodleItem

data class FilteredMoodleItem(
    val item: FetchedMoodleItem,
    val filteredOut: Boolean,
    val needsReview: Boolean = false,
    val signature: String = ""
)

data class CalibrationProposal(
    val proposedMineCodes: List<String>,
    val proposedNotMineCodes: List<String>
)

object MoodleItemFilter {

    private val CAMEL_CASE_SPLIT = Regex("""(?<=[a-z])(?=[A-Z])""")
    private val TOKEN_RUN_PATTERN = Regex("""[A-Za-z]+\d*""")

    val COMMON_SHORT_STOP_WORDS = setOf(
        "OF", "IN", "TO", "AT", "ON", "BY", "IS", "IT", "OR", "AS", "IF", "NO", "DO", "SO", "MY", "UP", "WE", "HE", "ME", "US"
    )

    /**
     * Strips all delimiters (-, spaces, parentheses, brackets, underscores, etc.)
     * and splits into alphabetic-prefix + numeric-suffix runs, uppercased.
     * Tokens must have length >= 2 to exclude standalone single-character artifacts.
     *
     * Examples:
     * - "E4-T2-D4(SB)" -> {"E4", "T2", "D4", "SB"}
     * - "DS-Exp1-DivC-C3-T1" -> {"DS", "EXP1", "DIV", "C3", "T1"}
     * - "Exp2C3T1" -> {"EXP2", "C3", "T1"}
     * - "E3-T2 - B3 (AN)" -> {"E3", "T2", "B3", "AN"}
     */
    fun tokenize(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        val spaced = text.replace(CAMEL_CASE_SPLIT, " ")
        return TOKEN_RUN_PATTERN.findAll(spaced)
            .map { it.value.uppercase() }
            .filter { it.length >= 2 }
            .toSet()
    }

    /**
     * Applies tokenize to the blob built from ancestorHeadings + item title.
     */
    fun tokenizeItem(item: FetchedMoodleItem): Set<String> {
        val texts = item.ancestorHeadings + item.title
        val blob = texts.joinToString(" ")
        return tokenize(blob)
    }

    /**
     * Heuristic to determine if a token looks like a section/batch/tutorial code or TA initials.
     *
     * An item counts as having a batch-shaped token if any token is short (<= 5 chars),
     * alphanumeric, with a letter prefix and digit suffix (C3, D1, T2, B4, EXP1, etc.)
     * or short 2-character letter codes (SB, AK, FM, AM, AN, etc.).
     * Longer descriptive tokens (APPLICATION, SUPERMARKET, PRACTICE) don't count toward this,
     * so an item like "A1-Application-4-Supermarket Point-of-Sale (POS) Terminal-AVN" still registers
     * as having a batch-shaped token (A1) and is correctly subject to allow-list filtering, not blanket-kept.
     *
     * Note: This heuristic is approximate and can misfire in either direction — that's what
     * the hide/unhide quick actions in the Home screen / Overlay are for.
     */
    fun isBatchShapedToken(token: String): Boolean {
        val upper = token.trim().uppercase()
        if (upper.length !in 2..5) return false
        // Letter prefix + digit suffix: e.g. C3, D1, T2, B4, EXP1, A1, E4
        if (upper.matches(Regex("""^[A-Z]+\d+$"""))) return true
        // Short 2-letter uppercase codes (e.g. TA initials like SB, AK, FM, AM, AN) excluding common short words
        if (upper.length == 2 && upper.all { it.isLetter() } && upper !in COMMON_SHORT_STOP_WORDS) return true
        return false
    }

    fun hasBatchShapedToken(tokens: Set<String>): Boolean {
        return tokens.any { isBatchShapedToken(it) }
    }

    /**
     * Computes a normalized, uppercase, alphabetically sorted, comma-joined signature string
     * from a set of tokens (e.g. {D3, W1, T1} -> "D3,T1,W1").
     */
    fun computeSignature(tokens: Set<String>): String {
        return tokens
            .filter { isBatchShapedToken(it) }
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .sorted()
            .joinToString(",")
    }

    /**
     * Evaluates a list of FetchedMoodleItem against configured FilterCodes per Subject.
     *
     * Matching = token-set membership, not substring search.
     * Allow-list default flip:
     * - Subject has zero isMine=true codes configured -> detection mode: keep everything (nothing hidden).
     * - Subject has at least one isMine=true code -> allow-list mode: an item is KEPT only if:
     *     (a) it has no batch-shaped tokens at all per step 4 heuristic (treated as course-wide), or
     *     (b) its token set contains at least one of Subject's isMine=true codes.
     *     Otherwise FILTERED OUT.
     * - Explicit exclusion: If an item contains any token matching an isMine=false code, it is FILTERED OUT.
     */
    fun applyFilterCodes(
        items: List<FetchedMoodleItem>,
        filterCodesBySubjectId: Map<String, List<FilterCode>>
    ): List<FilteredMoodleItem> {
        return items.map { item ->
            evaluateItemWithCodes(item, filterCodesBySubjectId[item.mappedSubjectId].orEmpty())
        }
    }

    fun applyDecisions(
        items: List<FetchedMoodleItem>,
        decisionsBySubjectId: Map<String, Map<String, SignatureDecision>>
    ): List<FilteredMoodleItem> {
        return items.map { item ->
            evaluateItem(item, decisionsBySubjectId[item.mappedSubjectId].orEmpty())
        }
    }

    fun evaluateItem(
        item: FetchedMoodleItem,
        subjectDecisions: Map<String, SignatureDecision>
    ): FilteredMoodleItem {
        val filterCodes = subjectDecisions.map { (code, dec) ->
            FilterCode(
                subjectId = item.mappedSubjectId,
                code = code,
                isMine = dec == SignatureDecision.INCLUDE
            )
        }
        return evaluateItemWithCodes(item, filterCodes)
    }

    /**
     * Checks if an item's token set matches a configured code (case-insensitive token membership).
     * If the code is a legacy comma-separated signature (e.g. "C1,E2,FM,T2"), checks if all parts are present.
     */
    fun tokenMatchesCode(tokens: Set<String>, code: String): Boolean {
        val upperCode = code.trim().uppercase()
        if (upperCode in tokens) return true
        if (upperCode.contains(",")) {
            val parts = upperCode.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty() && tokens.containsAll(parts)) return true
        }
        return false
    }

    fun evaluateItemWithCodes(
        item: FetchedMoodleItem,
        configuredCodes: List<FilterCode>
    ): FilteredMoodleItem {
        val tokens = tokenizeItem(item)
        val sig = computeSignature(tokens)

        val mineCodes = configuredCodes.filter { it.isMine }
        val excludeCodes = configuredCodes.filter { !it.isMine }

        // 1. Explicit exclusion: any matching isMine=false code filters it out immediately
        if (excludeCodes.any { tokenMatchesCode(tokens, it.code) }) {
            return FilteredMoodleItem(
                item = item,
                filteredOut = true,
                needsReview = false,
                signature = sig
            )
        }

        // 2. Allow-list default flip:
        // When Subject has at least one isMine=true code configured -> allow-list mode active
        if (mineCodes.isNotEmpty()) {
            val hasBatchTokens = hasBatchShapedToken(tokens)
            val filteredOut = if (!hasBatchTokens) {
                // (a) No batch-shaped tokens at all per step 4 heuristic: treated as course-wide -> KEPT
                false
            } else {
                // (b) Contains at least one isMine=true code -> KEPT, otherwise FILTERED OUT
                val matchesMine = mineCodes.any { tokenMatchesCode(tokens, it.code) }
                !matchesMine
            }
            return FilteredMoodleItem(
                item = item,
                filteredOut = filteredOut,
                needsReview = false,
                signature = sig
            )
        }

        // 3. Subject has zero isMine=true codes configured -> detection mode: keep everything
        return FilteredMoodleItem(
            item = item,
            filteredOut = false,
            needsReview = false,
            signature = sig
        )
    }

    /**
     * Step 5: Computes candidate codes from user labeled yes/no examples.
     * - tokens present in every "yes" item and absent from every "no" item -> propose as isMine=true
     * - tokens present in any "no" item and absent from all "yes" items -> propose as isMine=false
     */
    fun computeCalibrationProposals(
        labeledExamples: List<Pair<Set<String>, Boolean>> // tokens to isYes (true = yes/mine, false = no/not mine)
    ): CalibrationProposal {
        val yesTokenSets = labeledExamples.filter { it.second }.map { it.first }
        val noTokenSets = labeledExamples.filter { !it.second }.map { it.first }

        val proposedMine = if (yesTokenSets.isNotEmpty()) {
            val intersectionYes = yesTokenSets.reduce { acc, set -> acc.intersect(set) }
            val allNoTokens = noTokenSets.flatten().toSet()
            intersectionYes - allNoTokens
        } else {
            emptySet()
        }

        val proposedNotMine = if (noTokenSets.isNotEmpty()) {
            val allYesTokens = yesTokenSets.flatten().toSet()
            val allNoTokens = noTokenSets.flatten().toSet()
            allNoTokens - allYesTokens
        } else {
            emptySet()
        }

        return CalibrationProposal(
            proposedMineCodes = proposedMine.sorted(),
            proposedNotMineCodes = proposedNotMine.sorted()
        )
    }

    /**
     * Step 7: Checks if an item has batch-shaped tokens that match NO configured code
     * (isMine true or false).
     */
    fun isItemUnresolved(item: FetchedMoodleItem, configuredCodes: List<FilterCode>): Boolean {
        val tokens = tokenizeItem(item)
        if (!hasBatchShapedToken(tokens)) return false // Course-wide items do not need resolution
        return configuredCodes.none { tokenMatchesCode(tokens, it.code) }
    }

    fun countUnresolvedItems(items: List<FetchedMoodleItem>, configuredCodes: List<FilterCode>): Int {
        return items.count { isItemUnresolved(it, configuredCodes) }
    }
}

object MoodleTokenScanner {
    private val BRACKET_PATTERN = Regex("""[\(\[\{]([A-Za-z0-9_-]{1,10})[\)\]\}]""")
    private val LETTER_DIGIT_PATTERN = Regex("""\b([A-Za-z]\d{1,3})\b""")
    private val BATCH_PREFIX_PATTERN = Regex("""\b((?:Batch|Sec|Section|Div|Division)[-_ ]?[A-Za-z0-9]{1,6})\b""", RegexOption.IGNORE_CASE)

    private val STOP_WORDS = setOf("PDF", "PPT", "PPTX", "DOC", "DOCX", "ZIP", "SUBMISSION", "ASSIGNMENT")

    fun extractTokensForItem(item: FetchedMoodleItem): Set<String> {
        val texts = item.ancestorHeadings + item.title
        val found = mutableSetOf<String>()

        for (text in texts) {
            BRACKET_PATTERN.findAll(text).forEach { m ->
                val token = m.groupValues[1].trim()
                if (isValidToken(token)) found.add(token)
            }
            LETTER_DIGIT_PATTERN.findAll(text).forEach { m ->
                val token = m.groupValues[1].trim()
                if (isValidToken(token)) found.add(token)
            }
            BATCH_PREFIX_PATTERN.findAll(text).forEach { m ->
                val token = m.groupValues[1].trim()
                if (isValidToken(token)) found.add(token)
            }
        }
        return found
    }

    fun extractTokens(items: List<FetchedMoodleItem>): List<String> {
        val frequencies = mutableMapOf<String, Int>()

        for (item in items) {
            val foundInThisItem = extractTokensForItem(item)
            for (tok in foundInThisItem) {
                frequencies[tok] = (frequencies[tok] ?: 0) + 1
            }
        }

        val minCount = if (items.size >= 2) 2 else 1
        return frequencies.entries
            .filter { it.value >= minCount }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
    }

    private fun isValidToken(token: String): Boolean {
        if (token.length < 2 || token.length > 12) return false
        if (token.all { it.isDigit() }) return false
        if (STOP_WORDS.contains(token.uppercase())) return false
        return true
    }
}

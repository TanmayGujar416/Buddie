package com.tanmay.buddie.presentation.screens.timetable

import java.time.LocalTime

data class PositionedItem<T>(
    val item: T,
    val colIndex: Int,
    val totalCols: Int
)

/**
 * Computes Google Calendar-style side-by-side positioning for overlapping time intervals.
 *
 * 1. Groups overlapping slots into clusters (connected components).
 * 2. Assigns column indices within each cluster using greedy interval coloring.
 * 3. Non-overlapping items get totalCols = 1, colIndex = 0 (100% width).
 */
fun <T> computeOverlappingLayout(
    items: List<T>,
    getTimes: (T) -> Pair<LocalTime, LocalTime>
): List<PositionedItem<T>> {
    if (items.isEmpty()) return emptyList()
    if (items.size == 1) {
        return listOf(PositionedItem(items.first(), colIndex = 0, totalCols = 1))
    }

    // Sort by start time ascending, then by end time descending
    val sorted = items.sortedWith(
        compareBy<T> { getTimes(it).first }
            .thenByDescending { getTimes(it).second }
    )

    // Partition into connected clusters where time ranges overlap
    val clusters = mutableListOf<MutableList<T>>()
    var currentCluster = mutableListOf<T>()
    var currentClusterEnd: LocalTime? = null

    for (item in sorted) {
        val (start, end) = getTimes(item)
        if (currentCluster.isEmpty()) {
            currentCluster.add(item)
            currentClusterEnd = end
        } else {
            val clusterEnd = currentClusterEnd!!
            if (start.isBefore(clusterEnd)) {
                currentCluster.add(item)
                if (end.isAfter(clusterEnd)) {
                    currentClusterEnd = end
                }
            } else {
                clusters.add(currentCluster)
                currentCluster = mutableListOf(item)
                currentClusterEnd = end
            }
        }
    }
    if (currentCluster.isNotEmpty()) {
        clusters.add(currentCluster)
    }

    // For each cluster, assign columns
    val result = mutableListOf<PositionedItem<T>>()

    for (cluster in clusters) {
        if (cluster.size == 1) {
            result.add(PositionedItem(cluster.first(), colIndex = 0, totalCols = 1))
            continue
        }

        val colEndTimes = mutableListOf<LocalTime>()
        val clusterAssignments = mutableListOf<Pair<T, Int>>()

        for (item in cluster) {
            val (start, end) = getTimes(item)
            var placedCol = -1

            for (col in colEndTimes.indices) {
                if (!colEndTimes[col].isAfter(start)) {
                    placedCol = col
                    colEndTimes[col] = end
                    break
                }
            }

            if (placedCol == -1) {
                placedCol = colEndTimes.size
                colEndTimes.add(end)
            }

            clusterAssignments.add(item to placedCol)
        }

        val totalCols = colEndTimes.size
        for ((item, colIndex) in clusterAssignments) {
            result.add(PositionedItem(item, colIndex, totalCols))
        }
    }

    return result
}

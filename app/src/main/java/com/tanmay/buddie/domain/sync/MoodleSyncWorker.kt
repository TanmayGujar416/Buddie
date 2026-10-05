package com.tanmay.buddie.domain.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tanmay.buddie.data.local.security.MoodleTokenStorage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class MoodleSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MoodleSyncWorkerEntryPoint {
        fun syncEngine(): MoodleContentSyncEngine
        fun tokenStorage(): MoodleTokenStorage
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                MoodleSyncWorkerEntryPoint::class.java
            )
            val tokenStorage = entryPoint.tokenStorage()
            if (!tokenStorage.hasToken()) {
                Log.d(TAG, "Moodle token not present, skipping background sync")
                return Result.success()
            }

            val syncEngine = entryPoint.syncEngine()
            Log.d(TAG, "Starting periodic Moodle 3-hour background sync")
            val syncResult = syncEngine.runFullSync(forceRefreshCourseStructure = false)
            if (syncResult.isSuccess) {
                val merge = syncResult.getOrThrow()
                Log.d(TAG, "Moodle periodic sync succeeded: ${merge.insertedCount} inserted, ${merge.updatedCount} updated")
                Result.success()
            } else {
                Log.w(TAG, "Moodle periodic sync failed: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in MoodleSyncWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "MoodleSyncWorker"
        const val WORK_NAME = "moodle_3h_periodic_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<MoodleSyncWorker>(
                3, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Scheduled Moodle 3-hour periodic sync worker")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled Moodle periodic sync worker")
        }
    }
}

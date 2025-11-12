package com.thewizrd.simpleweather.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thewizrd.common.utils.LiveDataUtils.awaitWithTimeout
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.preferences.UpdateSettings
import com.thewizrd.shared_resources.remoteconfig.remoteConfigService
import com.thewizrd.shared_resources.utils.AnalyticsLogger
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.simpleweather.images.ImageDatabase
import com.thewizrd.simpleweather.images.imageDataService
import com.thewizrd.simpleweather.services.ImageDatabaseWorkerActions.ACTION_CANCELALARM
import com.thewizrd.simpleweather.services.ImageDatabaseWorkerActions.ACTION_CHECKUPDATETIME
import com.thewizrd.simpleweather.services.ImageDatabaseWorkerActions.ACTION_STARTALARM
import com.thewizrd.simpleweather.services.ImageDatabaseWorkerActions.ACTION_UPDATEALARM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ImageDatabaseWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "ImageDatabaseWorker"

        @JvmStatic
        @JvmOverloads
        fun enqueueAction(context: Context, intentAction: String, onBoot: Boolean = false) {
            // For immediate action
            when (intentAction) {
                ACTION_UPDATEALARM -> enqueueWork(context.applicationContext)
                ACTION_STARTALARM -> {
                    appLib.appScope.launch(Dispatchers.Default) {
                        if (onBoot || !isWorkScheduled(context.applicationContext)) {
                            startWork(context.applicationContext)
                        }
                    }
                }
                ACTION_CHECKUPDATETIME -> {
                    // For immediate action
                    startWork(context.applicationContext)
                }
                ACTION_CANCELALARM -> cancelWork(context.applicationContext)
            }
        }

        private fun startWork(context: Context) {
            Logger.writeLine(Log.INFO, "%s: Requesting to start work", TAG)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(false)
                .build()
            val updateRequest = OneTimeWorkRequest.Builder(ImageDatabaseWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueue(updateRequest)

            Logger.writeLine(Log.INFO, "%s: One-time work enqueued", TAG)

            // Enqueue periodic task as well
            enqueueWork(context)
        }

        private fun enqueueWork(context: Context) {
            Logger.writeLine(Log.INFO, "%s: Requesting work", TAG)

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .build()

            val updateRequest = PeriodicWorkRequest.Builder(ImageDatabaseWorker::class.java, 7, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .addTag(TAG)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, updateRequest)

            Logger.writeLine(Log.INFO, "%s: Work enqueued", TAG)
        }

        private suspend fun isWorkScheduled(context: Context): Boolean {
            val workMgr = WorkManager.getInstance(context)
            val statuses = workMgr.getWorkInfosForUniqueWorkLiveData(TAG).awaitWithTimeout(10000)
            if (statuses.isNullOrEmpty()) return false
            var running = false
            for (workStatus in statuses) {
                running = (workStatus.state == WorkInfo.State.RUNNING
                        || workStatus.state == WorkInfo.State.ENQUEUED)
            }
            return running
        }

        private fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG)
            Logger.writeLine(Log.INFO, "%s: Canceled work", TAG)
        }
    }

    override suspend fun doWork(): Result {
        Logger.writeLine(Log.INFO, "%s: Work started", TAG)

        // Check if cache is populated
        if (!imageDataService.isEmpty && !UpdateSettings.isUpdateAvailable) {
            // If so, check the last time the Image Database (Firestore) was updated
            val remoteDBVersionTimestamp = try {
                ImageDatabase.getVersionTimestamp()
            } catch (e: Exception) {
                Logger.writeLine(Log.ERROR, e)
                0L
            }

            val localDBVersionTimestamp = imageDataService.getImageDBVersionTimestamp()
            val localDBUpdateTime = imageDataService.getImageDBUpdateTime()
            val localCachedDays =
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - localDBUpdateTime)
            val validDays =
                (remoteConfigService.getLong("key_imagedb_valid_days").takeIf { it != 0L }
                    ?: 30) + Random.nextLong(0, 8)

            // Check if a new image db version is available or local image db cache time has expired
            if (remoteDBVersionTimestamp > localDBVersionTimestamp || localCachedDays > validDays) {
                AnalyticsLogger.logEvent("ImgDBWorker: clearing image cache", Bundle().apply {
                    putLong("remoteDBVersionTimestamp", remoteDBVersionTimestamp)
                    putLong("localDBVersionTimestamp", localDBVersionTimestamp)
                    putLong("localCachedDays", localCachedDays)
                })

                // if so, invalidate
                imageDataService.setImageDBVersionTimestamp(remoteDBVersionTimestamp)
                imageDataService.setImageDBUpdateTime(System.currentTimeMillis())
                imageDataService.clearCachedImageData()
                imageDataService.invalidateCache(true)
            }
        }

        return Result.success()
    }
}
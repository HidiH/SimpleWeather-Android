package com.thewizrd.simpleweather.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thewizrd.shared_resources.preferences.UpdateSettings
import com.thewizrd.shared_resources.utils.AnalyticsLogger
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.simpleweather.images.ImageDatabase
import com.thewizrd.simpleweather.images.imageDataService
import java.util.concurrent.TimeUnit

class FCMWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "FCMWorker"

        const val ACTION_INVALIDATE = "SimpleWeather.Droid.action.INVALIDATE"

        @JvmStatic
        fun enqueueAction(context: Context, intentAction: String) {
            if (ACTION_INVALIDATE == intentAction) {
                startWork(context.applicationContext)
            }
        }

        private fun startWork(context: Context) {
            Logger.writeLine(Log.INFO, "%s: Requesting to start work", TAG)

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .build()

            val updateRequest = OneTimeWorkRequest.Builder(FCMWorker::class.java)
                    .setConstraints(constraints)
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .build()

            WorkManager.getInstance(context.applicationContext).enqueue(updateRequest)

            Logger.writeLine(Log.INFO, "%s: One-time work enqueued", TAG)
        }
    }

    override suspend fun doWork(): Result {
        Logger.writeLine(Log.INFO, "%s: Work started", TAG)

        // Check if cache is populated
        if (!imageDataService.isEmpty && !UpdateSettings.isUpdateAvailable) {
            // If so, check if we need to invalidate
            val remoteDBVersionTimestamp = try {
                ImageDatabase.getVersionTimestamp()
            } catch (e: Exception) {
                Logger.writeLine(Log.ERROR, e)
                0L
            }

            val localDBVersionTimestamp = imageDataService.getImageDBVersionTimestamp()
            val localDBUpdateTime = imageDataService.getImageDBUpdateTime()

            if (remoteDBVersionTimestamp > localDBVersionTimestamp) {
                AnalyticsLogger.logEvent("$TAG: clearing image cache", Bundle().apply {
                    putLong("remoteDBVersionTimestamp", remoteDBVersionTimestamp)
                    putLong("localDBVersionTimestamp", localDBVersionTimestamp)
                    putLong("localDBUpdateTime", localDBUpdateTime)
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
package com.thewizrd.simpleweather.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.thewizrd.common.utils.LiveDataUtils.awaitWithTimeout
import com.thewizrd.common.weatherdata.WeatherDataLoader
import com.thewizrd.common.weatherdata.WeatherRequest
import com.thewizrd.common.weatherdata.WeatherResult
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.exceptions.ErrorStatus
import com.thewizrd.shared_resources.exceptions.WeatherException
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.simpleweather.services.ServiceNotificationHelper.JOB_ID
import com.thewizrd.simpleweather.services.ServiceNotificationHelper.getForegroundNotification
import com.thewizrd.simpleweather.services.ServiceNotificationHelper.initChannel
import com.thewizrd.simpleweather.wearable.WearableWorker
import com.thewizrd.simpleweather.wearable.WearableWorkerActions
import com.thewizrd.simpleweather.wearable.complications.WeatherComplicationHelper
import com.thewizrd.simpleweather.wearable.tiles.WeatherTileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WidgetUpdaterWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "WidgetUpdaterWorker"

        const val ACTION_UPDATEWIDGETS = "SimpleWeather.Droid.action.UPDATE_WIDGETS"
        const val ACTION_ENQUEUEWORK = "SimpleWeather.Droid.action.START_ALARM"
        const val ACTION_CANCELWORK = "SimpleWeather.Droid.action.CANCEL_ALARM"
        const val ACTION_REQUEUEWORK = "SimpleWeather.Droid.action.UPDATE_ALARM"

        suspend fun requestWidgetUpdate(context: Context) {
            WidgetUpdaterWork.requestWidgetUpdate(context.applicationContext)
        }

        suspend fun executeWork(context: Context) {
            WidgetUpdaterWork.executeWork(context.applicationContext)
        }

        @JvmStatic
        @JvmOverloads
        fun enqueueAction(context: Context, intentAction: String, onBoot: Boolean = false) {
            when (intentAction) {
                ACTION_REQUEUEWORK -> enqueueWork(context.applicationContext)
                ACTION_ENQUEUEWORK ->
                    appLib.appScope.launch(Dispatchers.Default) {
                        if (onBoot || !isWorkScheduled(context.applicationContext)) {
                            startWork(context.applicationContext)
                        }
                    }
                ACTION_UPDATEWIDGETS ->
                    // For immediate action
                    startWork(context.applicationContext)
                ACTION_CANCELWORK -> cancelWork(context.applicationContext)
            }
        }

        private fun startWork(context: Context) {
            Logger.writeLine(Log.INFO, "%s: Requesting to start work", TAG)

            val updateRequest = OneTimeWorkRequestBuilder<WidgetUpdaterWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueue(updateRequest)

            Logger.writeLine(Log.INFO, "%s: One-time work enqueued", TAG)

            // Enqueue periodic task as well
            enqueueWork(context.applicationContext)
        }

        private fun enqueueWork(context: Context) {
            Logger.writeLine(Log.INFO, "%s: Requesting work", TAG)

            val updateRequest = PeriodicWorkRequest.Builder(
                WidgetUpdaterWorker::class.java,
                60,
                TimeUnit.MINUTES,
                5,
                TimeUnit.MINUTES
            )
                .setConstraints(Constraints.NONE)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context.applicationContext)
                    .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, updateRequest)

            Logger.writeLine(Log.INFO, "%s: Work enqueued", TAG)
        }

        private suspend fun isWorkScheduled(context: Context): Boolean {
            val workMgr = WorkManager.getInstance(context.applicationContext)
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
            // Cancel alarm if dependent features are turned off
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(TAG)
            Logger.writeLine(Log.INFO, "%s: Canceled work", TAG)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        initChannel(applicationContext)
        return ForegroundInfo(JOB_ID, getForegroundNotification(applicationContext))
    }

    override suspend fun doWork(): Result {
        return WidgetUpdaterWork.executeWork(applicationContext)
    }

    private object WidgetUpdaterWork {
        suspend fun executeWork(context: Context): Result {
            var result = Result.success()

            val settingsManager = SettingsManager(context.applicationContext)

            if (settingsManager.isWeatherLoaded()) {
                // If saved data DNE (for current location), refresh weather
                var weatherResult = loadWeather()
                if (weatherResult !is WeatherResult.Success && weatherResult !is WeatherResult.WeatherWithError) {
                    if (settingsManager.getDataSync() != WearableDataSync.OFF) {
                        // Check if data has been updated
                        WearableWorker.enqueueAction(
                            context,
                            WearableWorkerActions.ACTION_REQUESTWEATHERUPDATE
                        )
                    } else {
                        weatherResult = loadWeather(true)
                    }
                }

                result = when (weatherResult) {
                    is WeatherResult.Success -> Result.success()
                    is WeatherResult.NoWeather -> Result.failure()
                    is WeatherResult.Error,
                    is WeatherResult.WeatherWithError -> Result.retry()
                }

                requestWidgetUpdate(context)
            }

            when (result) {
                Result.success() -> {
                    Timber.tag(TAG).i("Work completed successfully...")
                }

                Result.retry() -> {
                    Timber.tag(TAG).w("Work failed. Will retry...")
                }

                Result.failure() -> {
                    Timber.tag(TAG).e("Work failed...")
                }
            }

            return result
        }

        fun requestWidgetUpdate(context: Context) {
            // Update complications
            WeatherComplicationHelper.requestComplicationUpdateAll(context.applicationContext)

            // Update tiles
            WeatherTileHelper.requestTileUpdateAll(context.applicationContext)
        }

        private suspend fun loadWeather(forceRefresh: Boolean = false): WeatherResult =
            withContext(Dispatchers.IO) {
                Timber.tag(TAG).d("Getting weather data...")

                val locData =
                    settingsManager.getHomeData() ?: return@withContext WeatherResult.Error(
                        WeatherException(ErrorStatus.NOWEATHER)
                    )

                WeatherDataLoader(locData)
                    .loadWeatherResult(
                        WeatherRequest.Builder()
                            .run {
                                if (forceRefresh && settingsManager.getDataSync() == WearableDataSync.OFF) {
                                    this.forceRefresh(false)
                                        .loadAlerts()
                                } else {
                                    this.forceLoadSavedData()
                                }
                            }
                            .build()
                    )
            }
    }
}
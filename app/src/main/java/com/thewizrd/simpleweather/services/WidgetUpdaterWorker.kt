package com.thewizrd.simpleweather.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.IntDef
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.multiprocess.RemoteWorkManager
import com.thewizrd.common.utils.LiveDataUtils.awaitWithTimeout
import com.thewizrd.common.wearable.WearableSettings
import com.thewizrd.common.weatherdata.WeatherDataLoader
import com.thewizrd.common.weatherdata.WeatherRequest
import com.thewizrd.common.weatherdata.WeatherResult
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.di.localBroadcastManager
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.exceptions.ErrorStatus
import com.thewizrd.shared_resources.exceptions.WeatherException
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.utils.CommonActions
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.simpleweather.notifications.PoPChanceNotificationHelper
import com.thewizrd.simpleweather.notifications.WeatherNotificationWorker
import com.thewizrd.simpleweather.shortcuts.ShortcutCreatorWorker
import com.thewizrd.simpleweather.widgets.WidgetUpdaterHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.TimeUnit

class WidgetUpdaterWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "WidgetUpdaterWorker"
        private const val KEY_ACTION = "action"

        const val ACTION_UPDATEWIDGETS = "SimpleWeather.Droid.action.UPDATE_WIDGETS"
        const val ACTION_ENQUEUEWORK = "SimpleWeather.Droid.action.START_ALARM"
        const val ACTION_CANCELWORK = "SimpleWeather.Droid.action.CANCEL_ALARM"
        const val ACTION_REQUEUEWORK = "SimpleWeather.Droid.action.UPDATE_ALARM"

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

            RemoteWorkManager.getInstance(context).enqueue(updateRequest)

            Logger.writeLine(Log.INFO, "%s: One-time work enqueued", TAG)

            // Enqueue periodic task as well
            enqueueWork(context)
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

            RemoteWorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, updateRequest)

            Logger.writeLine(Log.INFO, "%s: Work enqueued", TAG)
        }

        @JvmStatic
        suspend fun schedulePoPNotification(
            context: Context,
            duration: Duration,
            expedited: Boolean = false
        ) {
            // Check if there is existing work pending
            val tag = "${TAG}_sched_pop"
            val workMgr = WorkManager.getInstance(context.applicationContext)
            val workInfos = withContext(Dispatchers.IO) {
                runCatching {
                    workMgr.getWorkInfosForUniqueWork(tag).get(10, TimeUnit.SECONDS)
                }.getOrElse { emptyList() }
            }

            var existingWorkPolicy = ExistingWorkPolicy.REPLACE
            val requestedDelayInMillis = duration.toMillis()
            val nextScheduleTimeMillis = System.currentTimeMillis() + requestedDelayInMillis

            // If any existing work exists with a shorter delay, keep it, else replace it
            for (workInfo in workInfos) {
                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                    val estimatedDelayInMillis =
                        workInfo.nextScheduleTimeMillis - System.currentTimeMillis()

                    if (estimatedDelayInMillis in 1..<nextScheduleTimeMillis) {
                        existingWorkPolicy = ExistingWorkPolicy.KEEP
                        break
                    }
                }
            }

            val request = OneTimeWorkRequestBuilder<WidgetWorker>()
                .apply {
                    if (expedited) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .setConstraints(Constraints.NONE)
                .setInputData(
                    Data.Builder()
                        .putInt(KEY_ACTION, WidgetUpdaterWork.ACTION_UPDATEPOPNOTIFICATION)
                        .build()
                )
                .setInitialDelay(requestedDelayInMillis, TimeUnit.MILLISECONDS)
                .build()

            RemoteWorkManager.getInstance(context)
                .enqueueUniqueWork(tag, existingWorkPolicy, request)
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
            RemoteWorkManager.getInstance(context.applicationContext).cancelUniqueWork(TAG)
            Logger.writeLine(Log.INFO, "%s: Canceled work", TAG)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceNotificationHelper.initChannel(applicationContext)
        }

        return ForegroundInfo(
            ServiceNotificationHelper.JOB_ID,
            ServiceNotificationHelper.createForegroundNotification(applicationContext)
        )
    }

    override suspend fun doWork(): Result {
        val action = inputData.getInt(KEY_ACTION, WidgetUpdaterWork.ACTION_UPDATEALL)
        return WidgetUpdaterWork.executeWork(applicationContext, action)
    }

    private object WidgetUpdaterWork {
        const val ACTION_UPDATEALL = 0.inv()
        const val ACTION_UPDATEWIDGETS = 1
        const val ACTION_UPDATENOTIFICATION = 1 shl 1
        const val ACTION_UPDATEPOPNOTIFICATION = 1 shl 2

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            ACTION_UPDATEALL,
            ACTION_UPDATEWIDGETS,
            ACTION_UPDATENOTIFICATION,
            ACTION_UPDATEPOPNOTIFICATION
        )
        annotation class UpdateAction

        suspend fun executeWork(
            context: Context,
            @UpdateAction action: Int = ACTION_UPDATEALL
        ): Result {
            var result = Result.success()

            val settingsManager = SettingsManager(context.applicationContext)

            if (settingsManager.isWeatherLoaded()) {
                // If saved data DNE (for current location), refresh weather
                var weatherResult = loadWeather()
                if (weatherResult !is WeatherResult.Success && weatherResult !is WeatherResult.WeatherWithError) {
                    weatherResult = loadWeather(true)

                    if (weatherResult.let { it is WeatherResult.Success && !it.isSavedData }) {
                        localBroadcastManager.sendBroadcast(
                            Intent(CommonActions.ACTION_WEATHER_SENDWEATHERUPDATE).apply {
                                putExtra(WearableSettings.KEY_PARTIAL_WEATHER_UPDATE, true)
                            }
                        )
                    }

                    result = when (weatherResult) {
                        is WeatherResult.Success -> Result.success()
                        is WeatherResult.NoWeather -> Result.failure()
                        is WeatherResult.Error -> Result.retry()
                        is WeatherResult.WeatherWithError -> if (action == ACTION_UPDATENOTIFICATION || action == ACTION_UPDATEPOPNOTIFICATION) {
                            Result.success()
                        } else {
                            Result.retry()
                        }
                    }
                }

                val willRetry = result == Result.retry()

                if (action and ACTION_UPDATEWIDGETS != 0 && WidgetUpdaterHelper.widgetsExist()) {
                    WidgetUpdaterHelper.refreshWidgets(context, resetIfUnavailable = !willRetry)
                }

                if (action and ACTION_UPDATENOTIFICATION != 0 && settingsManager.showOngoingNotification()) {
                    WeatherNotificationWorker.refreshNotification(
                        context,
                        resetIfUnavailable = !willRetry
                    )
                }

                if (action and ACTION_UPDATEPOPNOTIFICATION != 0 && settingsManager.isPoPChanceNotificationEnabled()) {
                    PoPChanceNotificationHelper.postNotification(context)
                }

                if (action and ACTION_UPDATEWIDGETS != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutCreatorWorker.updateShortcuts(context)
                }
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

        private suspend fun loadWeather(forceRefresh: Boolean = false): WeatherResult =
            withContext(Dispatchers.IO) {
                Timber.tag(TAG).d("Getting weather data for home...")

                val locData =
                    settingsManager.getHomeData() ?: return@withContext WeatherResult.Error(
                        WeatherException(ErrorStatus.NOWEATHER)
                    )

                WeatherDataLoader(locData)
                    .loadWeatherResult(
                        WeatherRequest.Builder()
                            .run {
                                if (forceRefresh) {
                                    this.forceRefresh(false)
                                        .loadAlerts()
                                        .loadForecasts()
                                } else {
                                    this.forceLoadSavedData()
                                }
                            }
                            .build()
                    )
            }
    }
}
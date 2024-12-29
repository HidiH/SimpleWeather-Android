package com.thewizrd.simpleweather.wearable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.common.wearable.WearableSettings
import com.thewizrd.shared_resources.di.localBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber

class WeatherDataSyncWorker(
    private val context: Context,
    private val onLoadData: () -> Unit,
    private val onCancelSync: () -> Unit,
) {
    companion object {
        private const val TAG_SYNCRECEIVER = "SyncDataReceiver"
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val syncDataReceiver = SyncDataReceiver()
    private var syncTimerJob: Job? = null

    fun cancelDataSync() {
        syncTimerJob?.cancel()
    }

    fun startSyncTimer() {
        syncTimerJob = scope.launch {
            supervisorScope {
                delay(35000)

                ensureActive()

                // We hit the interval
                // Data syncing is taking a long time to setup
                // Stop and load saved data
                Timber.tag(TAG_SYNCRECEIVER).d("resetTimer: timeout")

                cancelDataSync()
            }
        }
    }

    fun registerSyncReceiver() {
        localBroadcastManager.registerReceiver(syncDataReceiver, IntentFilter().apply {
            addAction(WearableHelper.LocationPath)
            addAction(WearableHelper.WeatherPath)
        })
    }

    fun unRegisterSyncReceiver() {
        localBroadcastManager.unregisterReceiver(syncDataReceiver)
    }

    fun close() {
        syncTimerJob?.cancel()
        unRegisterSyncReceiver()
        scope.cancel()
    }

    private inner class SyncDataReceiver : BroadcastReceiver() {
        private var locationDataReceived = false
        private var weatherDataReceived = false

        override fun onReceive(context: Context, intent: Intent) {
            scope.launch {
                if (WearableHelper.LocationPath == intent.action || WearableHelper.WeatherPath == intent.action) {
                    if (WearableHelper.WeatherPath == intent.action) {
                        weatherDataReceived = true
                        if (intent.getBooleanExtra(
                                WearableSettings.KEY_PARTIAL_WEATHER_UPDATE,
                                false
                            )
                        ) {
                            // Location is already up-to-date; we're just updating the weather
                            locationDataReceived = true
                        }
                    }

                    if (WearableHelper.LocationPath == intent.action) {
                        // We got the location data
                        locationDataReceived = true
                    }

                    Timber.tag(TAG_SYNCRECEIVER).d("Action: %s", intent.action)

                    if (locationDataReceived && weatherDataReceived) {
                        syncTimerJob?.cancel()

                        Timber.tag(TAG_SYNCRECEIVER).d("Loading data...")

                        // We got all our data; now load the weather
                        onLoadData.invoke()

                        weatherDataReceived = false
                        locationDataReceived = false
                    }
                } else if (WearableHelper.ErrorPath == intent.action) {
                    // An error occurred; cancel the sync operation
                    weatherDataReceived = false
                    locationDataReceived = false
                    onCancelSync.invoke()
                }
            }
        }
    }
}
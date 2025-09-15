package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.common.wearable.WearableSettings
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.di.localBroadcastManager
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.simpleweather.BuildConfig
import com.thewizrd.simpleweather.wearable.WearableListenerActions
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_REQUESTREFRESHWEATHER
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_SYNCSETTINGUPDATED
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_UPDATESYNCSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_SUCCESS
import com.thewizrd.simpleweather.wearable.WearableWorker
import com.thewizrd.simpleweather.wearable.WearableWorkerActions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber

class WeatherDataSyncViewModel(app: Application) : WearableListenerViewModel(app),
    SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG_SYNCRECEIVER = "SyncDataReceiver"
    }

    private val viewModelState = MutableStateFlow(WeatherDataSyncState())

    private val syncDataReceiver = SyncDataReceiver()
    private var syncTimerJob: Job? = null

    init {
        if (!BuildConfig.IS_NONGMS) {
            localBroadcastManager.registerReceiver(syncDataReceiver, IntentFilter().apply {
                addAction(WearableHelper.LocationPath)
                addAction(WearableHelper.WeatherPath)
            })
            appLib.registerAppSharedPreferenceListener(this)
        }

        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event.eventType) {
                    WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS -> {
                        val status =
                            WearConnectionStatus.valueOf(event.data.getInt(WearableListenerActions.EXTRA_CONNECTIONSTATUS))
                        updateConnectionStatus(status)
                    }
                }
            }
        }
    }

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    private fun cancelDataSync() {
        if (!BuildConfig.IS_NONGMS) {
            cancelDataSyncTimer()

            // Notify sync failed
            _eventsFlow.tryEmit(WearableEvent(ACTION_UPDATESYNCSTATUS, Bundle().apply {
                putBoolean(EXTRA_SUCCESS, false)
            }))
        }
    }

    fun syncWeather() {
        if (!BuildConfig.IS_NONGMS) {
            viewModelState.update {
                it.copy(
                    isSyncInProgress = true
                )
            }

            // Request update from connected device
            WearableWorker.enqueueAction(
                appContext,
                WearableWorkerActions.ACTION_REQUESTUPDATE,
                true
            )
            startSyncTimer()
        }
    }

    private fun updateConnectionStatus(connectionStatus: WearConnectionStatus) {
        viewModelState.update {
            it.copy(showDisconnectedView = settingsManager.getDataSync() != WearableDataSync.OFF && connectionStatus != WearConnectionStatus.CONNECTED)
        }
    }

    /* Timer Worker */
    private fun cancelDataSyncTimer() {
        syncTimerJob?.cancel()
    }

    private fun startSyncTimer() {
        cancelDataSyncTimer()
        syncTimerJob = viewModelScope.launch {
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

    override fun onCleared() {
        super.onCleared()
        if (!BuildConfig.IS_NONGMS) {
            cancelDataSyncTimer()
            localBroadcastManager.unregisterReceiver(syncDataReceiver)
            appLib.unregisterAppSharedPreferenceListener(this)
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (key.isNullOrBlank()) return

        when (key) {
            SettingsManager.KEY_DATASYNC -> {
                // If data sync settings changes,
                // reset so we can properly reload
                _eventsFlow.tryEmit(WearableEvent(ACTION_SYNCSETTINGUPDATED))
            }

            SettingsManager.KEY_TEMPUNIT,
            SettingsManager.KEY_DISTANCEUNIT,
            SettingsManager.KEY_PRECIPITATIONUNIT,
            SettingsManager.KEY_PRESSUREUNIT,
            SettingsManager.KEY_SPEEDUNIT,
            SettingsManager.KEY_ICONSSOURCE -> {
                _eventsFlow.tryEmit(WearableEvent(ACTION_REQUESTREFRESHWEATHER))
            }
        }
    }

    private inner class SyncDataReceiver : BroadcastReceiver() {
        private var locationDataReceived = false
        private var weatherDataReceived = false

        override fun onReceive(context: Context, intent: Intent) {
            viewModelScope.launch {
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
                        _eventsFlow.tryEmit(WearableEvent(ACTION_UPDATESYNCSTATUS, Bundle().apply {
                            putBoolean(EXTRA_SUCCESS, true)
                        }))

                        weatherDataReceived = false
                        locationDataReceived = false
                    }
                } else if (WearableHelper.ErrorPath == intent.action) {
                    // An error occurred; cancel the sync operation
                    weatherDataReceived = false
                    locationDataReceived = false
                    cancelDataSync()
                }
            }
        }
    }
}
package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.shared_resources.di.localBroadcastManager
import com.thewizrd.simpleweather.BuildConfig
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.wearable.WearableDataListenerService
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_SHOWSTORELISTING
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_UPDATESYNCSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_CONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_DEVICESETUPSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_SHOWANIMATION
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_SUCCESS
import com.thewizrd.simpleweather.wearable.WearableWorker
import com.thewizrd.simpleweather.wearable.WearableWorkerActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class SetupSyncViewModel(app: Application) : WearableListenerViewModel(app) {
    private val viewModelState = MutableStateFlow(SetupSyncState())

    private val syncDataReceiver = SyncDataReceiver()

    init {
        if (!BuildConfig.IS_NONGMS) {
            localBroadcastManager.registerReceiver(syncDataReceiver, IntentFilter().apply {
                addAction(WearableHelper.LocationPath)
                addAction(WearableHelper.SettingsPath)
                addAction(WearableHelper.WeatherPath)
                addAction(WearableHelper.ErrorPath)
            })

            viewModelScope.launch {
                eventFlow.collect { event ->
                    when (event.eventType) {
                        WearableHelper.IsSetupPath -> {
                            val isDeviceSetup =
                                event.data.getBoolean(EXTRA_DEVICESETUPSTATUS, false)
                            start(isDeviceSetup)
                        }

                        ACTION_UPDATECONNECTIONSTATUS -> {
                            val status = WearConnectionStatus.valueOf(
                                event.data.getInt(
                                    EXTRA_CONNECTIONSTATUS,
                                    0
                                )
                            )

                            when (status) {
                                WearConnectionStatus.DISCONNECTED -> {
                                    setErrorState(R.string.status_disconnected)
                                }

                                WearConnectionStatus.CONNECTING -> {
                                    resetTimer(R.string.status_connecting)
                                }

                                WearConnectionStatus.APPNOTINSTALLED -> {
                                    setErrorState(R.string.error_notinstalled)
                                    _eventsFlow.tryEmit(
                                        WearableEvent(
                                            ACTION_SHOWSTORELISTING,
                                            Bundle().apply {
                                                putBoolean(EXTRA_SHOWANIMATION, true)
                                            })
                                    )
                                }

                                WearConnectionStatus.CONNECTED -> {
                                    resetTimer(R.string.status_connected)
                                    sendSetupStatusRequest()
                                }
                            }
                        }
                    }
                }
            }

            isAcceptingDataUpdates = true
            WearableDataListenerService.setAcceptDataUpdates(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        isAcceptingDataUpdates = false
        WearableDataListenerService.setAcceptDataUpdates(false)
    }

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    private fun resetTimer(@StringRes messageStringResId: Int = 0) {
        viewModelState.update {
            it.copy(
                messageStringResId = if (messageStringResId != 0) {
                    messageStringResId
                } else {
                    it.messageStringResId
                },
                progressBarState = ProgressBarState()
            )
        }
    }

    private fun setErrorState(@StringRes messageStringResId: Int = 0) {
        viewModelState.update {
            it.copy(
                messageStringResId = if (messageStringResId != 0) {
                    messageStringResId
                } else {
                    it.messageStringResId
                },
                settingsDataReceived = false,
                locationDataReceived = false,
                weatherDataReceived = false,
                progressBarState = ProgressBarState(
                    isIndeterminate = false,
                    timeInMillis = 5000
                )
            )
        }

        viewModelScope.launch {
            delay(5000)

            _eventsFlow.emit(WearableEvent(ACTION_UPDATESYNCSTATUS, Bundle().apply {
                putBoolean(EXTRA_SUCCESS, false)
            }))
        }
    }

    private fun setSuccessState() {
        viewModelState.update {
            it.copy(
                messageStringResId = R.string.message_synccompleted,
                progressBarState = ProgressBarState(
                    isIndeterminate = false,
                    timeInMillis = 1000
                )
            )
        }

        viewModelScope.launch {
            delay(1500)

            _eventsFlow.emit(WearableEvent(ACTION_UPDATESYNCSTATUS, Bundle().apply {
                putBoolean(EXTRA_SUCCESS, true)
            }))
        }
    }

    private fun start(isDeviceSetup: Boolean) {
        if (isDeviceSetup) {
            viewModelState.update {
                it.copy(
                    messageStringResId = R.string.message_retrievingdata
                )
            }

            WearableWorker.enqueueAction(
                appContext,
                WearableWorkerActions.ACTION_REQUESTSETTINGSUPDATE,
                true
            )
            WearableWorker.enqueueAction(
                appContext,
                WearableWorkerActions.ACTION_REQUESTLOCATIONUPDATE,
                true
            )
            WearableWorker.enqueueAction(
                appContext,
                WearableWorkerActions.ACTION_REQUESTWEATHERUPDATE,
                true
            )
        } else {
            viewModelState.update {
                it.copy(
                    messageStringResId = R.string.message_continueondevice
                )
            }
        }
    }

    private inner class SyncDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModelScope.launch(Dispatchers.Main) {
                when (intent.action) {
                    WearableHelper.ErrorPath -> {
                        setErrorState(R.string.error_syncing)
                    }

                    WearableHelper.SettingsPath -> {
                        val state = viewModelState.updateAndGet {
                            it.copy(
                                messageStringResId = R.string.message_settingsretrieved,
                                settingsDataReceived = true
                            )
                        }

                        if (state.isSyncComplete())
                            setSuccessState()
                    }

                    WearableHelper.LocationPath -> {
                        val state = viewModelState.updateAndGet {
                            it.copy(
                                messageStringResId = R.string.message_locationretrieved,
                                locationDataReceived = true
                            )
                        }

                        if (state.isSyncComplete())
                            setSuccessState()
                    }

                    WearableHelper.WeatherPath -> {
                        val state = viewModelState.updateAndGet {
                            it.copy(
                                messageStringResId = R.string.message_weatherretrieved,
                                weatherDataReceived = true
                            )
                        }

                        if (state.isSyncComplete())
                            setSuccessState()
                    }
                }
            }
        }
    }
}
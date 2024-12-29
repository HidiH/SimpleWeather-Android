package com.thewizrd.simpleweather.setup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.CircularProgressLayout.OnTimerFinishedListener
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.shared_resources.store.PlayStoreUtils
import com.thewizrd.shared_resources.utils.AnalyticsLogger
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.databinding.ActivitySetupSyncBinding
import com.thewizrd.simpleweather.helpers.showConfirmationOverlay
import com.thewizrd.simpleweather.locale.UserLocaleActivity
import com.thewizrd.simpleweather.wearable.WearableDataListenerService
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_CONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_DEVICESETUPSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerManager
import com.thewizrd.simpleweather.wearable.WearableWorker
import com.thewizrd.simpleweather.wearable.WearableWorkerActions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupSyncActivity : UserLocaleActivity() {
    companion object {
        private const val TAG = "SetupSyncActivity"
    }

    private var settingsDataReceived = false
    private var locationDataReceived = false
    private var weatherDataReceived = false

    private lateinit var binding: ActivitySetupSyncBinding

    private val wearListenerMgr = WearableListenerManager(this,
        WearBroadcastReceiver(),
        IntentFilter().apply {
            addAction(WearableHelper.IsSetupPath)
            addAction(WearableHelper.LocationPath)
            addAction(WearableHelper.SettingsPath)
            addAction(WearableHelper.WeatherPath)
            addAction(WearableHelper.ErrorPath)
        }
    )

    override fun enableLocaleChangeListener(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AnalyticsLogger.logEvent("$TAG: onCreate")

        binding = ActivitySetupSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.circularProgress.isIndeterminate = true
        binding.circularProgress.setOnClickListener {
            // User canceled, abort the action
            binding.circularProgress.stopTimer()
            setResult(RESULT_CANCELED)
            finish()
        }
        binding.circularProgress.onTimerFinishedListener = OnTimerFinishedListener {
            // User didn't cancel, perform the action
            // All data received finish activity
            if (settingsDataReceived && locationDataReceived && weatherDataReceived)
                setResult(RESULT_OK)
            else
                setResult(RESULT_CANCELED)
            finish()
        }

        binding.message.setText(R.string.message_gettingstatus)
    }

    override fun onStart() {
        wearListenerMgr.onStart()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        wearListenerMgr.onResume()

        AnalyticsLogger.logEvent("$TAG: onResume")

        // Allow service to parse OnDataChanged updates
        wearListenerMgr.isAcceptingDataUpdates = true
        WearableDataListenerService.setAcceptDataUpdates(true)

        lifecycleScope.launch {
            wearListenerMgr.sendSetupStatusRequest()
        }
    }

    override fun onPause() {
        AnalyticsLogger.logEvent("$TAG: onPause")

        settingsDataReceived = false
        locationDataReceived = false
        weatherDataReceived = false

        // Disallow service to parse OnDataChanged updates
        WearableDataListenerService.setAcceptDataUpdates(false)
        wearListenerMgr.isAcceptingDataUpdates = false

        wearListenerMgr.onPause()
        super.onPause()
    }

    private fun errorProgress() {
        binding.circularProgress.isIndeterminate = false
        binding.circularProgress.totalTime = 5000
        binding.circularProgress.startTimer()

        settingsDataReceived = false
        locationDataReceived = false
        weatherDataReceived = false
    }

    private fun resetTimer() {
        binding.circularProgress.stopTimer()
        binding.circularProgress.isIndeterminate = true
    }

    private fun successProgress() {
        binding.message.setText(R.string.message_synccompleted)

        binding.circularProgress.isIndeterminate = false
        binding.circularProgress.totalTime = 1
        binding.circularProgress.startTimer()
    }

    private fun start(isDeviceSetup: Boolean) {
        if (isDeviceSetup) {
            binding.message.setText(R.string.message_retrievingdata)
            WearableWorker.enqueueAction(
                this,
                WearableWorkerActions.ACTION_REQUESTSETTINGSUPDATE,
                true
            )
            WearableWorker.enqueueAction(
                this,
                WearableWorkerActions.ACTION_REQUESTLOCATIONUPDATE,
                true
            )
            WearableWorker.enqueueAction(
                this,
                WearableWorkerActions.ACTION_REQUESTWEATHERUPDATE,
                true
            )
        } else {
            binding.message.setText(R.string.message_continueondevice)
        }
    }

    private inner class WearBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                when (intent.action) {
                    ACTION_UPDATECONNECTIONSTATUS -> {
                        val connStatus = WearConnectionStatus.valueOf(
                            intent.getIntExtra(
                                EXTRA_CONNECTIONSTATUS,
                                0
                            )
                        )
                        when (connStatus) {
                            WearConnectionStatus.DISCONNECTED -> {
                                binding.message.setText(R.string.status_disconnected)
                                errorProgress()
                            }

                            WearConnectionStatus.CONNECTING -> {
                                binding.message.setText(R.string.status_connecting)
                                resetTimer()
                            }

                            WearConnectionStatus.APPNOTINSTALLED -> {
                                binding.message.setText(R.string.error_notinstalled)
                                resetTimer()

                                // Open store on remote device
                                val intentAndroid = Intent(Intent.ACTION_VIEW)
                                    .addCategory(Intent.CATEGORY_BROWSABLE)
                                    .setData(PlayStoreUtils.getPlayStoreURI())

                                runCatching {
                                    wearListenerMgr.startRemoteActivity(intentAndroid)
                                    showConfirmationOverlay(true)
                                }.onFailure {
                                    if (it !is CancellationException) {
                                        showConfirmationOverlay(false)
                                    }
                                }

                                errorProgress()
                            }

                            WearConnectionStatus.CONNECTED -> {
                                binding.message.setText(R.string.status_connected)
                                resetTimer()
                                // Continue operation
                                wearListenerMgr.sendSetupStatusRequest()
                            }
                        }
                    }

                    WearableHelper.ErrorPath -> {
                        binding.message.setText(R.string.error_syncing)
                        errorProgress()
                    }

                    WearableHelper.IsSetupPath -> {
                        val isDeviceSetup =
                            intent.getBooleanExtra(EXTRA_DEVICESETUPSTATUS, false)
                        start(isDeviceSetup)
                    }

                    WearableHelper.SettingsPath -> {
                        binding.message.setText(R.string.message_settingsretrieved)
                        settingsDataReceived = true

                        if (settingsDataReceived && locationDataReceived && weatherDataReceived)
                            successProgress()
                    }

                    WearableHelper.LocationPath -> {
                        binding.message.setText(R.string.message_locationretrieved)
                        locationDataReceived = true

                        if (settingsDataReceived && locationDataReceived && weatherDataReceived)
                            successProgress()
                    }

                    WearableHelper.WeatherPath -> {
                        binding.message.setText(R.string.message_weatherretrieved)
                        weatherDataReceived = true

                        if (settingsDataReceived && locationDataReceived && weatherDataReceived)
                            successProgress()
                    }
                }
            }
        }
    }
}
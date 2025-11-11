package com.thewizrd.simpleweather.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.concurrent.futures.await
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.phone.interactions.PhoneTypeHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableStatusCodes
import com.thewizrd.common.utils.ErrorMessage
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.store.PlayStoreUtils
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.wearable.WearableListener
import com.thewizrd.simpleweather.wearable.WearableListenerActions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

abstract class WearableListenerViewModel(private val app: Application) : AndroidViewModel(app),
    WearableListener, MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    protected val appContext: Context
        get() = app.applicationContext

    @SuppressLint("StaticFieldLeak")
    protected var activityContext: Activity? = null

    @Volatile
    protected var mPhoneNodeWithApp: Node? = null
    private var mConnectionStatus = WearConnectionStatus.CONNECTING
    var isAcceptingDataUpdates = false

    protected val remoteActivityHelper: RemoteActivityHelper = RemoteActivityHelper(appContext)

    protected val _eventsFlow = MutableSharedFlow<WearableEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<WearableEvent> = _eventsFlow

    protected val _channelEventsFlow = MutableSharedFlow<WearableEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val channelEventsFlow: SharedFlow<WearableEvent> = _channelEventsFlow

    protected val _errorMessagesFlow = MutableSharedFlow<ErrorMessage>(replay = 0)
    val errorMessagesFlow: SharedFlow<ErrorMessage> = _errorMessagesFlow

    init {
        Wearable.getCapabilityClient(appContext)
            .addListener(this, WearableHelper.CAPABILITY_PHONE_APP)
        Wearable.getMessageClient(appContext).addListener(this)
    }

    override fun initActivityContext(activity: Activity) {
        activityContext = activity
    }

    override fun onCleared() {
        super.onCleared()
        Wearable.getCapabilityClient(appContext)
            .removeListener(this, WearableHelper.CAPABILITY_PHONE_APP)
        Wearable.getMessageClient(appContext).removeListener(this)
        activityContext = null
    }

    override fun openAppOnPhone(showAnimation: Boolean) {
        viewModelScope.launch {
            connect()

            if (mPhoneNodeWithApp == null) {
                _errorMessagesFlow.tryEmit(ErrorMessage.Resource(R.string.status_node_unavailable))

                when (PhoneTypeHelper.getPhoneDeviceType(appContext)) {
                    PhoneTypeHelper.DEVICE_TYPE_ANDROID -> {
                        openPlayStore(showAnimation)
                    }

                    PhoneTypeHelper.DEVICE_TYPE_IOS -> {
                        _errorMessagesFlow.tryEmit(ErrorMessage.Resource(R.string.status_node_notsupported))
                    }

                    else -> {
                        _errorMessagesFlow.tryEmit(ErrorMessage.Resource(R.string.status_node_notsupported))
                    }
                }
            } else {
                // Send message to device to start activity
                val success = runCatching {
                    val intent = WearableHelper.createRemoteActivityIntent(
                        WearableHelper.getPackageName(),
                        "${WearableHelper.PACKAGE_NAME}.LaunchActivity"
                    )
                    startRemoteActivity(intent)
                }.getOrDefault(false)

                if (showAnimation) {
                    sendConfirmationEvent(success)
                }
            }
        }
    }

    override suspend fun openPlayStore(showAnimation: Boolean) {
        // Open store on remote device
        val intentAndroid = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(PlayStoreUtils.getPlayStoreURI())

        runCatching {
            remoteActivityHelper.startRemoteActivity(intentAndroid)
                .await()

            if (showAnimation) {
                sendConfirmationEvent(true)
            }
        }.onFailure {
            if (it !is CancellationException && showAnimation) {
                sendConfirmationEvent(false)
            }
        }
    }

    protected fun sendConfirmationEvent(success: Boolean) {
        if (success) {
            sendConfirmationEvent(ConfirmationType.OpenOnPhone)
        } else {
            sendConfirmationEvent(ConfirmationType.Failure)
        }
    }

    protected fun sendConfirmationEvent(confirmationType: ConfirmationType) {
        _eventsFlow.tryEmit(
            WearableEvent(
                WearableListenerActions.ACTION_SHOWCONFIRMATION,
                Bundle().apply {
                    putString(
                        WearableListenerActions.EXTRA_EVENTDATA,
                        JSONParser.serializer(
                            ConfirmationData(
                                confirmationType = confirmationType
                            ), ConfirmationData::class.java
                        )
                    )
                }
            )
        )
    }

    override suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String?): Boolean {
        return runCatching {
            remoteActivityHelper.startRemoteActivity(targetIntent, targetNodeId).await()
            true
        }.onFailure {
            Logger.writeLine(Log.ERROR, it, "Error starting remote activity")
        }.getOrDefault(false)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (settingsManager.getDataSync() != WearableDataSync.OFF || isAcceptingDataUpdates) {
            if (messageEvent.path == WearableHelper.IsSetupPath) {
                val data = messageEvent.data
                val isDeviceSetup = data[0] != 0.toByte()
                _eventsFlow.tryEmit(WearableEvent(WearableHelper.IsSetupPath, Bundle().apply {
                    putBoolean(WearableListenerActions.EXTRA_DEVICESETUPSTATUS, isDeviceSetup)
                    putInt(WearableListenerActions.EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
                }))
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            val connectedNodes = getConnectedNodes()
            mPhoneNodeWithApp = pickBestNodeId(capabilityInfo.nodes)

            if (mPhoneNodeWithApp == null) {
                /*
                 * If a device is disconnected from the wear network, capable nodes are empty
                 *
                 * No capable nodes can mean the app is not installed on the remote device or the
                 * device is disconnected.
                 *
                 * Verify if we're connected to any nodes; if not, we're truly disconnected
                 */
                mConnectionStatus = if (connectedNodes.isNullOrEmpty()) {
                    WearConnectionStatus.DISCONNECTED
                } else {
                    WearConnectionStatus.APPNOTINSTALLED
                }
            } else {
                if (mPhoneNodeWithApp!!.isNearby && connectedNodes.any { it.id == mPhoneNodeWithApp!!.id }) {
                    mConnectionStatus = WearConnectionStatus.CONNECTED
                } else {
                    try {
                        sendPing(mPhoneNodeWithApp!!.id)
                        mConnectionStatus = WearConnectionStatus.CONNECTED
                    } catch (e: ApiException) {
                        if (e.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                            mConnectionStatus = WearConnectionStatus.DISCONNECTED
                        } else {
                            Logger.writeLine(Log.ERROR, e)
                        }
                    }
                }
            }

            _eventsFlow.tryEmit(
                WearableEvent(
                    WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS,
                    Bundle().apply {
                        putInt(
                            WearableListenerActions.EXTRA_CONNECTIONSTATUS,
                            mConnectionStatus.value
                        )
                    })
            )
        }
    }

    override suspend fun sendSetupStatusRequest() {
        if (!connect()) {
            _eventsFlow.tryEmit(WearableEvent(WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS))
            return
        }

        try {
            Wearable.getMessageClient(appContext)
                .sendMessage(mPhoneNodeWithApp!!.id, WearableHelper.IsSetupPath, ByteArray(0))
                .await()
        } catch (e: Exception) {
            logError(e)
        }
    }

    protected suspend fun updateConnectionStatus() {
        checkConnectionStatus()

        _eventsFlow.tryEmit(
            WearableEvent(
                WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS,
                Bundle().apply {
                    putInt(WearableListenerActions.EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
                })
        )
    }

    protected suspend fun checkConnectionStatus() {
        val connectedNodes = getConnectedNodes()
        mPhoneNodeWithApp = checkIfPhoneHasApp()

        if (mPhoneNodeWithApp == null) {
            /*
             * If a device is disconnected from the wear network, capable nodes are empty
             *
             * No capable nodes can mean the app is not installed on the remote device or the
             * device is disconnected.
             *
             * Verify if we're connected to any nodes; if not, we're truly disconnected
             */
            mConnectionStatus = if (connectedNodes.isEmpty()) {
                WearConnectionStatus.DISCONNECTED
            } else {
                WearConnectionStatus.APPNOTINSTALLED
            }
        } else {
            if (mPhoneNodeWithApp!!.isNearby && connectedNodes.any { it.id == mPhoneNodeWithApp!!.id }) {
                mConnectionStatus = WearConnectionStatus.CONNECTED
            } else {
                try {
                    sendPing(mPhoneNodeWithApp!!.id)
                    mConnectionStatus = WearConnectionStatus.CONNECTED
                } catch (e: ApiException) {
                    if (e.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                        mConnectionStatus = WearConnectionStatus.DISCONNECTED
                    } else {
                        Logger.writeLine(Log.ERROR, e)
                    }
                }
            }
        }
    }

    override suspend fun getConnectionStatus(): WearConnectionStatus {
        checkConnectionStatus()
        return mConnectionStatus
    }

    protected suspend fun checkIfPhoneHasApp(): Node? {
        var node: Node? = null

        try {
            val capabilityInfo = Wearable.getCapabilityClient(appContext)
                .getCapability(
                    WearableHelper.CAPABILITY_PHONE_APP,
                    CapabilityClient.FILTER_ALL
                )
                .await()
            node = pickBestNodeId(capabilityInfo.nodes)
        } catch (e: Exception) {
            logError(e)
        }

        return node
    }

    protected suspend fun connect(): Boolean {
        if (mPhoneNodeWithApp == null)
            mPhoneNodeWithApp = checkIfPhoneHasApp()

        return mPhoneNodeWithApp != null
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    protected fun pickBestNodeId(nodes: Collection<Node>): Node? {
        var bestNode: Node? = null

        // Find a nearby node/phone or pick one arbitrarily. Realistically, there is only one phone.
        for (node in nodes) {
            if (node.isNearby) {
                return node
            }
            bestNode = node
        }
        return bestNode
    }

    private suspend fun getConnectedNodes(): List<Node> {
        try {
            return Wearable.getNodeClient(appContext)
                .connectedNodes
                .await()
        } catch (e: Exception) {
            Logger.writeLine(Log.ERROR, e)
        }

        return emptyList()
    }

    protected suspend fun sendMessage(nodeID: String, path: String, data: ByteArray?): Int? {
        try {
            return Wearable.getMessageClient(appContext)
                .sendMessage(nodeID, path, data).await()
        } catch (e: Exception) {
            if (e is ApiException || e.cause is ApiException) {
                val apiException = e.cause as? ApiException ?: e as? ApiException
                if (apiException?.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                    mConnectionStatus = WearConnectionStatus.DISCONNECTED

                    _eventsFlow.tryEmit(
                        WearableEvent(
                            WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS,
                            Bundle().apply {
                                putInt(
                                    WearableListenerActions.EXTRA_CONNECTIONSTATUS,
                                    mConnectionStatus.value
                                )
                            })
                    )
                }
            }

            Logger.writeLine(Log.ERROR, e)
        }

        return -1
    }

    @Throws(ApiException::class)
    protected suspend fun sendPing(nodeID: String) {
        try {
            Wearable.getMessageClient(appContext)
                .sendMessage(nodeID, WearableHelper.PingPath, null)
                .await()
        } catch (e: Exception) {
            if (e is ApiException || e.cause is ApiException) {
                val apiException = e.cause as? ApiException ?: e as ApiException
                throw apiException
            }
            logError(e)
        }
    }

    private fun logError(e: Exception) {
        if (e is ApiException || e.cause is ApiException) {
            val apiException = e.cause as? ApiException ?: e as? ApiException
            if (apiException?.statusCode == WearableStatusCodes.API_NOT_CONNECTED ||
                apiException?.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED
            ) {
                // Ignore this error
                return
            }
        } else if (e is CancellationException || e is InterruptedException) {
            // Ignore this error
            return
        }

        Timber.e(e)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected fun setConnectionStatus(status: WearConnectionStatus) {
        mConnectionStatus = status

        _eventsFlow.tryEmit(
            WearableEvent(
                WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS,
                Bundle().apply {
                    putInt(WearableListenerActions.EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
                })
        )
    }
}
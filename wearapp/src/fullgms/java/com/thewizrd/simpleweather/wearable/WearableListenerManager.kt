package com.thewizrd.simpleweather.wearable

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.lifecycleScope
import androidx.wear.phone.interactions.PhoneTypeHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.wearable.WearableHelper
import com.thewizrd.shared_resources.di.localBroadcastManager
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.store.PlayStoreUtils
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.helpers.showConfirmationOverlay
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_UPDATECONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_CONNECTIONSTATUS
import com.thewizrd.simpleweather.wearable.WearableListenerActions.EXTRA_DEVICESETUPSTATUS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class WearableListenerManager(
    protected val activityContext: ComponentActivity,
    protected val broadcastReceiver: BroadcastReceiver,
    protected val intentFilter: IntentFilter
) : WearableListener, OnMessageReceivedListener, OnCapabilityChangedListener {
    companion object {
        /*
         * There should only ever be one phone in a node set (much less w/ the correct capability), so
         * I am just grabbing the first one (which should be the only one).
         */
        private fun pickBestNodeId(nodes: Collection<Node>): Node? {
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
    }

    private var isReceiverRegistered = false

    @Volatile
    private var mPhoneNodeWithApp: Node? = null
    private var mConnectionStatus = WearConnectionStatus.CONNECTING
    var isAcceptingDataUpdates = false

    private val lifecycleScope = activityContext.lifecycleScope

    private val remoteActivityHelper = RemoteActivityHelper(activityContext)

    /**
     * Register listeners before fragments are started
     */
    override fun onStart() {
        Wearable.getCapabilityClient(activityContext)
            .addListener(this, WearableHelper.CAPABILITY_PHONE_APP)
        Wearable.getMessageClient(activityContext).addListener(this)

        if (!isReceiverRegistered) {
            localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
        }

        lifecycleScope.launch { checkConnectionStatus() }
    }

    override fun onResume() {
        if (!isReceiverRegistered) {
            localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    /**
     * Unregister listeners before pause
     */
    override fun onPause() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
        isReceiverRegistered = false

        Wearable.getCapabilityClient(activityContext)
            .removeListener(this, WearableHelper.CAPABILITY_PHONE_APP)
        Wearable.getMessageClient(activityContext).removeListener(this)
    }

    override fun openAppOnPhone(showAnimation: Boolean) {
        lifecycleScope.launch {
            connect()

            if (mPhoneNodeWithApp == null) {
                Toast.makeText(
                    activityContext,
                    R.string.status_node_unavailable,
                    Toast.LENGTH_SHORT
                ).show()

                when (PhoneTypeHelper.getPhoneDeviceType(activityContext)) {
                    PhoneTypeHelper.DEVICE_TYPE_ANDROID -> {
                        val intentAndroid = Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(PlayStoreUtils.getPlayStoreURI())

                        runCatching {
                            remoteActivityHelper.startRemoteActivity(intentAndroid)
                                .await()

                            activityContext.showConfirmationOverlay(true)
                        }.onFailure {
                            if (it !is CancellationException) {
                                activityContext.showConfirmationOverlay(false)
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(
                            activityContext,
                            R.string.status_node_notsupported,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Send message to device to start activity
                val result = sendMessage(
                    mPhoneNodeWithApp!!.id,
                    WearableHelper.StartActivityPath,
                    ByteArray(0)
                )

                val success = result != -1

                if (showAnimation) {
                    launch(Dispatchers.Main) {
                        ConfirmationOverlay()
                            .setType(if (success) ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION else ConfirmationOverlay.FAILURE_ANIMATION)
                            .showOn(activityContext)
                    }
                }
            }
        }
    }

    override fun openPlayStoreOnPhone(showAnimation: Boolean) {
        val intentAndroid = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(PlayStoreUtils.getPlayStoreURI())

        lifecycleScope.launch {
            runCatching {
                startRemoteActivity(intentAndroid)
                if (showAnimation) {
                    activityContext.showConfirmationOverlay(true)
                }
            }.onFailure {
                if (showAnimation && it !is CancellationException) {
                    activityContext.showConfirmationOverlay(false)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (settingsManager.getDataSync() != WearableDataSync.OFF || isAcceptingDataUpdates) {
            if (messageEvent.path == WearableHelper.IsSetupPath) {
                val data = messageEvent.data
                val isDeviceSetup = data[0] != 0.toByte()
                localBroadcastManager.sendBroadcast(
                    Intent(WearableHelper.IsSetupPath)
                        .putExtra(EXTRA_DEVICESETUPSTATUS, isDeviceSetup)
                        .putExtra(EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
                )
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        lifecycleScope.launch(Dispatchers.Default) {
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

            localBroadcastManager.sendBroadcast(
                Intent(ACTION_UPDATECONNECTIONSTATUS)
                    .putExtra(EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
            )
        }
    }

    override suspend fun sendSetupStatusRequest() {
        if (!connect()) {
            localBroadcastManager.sendBroadcast(Intent(WearableHelper.ErrorPath))
            return
        }

        try {
            Wearable.getMessageClient(activityContext)
                .sendMessage(mPhoneNodeWithApp!!.id, WearableHelper.IsSetupPath, ByteArray(0))
                .await()
        } catch (e: Exception) {
            logError(e)
        }
    }

    override suspend fun updateConnectionStatus() {
        checkConnectionStatus()

        localBroadcastManager.sendBroadcast(
            Intent(ACTION_UPDATECONNECTIONSTATUS)
                .putExtra(EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
        )
    }

    override suspend fun checkConnectionStatus() {
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
    }

    override suspend fun getConnectionStatus(): WearConnectionStatus {
        checkConnectionStatus()
        return mConnectionStatus
    }

    private suspend fun checkIfPhoneHasApp(): Node? = withContext(Dispatchers.IO) {
        var node: Node? = null
        try {
            val capabilityInfo = Wearable.getCapabilityClient(activityContext)
                .getCapability(
                    WearableHelper.CAPABILITY_PHONE_APP,
                    CapabilityClient.FILTER_ALL
                )
                .await()
            node = pickBestNodeId(capabilityInfo.nodes)
        } catch (e: Exception) {
            logError(e)
        }
        return@withContext node
    }

    private suspend fun connect(): Boolean {
        if (mPhoneNodeWithApp == null)
            mPhoneNodeWithApp = checkIfPhoneHasApp()

        return mPhoneNodeWithApp != null
    }

    private suspend fun getConnectedNodes(): List<Node> {
        try {
            return Wearable.getNodeClient(activityContext)
                .connectedNodes
                .await()
        } catch (e: Exception) {
            Logger.writeLine(Log.ERROR, e)
        }

        return emptyList()
    }

    private suspend fun sendMessage(nodeID: String, path: String, data: ByteArray?): Int? {
        try {
            return Wearable.getMessageClient(activityContext)
                .sendMessage(nodeID, path, data).await()
        } catch (e: Exception) {
            if (e is ApiException || e.cause is ApiException) {
                val apiException = e.cause as? ApiException ?: e as? ApiException
                if (apiException?.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                    mConnectionStatus = WearConnectionStatus.DISCONNECTED

                    localBroadcastManager.sendBroadcast(
                        Intent(ACTION_UPDATECONNECTIONSTATUS)
                            .putExtra(EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
                    )
                }
            }

            Logger.writeLine(Log.ERROR, e)
        }

        return -1
    }

    @Throws(ApiException::class)
    private suspend fun sendPing(nodeID: String) = withContext(Dispatchers.IO) {
        try {
            Wearable.getMessageClient(activityContext)
                .sendMessage(nodeID, WearableHelper.PingPath, null)
                .await()
        } catch (e: Exception) {
            if (e is ApiException) {
                throw e
            }
            if (e.cause is ApiException) {
                throw e.cause as ApiException
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
    override fun setConnectionStatus(status: WearConnectionStatus) {
        mConnectionStatus = status

        localBroadcastManager.sendBroadcast(
            Intent(ACTION_UPDATECONNECTIONSTATUS)
                .putExtra(EXTRA_CONNECTIONSTATUS, mConnectionStatus.value)
        )
    }

    override suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String?) {
        remoteActivityHelper.startRemoteActivity(targetIntent, targetNodeId).await()
    }
}
package com.thewizrd.simpleweather.wearable

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import com.thewizrd.common.wearable.WearConnectionStatus

class WearableListenerManager(
    protected val activityContext: ComponentActivity,
    protected val broadcastReceiver: BroadcastReceiver,
    protected val intentFilter: IntentFilter
) : WearableListener {
    override fun onStart() {
        // no-op
    }

    override fun onResume() {
        // no-op
    }

    override fun onPause() {
        // no-op
    }

    override fun openAppOnPhone(showAnimation: Boolean) {
        // no-op
    }

    override fun openPlayStoreOnPhone(showAnimation: Boolean) {
        // no-op
    }

    override suspend fun sendSetupStatusRequest() {
        // no-op
    }

    override suspend fun updateConnectionStatus() {
        // no-op
    }

    override suspend fun checkConnectionStatus() {
        // no-op
    }

    override suspend fun getConnectionStatus(): WearConnectionStatus =
        WearConnectionStatus.APPNOTINSTALLED

    override fun setConnectionStatus(status: WearConnectionStatus) {
        // no-op
    }

    override suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String?) {
        // no-op
    }
}
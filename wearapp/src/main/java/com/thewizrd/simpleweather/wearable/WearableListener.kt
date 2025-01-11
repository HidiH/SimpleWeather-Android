package com.thewizrd.simpleweather.wearable

import android.content.Intent
import com.thewizrd.common.wearable.WearConnectionStatus

interface WearableListener {
    fun onStart()
    fun onResume()
    fun onPause()

    fun openAppOnPhone(showAnimation: Boolean = true)
    fun openPlayStoreOnPhone(showAnimation: Boolean = true)

    suspend fun sendSetupStatusRequest()
    suspend fun updateConnectionStatus()
    suspend fun checkConnectionStatus()
    suspend fun getConnectionStatus(): WearConnectionStatus
    fun setConnectionStatus(status: WearConnectionStatus)
    suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String? = null)
}
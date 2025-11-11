package com.thewizrd.simpleweather.wearable

import android.app.Activity
import android.content.Intent
import com.thewizrd.common.wearable.WearConnectionStatus

interface WearableListener {
    fun initActivityContext(activity: Activity)

    fun openAppOnPhone(showAnimation: Boolean = true)
    suspend fun openPlayStore(showAnimation: Boolean = true)
    suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String? = null): Boolean

    suspend fun sendSetupStatusRequest()
    suspend fun getConnectionStatus(): WearConnectionStatus
}
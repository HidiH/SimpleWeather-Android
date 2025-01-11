package com.thewizrd.simpleweather.wearable

import android.content.Context

class WeatherDataSyncWorker(
    private val context: Context,
    private val onLoadData: () -> Unit,
    private val onCancelSync: () -> Unit
) {
    fun cancelDataSync() {}
    fun startSyncTimer() {}
    fun registerSyncReceiver() {}
    fun unRegisterSyncReceiver() {}
    fun close() {}
}
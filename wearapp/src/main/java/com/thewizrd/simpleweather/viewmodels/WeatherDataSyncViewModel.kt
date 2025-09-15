package com.thewizrd.simpleweather.viewmodels

data class WeatherDataSyncState(
    val showDisconnectedView: Boolean = false,
    val isSyncInProgress: Boolean = false
)
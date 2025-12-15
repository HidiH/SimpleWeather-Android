package com.thewizrd.simpleweather.viewmodels

import androidx.annotation.StringRes
import com.thewizrd.simpleweather.R

data class SetupSyncState(
    private val settingsDataReceived: Boolean = false,
    private val locationDataReceived: Boolean = false,
    private val weatherDataReceived: Boolean = false,
    @StringRes val messageStringResId: Int = R.string.message_gettingstatus,
    val progressBarState: ProgressBarState = ProgressBarState(isIndeterminate = true)
) {
    fun isSyncComplete(): Boolean {
        return settingsDataReceived && locationDataReceived && weatherDataReceived
    }
}

data class ProgressBarState(
    val isIndeterminate: Boolean = true,
    val timeInMillis: Int = 1000
)
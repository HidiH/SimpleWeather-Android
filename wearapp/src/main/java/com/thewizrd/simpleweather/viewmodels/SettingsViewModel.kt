package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : WearableListenerViewModel(app) {
    fun requestConnectionStatus() {
        viewModelScope.launch {
            updateConnectionStatus()
        }
    }
}
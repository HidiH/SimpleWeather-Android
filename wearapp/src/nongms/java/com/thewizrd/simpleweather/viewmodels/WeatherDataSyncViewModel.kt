package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WeatherDataSyncViewModel(app: Application) : WearableListenerViewModel(app),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val viewModelState = MutableStateFlow(WeatherDataSyncState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    fun syncWeather() {
        // no-op
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
    }
}
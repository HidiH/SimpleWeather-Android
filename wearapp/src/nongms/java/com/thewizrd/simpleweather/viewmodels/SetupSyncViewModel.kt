package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SetupSyncViewModel(app: Application) : WearableListenerViewModel(app) {
    private val viewModelState = MutableStateFlow(SetupSyncState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )
}
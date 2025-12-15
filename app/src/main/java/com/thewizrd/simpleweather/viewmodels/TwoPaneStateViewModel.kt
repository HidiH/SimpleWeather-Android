package com.thewizrd.simpleweather.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class TwoPaneState(
    val isSideBySide: Boolean = false,
    val isOpened: Boolean = false
)

class TwoPaneStateViewModel : ViewModel() {
    private val viewModelState = MutableStateFlow(TwoPaneState())

    val twoPaneState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )

    fun updateSideBySide(isSideBySide: Boolean) {
        viewModelState.update {
            it.copy(isSideBySide = isSideBySide)
        }
    }

    fun updateIsOpened(isOpened: Boolean) {
        viewModelState.update {
            it.copy(isOpened = isOpened)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
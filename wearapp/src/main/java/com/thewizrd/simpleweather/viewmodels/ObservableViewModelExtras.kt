package com.thewizrd.simpleweather.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.databinding.Observable
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun <T> Observable.observeAsState(propertyId: Int, block: () -> T): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(block()) }

    DisposableEffect(this, lifecycleOwner) {
        val callback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propId: Int) {
                if (propertyId == propId) {
                    state.value = block()
                }
            }
        }
        addOnPropertyChangedCallback(callback)
        onDispose {
            removeOnPropertyChangedCallback(callback)
        }
    }

    return state
}
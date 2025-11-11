package com.thewizrd.simpleweather.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.thewizrd.common.utils.ErrorMessage
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.simpleweather.wearable.WearableListener
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class WearableListenerViewModel(private val app: Application) : AndroidViewModel(app),
    WearableListener {
    protected val _eventsFlow = MutableSharedFlow<WearableEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<WearableEvent> = _eventsFlow

    protected val _channelEventsFlow = MutableSharedFlow<WearableEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val channelEventsFlow: SharedFlow<WearableEvent> = _channelEventsFlow

    protected val _errorMessagesFlow = MutableSharedFlow<ErrorMessage>(replay = 0)
    val errorMessagesFlow: SharedFlow<ErrorMessage> = _errorMessagesFlow

    override fun initActivityContext(activity: Activity) {
        // no-op
    }

    override fun openAppOnPhone(showAnimation: Boolean) {
        // no-op
    }

    override suspend fun openPlayStore(showAnimation: Boolean) {
        // no-op
    }

    override suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String?): Boolean {
        return false
    }

    override suspend fun sendSetupStatusRequest() {
        // no-op
    }

    protected suspend fun updateConnectionStatus() {

    }

    override suspend fun getConnectionStatus(): WearConnectionStatus =
        WearConnectionStatus.APPNOTINSTALLED
}
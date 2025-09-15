package com.thewizrd.simpleweather.wearable

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.simpleweather.wearable.WearableListener
import com.thewizrd.simpleweather.wearable.WearableListenerActions

abstract class WearableListenerViewModel(private val app: Application) : AndroidViewModel(app),
    WearableListener {
    override fun initActivityContext(activity: Activity) {
        // no-op
    }

    override fun openAppOnPhone(activity: Activity, showAnimation: Boolean) {
        // no-op
    }

    override fun openPlayStore(activity: Activity, showAnimation: Boolean) {
        // no-op
    }

    override suspend fun startRemoteActivity(targetIntent: Intent, targetNodeId: String?): Boolean {
        return false
    }

    override suspend fun sendSetupStatusRequest() {
        // no-op
    }

    override suspend fun getConnectionStatus(): WearConnectionStatus =
        WearConnectionStatus.APPNOTINSTALLED
}
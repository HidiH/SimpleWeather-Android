package com.thewizrd.shared_resources.utils

import android.util.Log
import com.thewizrd.shared_resources.BuildConfig
import com.thewizrd.shared_resources.utils.Logger.DEBUG_MODE_ENABLED
import timber.log.Timber

class AndroidLoggingTree : Timber.DebugTree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return if (BuildConfig.DEBUG || DEBUG_MODE_ENABLED) {
            true
        } else {
            priority > Log.DEBUG
        }
    }
}
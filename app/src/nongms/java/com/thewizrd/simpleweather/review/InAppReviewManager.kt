package com.thewizrd.simpleweather.review

import android.app.Activity
import android.content.Context

class InAppReviewManager private constructor(context: Context) {
    companion object {
        @JvmStatic
        fun create(context: Context): InAppReviewManager {
            return InAppReviewManager(context)
        }
    }

    fun shouldShowReviewFlow(): Boolean = false

    suspend fun showReviewFlow(activity: Activity) {
        // no-op
    }

    private fun resetReviewPrompt() {
        // no-op
    }

    fun incrementCounter() {
        // no-op
    }
}
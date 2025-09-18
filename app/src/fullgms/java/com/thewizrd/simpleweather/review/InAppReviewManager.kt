package com.thewizrd.simpleweather.review

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

class InAppReviewManager private constructor(context: Context) {
    companion object {
        private const val TAG = "InAppReviewManager"

        private const val KEY_IN_APP_REVIEW = "in_app_review"
        private const val KEY_REVIEW_COUNTER = "iar_counter"
        private const val KEY_NEXT_PROMPT_DATE = "next_prompt_date"

        private const val PROMPT_THRESHOLD = 20

        @JvmStatic
        fun create(context: Context): InAppReviewManager {
            return InAppReviewManager(context)
        }
    }

    private val prefs =
        context.applicationContext.getSharedPreferences(KEY_IN_APP_REVIEW, Context.MODE_PRIVATE)
    private val manager = ReviewManagerFactory.create(context)

    fun shouldShowReviewFlow(): Boolean {
        return runCatching {
            prefs.run {
                val now = Instant.now().epochSecond
                getInt(KEY_REVIEW_COUNTER, 0) >= PROMPT_THRESHOLD && now > getLong(
                    KEY_NEXT_PROMPT_DATE,
                    0
                )
            }
        }.getOrDefault(false)
    }

    suspend fun showReviewFlow(activity: Activity) {
        runCatching {
            val reviewInfo = manager.requestReview()
            if (coroutineContext.isActive) {
                manager.launchReview(activity, reviewInfo)
                resetReviewPrompt()
            }
        }
    }

    private fun resetReviewPrompt() {
        prefs.edit {
            val nextDate = Instant.now().plus(90, ChronoUnit.DAYS).epochSecond

            putInt(KEY_REVIEW_COUNTER, 0)
            putLong(KEY_NEXT_PROMPT_DATE, nextDate)
        }
    }

    fun incrementCounter() {
        val count = prefs.getInt(KEY_REVIEW_COUNTER, 0)
        prefs.edit {
            putInt(KEY_REVIEW_COUNTER, count + 1)
        }
    }
}
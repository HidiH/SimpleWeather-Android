package com.thewizrd.simpleweather.review

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.thewizrd.shared_resources.utils.AnalyticsLogger
import com.thewizrd.simpleweather.BuildConfig
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

class InAppReviewManager private constructor(context: Context) {
    companion object {
        private const val TAG = "InAppReviewManager"

        private const val KEY_IN_APP_REVIEW = "in_app_review"
        private const val KEY_REVIEW_COUNTER = "iar_counter"
        private const val KEY_NEXT_PROMPT_DATE = "next_prompt_date"
        private const val KEY_LAST_PROMPT_DATE = "last_prompt_date"

        private const val PROMPT_THRESHOLD = 25

        @JvmStatic
        fun create(context: Context): InAppReviewManager {
            return InAppReviewManager(context)
        }
    }

    private val prefs =
        context.applicationContext.getSharedPreferences(KEY_IN_APP_REVIEW, Context.MODE_PRIVATE)
    private val manager: ReviewManager = if (BuildConfig.DEBUG) {
        FakeReviewManager(context)
    } else {
        ReviewManagerFactory.create(context)
    }

    fun shouldShowReviewFlow(): Boolean {
        return runCatching {
            prefs.run {
                val now = Instant.now(Clock.systemUTC())
                    .epochSecond

                val reviewPromptCounter = getInt(KEY_REVIEW_COUNTER, 0)
                val nextPromptEpochDate = getLong(KEY_NEXT_PROMPT_DATE, 0)

                val shouldShowReview =
                    reviewPromptCounter >= PROMPT_THRESHOLD && now > nextPromptEpochDate

                AnalyticsLogger.logEvent("${TAG}_shouldShowReview", Bundle().apply {
                    putInt(KEY_REVIEW_COUNTER, reviewPromptCounter)
                    putLong("current_date", now)
                    putLong(KEY_NEXT_PROMPT_DATE, nextPromptEpochDate)
                    putBoolean("criteria_met", shouldShowReview)
                })

                shouldShowReview
            }
        }.getOrDefault(false)
    }

    suspend fun showReviewFlow(activity: Activity) {
        runCatching {
            val reviewInfo = manager.requestReview()
            if (coroutineContext.isActive) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(activity, "Showing review flow", Toast.LENGTH_LONG).show()
                }

                AnalyticsLogger.logEvent("${TAG}_showReview", Bundle().apply {
                    putInt(KEY_REVIEW_COUNTER, prefs.getInt(KEY_REVIEW_COUNTER, 0))
                    putLong(KEY_LAST_PROMPT_DATE, prefs.getLong(KEY_NEXT_PROMPT_DATE, 0))
                })

                runCatching {
                    manager.launchReview(activity, reviewInfo)
                }

                resetReviewPrompt()
            }
        }
    }

    private fun resetReviewPrompt() {
        prefs.edit {
            val nextDate = Instant.now(Clock.systemUTC())
                .plus(90, ChronoUnit.DAYS)
                .epochSecond

            putInt(KEY_REVIEW_COUNTER, 0)
            putLong(KEY_NEXT_PROMPT_DATE, nextDate)

            AnalyticsLogger.logEvent("${TAG}_reset", Bundle().apply {
                putInt(KEY_REVIEW_COUNTER, 0)
                putLong(KEY_NEXT_PROMPT_DATE, nextDate)
            })
        }
    }

    fun incrementCounter() {
        val count = prefs.getInt(KEY_REVIEW_COUNTER, 0)

        if (count <= PROMPT_THRESHOLD) {
            prefs.edit {
                putInt(KEY_REVIEW_COUNTER, count + 1)
            }
        }

        AnalyticsLogger.logEvent("${TAG}_incrementCounter", Bundle().apply {
            putInt(KEY_REVIEW_COUNTER, count + 1)
        })
    }
}
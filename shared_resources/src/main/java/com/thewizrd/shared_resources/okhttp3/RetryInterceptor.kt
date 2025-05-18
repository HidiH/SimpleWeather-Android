package com.thewizrd.shared_resources.okhttp3

import com.thewizrd.shared_resources.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.IOException
import java.net.HttpURLConnection
import kotlin.math.pow

class RetryPolicyInterceptor : Interceptor {
    companion object {
        private const val TAG = "RetryPolicyInterceptor"

        private val RETRYABLE_STATUSES = setOf(
            HttpURLConnection.HTTP_INTERNAL_ERROR,
            HttpURLConnection.HTTP_BAD_GATEWAY,
            //HttpURLConnection.HTTP_UNAVAILABLE, -- RetryAndFollowUpInterceptor
            HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
        )

        private const val DEFAULT_RETRY_COUNT = 2
        private const val DEFAULT_RETRY_DELAY_MS = 300

        private const val HEADER_RETRY_COUNT = "X-Retry-Count"
        private const val HEADER_RETRY_AFTER = "Retry-After"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        val retryDelay = (response.header(HEADER_RETRY_AFTER)?.toIntOrNull()?.times(1000)
            ?: DEFAULT_RETRY_DELAY_MS).coerceAtMost(10000)
        val retryCount = (request.header(HEADER_RETRY_COUNT)?.toIntOrNull() ?: DEFAULT_RETRY_COUNT)

        var tryCount = 0

        while ((!response.isSuccessful && response.code in RETRYABLE_STATUSES) && tryCount < retryCount) {
            response.closeQuietly()

            val expDelay = retryDelay * 2f.pow(tryCount)
            tryCount++

            runBlocking {
                try {
                    delay(expDelay.toLong())
                    response = chain.proceed(request.newBuilder().build())
                    Logger.debug(
                        TAG,
                        "retried request - tryCount = $tryCount | host: ${request.url.host} | statusCode: ${response.code}"
                    )
                } catch (e: IOException) {
                    Logger.error(TAG, e)
                }
            }
        }

        return response
    }
}
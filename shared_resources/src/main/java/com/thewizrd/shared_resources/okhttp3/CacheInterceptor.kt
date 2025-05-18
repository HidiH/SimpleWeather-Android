package com.thewizrd.shared_resources.okhttp3

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheInterceptor : Interceptor {
    companion object {
        private const val CACHE_CONTROL_HEADER = "Cache-Control"
        private const val CACHE_CONTROL_NO_CACHE = "no-cache"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val shouldUseCache =
            !CACHE_CONTROL_NO_CACHE.equals(request.header(CACHE_CONTROL_HEADER), ignoreCase = true)

        if (!shouldUseCache) {
            return response
        }

        val hasCacheHeader = !request.header(CACHE_CONTROL_HEADER).isNullOrEmpty()

        // Override server cache protocol
        val builder = response.newBuilder()
            .removeHeader("Pragma")

        if (!hasCacheHeader) {
            // If original response does not contain a Cache-Control header
            // cache the response for a minimum of 2 min to avoid repeat requests
            val cacheControl = CacheControl.Builder()
                .maxAge(2, TimeUnit.MINUTES)
                .build()

            builder.header(CACHE_CONTROL_HEADER, cacheControl.toString())
        } else {
            builder.header(CACHE_CONTROL_HEADER, request.cacheControl.toString())
        }

        return builder.build()
    }
}
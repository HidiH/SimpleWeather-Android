package com.thewizrd.weather_api.google.pollen

import android.util.Log
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.okhttp3.OkHttp3Utils.await
import com.thewizrd.shared_resources.okhttp3.OkHttp3Utils.getStream
import com.thewizrd.shared_resources.sharedDeps
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.weatherdata.PollenProvider
import com.thewizrd.shared_resources.weatherdata.WeatherAPI
import com.thewizrd.shared_resources.weatherdata.model.Pollen
import com.thewizrd.shared_resources.weatherdata.model.Pollen.PollenCount
import com.thewizrd.weather_api.google.utils.addGoogleAuth
import com.thewizrd.weather_api.keys.Keys
import com.thewizrd.weather_api.utils.APIRequestUtils
import com.thewizrd.weather_api.utils.APIRequestUtils.addUserAgent
import com.thewizrd.weather_api.utils.APIRequestUtils.checkForErrors
import com.thewizrd.weather_api.utils.RateLimitedRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class GooglePollenProvider : PollenProvider, RateLimitedRequest {
    companion object {
        private const val QUERY_URL =
            "https://pollen.googleapis.com/v1/forecast:lookup?location.latitude=%s&location.longitude=%s&days=1&languageCode=en&plantsDescription=0&key=%s"
        private const val API_ID = WeatherAPI.GOOGLE
    }

    override fun getRetryTime(): Long {
        return 60000 // 1 min
    }

    override suspend fun getPollenData(location: LocationData): Pollen? =
        withContext(Dispatchers.IO) {
            var pollenData: Pollen? = null

            val key = Keys.getGPollenKey()

            if (key.isNullOrBlank()) return@withContext null

            val client = sharedDeps.httpClient
            var response: Response? = null

            try {
                // If were under rate limit, deny request
                APIRequestUtils.checkRateLimit(API_ID)

                val context = sharedDeps.context

                val df = DecimalFormat.getInstance(Locale.ROOT) as DecimalFormat
                df.applyPattern("0.####")

                val request = Request.Builder()
                    .cacheControl(
                        CacheControl.Builder()
                            .maxAge(12, TimeUnit.HOURS)
                            .build()
                    )
                    .url(
                        String.format(
                            Locale.ROOT,
                            QUERY_URL,
                            df.format(location.latitude),
                            df.format(location.longitude),
                            key
                        )
                    )
                    .addUserAgent(context)
                    .addGoogleAuth(context)
                    .build()

                // Connect to webstream
                response = client.newCall(request).await()
                response.checkForErrors(API_ID)

                val stream = response.getStream()

                // Load data
                val root =
                    JSONParser.deserializer<PollenResponse>(stream, PollenResponse::class.java)

                root?.let {
                    val pollenTypeInfo = checkNotNull(it.dailyInfo?.first()?.pollenTypeInfo)

                    pollenData = Pollen().apply {
                        pollenTypeInfo.filterNotNull().forEach { pollenItem ->
                            when (pollenItem.code) {
                                "TREE" -> treePollenCount = pollenItem.indexInfo.toPollenCount()
                                "GRASS" -> grassPollenCount = pollenItem.indexInfo.toPollenCount()
                                "WEED" -> ragweedPollenCount = pollenItem.indexInfo.toPollenCount()
                            }
                        }

                        attribution = "Google"
                    }
                }

                // End Stream
                stream.closeQuietly()
            } catch (ex: Exception) {
                pollenData = null
                Logger.writeLine(
                    Log.ERROR,
                    ex,
                    "GooglePollenProvider: error getting pollen data"
                )
            } finally {
                response?.closeQuietly()
            }

            return@withContext pollenData
        }

    private fun IndexInfo?.toPollenCount(): PollenCount = when (this?.value) {
        1, 2 -> PollenCount.LOW
        3 -> PollenCount.MODERATE
        4 -> PollenCount.HIGH
        5 -> PollenCount.VERY_HIGH
        else -> PollenCount.UNKNOWN
    }
}
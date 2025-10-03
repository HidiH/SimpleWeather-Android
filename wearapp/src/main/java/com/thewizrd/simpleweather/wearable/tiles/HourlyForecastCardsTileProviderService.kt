package com.thewizrd.simpleweather.wearable.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.weatherdata.model.Weather
import com.thewizrd.simpleweather.wearable.tiles.layouts.hourlyForecastPillsTileLayout
import com.thewizrd.weather_api.weatherModule
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HourlyForecastPillsTileProviderService : WeatherCoroutinesTileService() {
    companion object {
        private const val TAG = "HourlyForecastPillsTileProviderService"
        private const val MAX_FORECAST_LENGTH = 3
    }

    override fun renderTile(
        weather: Weather?,
        requestParams: RequestBuilders.TileRequest
    ): LayoutElementBuilders.LayoutElement {
        resources.clear()
        resources.add("${ID_WEATHER_ICON_PREFIX}${weather?.condition?.icon ?: WeatherIcons.NA}")

        // Add forecast icons to resources
        weather?.hrForecast?.take(MAX_FORECAST_LENGTH)?.forEach { forecast ->
            resources.add("${ID_WEATHER_ICON_PREFIX}${forecast.icon ?: WeatherIcons.NA}")
        }

        return hourlyForecastPillsTileLayout(weather, this, requestParams)
    }

    override suspend fun getWeather(): Weather? {
        val weather = super.getWeather()

        if (weather != null && weather.hrForecast.isNullOrEmpty()) {
            val locationData = settingsManager.getHomeData()

            if (locationData?.isValid == true) {
                if (weather.hrForecast.isNullOrEmpty()) {
                    val now = ZonedDateTime.now().withZoneSameInstant(locationData.tzOffset)
                    val hrInterval = weatherModule.weatherManager.getHourlyForecastInterval()

                    val hrforecasts =
                        settingsManager.getHourlyForecastsByQueryOrderByDateByLimitFilterByDate(
                            locationData.query,
                            MAX_FORECAST_LENGTH,
                            now.minusHours((hrInterval * 0.5).toLong())
                                .truncatedTo(ChronoUnit.HOURS)
                        )

                    weather.hrForecast = hrforecasts.take(MAX_FORECAST_LENGTH)
                }
            }
        }

        return weather
    }
}
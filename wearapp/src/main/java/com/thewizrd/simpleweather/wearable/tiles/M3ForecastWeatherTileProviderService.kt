package com.thewizrd.simpleweather.wearable.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.weatherdata.model.Weather
import com.thewizrd.simpleweather.wearable.tiles.layouts.m3ForecastWeatherTileLayout

class M3ForecastWeatherTileProviderService : WeatherCoroutinesTileService() {
    companion object {
        private const val TAG = "M3ForecastWeatherTileProviderService"
        private const val FORECAST_LENGTH = 3
    }

    override fun renderTile(
        weather: Weather?,
        requestParams: RequestBuilders.TileRequest
    ): LayoutElementBuilders.LayoutElement {
        resources.clear()
        resources.add("${ID_WEATHER_ICON_PREFIX}${weather?.condition?.icon ?: WeatherIcons.NA}")

        // Add forecast icons to resources
        weather?.forecast?.take(FORECAST_LENGTH)?.forEach { forecast ->
            resources.add("${ID_WEATHER_ICON_PREFIX}${forecast.icon ?: WeatherIcons.NA}")
        }

        return m3ForecastWeatherTileLayout(weather, this, requestParams)
    }

    override suspend fun getWeather(): Weather? {
        val weather = super.getWeather()

        if (weather != null && weather.forecast.isNullOrEmpty()) {
            val locationData = settingsManager.getHomeData()

            if (locationData?.isValid == true) {
                if (weather.forecast.isNullOrEmpty()) {
                    val forecasts = settingsManager.getWeatherForecastData(locationData.query)
                    weather.forecast = forecasts?.forecast?.take(FORECAST_LENGTH)
                }
            }
        }

        return weather
    }
}
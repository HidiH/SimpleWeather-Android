package com.thewizrd.weather_api.nws.points

import com.thewizrd.shared_resources.DateTimeConstants
import com.thewizrd.shared_resources.utils.getWindDirection
import com.thewizrd.weather_api.nws.hourly.HourlyForecastResponse
import com.thewizrd.weather_api.nws.hourly.PeriodsItem
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun HourlyPointsResponse.toResponse(pointsResponse: PointsResponse): HourlyForecastResponse {
    val timeFormatter = DateTimeFormatter.ofPattern("h a")
    val periodNameFormatter =
        DateTimeFormatter.ofPattern(DateTimeConstants.DAY_OF_THE_WEEK, Locale.US)

    return HourlyForecastResponse().apply {
        creationDate = updateTime
        periodsItems = periods?.map {
            PeriodsItem().apply {
                val dt = ZonedDateTime.parse(it.startTime!!)

                this.time = listOf(dt.format(timeFormatter).lowercase())
                this.unixtime = listOf(dt.toInstant().epochSecond.toString())
                this.periodName = if (!it.name.isNullOrBlank()) {
                    it.name
                } else {
                    periodNameFormatter.format(dt) + (if (it.isDaytime == true) " Night" else "")
                }
                this.windChill = listOf(null)
                this.windGust = listOf(null)
                this.pop = it.probabilityOfPrecipitation?.value.let {
                    listOf(it?.toString())
                }
                this.iconLink = listOf(it.icon)
                this.relativeHumidity = it.relativeHumidity?.value.let {
                    listOf(it?.toString())
                }
                this.temperature = listOf(it.temperature?.toString())
                this.weather = listOf(it.shortForecast)
                this.windDirection =
                    listOf(it.windDirection?.let { dir -> getWindDirection(dir).toString() })
                this.windSpeed = listOf(it.windSpeed?.split(" mph")?.firstOrNull())
                this.cloudAmount = listOf(null)
            }
        }
    }
}
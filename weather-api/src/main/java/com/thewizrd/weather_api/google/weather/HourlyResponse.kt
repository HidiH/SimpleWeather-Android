package com.thewizrd.weather_api.google.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HourlyResponse(

    @Json(name = "timeZone")
    var timeZone: TimeZone? = null,

    @Json(name = "forecastHours")
    var forecastHours: List<ForecastHoursItem>? = null,

    @Json(name = "nextPageToken")
    var nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class WetBulbTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Any? = null
)

@JsonClass(generateAdapter = true)
data class ForecastHoursItem(

    @Json(name = "displayDateTime")
    var displayDateTime: DisplayDateTime? = null,

    @Json(name = "temperature")
    var temperature: Temperature? = null,

    @Json(name = "interval")
    var interval: Interval? = null,

    @Json(name = "isDaytime")
    var isDaytime: Boolean? = null,

    @Json(name = "weatherCondition")
    var weatherCondition: WeatherCondition? = null,

    @Json(name = "windChill")
    var windChill: WindChill? = null,

    @Json(name = "iceThickness")
    var iceThickness: IceThickness? = null,

    @Json(name = "visibility")
    var visibility: Visibility? = null,

    @Json(name = "airPressure")
    var airPressure: AirPressure? = null,

    @Json(name = "thunderstormProbability")
    var thunderstormProbability: Int? = null,

    @Json(name = "heatIndex")
    var heatIndex: HeatIndex? = null,

    @Json(name = "cloudCover")
    var cloudCover: Int? = null,

    @Json(name = "dewPoint")
    var dewPoint: DewPoint? = null,

    @Json(name = "feelsLikeTemperature")
    var feelsLikeTemperature: FeelsLikeTemperature? = null,

    @Json(name = "precipitation")
    var precipitation: Precipitation? = null,

    @Json(name = "relativeHumidity")
    var relativeHumidity: Int? = null,

    @Json(name = "uvIndex")
    var uvIndex: Float? = null,

    @Json(name = "wetBulbTemperature")
    var wetBulbTemperature: WetBulbTemperature? = null,

    @Json(name = "wind")
    var wind: Wind? = null
)

@JsonClass(generateAdapter = true)
data class DisplayDateTime(

    @Json(name = "hours")
    var hours: Int? = null,

    @Json(name = "utcOffset")
    var utcOffset: String? = null,

    @Json(name = "month")
    var month: Int? = null,

    @Json(name = "year")
    var year: Int? = null,

    @Json(name = "day")
    var day: Int? = null
)
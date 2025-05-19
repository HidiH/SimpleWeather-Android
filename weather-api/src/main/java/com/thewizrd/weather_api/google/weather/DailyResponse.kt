package com.thewizrd.weather_api.google.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DailyResponse(

    @Json(name = "forecastDays")
    var forecastDays: List<ForecastDaysItem>? = null,

    @Json(name = "timeZone")
    var timeZone: TimeZone? = null,

    @Json(name = "nextPageToken")
    var nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class FeelsLikeMaxTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class NighttimeForecast(

    @Json(name = "precipitation")
    var precipitation: Precipitation? = null,

    @Json(name = "thunderstormProbability")
    var thunderstormProbability: Int? = null,

    @Json(name = "relativeHumidity")
    var relativeHumidity: Int? = null,

    @Json(name = "cloudCover")
    var cloudCover: Int? = null,

    @Json(name = "interval")
    var interval: Interval? = null,

    @Json(name = "weatherCondition")
    var weatherCondition: WeatherCondition? = null,

    @Json(name = "uvIndex")
    var uvIndex: Float? = null,

    @Json(name = "wind")
    var wind: Wind? = null
)

@JsonClass(generateAdapter = true)
data class DisplayDate(

    @Json(name = "month")
    var month: Int? = null,

    @Json(name = "year")
    var year: Int? = null,

    @Json(name = "day")
    var day: Int? = null
)

@JsonClass(generateAdapter = true)
data class SunEvents(

    @Json(name = "sunsetTime")
    var sunsetTime: String? = null,

    @Json(name = "sunriseTime")
    var sunriseTime: String? = null
)

@JsonClass(generateAdapter = true)
data class ForecastDaysItem(

    @Json(name = "displayDate")
    var displayDate: DisplayDate? = null,

    @Json(name = "interval")
    var interval: Interval? = null,

    @Json(name = "daytimeForecast")
    var daytimeForecast: DaytimeForecast? = null,

    @Json(name = "maxTemperature")
    var maxTemperature: MaxTemperature? = null,

    @Json(name = "iceThickness")
    var iceThickness: IceThickness? = null,

    @Json(name = "sunEvents")
    var sunEvents: SunEvents? = null,

    @Json(name = "moonEvents")
    var moonEvents: MoonEvents? = null,

    @Json(name = "minTemperature")
    var minTemperature: MinTemperature? = null,

    @Json(name = "feelsLikeMinTemperature")
    var feelsLikeMinTemperature: FeelsLikeMinTemperature? = null,

    @Json(name = "maxHeatIndex")
    var maxHeatIndex: MaxHeatIndex? = null,

    @Json(name = "feelsLikeMaxTemperature")
    var feelsLikeMaxTemperature: FeelsLikeMaxTemperature? = null,

    @Json(name = "nighttimeForecast")
    var nighttimeForecast: NighttimeForecast? = null
)

@JsonClass(generateAdapter = true)
data class DaytimeForecast(

    @Json(name = "interval")
    var interval: Interval? = null,

    @Json(name = "weatherCondition")
    var weatherCondition: WeatherCondition? = null,

    @Json(name = "precipitation")
    var precipitation: Precipitation? = null,

    @Json(name = "thunderstormProbability")
    var thunderstormProbability: Int? = null,

    @Json(name = "relativeHumidity")
    var relativeHumidity: Int? = null,

    @Json(name = "cloudCover")
    var cloudCover: Int? = null,

    @Json(name = "uvIndex")
    var uvIndex: Float? = null,

    @Json(name = "wind")
    var wind: Wind? = null
)

@JsonClass(generateAdapter = true)
data class FeelsLikeMinTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class MoonEvents(

    @Json(name = "moonriseTimes")
    var moonriseTimes: List<String?>? = null,

    @Json(name = "moonsetTimes")
    var moonsetTimes: List<String?>? = null,

    @Json(name = "moonPhase")
    var moonPhase: String? = null
)

@JsonClass(generateAdapter = true)
data class MaxHeatIndex(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)
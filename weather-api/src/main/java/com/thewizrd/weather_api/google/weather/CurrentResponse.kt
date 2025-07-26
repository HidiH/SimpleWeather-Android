package com.thewizrd.weather_api.google.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrentResponse(

    @Json(name = "windChill")
    var windChill: WindChill? = null,

    @Json(name = "currentConditionsHistory")
    var currentConditionsHistory: CurrentConditionsHistory? = null,

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

    @Json(name = "timeZone")
    var timeZone: TimeZone? = null,

    @Json(name = "dewPoint")
    var dewPoint: DewPoint? = null,

    @Json(name = "feelsLikeTemperature")
    var feelsLikeTemperature: FeelsLikeTemperature? = null,

    @Json(name = "currentTime")
    var currentTime: String? = null,

    @Json(name = "precipitation")
    var precipitation: Precipitation? = null,

    @Json(name = "temperature")
    var temperature: Temperature? = null,

    @Json(name = "relativeHumidity")
    var relativeHumidity: Int? = null,

    @Json(name = "isDaytime")
    var isDaytime: Boolean? = null,

    @Json(name = "weatherCondition")
    var weatherCondition: WeatherCondition? = null,

    @Json(name = "uvIndex")
    var uvIndex: Float? = null,

    @Json(name = "wind")
    var wind: Wind? = null
)

@JsonClass(generateAdapter = true)
data class TemperatureChange(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class CurrentConditionsHistory(

    @Json(name = "maxTemperature")
    var maxTemperature: MaxTemperature? = null,

    @Json(name = "minTemperature")
    var minTemperature: MinTemperature? = null,

    @Json(name = "qpf")
    var qpf: Qpf? = null,

    @Json(name = "temperatureChange")
    var temperatureChange: TemperatureChange? = null
)

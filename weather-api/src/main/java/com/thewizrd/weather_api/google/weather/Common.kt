package com.thewizrd.weather_api.google.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Direction(

    @Json(name = "cardinal")
    var cardinal: String? = null,

    @Json(name = "degrees")
    var degrees: Int? = null
)

@JsonClass(generateAdapter = true)
data class Wind(

    @Json(name = "speed")
    var speed: Speed? = null,

    @Json(name = "direction")
    var direction: Direction? = null,

    @Json(name = "gust")
    var gust: Gust? = null
)

@JsonClass(generateAdapter = true)
data class Speed(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "value")
    var value: Float? = null
)

@JsonClass(generateAdapter = true)
data class Description(

    @Json(name = "text")
    var text: String? = null,

    @Json(name = "languageCode")
    var languageCode: String? = null
)

@JsonClass(generateAdapter = true)
data class WeatherCondition(

    @Json(name = "iconBaseUri")
    var iconBaseUri: String? = null,

    @Json(name = "description")
    var description: Description? = null,

    @Json(name = "type")
    var type: String? = null
)

@JsonClass(generateAdapter = true)
data class Gust(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "value")
    var value: Float? = null
)

@JsonClass(generateAdapter = true)
data class Probability(

    @Json(name = "type")
    var type: String? = null,

    @Json(name = "percent")
    var percent: Int? = null
)

@JsonClass(generateAdapter = true)
data class Qpf(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "quantity")
    var quantity: Float? = null
)

@JsonClass(generateAdapter = true)
data class MaxTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class TimeZone(

    @Json(name = "id")
    var id: String? = null
)

@JsonClass(generateAdapter = true)
data class MinTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class Precipitation(

    @Json(name = "probability")
    var probability: Probability? = null,

    @Json(name = "qpf")
    var qpf: Qpf? = null
)

@JsonClass(generateAdapter = true)
data class Temperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class WindChill(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class FeelsLikeTemperature(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class IceThickness(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "thickness")
    var thickness: Float? = null
)

@JsonClass(generateAdapter = true)
data class Visibility(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "distance")
    var distance: Float? = null
)

@JsonClass(generateAdapter = true)
data class HeatIndex(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class DewPoint(

    @Json(name = "unit")
    var unit: String? = null,

    @Json(name = "degrees")
    var degrees: Float? = null
)

@JsonClass(generateAdapter = true)
data class Interval(

    @Json(name = "startTime")
    var startTime: String? = null,

    @Json(name = "endTime")
    var endTime: String? = null
)

@JsonClass(generateAdapter = true)
data class AirPressure(

    @Json(name = "meanSeaLevelMillibars")
    var meanSeaLevelMillibars: Float? = null
)
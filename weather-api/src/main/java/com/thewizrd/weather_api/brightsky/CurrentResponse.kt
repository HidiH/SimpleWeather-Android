package com.thewizrd.weather_api.brightsky

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrentResponse(

    @Json(name = "sources")
    var sources: List<SourcesItem>? = null,

    @Json(name = "weather")
    var weather: Weather? = null
)

@JsonClass(generateAdapter = true)
data class Weather(

    @Json(name = "wind_gust_direction_10")
    var windGustDirection10: Int? = null,

    @Json(name = "icon")
    var icon: String? = null,

    @Json(name = "solar_30")
    var solar30: Float? = null,

    @Json(name = "wind_gust_direction_30")
    var windGustDirection30: Int? = null,

    @Json(name = "wind_direction_60")
    var windDirection60: Int? = null,

    @Json(name = "solar_10")
    var solar10: Float? = null,

    @Json(name = "sunshine_60")
    var sunshine60: Float? = null,

    @Json(name = "precipitation_60")
    var precipitation60: Float? = null,

    @Json(name = "cloud_cover")
    var cloudCover: Float? = null,

    @Json(name = "temperature")
    var temperature: Float? = null,

    @Json(name = "wind_gust_speed_60")
    var windGustSpeed60: Float? = null,

    @Json(name = "wind_speed_60")
    var windSpeed60: Float? = null,

    @Json(name = "timestamp")
    var timestamp: String? = null,

    @Json(name = "pressure_msl")
    var pressureMsl: Float? = null,

    @Json(name = "visibility")
    var visibility: Int? = null,

    @Json(name = "fallback_source_ids")
    var fallbackSourceIds: FallbackSourceIds? = null,

    @Json(name = "wind_gust_speed_10")
    var windGustSpeed10: Float? = null,

    @Json(name = "wind_direction_30")
    var windDirection30: Int? = null,

    @Json(name = "solar_60")
    var solar60: Float? = null,

    @Json(name = "wind_direction_10")
    var windDirection10: Int? = null,

    @Json(name = "wind_gust_direction_60")
    var windGustDirection60: Int? = null,

    @Json(name = "condition")
    var condition: String? = null,

    @Json(name = "sunshine_30")
    var sunshine30: Float? = null,

    @Json(name = "dew_point")
    var dewPoint: Float? = null,

    @Json(name = "wind_gust_speed_30")
    var windGustSpeed30: Float? = null,

    @Json(name = "precipitation_30")
    var precipitation30: Float? = null,

    @Json(name = "wind_speed_10")
    var windSpeed10: Float? = null,

    @Json(name = "source_id")
    var sourceId: Int? = null,

    @Json(name = "precipitation_10")
    var precipitation10: Float? = null,

    @Json(name = "wind_speed_30")
    var windSpeed30: Float? = null,

    @Json(name = "relative_humidity")
    var relativeHumidity: Int? = null
)

@JsonClass(generateAdapter = true)
data class SourcesItem(

    @Json(name = "station_name")
    var stationName: String? = null,

    @Json(name = "last_record")
    var lastRecord: String? = null,

    @Json(name = "wmo_station_id")
    var wmoStationId: String? = null,

    @Json(name = "distance")
    var distance: Int? = null,

    @Json(name = "first_record")
    var firstRecord: String? = null,

    @Json(name = "lon")
    var lon: Float? = null,

    @Json(name = "id")
    var id: Int? = null,

    @Json(name = "dwd_station_id")
    var dwdStationId: String? = null,

    @Json(name = "lat")
    var lat: Float? = null,

    @Json(name = "observation_type")
    var observationType: String? = null,

    @Json(name = "height")
    var height: Float? = null
)

@JsonClass(generateAdapter = true)
data class FallbackSourceIds(
    var any: Any? = null
)

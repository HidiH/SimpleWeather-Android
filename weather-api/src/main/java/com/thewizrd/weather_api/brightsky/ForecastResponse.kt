package com.thewizrd.weather_api.brightsky

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastResponse(

    @Json(name = "sources")
    var sources: List<SourcesItem>? = null,

    @Json(name = "weather")
    var weather: List<WeatherItem>? = null
)

/*
@JsonClass(generateAdapter = true)
data class SourcesItem(

	@Json(name="station_name")
	var stationName: String? = null,

	@Json(name="last_record")
	var lastRecord: String? = null,

	@Json(name="wmo_station_id")
	var wmoStationId: String? = null,

	@Json(name="distance")
	var distance: Int? = null,

	@Json(name="first_record")
	var firstRecord: String? = null,

	@Json(name="lon")
	var lon: Float? = null,

	@Json(name="id")
	var id: Int? = null,

	@Json(name="dwd_station_id")
	var dwdStationId: String? = null,

	@Json(name="lat")
	var lat: Float? = null,

	@Json(name="observation_type")
	var observationType: String? = null,

	@Json(name="height")
	var height: Int? = null
)
*/

@JsonClass(generateAdapter = true)
data class WeatherItem(

    @Json(name = "pressure_msl")
    var pressureMsl: Float? = null,

    @Json(name = "visibility")
    var visibility: Float? = null,

    @Json(name = "sunshine")
    var sunshine: Float? = null,

    @Json(name = "icon")
    var icon: String? = null,

    @Json(name = "solar")
    var solar: Float? = null,

    @Json(name = "wind_direction")
    var windDirection: Float? = null,

    @Json(name = "precipitation_probability_6h")
    var precipitationProbability6h: Int? = null,

    @Json(name = "precipitation")
    var precipitation: Float? = null,

    @Json(name = "condition")
    var condition: String? = null,

    @Json(name = "dew_point")
    var dewPoint: Float? = null,

    @Json(name = "wind_gust_speed")
    var windGustSpeed: Float? = null,

    @Json(name = "precipitation_probability")
    var precipitationProbability: Int? = null,

    @Json(name = "temperature")
    var temperature: Float? = null,

    @Json(name = "cloud_cover")
    var cloudCover: Float? = null,

    @Json(name = "wind_speed")
    var windSpeed: Float? = null,

    @Json(name = "source_id")
    var sourceId: Int? = null,

    @Json(name = "relative_humidity")
    var relativeHumidity: Float? = null,

    @Json(name = "wind_gust_direction")
    var windGustDirection: Float? = null,

    @Json(name = "timestamp")
    var timestamp: String? = null
)

package com.thewizrd.weather_api.nws.points

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HourlyPointsResponse(

	//@field:Json(name="elevation")
	//var elevation: Elevation? = null,

	//@field:Json(name="validTimes")
	//var validTimes: String? = null,

	//@field:Json(name="forecastGenerator")
	//var forecastGenerator: String? = null,

	@field:Json(name = "generatedAt")
	var generatedAt: String? = null,

	@field:Json(name = "periods")
	var periods: List<PeriodsItem>? = null,

	//@field:Json(name="geometry")
	//var geometry: String? = null,

	@field:Json(name = "updateTime")
	var updateTime: String? = null,

	@field:Json(name = "units")
	var units: String? = null,

	//@field:Json(name="@context")
	//var context: HourlyContext? = null,

	@field:Json(name = "updated")
	var updated: String? = null
)

@JsonClass(generateAdapter = true)
data class ProbabilityOfPrecipitation(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Int? = null
)

@JsonClass(generateAdapter = true)
data class RelativeHumidity(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Int? = null
)

@JsonClass(generateAdapter = true)
data class Elevation(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Any? = null
)

@JsonClass(generateAdapter = true)
data class PeriodsItem(

	@field:Json(name = "detailedForecast")
	var detailedForecast: String? = null,

	@field:Json(name = "dewpoint")
	var dewpoint: Dewpoint? = null,

	@field:Json(name = "temperatureTrend")
	var temperatureTrend: String? = null,

	@field:Json(name = "shortForecast")
	var shortForecast: String? = null,

	@field:Json(name = "icon")
	var icon: String? = null,

	@field:Json(name = "number")
	var number: Int? = null,

	@field:Json(name = "temperatureUnit")
	var temperatureUnit: String? = null,

	@field:Json(name = "probabilityOfPrecipitation")
	var probabilityOfPrecipitation: ProbabilityOfPrecipitation? = null,

	@field:Json(name = "name")
	var name: String? = null,

	@field:Json(name = "temperature")
	var temperature: Int? = null,

	@field:Json(name = "relativeHumidity")
	var relativeHumidity: RelativeHumidity? = null,

	@field:Json(name = "startTime")
	var startTime: String? = null,

	@field:Json(name = "isDaytime")
	var isDaytime: Boolean? = null,

	@field:Json(name = "endTime")
	var endTime: String? = null,

	@field:Json(name = "windDirection")
	var windDirection: String? = null,

	@field:Json(name = "windSpeed")
	var windSpeed: String? = null
)

@JsonClass(generateAdapter = true)
data class HourlyContext(

	@field:Json(name = "geo")
	var geo: String? = null,

	@field:Json(name = "wx")
	var wx: String? = null,

	@field:Json(name = "unit")
	var unit: String? = null,

	@field:Json(name = "@vocab")
	var vocab: String? = null,

	@field:Json(name = "@version")
	var version: String? = null
)

@JsonClass(generateAdapter = true)
data class Dewpoint(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Any? = null
)

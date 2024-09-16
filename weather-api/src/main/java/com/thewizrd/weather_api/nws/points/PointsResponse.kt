package com.thewizrd.weather_api.nws.points

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PointsResponse(

	//@field:Json(name="radarStation")
	//var radarStation: String? = null,

	//@field:Json(name="fireWeatherZone")
	//var fireWeatherZone: String? = null,

	//@field:Json(name="@type")
	//var type: String? = null,

	//@field:Json(name="forecastZone")
	//var forecastZone: String? = null,

	//@field:Json(name="county")
	//var county: String? = null,

	@field:Json(name = "timeZone")
	var timeZone: String? = null,

	@field:Json(name = "forecast")
	var forecast: String? = null,

	//@field:Json(name="cwa")
	//var cwa: String? = null,

	//@field:Json(name="@context")
	//var context: Context? = null,

	//@field:Json(name="relativeLocation")
	//var relativeLocation: RelativeLocation? = null,

	@field:Json(name = "forecastHourly")
	var forecastHourly: String? = null,

	@field:Json(name = "observationStations")
	var observationStations: String? = null,

	//@field:Json(name="gridX")
	//var gridX: Int? = null,

	@field:Json(name = "forecastGridData")
	var forecastGridData: String? = null,

	//@field:Json(name="gridY")
	//var gridY: Int? = null,

	//@field:Json(name="forecastOffice")
	//var forecastOffice: String? = null,

	//@field:Json(name="geometry")
	//var geometry: String? = null,

	//@field:Json(name="@id")
	//var id: String? = null,

	//@field:Json(name="gridId")
	//var gridId: String? = null
)

@JsonClass(generateAdapter = true)
data class Bearing(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Int? = null,

	@field:Json(name = "@type")
	var type: String? = null
)

@JsonClass(generateAdapter = true)
data class Geometry(

	@field:Json(name = "@type")
	var type: String? = null,

	@field:Json(name = "@id")
	var id: String? = null
)

@JsonClass(generateAdapter = true)
data class UnitCode(

	@field:Json(name = "@type")
	var type: String? = null,

	@field:Json(name = "@id")
	var id: String? = null
)

@JsonClass(generateAdapter = true)
data class ForecastOffice(

	@field:Json(name = "@type")
	var type: String? = null
)

@JsonClass(generateAdapter = true)
data class County(

	@field:Json(name = "@type")
	var type: String? = null
)

@JsonClass(generateAdapter = true)
data class Value(

	@field:Json(name = "@id")
	var id: String? = null
)

@JsonClass(generateAdapter = true)
data class PublicZone(

	@field:Json(name = "@type")
	var type: String? = null
)

@JsonClass(generateAdapter = true)
data class RelativeLocation(

	@field:Json(name = "distance")
	var distance: Distance? = null,

	@field:Json(name = "city")
	var city: String? = null,

	@field:Json(name = "bearing")
	var bearing: Bearing? = null,

	@field:Json(name = "geometry")
	var geometry: String? = null,

	@field:Json(name = "state")
	var state: String? = null
)

@JsonClass(generateAdapter = true)
data class Distance(

	@field:Json(name = "unitCode")
	var unitCode: String? = null,

	@field:Json(name = "value")
	var value: Any? = null,

	@field:Json(name = "@type")
	var type: String? = null,

	@field:Json(name = "@id")
	var id: String? = null
)

@JsonClass(generateAdapter = true)
data class Context(

	@field:Json(name = "wx")
	var wx: String? = null,

	@field:Json(name = "@vocab")
	var vocab: String? = null,

	@field:Json(name = "distance")
	var distance: Distance? = null,

	@field:Json(name = "city")
	var city: String? = null,

	@field:Json(name = "bearing")
	var bearing: Bearing? = null,

	@field:Json(name = "county")
	var county: County? = null,

	@field:Json(name = "geo")
	var geo: String? = null,

	@field:Json(name = "unit")
	var unit: String? = null,

	@field:Json(name = "forecastGridData")
	var forecastGridData: ForecastGridData? = null,

	@field:Json(name = "s")
	var s: String? = null,

	@field:Json(name = "publicZone")
	var publicZone: PublicZone? = null,

	@field:Json(name = "unitCode")
	var unitCode: UnitCode? = null,

	@field:Json(name = "@version")
	var version: String? = null,

	@field:Json(name = "forecastOffice")
	var forecastOffice: ForecastOffice? = null,

	@field:Json(name = "geometry")
	var geometry: Geometry? = null,

	@field:Json(name = "state")
	var state: String? = null,

	@field:Json(name = "value")
	var value: Value? = null
)

@JsonClass(generateAdapter = true)
data class ForecastGridData(

	@field:Json(name = "@type")
	var type: String? = null
)

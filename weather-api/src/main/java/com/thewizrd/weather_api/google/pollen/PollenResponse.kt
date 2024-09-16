package com.thewizrd.weather_api.google.pollen

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollenResponse(

	@field:Json(name = "dailyInfo")
	var dailyInfo: List<DailyInfoItem?>? = null
)

@JsonClass(generateAdapter = true)
data class PollenTypeInfoItem(

	@field:Json(name = "code")
	var code: String? = null,

	@field:Json(name = "displayName")
	var displayName: String? = null,

	@field:Json(name = "inSeason")
	var inSeason: Boolean? = null,

	@field:Json(name = "healthRecommendations")
	var healthRecommendations: List<String?>? = null,

	@field:Json(name = "indexInfo")
	var indexInfo: IndexInfo? = null
)

@JsonClass(generateAdapter = true)
data class PlantInfoItem(

	@field:Json(name = "code")
	var code: String? = null,

	@field:Json(name = "displayName")
	var displayName: String? = null,

	@field:Json(name = "inSeason")
	var inSeason: Boolean? = null,

	@field:Json(name = "indexInfo")
	var indexInfo: IndexInfo? = null
)

@JsonClass(generateAdapter = true)
data class IndexInfo(

	@field:Json(name = "code")
	var code: String? = null,

	@field:Json(name = "color")
	var color: Color? = null,

	@field:Json(name = "displayName")
	var displayName: String? = null,

	@field:Json(name = "category")
	var category: String? = null,

	@field:Json(name = "value")
	var value: Int? = null,

	@field:Json(name = "indexDescription")
	var indexDescription: String? = null
)

@JsonClass(generateAdapter = true)
data class Color(

	@field:Json(name = "green")
	var green: Any? = null,

	@field:Json(name = "blue")
	var blue: Any? = null,

	@field:Json(name = "red")
	var red: Any? = null
)

@JsonClass(generateAdapter = true)
data class Date(

	@field:Json(name = "month")
	var month: Int? = null,

	@field:Json(name = "year")
	var year: Int? = null,

	@field:Json(name = "day")
	var day: Int? = null
)

@JsonClass(generateAdapter = true)
data class DailyInfoItem(

	@field:Json(name = "date")
	var date: Date? = null,

	@field:Json(name = "pollenTypeInfo")
	var pollenTypeInfo: List<PollenTypeInfoItem?>? = null,

	@field:Json(name = "plantInfo")
	var plantInfo: List<PlantInfoItem?>? = null
)

package com.thewizrd.weather_api.eccc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationResponse(
    @Json(name = "LocationResponse")
    var locationResponse: List<LocationResponseItem>? = null
)

@JsonClass(generateAdapter = true)
data class Temperature(

    @Json(name = "metric")
    var metric: String? = null,

    @Json(name = "imperialUnrounded")
    var imperialUnrounded: String? = null,

    @Json(name = "imperial")
    var imperial: String? = null,

    @Json(name = "metricUnrounded")
    var metricUnrounded: String? = null
)

@JsonClass(generateAdapter = true)
data class DailyTemperature(

    @Json(name = "metric")
    var metric: String? = null,

    @Json(name = "imperial")
    var imperial: String? = null,

    @Json(name = "periodHigh")
    var periodHigh: Int? = null,

    @Json(name = "periodLow")
    var periodLow: Int? = null
)

@JsonClass(generateAdapter = true)
data class LocationResponseItem(

    //@Json(name="riseSetNextDay")
    //var riseSetNextDay: RiseSetNextDay? = null,

    //@Json(name="metNotes")
    //var metNotes: List<Any>? = null,

    @Json(name = "displayName")
    var displayName: String? = null,

    @Json(name = "observation")
    var observation: Observation? = null,

    @Json(name = "dailyFcst")
    var dailyFcst: DailyFcst? = null,

    @Json(name = "hourlyFcst")
    var hourlyFcst: HourlyFcst? = null,

    //@Json(name="cgndb")
    //var cgndb: String? = null,

    @Json(name = "riseSet")
    var riseSet: RiseSet? = null,

    @Json(name = "lastUpdated")
    var lastUpdated: Long? = null,

    //@Json(name="riseData")
    //var riseData: List<RiseDataItem>? = null,

    //@Json(name="zonePoly")
    //var zonePoly: String? = null,

    @Json(name = "alert")
    var alert: Alert? = null,

    // @Json(name="aqhi")
    //var aqhi: Aqhi? = null
)

@JsonClass(generateAdapter = true)
data class Imperial(

    @Json(name = "lowTemp")
    var lowTemp: Int? = null,

    @Json(name = "highTemp")
    var highTemp: Int? = null,

    @Json(name = "text")
    var text: String? = null
)
/*
@JsonClass(generateAdapter = true)
data class Aqhi(

	@Json(name="url")
	var url: String? = null
)
*/

@JsonClass(generateAdapter = true)
data class DailyFcst(

    @Json(name = "dailyIssuedTimeShrt")
    var dailyIssuedTimeShrt: String? = null,

    //@Json(name="regionalNormals")
    //var regionalNormals: RegionalNormals? = null,

    @Json(name = "daily")
    var daily: List<DailyItem>? = null,

    @Json(name = "dailyIssuedTime")
    var dailyIssuedTime: String? = null,

    @Json(name = "dailyIssuedTimeEpoch")
    var dailyIssuedTimeEpoch: String? = null
)

@JsonClass(generateAdapter = true)
data class RegionalNormals(

    @Json(name = "metric")
    var metric: Metric? = null,

    @Json(name = "imperial")
    var imperial: Imperial? = null
)

@JsonClass(generateAdapter = true)
data class AlertsItem(

    @Json(name = "eventOnsetTime")
    var eventOnsetTime: String? = null,

    @Json(name = "alertBannerText")
    var alertBannerText: String? = null,

    @Json(name = "alertCode")
    var alertCode: String? = null,

    @Json(name = "alertHeaderText")
    var alertHeaderText: String? = null,

    @Json(name = "issueTime")
    var issueTime: String? = null,

    @Json(name = "timezone")
    var timezone: String? = null,

    @Json(name = "tcisURL")
    var tcisURL: String? = null,

    @Json(name = "program")
    var program: String? = null,

    @Json(name = "type")
    var type: String? = null,

    @Json(name = "zones")
    var zones: List<String>? = null,

    @Json(name = "sequence")
    var sequence: Int? = null,

    @Json(name = "issueTimeText")
    var issueTimeText: String? = null,

    //@Json(name="special_text")
    //var specialText: List<SpecialTextItem>? = null,

    @Json(name = "issuingOfficeTZ")
    var issuingOfficeTZ: String? = null,

    @Json(name = "expiryTime")
    var expiryTime: String? = null,

    @Json(name = "eventEndTime")
    var eventEndTime: String? = null,

    @Json(name = "bannerColour")
    var bannerColour: String? = null,

    @Json(name = "zoneId")
    var zoneId: String? = null,

    @Json(name = "alertId")
    var alertId: String? = null,

    @Json(name = "text")
    var text: String? = null,

    @Json(name = "transitionStatus")
    var transitionStatus: String? = null,

    @Json(name = "status")
    var status: String? = null
)

@JsonClass(generateAdapter = true)
data class HourlyItem(

    @Json(name = "date")
    var date: String? = null,

    @Json(name = "feelsLike")
    var feelsLike: ItemValue? = null,

    @Json(name = "periodID")
    var periodID: Int? = null,

    @Json(name = "condition")
    var condition: String? = null,

    @Json(name = "precip")
    var precip: String? = null,

    @Json(name = "windGust")
    var windGust: ItemValue? = null,

    @Json(name = "temperature")
    var temperature: ItemValue? = null,

    @Json(name = "iconCode")
    var iconCode: String? = null,

    @Json(name = "time")
    var time: String? = null,

    @Json(name = "windDir")
    var windDir: String? = null,

    @Json(name = "windSpeed")
    var windSpeed: ItemValue? = null,

    @Json(name = "epochTime")
    var epochTime: Long? = null,

    @Json(name = "uv")
    var uv: Uv? = null,

    @Json(name = "dateShrt")
    var dateShrt: String? = null
)

@JsonClass(generateAdapter = true)
data class Alert(

    @Json(name = "alerts")
    var alerts: List<AlertsItem>? = null,

    //@Json(name="mostSevere")
    //var mostSevere: String? = null,

    //@Json(name="hwyAlerts")
    //var hwyAlerts: List<Any>? = null,

    //@Json(name="zoneId")
    //var zoneId: String? = null,

    //@Json(name="uuid")
    //var uuid: String? = null,

    //@Json(name="hwyMostSevere")
    //var hwyMostSevere: String? = null
)

/*
@JsonClass(generateAdapter = true)
data class RiseDataItem(

	@Json(name="set")
	var set: Set? = null,

	@Json(name="timeZone")
	var timeZone: String? = null,

	@Json(name="rise")
	var rise: Rise? = null
)
*/

@JsonClass(generateAdapter = true)
data class RiseSet(

    @Json(name = "set")
    var set: Set? = null,

    @Json(name = "timeZone")
    var timeZone: String? = null,

    @Json(name = "rise")
    var rise: Rise? = null
)

@JsonClass(generateAdapter = true)
data class ItemValue(

    @Json(name = "metric")
    var metric: String? = null,

    @Json(name = "imperial")
    var imperial: String? = null
)

@JsonClass(generateAdapter = true)
data class Observation(

    @Json(name = "dewpoint")
    var dewpoint: Temperature? = null,

    @Json(name = "visibility")
    var visibility: ItemValue? = null,

    @Json(name = "tendency")
    var tendency: String? = null,

    @Json(name = "windGust")
    var windGust: ItemValue? = null,

    @Json(name = "observedAt")
    var observedAt: String? = null,

    @Json(name = "provinceCode")
    var provinceCode: String? = null,

    @Json(name = "windBearing")
    var windBearing: String? = null,

    @Json(name = "pressure")
    var pressure: ItemValue? = null,

    //@Json(name="windDirectionQAValue")
    //var windDirectionQAValue: Int? = null,

    @Json(name = "timeStamp")
    var timeStamp: String? = null,

    @Json(name = "feelsLike")
    var feelsLike: ItemValue? = null,

    @Json(name = "climateId")
    var climateId: String? = null,

    @Json(name = "condition")
    var condition: String? = null,

    @Json(name = "temperature")
    var temperature: Temperature? = null,

    @Json(name = "humidity")
    var humidity: String? = null,

    @Json(name = "iconCode")
    var iconCode: String? = null,

    //@Json(name="humidityQaValue")
    //var humidityQaValue: Int? = null,

    @Json(name = "windDirection")
    var windDirection: String? = null,

    @Json(name = "tcid")
    var tcid: String? = null,

    @Json(name = "windSpeed")
    var windSpeed: ItemValue? = null,

    @Json(name = "timeStampText")
    var timeStampText: String? = null
)

@JsonClass(generateAdapter = true)
data class Set(

    @Json(name = "time12h")
    var time12h: String? = null,

    @Json(name = "epochTimeRounded")
    var epochTimeRounded: String? = null,

    @Json(name = "time")
    var time: String? = null
)

@JsonClass(generateAdapter = true)
data class SpecialTextItem(

    @Json(name = "link")
    var link: String? = null,

    @Json(name = "type")
    var type: String? = null
)

/*
@JsonClass(generateAdapter = true)
data class Humidex(

	@Json(name="textSummary")
	var textSummary: String? = null,

	@Json(name="calculated")
	var calculated: List<CalculatedItem>? = null
)

@JsonClass(generateAdapter = true)
data class WindChill(

	@Json(name="calculated")
	var calculated: List<Any>? = null,

	@Json(name="textSummary")
	var textSummary: String? = null
)
*/

@JsonClass(generateAdapter = true)
data class Sun(

    @Json(name = "units")
    var units: String? = null,

    @Json(name = "value")
    var value: String? = null
)

@JsonClass(generateAdapter = true)
data class RiseSetNextDay(

    @Json(name = "set")
    var set: Set? = null,

    @Json(name = "timeZone")
    var timeZone: String? = null,

    @Json(name = "rise")
    var rise: Rise? = null
)

@JsonClass(generateAdapter = true)
data class Uv(

    @Json(name = "index")
    var index: String? = null
)

@JsonClass(generateAdapter = true)
data class DailyItem(

    @Json(name = "date")
    var date: String? = null,

    @Json(name = "summary")
    var summary: String? = null,

    @Json(name = "periodID")
    var periodID: Int? = null,

    @Json(name = "periodLabel")
    var periodLabel: String? = null,

    //@Json(name="windChill")
    //var windChill: WindChill? = null,

    //@Json(name="sun")
    //var sun: List<Any>? = null,

    @Json(name = "temperatureText")
    var temperatureText: String? = null,

    //@Json(name="humidex")
    //var humidex: List<Any>? = null,

    @Json(name = "precip")
    var precip: String? = null,

    @Json(name = "titleText")
    var titleText: String? = null,

    @Json(name = "temperature")
    var temperature: DailyTemperature? = null,

    @Json(name = "iconCode")
    var iconCode: String? = null,

    @Json(name = "text")
    var text: String? = null,

    //@Json(name="visibility")
    //var visibility: ItemValue? = null,

    //@Json(name="frost")
    //var frost: Frost? = null
)

@JsonClass(generateAdapter = true)
data class HoursItem(

    @Json(name = "timeStamp")
    var timeStamp: String? = null,

    @Json(name = "temperatureQaValue")
    var temperatureQaValue: Int? = null,

    @Json(name = "dewpoint")
    var dewpoint: Double? = null,

    @Json(name = "windSpeedQaValue")
    var windSpeedQaValue: Int? = null,

    @Json(name = "temperature")
    var temperature: Double? = null,

    @Json(name = "windDirectionQaValue")
    var windDirectionQaValue: Int? = null,

    @Json(name = "humidity")
    var humidity: Int? = null,

    @Json(name = "pressure")
    var pressure: Int? = null,

    @Json(name = "humidityQaValue")
    var humidityQaValue: Int? = null,

    @Json(name = "windDirection")
    var windDirection: String? = null,

    @Json(name = "windSpeed")
    var windSpeed: Int? = null,

    @Json(name = "windGust")
    var windGust: Int? = null,

    @Json(name = "windGustQaValue")
    var windGustQaValue: Int? = null
)

@JsonClass(generateAdapter = true)
data class Metric(

    @Json(name = "lowTemp")
    var lowTemp: Int? = null,

    @Json(name = "highTemp")
    var highTemp: Int? = null,

    @Json(name = "text")
    var text: String? = null
)

@JsonClass(generateAdapter = true)
data class HourlyFcst(

    @Json(name = "hourlyIssuedTimeShrt")
    var hourlyIssuedTimeShrt: String? = null,

    @Json(name = "hourly")
    var hourly: List<HourlyItem>? = null
)

@JsonClass(generateAdapter = true)
data class Rise(

    @Json(name = "time12h")
    var time12h: String? = null,

    @Json(name = "epochTimeRounded")
    var epochTimeRounded: String? = null,

    @Json(name = "time")
    var time: String? = null
)

@JsonClass(generateAdapter = true)
data class Frost(

    @Json(name = "textSummary")
    var textSummary: String? = null
)

@JsonClass(generateAdapter = true)
data class CalculatedItem(

    @Json(name = "value")
    var value: String? = null
)

@JsonClass(generateAdapter = true)
data class OtherVisib(

    @Json(name = "cause")
    var cause: String? = null,

    @Json(name = "textSummary")
    var textSummary: String? = null
)

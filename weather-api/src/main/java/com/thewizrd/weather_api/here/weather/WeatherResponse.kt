package com.thewizrd.weather_api.here.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(

    @field:Json(name = "places")
    var places: List<PlacesItem>? = null,

    @field:Json(name = "status")
    var status: String? = null,

    @field:Json(name = "error")
    var error: String? = null
)

@JsonClass(generateAdapter = true)
data class ProvincesItem(

    @field:Json(name = "country")
    var country: String? = null,

    @field:Json(name = "province")
    var province: String? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "location")
    var location: Location? = null,

    @field:Json(name = "countryName")
    var countryName: String? = null,

    @field:Json(name = "provinceName")
    var provinceName: String? = null,

    @field:Json(name = "value")
    var value: String? = null
)

@JsonClass(generateAdapter = true)
data class Address(

    @field:Json(name = "city")
    var city: String? = null,

    @field:Json(name = "countryName")
    var countryName: String? = null,

    @field:Json(name = "state")
    var state: String? = null,

    @field:Json(name = "countryCode")
    var countryCode: String? = null
)

@JsonClass(generateAdapter = true)
data class PlacesItem(

    @field:Json(name = "alerts")
    var alerts: List<AlertsItem>? = null,

    @field:Json(name = "hourlyForecasts")
    var hourlyForecasts: List<HourlyForecastsItem>? = null,

    @field:Json(name = "observations")
    var observations: List<ObservationsItem>? = null,

    @field:Json(name = "astronomyForecasts")
    var astronomyForecasts: List<AstronomyForecastsItem>? = null,

    @field:Json(name = "nwsAlerts")
    var nwsAlerts: NwsAlerts? = null,

    @field:Json(name = "dailyForecasts")
    var dailyForecasts: List<DailyForecastsItem>? = null
)

@JsonClass(generateAdapter = true)
data class ObservationsItem(

    @field:Json(name = "precipitationProbability")
    var precipitationProbability: String? = null,

    @field:Json(name = "barometerTrend")
    var barometerTrend: String? = null,

    @field:Json(name = "ageMinutes")
    var ageMinutes: String? = null,

    @field:Json(name = "description")
    var description: String? = null,

    @field:Json(name = "windDesc")
    var windDesc: String? = null,

    @field:Json(name = "airInfo")
    var airInfo: String? = null,

    @field:Json(name = "lowTemperature")
    var lowTemperature: String? = null,

    @field:Json(name = "precipitation6H")
    var precipitation6H: String? = null,

    @field:Json(name = "iconLink")
    var iconLink: String? = null,

    @field:Json(name = "precipitationDesc")
    var precipitationDesc: String? = null,

    @field:Json(name = "temperature")
    var temperature: String? = null,

    @field:Json(name = "humidity")
    var humidity: String? = null,

    @field:Json(name = "barometerPressure")
    var barometerPressure: String? = null,

    @field:Json(name = "place")
    var place: Place? = null,

    @field:Json(name = "windDirection")
    var windDirection: String? = null,

    @field:Json(name = "windSpeed")
    var windSpeed: String? = null,

    @field:Json(name = "skyDesc")
    var skyDesc: String? = null,

    @field:Json(name = "windDescShort")
    var windDescShort: String? = null,

    @field:Json(name = "skyInfo")
    var skyInfo: String? = null,

    @field:Json(name = "temperatureDesc")
    var temperatureDesc: String? = null,

    @field:Json(name = "iconId")
    var iconId: String? = null,

    @field:Json(name = "precipitation3H")
    var precipitation3H: String? = null,

    @field:Json(name = "precipitation1H")
    var precipitation1H: String? = null,

    @field:Json(name = "visibility")
    var visibility: String? = null,

    @field:Json(name = "iconName")
    var iconName: String? = null,

    @field:Json(name = "highTemperature")
    var highTemperature: String? = null,

    @field:Json(name = "airDesc")
    var airDesc: String? = null,

    @field:Json(name = "dewPoint")
    var dewPoint: String? = null,

    @field:Json(name = "comfort")
    var comfort: String? = null,

    @field:Json(name = "rainFall")
    var rainFall: String? = null,

    @field:Json(name = "snowFall")
    var snowFall: String? = null,

    @field:Json(name = "snowCover")
    var snowCover: String? = null,

    @field:Json(name = "daylight")
    var daylight: String? = null,

    @field:Json(name = "precipitation12H")
    var precipitation12H: String? = null,

    @field:Json(name = "activeAlerts")
    var activeAlerts: String? = null,

    @field:Json(name = "time")
    var time: String? = null,

    @field:Json(name = "precipitation24H")
    var precipitation24H: String? = null
)

@JsonClass(generateAdapter = true)
data class Location(

    @field:Json(name = "lng")
    var lng: Float? = null,

    @field:Json(name = "lat")
    var lat: Float? = null
)

@JsonClass(generateAdapter = true)
data class NwsAlerts(

    @field:Json(name = "watches")
    var watches: List<WatchesItem>? = null,

    @field:Json(name = "warnings")
    var warnings: List<WarningsItem>? = null
)

@JsonClass(generateAdapter = true)
data class Place(

    @field:Json(name = "address")
    var address: Address? = null,

    @field:Json(name = "distance")
    var distance: Float? = null,

    @field:Json(name = "location")
    var location: Location? = null
)

@JsonClass(generateAdapter = true)
data class WatchesItem(

    @field:Json(name = "severity")
    var severity: Int? = null,

    @field:Json(name = "validUntilTimeLocal")
    var validUntilTimeLocal: String? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "description")
    var description: String? = null,

    @field:Json(name = "type")
    var type: String? = null,

    @field:Json(name = "message")
    var message: String? = null,

    @field:Json(name = "validFromTimeLocal")
    var validFromTimeLocal: String? = null,

    @field:Json(name = "counties")
    var counties: List<ZonesItem>? = null,

    @field:Json(name = "zones")
    var zones: List<ZonesItem>? = null,

    @field:Json(name = "provinces")
    var provinces: List<ProvincesItem>? = null
)

@JsonClass(generateAdapter = true)
data class ZonesItem(

    @field:Json(name = "country")
    var country: String? = null,

    @field:Json(name = "stateName")
    var stateName: String? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "location")
    var location: Location? = null,

    @field:Json(name = "countryName")
    var countryName: String? = null,

    @field:Json(name = "state")
    var state: String? = null,

    @field:Json(name = "value")
    var value: String? = null
)

@JsonClass(generateAdapter = true)
data class DailyForecastsItem(

    @field:Json(name = "place")
    var place: Place? = null,

    @field:Json(name = "forecasts")
    var forecasts: List<ForecastsItem>? = null
)

@JsonClass(generateAdapter = true)
data class TimeSegmentsItem(

    @field:Json(name = "segment")
    var segment: String? = null,

    @field:Json(name = "weekday")
    var weekday: String? = null
)

@JsonClass(generateAdapter = true)
data class AlertsItem(

    @field:Json(name = "timeSegments")
    var timeSegments: List<TimeSegmentsItem>? = null,

    @field:Json(name = "description")
    var description: String? = null,

    @field:Json(name = "place")
    var place: Place? = null,

    @field:Json(name = "type")
    var type: String? = null
)

@JsonClass(generateAdapter = true)
data class HourlyForecastsItem(

    @field:Json(name = "place")
    var place: Place? = null,

    @field:Json(name = "forecasts")
    var forecasts: List<ForecastsItem>? = null
)

@JsonClass(generateAdapter = true)
data class WarningsItem(

    @field:Json(name = "severity")
    var severity: Int? = null,

    @field:Json(name = "validUntilTimeLocal")
    var validUntilTimeLocal: String? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "description")
    var description: String? = null,

    @field:Json(name = "type")
    var type: String? = null,

    @field:Json(name = "message")
    var message: String? = null,

    @field:Json(name = "validFromTimeLocal")
    var validFromTimeLocal: String? = null,

    @field:Json(name = "counties")
    var counties: List<ZonesItem>? = null,

    @field:Json(name = "zones")
    var zones: List<ZonesItem>? = null,

    @field:Json(name = "provinces")
    var provinces: List<ProvincesItem>? = null
)

@JsonClass(generateAdapter = true)
data class AstronomyForecastsItem(

    @field:Json(name = "place")
    var place: Place? = null,

    @field:Json(name = "forecasts")
    var forecasts: List<AstronomyItem>? = null
)

@JsonClass(generateAdapter = true)
data class ForecastsItem(

    @field:Json(name = "precipitationProbability")
    var precipitationProbability: String? = null,

    @field:Json(name = "icon")
    var icon: String? = null,

    @field:Json(name = "weekday")
    var weekday: String? = null,

    @field:Json(name = "description")
    var description: String? = null,

    @field:Json(name = "windDesc")
    var windDesc: String? = null,

    @field:Json(name = "airInfo")
    var airInfo: String? = null,

    @field:Json(name = "iconLink")
    var iconLink: String? = null,

    @field:Json(name = "precipitationDesc")
    var precipitationDesc: String? = null,

    @field:Json(name = "temperature")
    var temperature: String? = null,

    @field:Json(name = "humidity")
    var humidity: String? = null,

    @field:Json(name = "windDirection")
    var windDirection: String? = null,

    @field:Json(name = "windSpeed")
    var windSpeed: String? = null,

    @field:Json(name = "skyDesc")
    var skyDesc: String? = null,

    @field:Json(name = "windDescShort")
    var windDescShort: String? = null,

    @field:Json(name = "skyInfo")
    var skyInfo: String? = null,

    @field:Json(name = "temperatureDesc")
    var temperatureDesc: String? = null,

    @field:Json(name = "visibility")
    var visibility: String? = null,

    @field:Json(name = "iconName")
    var iconName: String? = null,

    @field:Json(name = "airDesc")
    var airDesc: String? = null,

    @field:Json(name = "dewPoint")
    var dewPoint: String? = null,

    @field:Json(name = "comfort")
    var comfort: String? = null,

    @field:Json(name = "rainFall")
    var rainFall: String? = null,

    @field:Json(name = "snowFall")
    var snowFall: String? = null,

    @field:Json(name = "daylight")
    var daylight: String? = null,

    @field:Json(name = "time")
    var time: String? = null,

    @field:Json(name = "iconId")
    var iconId: String? = null,

    @field:Json(name = "moonSet")
    var moonSet: String? = null,

    @field:Json(name = "sunSet")
    var sunSet: String? = null,

    @field:Json(name = "moonRise")
    var moonRise: String? = null,

    @field:Json(name = "moonPhaseDescription")
    var moonPhaseDescription: String? = null,

    @field:Json(name = "moonPhase")
    var moonPhase: String? = null,

    @field:Json(name = "sunRise")
    var sunRise: String? = null,

    @field:Json(name = "lowTemperature")
    var lowTemperature: String? = null,

    @field:Json(name = "uvDesc")
    var uvDesc: String? = null,

    @field:Json(name = "barometerPressure")
    var barometerPressure: String? = null,

    @field:Json(name = "beaufortDesc")
    var beaufortDesc: String? = null,

    @field:Json(name = "highTemperature")
    var highTemperature: String? = null,

    @field:Json(name = "uvIndex")
    var uvIndex: String? = null,

    @field:Json(name = "beaufortScale")
    var beaufortScale: String? = null
)

@JsonClass(generateAdapter = true)
data class AstronomyItem(

    @field:Json(name = "sunRise")
    var sunRise: String? = null,

    @field:Json(name = "sunSet")
    var sunSet: String? = null,

    @field:Json(name = "moonRise")
    var moonRise: String? = null,

    @field:Json(name = "moonSet")
    var moonSet: String? = null,

    @field:Json(name = "moonPhaseDescription")
    var moonPhaseDescription: String? = null,

    @field:Json(name = "moonPhase")
    var moonPhase: String? = null,

    @field:Json(name = "iconName")
    var iconName: String? = null,

    @field:Json(name = "time")
    var time: String? = null
)

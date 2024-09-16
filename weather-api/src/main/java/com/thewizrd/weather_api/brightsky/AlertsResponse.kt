package com.thewizrd.weather_api.brightsky

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertsResponse(

    @Json(name = "alerts")
    var alerts: List<AlertsItem>? = null,

    @Json(name = "location")
    var location: Location? = null
)

@JsonClass(generateAdapter = true)
data class AlertsItem(

    @Json(name = "severity")
    var severity: String? = null,

    @Json(name = "expires")
    var expires: String? = null,

    @Json(name = "instruction_de")
    var instructionDe: String? = null,

    @Json(name = "headline_en")
    var headlineEn: String? = null,

    @Json(name = "description_en")
    var descriptionEn: String? = null,

    @Json(name = "response_type")
    var responseType: String? = null,

    @Json(name = "certainty")
    var certainty: String? = null,

    @Json(name = "instruction_en")
    var instructionEn: String? = null,

    @Json(name = "onset")
    var onset: String? = null,

    @Json(name = "description_de")
    var descriptionDe: String? = null,

    @Json(name = "effective")
    var effective: String? = null,

    @Json(name = "urgency")
    var urgency: String? = null,

    @Json(name = "event_de")
    var eventDe: String? = null,

    @Json(name = "event_code")
    var eventCode: Int? = null,

    @Json(name = "alert_id")
    var alertId: String? = null,

    @Json(name = "id")
    var id: Int? = null,

    @Json(name = "category")
    var category: String? = null,

    @Json(name = "event_en")
    var eventEn: String? = null,

    @Json(name = "headline_de")
    var headlineDe: String? = null,

    @Json(name = "status")
    var status: String? = null
)

@JsonClass(generateAdapter = true)
data class Location(

    @Json(name = "warn_cell_id")
    var warnCellId: Int? = null,

    @Json(name = "district")
    var district: String? = null,

    @Json(name = "name")
    var name: String? = null,

    @Json(name = "state")
    var state: String? = null,

    @Json(name = "name_short")
    var nameShort: String? = null,

    @Json(name = "state_short")
    var stateShort: String? = null
)

package com.thewizrd.weather_api.brightsky

import com.thewizrd.shared_resources.weatherdata.model.WeatherAlert
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlertSeverity
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlertType
import java.time.ZonedDateTime

fun createWeatherAlerts(alerts: AlertsResponse?): Collection<WeatherAlert>? {
    if (alerts?.alerts.isNullOrEmpty()) return null

    val weatherAlerts = ArrayList<WeatherAlert>(alerts!!.alerts!!.size)

    for (alert in alerts.alerts!!) {
        weatherAlerts.add(createWeatherAlert(alert))
    }

    return weatherAlerts
}

fun createWeatherAlert(alert: AlertsItem): WeatherAlert {
    return WeatherAlert().apply {
        attribution = "DWD"
        date = ZonedDateTime.parse(alert.onset ?: alert.effective)
        expiresDate = alert.expires?.let { ZonedDateTime.parse(it) } ?: date.plusDays(1)
        severity = when (alert.severity) {
            "extreme" -> WeatherAlertSeverity.EXTREME
            "severe" -> WeatherAlertSeverity.SEVERE
            "moderate" -> WeatherAlertSeverity.MODERATE
            "minor" -> WeatherAlertSeverity.MINOR
            else -> WeatherAlertSeverity.UNKNOWN
        }
        type = when (alert.eventCode) {
            /* 22 - Frost */
            22 -> WeatherAlertType.WINTERWEATHER
            /* Thunderstorm watches / warnings */
            31, 33, 34, 36, 38, 40, 41, 42, 44, 45, 46, 48, 49 -> WeatherAlertType.SEVERETHUNDERSTORMWARNING
            /* Strong wind warnings */
            51, 52, 53, 57 -> WeatherAlertType.HIGHWIND
            /* Hurricane winds */
            54, 55, 56 -> WeatherAlertType.HURRICANEWINDWARNING
            /* Storm */
            58 -> WeatherAlertType.SEVERETHUNDERSTORMWATCH
            /* Fog */
            59 -> WeatherAlertType.DENSEFOG
            /* Heavy Rain */
            61, 62, 63, 64, 65, 66 -> WeatherAlertType.SEVEREWEATHER
            /* Winter weather */
            70, 71, 72, 73, 74, 75, 76, 82, 84, 85, 86, 87, 88, 89 -> WeatherAlertType.WINTERWEATHER
            /* Thunderstorms */
            90, 91, 92, 93, 95, 96 -> WeatherAlertType.SEVERETHUNDERSTORMWARNING
            /* Coastal warnings */
            11, 12 -> WeatherAlertType.GALEWARNING
            13 -> WeatherAlertType.STORMWARNING
            /* Open Sea Warnings */
            14, 15, 16 -> WeatherAlertType.GALEWARNING
            /* Heat warnings */
            246, 247, 248 -> WeatherAlertType.HEAT
            /* Misc (79, 98, 99) */
            else -> WeatherAlertType.SPECIALWEATHERALERT
        }

        title = alert.eventDe ?: alert.headlineDe ?: alert.eventEn ?: alert.headlineEn
        message = buildString {
            if (!alert.headlineDe.isNullOrBlank()) {
                appendLine("Deutsch:")
                appendLine(alert.headlineDe)
                appendLine()
                appendLine(alert.descriptionDe)
                alert.instructionDe?.takeIf { it.isNotBlank() }?.let { appendLine().appendLine(it) }
            }

            appendLine()

            if (!alert.headlineEn.isNullOrBlank()) {
                appendLine("English:")
                appendLine(alert.headlineEn)
                appendLine()
                appendLine(alert.descriptionEn)
                alert.instructionEn?.takeIf { it.isNotBlank() }?.let { appendLine().appendLine(it) }
            }
        }.trimEnd()
    }
}
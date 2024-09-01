package com.thewizrd.weather_api.eccc

import android.annotation.SuppressLint
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlert
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlertSeverity
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlertType
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun createWeatherAlerts(alerts: Alert?): Collection<WeatherAlert>? {
    if (alerts?.alerts.isNullOrEmpty()) return null

    val weatherAlerts = ArrayList<WeatherAlert>(alerts!!.alerts!!.size)

    for (alert in alerts.alerts!!) {
        weatherAlerts.add(createWeatherAlert(alert))
    }

    return weatherAlerts
}

@SuppressLint("VisibleForTests")
fun createWeatherAlert(alert: AlertsItem): WeatherAlert {
    return WeatherAlert().apply {
        attribution = "Environment and Climate Change Canada (ECCC)"
        date = ZonedDateTime.ofInstant(
            Instant.parse(alert.eventOnsetTime ?: alert.issueTime),
            ZoneOffset.UTC
        )
        expiresDate = ZonedDateTime.ofInstant(Instant.parse(alert.eventEndTime), ZoneOffset.UTC)
        severity = when (alert.type) {
            "warning" -> WeatherAlertSeverity.SEVERE
            "watch" -> WeatherAlertSeverity.MODERATE
            "statement", "advisory" -> WeatherAlertSeverity.MINOR
            else -> WeatherAlertSeverity.UNKNOWN
        }
        // https://www.canada.ca/en/environment-climate-change/services/types-weather-forecasts-use/public/criteria-alerts.html
        type = alert.alertBannerText?.let {
            when {
                it.contains("arctic", ignoreCase = true) ||
                        it.contains("blizzard", ignoreCase = true) ||
                        it.contains("snow", ignoreCase = true) ||
                        it.contains("extreme cold", ignoreCase = true) ||
                        it.contains("freez", ignoreCase = true) ||
                        it.contains("frost", ignoreCase = true) ||
                        it.contains("winter", ignoreCase = true) -> WeatherAlertType.WINTERWEATHER

                it.contains("flooding", ignoreCase = true) -> WeatherAlertType.FLOODWARNING
                it.contains("dust", ignoreCase = true) -> WeatherAlertType.DUSTADVISORY
                it.contains("fog", ignoreCase = true) -> WeatherAlertType.DENSEFOG
                it.contains("heat", ignoreCase = true) -> WeatherAlertType.HEAT
                it.contains("hurricane", ignoreCase = true) -> WeatherAlertType.HURRICANEWINDWARNING
                it.contains(
                    "thunderstorm",
                    ignoreCase = true
                ) -> WeatherAlertType.SEVERETHUNDERSTORMWARNING

                it.contains("tornado", ignoreCase = true) -> WeatherAlertType.TORNADOWARNING
                it.contains("tropical storm", ignoreCase = true) -> WeatherAlertType.STORMWARNING
                else -> WeatherAlertType.SPECIALWEATHERALERT
            }
        } ?: WeatherAlertType.SPECIALWEATHERALERT

        title = alert.alertBannerText
        message = alert.text
    }
}
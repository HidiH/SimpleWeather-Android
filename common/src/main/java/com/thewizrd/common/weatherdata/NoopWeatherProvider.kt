package com.thewizrd.common.weatherdata

import android.location.Location
import androidx.annotation.RestrictTo
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.locationdata.LocationQuery
import com.thewizrd.shared_resources.locationdata.WeatherLocationProvider
import com.thewizrd.shared_resources.utils.Coordinate
import com.thewizrd.shared_resources.weatherdata.WeatherProvider
import com.thewizrd.shared_resources.weatherdata.auth.AuthType
import com.thewizrd.shared_resources.weatherdata.model.Weather
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlert

@RestrictTo(RestrictTo.Scope.LIBRARY)
class NoopWeatherProvider : WeatherProvider {
    override fun getWeatherAPI(): String = ""

    override fun isKeyRequired(): Boolean = false

    override fun supportsWeatherLocale(): Boolean = false

    override fun supportsAlerts(): Boolean = false

    override fun needsExternalAlertData(): Boolean = false

    override fun isRegionSupported(location: LocationData): Boolean = true

    override fun isRegionSupported(location: LocationQuery): Boolean = true

    override fun getHourlyForecastInterval(): Int = 1

    override suspend fun getLocations(ac_query: String?): Collection<LocationQuery> = emptyList()

    override suspend fun getLocation(coordinate: Coordinate): LocationQuery? = null

    override suspend fun getLocation(location: Location): LocationQuery? = null

    override suspend fun getWeather(location: LocationData?): Weather = Weather()

    override suspend fun getAlerts(location: LocationData): Collection<WeatherAlert>? = emptyList()

    override fun getWeatherIcon(icon: String?): String = WeatherIcons.NA

    override fun getWeatherIcon(isNight: Boolean, icon: String?): String = WeatherIcons.NA

    override fun getWeatherCondition(icon: String?): String = ""

    override fun getAuthType(): AuthType = AuthType.NONE

    override suspend fun isKeyValid(key: String?): Boolean = true

    override fun getAPIKey(): String? = null

    override fun isNight(weather: Weather): Boolean = false

    override fun localeToLangCode(iso: String, name: String): String = iso

    override suspend fun updateLocationData(location: LocationData) {}

    override suspend fun updateLocationQuery(weather: Weather): String = ""

    override suspend fun updateLocationQuery(location: LocationData): String = ""

    override fun getLocationProvider(): WeatherLocationProvider = NoopWeatherLocationProvider()

    internal class NoopWeatherLocationProvider : WeatherLocationProvider {
        override fun getLocationAPI(): String {
            TODO("Not yet implemented")
        }

        override fun isKeyRequired(): Boolean {
            TODO("Not yet implemented")
        }

        override fun supportsLocale(): Boolean {
            TODO("Not yet implemented")
        }

        override fun needsLocationFromID(): Boolean {
            TODO("Not yet implemented")
        }

        override fun needsLocationFromName(): Boolean {
            TODO("Not yet implemented")
        }

        override fun needsLocationFromGeocoder(): Boolean {
            TODO("Not yet implemented")
        }

        override suspend fun getLocations(
            ac_query: String?,
            weatherAPI: String?
        ): Collection<LocationQuery> {
            TODO("Not yet implemented")
        }

        override suspend fun getLocation(
            coordinate: Coordinate,
            weatherAPI: String?
        ): LocationQuery? {
            TODO("Not yet implemented")
        }

        override suspend fun getLocationFromID(model: LocationQuery): LocationQuery? {
            TODO("Not yet implemented")
        }

        override suspend fun getLocationFromName(model: LocationQuery): LocationQuery? {
            TODO("Not yet implemented")
        }

        override suspend fun isKeyValid(key: String?): Boolean {
            TODO("Not yet implemented")
        }

        override fun getAPIKey(): String? {
            TODO("Not yet implemented")
        }

        override fun localeToLangCode(iso: String, name: String): String {
            TODO("Not yet implemented")
        }

        override suspend fun updateLocationData(location: LocationData, weatherAPI: String) {
            TODO("Not yet implemented")
        }
    }
}
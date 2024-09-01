package com.thewizrd.simpleweather.radar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.annotation.StringDef
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.controls.ProviderEntry
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.remoteconfig.remoteConfigService
import com.thewizrd.shared_resources.weatherdata.WeatherAPI
import com.thewizrd.simpleweather.radar.eccc.ECCCRadarViewProvider
import com.thewizrd.simpleweather.radar.nws.NWSRadarViewProvider
import com.thewizrd.simpleweather.radar.openweather.OWMRadarViewProvider
import com.thewizrd.simpleweather.radar.rainviewer.RainViewerViewProvider
import com.thewizrd.simpleweather.radar.tomorrowio.TomorrowIoRadarViewProvider
import com.thewizrd.weather_api.weatherModule

object RadarProvider {
    const val KEY_RADARPROVIDER = "key_radarprovider"

    @StringDef(
        WeatherAPI.RAINVIEWER,
        WeatherAPI.NWS,
        WeatherAPI.ECCC,
        WeatherAPI.OPENWEATHERMAP,
        WeatherAPI.TOMORROWIO
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class RadarProviders

    @SuppressLint("WrongConstant")
    fun getRadarProviders(): List<ProviderEntry> {
        val apiRadarProviders = listOf(WeatherAPI.OPENWEATHERMAP, WeatherAPI.TOMORROWIO)

        var providers = FullRadarProviders

        apiRadarProviders.forEach { api ->
            val p = weatherModule.weatherManager.getWeatherProvider(api)

            if (settingsManager.getAPI() != p.getWeatherAPI() && (settingsManager.getAPIKey(p.getWeatherAPI())
                    .isNullOrBlank() && p.getAPIKey().isNullOrBlank())
            ) {
                providers = providers.filterNot { it.value == p.getWeatherAPI() }
            }
        }

        return providers
    }

    private val FullRadarProviders = listOf(
        ProviderEntry(
            "RainViewer", WeatherAPI.RAINVIEWER,
            "https://www.rainviewer.com/", "https://www.rainviewer.com/api.html"
        ),
        ProviderEntry(
            "National Weather Service (United States)", WeatherAPI.NWS,
            "https://radar.weather.gov/", "https://radar.weather.gov/"
        ),
        ProviderEntry(
            "Environment and Climate Change Canada (ECCC)", WeatherAPI.ECCC,
            "https://weather.gc.ca/", "https://weather.gc.ca/"
        ),
        ProviderEntry(
            "OpenWeatherMap", WeatherAPI.OPENWEATHERMAP,
            "http://www.openweathermap.org", "https://home.openweathermap.org/users/sign_up"
        ),
        ProviderEntry(
            "Tomorrow.io", WeatherAPI.TOMORROWIO,
            "https://www.tomorrow.io/weather-api/", "https://www.tomorrow.io/weather-api/"
        )
    )

    @JvmStatic
    @RadarProviders
    fun getRadarProvider(): String {
        val prefs = appLib.preferences
        val provider = prefs.getString(KEY_RADARPROVIDER, WeatherAPI.RAINVIEWER)!!

        if (provider == WeatherAPI.OPENWEATHERMAP) {
            val owm = weatherModule.weatherManager.getWeatherProvider(WeatherAPI.OPENWEATHERMAP)
            // Fallback to default since API KEY is unavailable
            if (owm.getAPIKey() == null && settingsManager.getAPIKey(WeatherAPI.OPENWEATHERMAP) == null) {
                return WeatherAPI.RAINVIEWER
            }
        } else if (provider == WeatherAPI.TOMORROWIO) {
            val tmr = weatherModule.weatherManager.getWeatherProvider(WeatherAPI.TOMORROWIO)
            // Fallback to default since API KEY is unavailable
            if (tmr.getAPIKey() == null && settingsManager.getAPIKey(WeatherAPI.TOMORROWIO) == null) {
                return WeatherAPI.RAINVIEWER
            }
        }

        return provider
    }

    @JvmStatic
    @RequiresApi(value = Build.VERSION_CODES.LOLLIPOP)
    fun getRadarViewProvider(context: Context, rootView: ViewGroup): RadarViewProvider {
        val radarProvider = getRadarProvider()
        val isEnabled = isRadarProviderEnabled(radarProvider)

        return if (radarProvider == WeatherAPI.OPENWEATHERMAP && isEnabled) {
            OWMRadarViewProvider(context, rootView)
        } else if (radarProvider == WeatherAPI.TOMORROWIO && isEnabled) {
            TomorrowIoRadarViewProvider(context, rootView)
        } else if (radarProvider == WeatherAPI.NWS && isEnabled) {
            NWSRadarViewProvider(context, rootView)
        } else if (radarProvider == WeatherAPI.ECCC && isEnabled) {
            ECCCRadarViewProvider(context, rootView)
        } else if (radarProvider == WeatherAPI.RAINVIEWER && isEnabled) {
            RainViewerViewProvider(context, rootView)
        } else {
            EmptyRadarViewProvider(context, rootView)
        }
    }

    private fun isRadarProviderEnabled(provider: String): Boolean =
        remoteConfigService.isProviderEnabled(provider)
}
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
import com.thewizrd.shared_resources.weatherdata.WeatherAPI
import com.thewizrd.simpleweather.radar.openweather.OWMRadarViewProvider
import com.thewizrd.simpleweather.radar.rainviewer.RainViewerViewProvider
import com.thewizrd.simpleweather.radar.tomorrowio.TomorrowIoRadarViewProvider
import com.thewizrd.weather_api.weatherModule

object RadarProvider {
    const val KEY_RADARPROVIDER = "key_radarprovider"

    @StringDef(WeatherAPI.RAINVIEWER, WeatherAPI.OPENWEATHERMAP, WeatherAPI.TOMORROWIO)
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
        return if (getRadarProvider() == WeatherAPI.OPENWEATHERMAP) {
            OWMRadarViewProvider(context, rootView)
        } else if (getRadarProvider() == WeatherAPI.TOMORROWIO) {
            TomorrowIoRadarViewProvider(context, rootView)
        } else {
            RainViewerViewProvider(context, rootView)
        }
    }
}
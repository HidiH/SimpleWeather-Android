package com.thewizrd.simpleweather.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.text.util.LocalePreferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.thewizrd.common.viewmodels.LocationSearchViewModel
import com.thewizrd.shared_resources.Constants
import com.thewizrd.shared_resources.di.localBroadcastManager
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.utils.AnalyticsLogger
import com.thewizrd.shared_resources.utils.CommonActions
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Units
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.simpleweather.locale.UserLocaleActivity
import com.thewizrd.simpleweather.main.MainActivity
import com.thewizrd.simpleweather.ui.setup.Setup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupActivity : UserLocaleActivity() {
    private val locationSearchViewModel: LocationSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsLogger.logEvent("SetupActivity: onCreate")

        // Set default units based on user locale
        PreferenceManager.getDefaultSharedPreferences(this).run {
            if (!contains(SettingsManager.KEY_USECELSIUS) && !contains(SettingsManager.KEY_TEMPUNIT)) {
                if (LocalePreferences.getTemperatureUnit() == LocalePreferences.TemperatureUnit.CELSIUS) {
                    settingsManager.setDefaultUnits(Units.CELSIUS)
                } else {
                    settingsManager.setDefaultUnits(Units.FAHRENHEIT)
                }
            }
        }

        setContent {
            Setup()
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            locationSearchViewModel.currentLocation.collectLatest { location ->
                if (location?.isValid == true) {
                    settingsManager.updateLocation(location)

                    settingsManager.setFollowGPS(true)
                    settingsManager.setWeatherLoaded(true)

                    // If we're changing locations, trigger an update
                    if (settingsManager.isWeatherLoaded()) {
                        localBroadcastManager.sendBroadcast(Intent(CommonActions.ACTION_WEATHER_SENDLOCATIONUPDATE))
                    }

                    settingsManager.setFollowGPS(true)
                    settingsManager.setWeatherLoaded(true)
                    settingsManager.setDataSync(WearableDataSync.OFF)

                    // Start WeatherNow Activity with weather data
                    val intent = Intent(this@SetupActivity, MainActivity::class.java).apply {
                        putExtra(Constants.KEY_DATA, JSONParser.serializer(location))
                    }
                    startActivity(intent)
                    finishAffinity()
                } else {
                    settingsManager.setFollowGPS(false)
                }
            }
        }

        lifecycleScope.launch {
            locationSearchViewModel.selectedSearchLocation.collectLatest { location ->
                location?.data?.takeIf { it.isValid }?.let {
                    settingsManager.updateLocation(it)

                    settingsManager.setFollowGPS(false)
                    settingsManager.setWeatherLoaded(true)
                    settingsManager.setDataSync(WearableDataSync.OFF)

                    // Start WeatherNow Activity with weather data
                    val intent = Intent(this@SetupActivity, MainActivity::class.java).apply {
                        putExtra(Constants.KEY_DATA, JSONParser.serializer(it))
                    }
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }
}
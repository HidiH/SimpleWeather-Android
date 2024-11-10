package com.thewizrd.simpleweather.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.wear.protolayout.material.layouts.LayoutDefaults.MultiButtonLayoutDefaults
import com.google.common.reflect.TypeToken
import com.thewizrd.common.controls.WeatherDetailsType
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.utils.JSONParser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

object DetailsWeatherTileUtils {
    const val MAX_BUTTONS = MultiButtonLayoutDefaults.MAX_BUTTONS
    private const val KEY_DETAILSWEATHERTILECONFIG = "key_detailsweathertileconfig"

    val DEFAULT_ITEMS by lazy {
        listOf(
            WeatherDetailsType.FEELSLIKE, WeatherDetailsType.POPCHANCE,
            WeatherDetailsType.AIRQUALITY, WeatherDetailsType.UV, WeatherDetailsType.WINDSPEED,
            WeatherDetailsType.SUNRISE, WeatherDetailsType.SUNSET,
        )
    }

    fun isTypeAllowed(detailsType: WeatherDetailsType): Boolean {
        return when (detailsType) {
            WeatherDetailsType.MOONPHASE,
            WeatherDetailsType.TREEPOLLEN,
            WeatherDetailsType.GRASSPOLLEN,
            WeatherDetailsType.RAGWEEDPOLLEN -> false

            else -> true
        }
    }

    fun getTileConfig(): List<WeatherDetailsType>? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(appLib.context)
        val configJSON = preferences.getString(KEY_DETAILSWEATHERTILECONFIG, null)
        return configJSON?.let {
            val arrListType = object : TypeToken<List<WeatherDetailsType>>() {}.type
            JSONParser.deserializer<List<WeatherDetailsType>>(it, arrListType)
        }
    }

    fun getTileConfigFlow() = callbackFlow {
        val preferences = PreferenceManager.getDefaultSharedPreferences(appLib.context)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_DETAILSWEATHERTILECONFIG -> trySend(getTileConfig())
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)

        // Send initial value
//        if (preferences.contains(KEY_DETAILSWEATHERTILECONFIG)) {
//            send(getTileConfig())
//        }

        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.buffer(Channel.UNLIMITED)

    fun setTileConfig(types: List<WeatherDetailsType>?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(appLib.context)
        preferences.edit {
            putString(KEY_DETAILSWEATHERTILECONFIG, types?.let {
                val arrListType = object : TypeToken<List<WeatherDetailsType>>() {}.type
                JSONParser.serializer(it, arrListType)
            })
        }
    }
}
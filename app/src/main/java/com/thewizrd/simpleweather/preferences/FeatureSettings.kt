package com.thewizrd.simpleweather.preferences

import android.content.Context
import androidx.core.content.edit
import com.thewizrd.shared_resources.appLib
import com.thewizrd.simpleweather.BuildConfig
import com.thewizrd.simpleweather.adapters.FeaturesAdapter

object FeatureSettings {
    private val preferences = appLib.preferences
    private const val KEY_FEATURES = "features"

    const val KEY_FEATURE_BGIMAGE = "key_feature_bgimage"
    const val KEY_FEATURE_FORECAST = "key_feature_forecast"
    const val KEY_FEATURE_HRFORECAST = "key_feature_hrforecast"
    const val KEY_FEATURE_CHARTS = "key_feature_charts"
    const val KEY_FEATURE_SUMMARY = "key_feature_summary"
    const val KEY_FEATURE_DETAILS = "key_feature_details"
    const val KEY_FEATURE_UV = "key_feature_uv"
    const val KEY_FEATURE_BEAUFORT = "key_feature_beaufort"
    const val KEY_FEATURE_AQINDEX = "key_feature_aqindex"
    const val KEY_FEATURE_MOONPHASE = "key_feature_moonphase"
    const val KEY_FEATURE_SUNPHASE = "key_feature_sunphase"
    const val KEY_FEATURE_RADAR = "key_feature_radar"
    const val KEY_FEATURE_LOCPANELIMG = "key_feature_locpanelimg"
    const val KEY_FEATURE_POLLEN = "key_feature_pollen"

    private const val KEY_FEATURE_ORDER = "key_feature_order"

    @JvmStatic
    val isBackgroundImageEnabled: Boolean
        get() = if (BuildConfig.IS_NONGMS) {
            false
        } else {
            preferences.getBoolean(
                KEY_FEATURE_BGIMAGE,
                true
            )
        }

    @JvmStatic
    val isForecastEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_FORECAST, true)

    @JvmStatic
    val isHourlyForecastEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_HRFORECAST, true)

    @JvmStatic
    val isChartsEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_CHARTS, true)

    @JvmStatic
    val isSummaryEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_SUMMARY, true)

    @JvmStatic
    val isDetailsEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_DETAILS, true)

    @JvmStatic
    val isUVEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_UV, true)

    @JvmStatic
    val isBeaufortEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_BEAUFORT, true)

    @JvmStatic
    val isAQIndexEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_AQINDEX, true)

    @JvmStatic
    val isMoonPhaseEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_MOONPHASE, true)

    @JvmStatic
    val isSunPhaseEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_SUNPHASE, true)

    @JvmStatic
    val isRadarEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_RADAR, true)

    @JvmStatic
    val isLocationPanelImageEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_LOCPANELIMG, true)

    @JvmStatic
    val isPollenEnabled: Boolean
        get() = preferences.getBoolean(KEY_FEATURE_POLLEN, true)

    fun isFeatureEnabled(key: String): Boolean = preferences.getBoolean(key, true)
    fun setFeatureEnabled(key: String, enabled: Boolean) {
        preferences.edit {
            putBoolean(key, enabled)
        }
    }

    fun setFeatureOrder(list: Collection<String>) {
        val preferences = appLib.context.getSharedPreferences(KEY_FEATURES, Context.MODE_PRIVATE)

        preferences.edit {
            putString(KEY_FEATURE_ORDER, list.joinToString(","))
        }
    }

    fun getFeatureOrder(): Set<String> {
        val preferences = appLib.context.getSharedPreferences(KEY_FEATURES, Context.MODE_PRIVATE)
        return preferences.getString(KEY_FEATURE_ORDER, null)?.split(',')?.toSet()
            ?: FeaturesAdapter.ORDERABLE_ITEMS
    }
}
package com.thewizrd.simpleweather.theming

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.utils.ColorMode

public lateinit var dynamicColorsHelper: DynamicColorsHelper

class DynamicColorsHelper(private val app: Application) {
    companion object {
        const val KEY_COLORTHEME = "key_colortheme"
        private const val KEY_COLOR = "last_color"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    fun applyToActivitiesIfAvailable() {
        DynamicColors.applyToActivitiesIfAvailable(app)
    }

    fun applyToActivitiesIfAvailable(@ColorInt color: Int) {
        DynamicColors.applyToActivitiesIfAvailable(
            app,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(color)
                .build()
        )
    }

    fun applyToActivitiesIfAvailable(bitmap: Bitmap) {
        DynamicColors.applyToActivitiesIfAvailable(
            app,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(bitmap)
                .build()
        )
    }

    fun applyToActivityIfAvailable(
        activity: Activity,
        onAppliedCallback: DynamicColors.OnAppliedCallback? = null
    ) {
        DynamicColors.applyToActivityIfAvailable(
            activity,
            DynamicColorsOptions.Builder()
                .apply {
                    if (onAppliedCallback != null) {
                        setOnAppliedCallback(onAppliedCallback)
                    }
                }
                .build()
        )
    }

    fun applyToActivityIfAvailable(
        activity: Activity,
        @ColorInt color: Int,
        onAppliedCallback: DynamicColors.OnAppliedCallback? = null
    ) {
        DynamicColors.applyToActivityIfAvailable(
            activity,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(color)
                .apply {
                    if (onAppliedCallback != null) {
                        setOnAppliedCallback(onAppliedCallback)
                    }
                }
                .build()
        )
    }

    fun applyToActivityIfAvailable(activity: Activity, bitmap: Bitmap) {
        DynamicColors.applyToActivityIfAvailable(
            activity,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(bitmap)
                .build()
        )
    }

    fun isDynamicColorsAvailable(): Boolean = DynamicColors.isDynamicColorAvailable()

    fun getColorMode(): ColorMode {
        return ColorMode.valueOf(preferences.getString(KEY_COLORTHEME, "0")!!.toInt())
    }

    fun setColorMode(value: ColorMode) {
        preferences.edit {
            putString(KEY_COLORTHEME, value.value.toString())
        }
    }

    fun getLastColor(): Int? {
        return appLib.properties.getInt(KEY_COLOR, 0).takeIf { it != 0 }
    }

    fun setLastColor(@ColorInt color: Int) {
        appLib.properties.putInt(KEY_COLOR, color)
    }
}
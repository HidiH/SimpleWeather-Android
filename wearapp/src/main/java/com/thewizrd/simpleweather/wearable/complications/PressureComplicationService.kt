package com.thewizrd.simpleweather.wearable.complications

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.utils.ConversionMethods
import com.thewizrd.shared_resources.utils.LocaleUtils
import com.thewizrd.shared_resources.utils.Units
import com.thewizrd.shared_resources.weatherdata.model.HourlyForecast
import com.thewizrd.shared_resources.weatherdata.model.Weather
import com.thewizrd.simpleweather.R
import java.text.DecimalFormat

class PressureComplicationService : WeatherHourlyForecastComplicationService() {
    companion object {
        private const val TAG = "PressureComplicationService"
    }

    override val supportedComplicationTypes =
        setOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.LONG_TEXT
        )
    private val complicationIconResId = R.drawable.wi_barometer

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (!supportedComplicationTypes.contains(type)) {
            return NoDataComplicationData()
        }

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    30.3f, 26f, 32f,
                    PlainComplicationText.Builder("Pressure: 30.3 inHg").build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).setText(
                    PlainComplicationText.Builder("30.3 in").build()
                ).build()
            }

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    PlainComplicationText.Builder("30.3 in").build(),
                    PlainComplicationText.Builder("Pressure: 30.3 inHg").build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).build()
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    PlainComplicationText.Builder(getString(R.string.label_pressure)).build(),
                    PlainComplicationText.Builder("Pressure: 30.3 inHg").build()
                ).setTitle(
                    PlainComplicationText.Builder("30.3 inHg").build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).build()
            }

            else -> {
                null
            }
        }
    }

    override fun buildUpdate(
        dataType: ComplicationType,
        weather: Weather?,
        hourlyForecast: HourlyForecast?
    ): ComplicationData? {
        if (weather == null || !weather.isValid || !supportedComplicationTypes.contains(dataType)) {
            return null
        }

        val pressureIn =
            weather.atmosphere?.pressureIn ?: hourlyForecast?.extras?.pressureIn ?: return null
        val pressureMb =
            weather.atmosphere?.pressureMb ?: hourlyForecast?.extras?.pressureMb ?: return null

        if (pressureIn < 0 || pressureMb < 0) return null

        val df = DecimalFormat.getInstance(LocaleUtils.getLocale()) as DecimalFormat
        df.applyPattern("0.#")

        val unit = settingsManager.getPressureUnit()
        val pressureVal: String
        val pressureUnit: String
        val pressureUnitShort: String

        when (unit) {
            Units.INHG -> {
                pressureVal = df.format(pressureIn)
                pressureUnit = getString(R.string.unit_inHg)
                pressureUnitShort = getString(R.string.unit_in)
            }

            Units.MILLIBAR -> {
                pressureVal = df.format(pressureMb)
                pressureUnit = getString(R.string.unit_mBar).also { pressureUnitShort = it }
            }

            Units.MMHG -> {
                pressureVal = df.format(ConversionMethods.inHgToMmHg(pressureIn))
                pressureUnit = getString(R.string.unit_mmHg)
                pressureUnitShort = getString(R.string.unit_mm)
            }

            else -> {
                pressureVal = df.format(pressureIn)
                pressureUnit = getString(R.string.unit_inHg)
                pressureUnitShort = getString(R.string.unit_in)
            }
        }

        val pressureStr = String.format(LocaleUtils.getLocale(), "%s %s", pressureVal, pressureUnit)
        val pressureStrShort =
            String.format(LocaleUtils.getLocale(), "%s %s", pressureVal, pressureUnitShort)

        return when (dataType) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    pressureIn, 26f, 32f,
                    PlainComplicationText.Builder(
                        String.format("%s: %s", getString(R.string.label_pressure), pressureStr)
                    ).build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).setText(
                    PlainComplicationText.Builder(pressureStrShort).build()
                ).setTapAction(
                    getTapIntent(this)
                ).build()
            }

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    PlainComplicationText.Builder(pressureStrShort).build(),
                    PlainComplicationText.Builder(
                        String.format("%s: %s", getString(R.string.label_pressure), pressureStr)
                    ).build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).setTapAction(
                    getTapIntent(this)
                ).build()
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    PlainComplicationText.Builder(getString(R.string.label_pressure)).build(),
                    PlainComplicationText.Builder(
                        String.format("%s: %s", getString(R.string.label_pressure), pressureStr)
                    ).build()
                ).setTitle(
                    PlainComplicationText.Builder(pressureStr).build()
                ).setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this, complicationIconResId)
                            .setTint(Colors.WHITESMOKE)
                    ).build()
                ).setTapAction(
                    getTapIntent(this)
                ).build()
            }

            else -> {
                null
            }
        }
    }
}
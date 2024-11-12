package com.thewizrd.simpleweather.wearable.tiles.layouts

import android.content.Context
import androidx.annotation.ColorInt
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.LayoutDefaults.MULTI_SLOT_LAYOUT_HORIZONTAL_SPACER_WIDTH
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.google.android.horologist.tiles.images.toImageResource
import com.thewizrd.common.controls.toUiModel
import com.thewizrd.common.utils.ImageUtils
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.utils.getColorFromTempF
import com.thewizrd.shared_resources.weatherdata.model.Weather
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.ui.tiles.tools.WearPreviewDevices
import com.thewizrd.simpleweather.wearable.tiles.ID_WEATHER_ICON_PREFIX

internal fun currentWeatherTileLayout(
    weather: Weather?,
    context: Context,
    deviceParameters: DeviceParameters
): LayoutElement {
    val viewModel = weather?.toUiModel()

    return currentWeatherTileLayout(
        context,
        deviceParameters,
        location = viewModel?.location ?: WeatherIcons.PLACEHOLDER,
        weatherIconId = "$ID_WEATHER_ICON_PREFIX${viewModel?.weatherIcon ?: WeatherIcons.NA}",
        currentTemperature = viewModel?.curTemp?.replace(viewModel.tempUnit ?: "", "")
            ?: WeatherIcons.PLACEHOLDER,
        currentTemperatureColor = weather?.condition?.tempF?.let {
            getColorFromTempF(
                it,
                Colors.WHITE
            )
        } ?: Colors.WHITE,
        lowTemperature = viewModel?.loTemp ?: WeatherIcons.PLACEHOLDER,
        highTemperature = viewModel?.hiTemp ?: WeatherIcons.PLACEHOLDER,
        showHiLo = viewModel?.isShowHiLo ?: true,
        weatherCondition = viewModel?.curCondition ?: WeatherIcons.EM_DASH
    )
}

internal fun currentWeatherTileLayout(
    context: Context,
    deviceParameters: DeviceParameters,
    location: String,
    weatherIconId: String,
    currentTemperature: String,
    @ColorInt currentTemperatureColor: Int,
    lowTemperature: String,
    highTemperature: String,
    showHiLo: Boolean = true,
    weatherCondition: String
): LayoutElement = PrimaryLayout.Builder(deviceParameters)
    .setPrimaryLabelTextContent(
        Text.Builder(context, location)
            .setColor(ColorBuilders.argb(Colors.SIMPLEBLUELIGHT))
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .build()
    )
    .setContent(
        Row.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
            .addContent(
                Column.Builder()
                    .setWidth(expand())
                    .addContent(
                        Image.Builder()
                            .setWidth(dp(32f))
                            .setHeight(dp(32f))
                            .setResourceId(weatherIconId)
                            .build()
                    )
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .build()
            )
            .addContent(
                Spacer.Builder()
                    .setWidth(MULTI_SLOT_LAYOUT_HORIZONTAL_SPACER_WIDTH)
                    .build()
            )
            .addContent(
                Box.Builder()
                    .setWidth(if (showHiLo) wrap() else expand())
                    .setHeight(wrap())
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .addContent(
                        Text.Builder(context, currentTemperature)
                            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                            .setColor(ColorBuilders.argb(currentTemperatureColor))
                            .setModifiers(
                                Modifiers.Builder()
                                    .setPadding(
                                        Padding.Builder()
                                            .setStart(dp(4f))
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .apply {
                if (showHiLo) {
                    addContent(
                        Spacer.Builder()
                            .setWidth(MULTI_SLOT_LAYOUT_HORIZONTAL_SPACER_WIDTH)
                            .build()
                    )

                    addContent(
                        Column.Builder()
                            .setWidth(expand())
                            .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                Text.Builder(context, highTemperature)
                                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                                    .setColor(ColorBuilders.argb(Colors.WHITE))
                                    .build()
                            )
                            .addContent(
                                Text.Builder(context, lowTemperature)
                                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                                    .setColor(ColorBuilders.argb(Colors.GRAY))
                                    .build()
                            )
                            .build()
                    )
                }
            }
            .build()
    )
    .setSecondaryLabelTextContent(
        Text.Builder(context, weatherCondition)
            .setColor(ColorBuilders.argb(Colors.WHITE))
            .setTypography(Typography.TYPOGRAPHY_TITLE3)
            .build()
    )
    .build()

@WearPreviewDevices
private fun currentWeatherTilePreview(context: Context) = TilePreviewData(
    onTileResourceRequest = {
        Resources.Builder()
            .addIdToImageMapping(
                "$ID_WEATHER_ICON_PREFIX${WeatherIcons.DAY_SUNNY}",
                ImageUtils.tintedBitmapFromDrawable(
                    context,
                    R.drawable.wi_day_sunny,
                    Colors.WHITE
                ).toImageResource()
            )
            .addIdToImageMapping(
                "$ID_WEATHER_ICON_PREFIX${WeatherIcons.NA}",
                ImageUtils.tintedBitmapFromDrawable(
                    context,
                    R.drawable.wi_na,
                    Colors.WHITE
                ).toImageResource()
            )
            .build()
    },
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            currentWeatherTileLayout(
                context,
                request.deviceConfiguration,
                location = "New York, New York",
                weatherIconId = "$ID_WEATHER_ICON_PREFIX${WeatherIcons.DAY_SUNNY}",
                currentTemperature = "70°",
                currentTemperatureColor = getColorFromTempF(70f),
                lowTemperature = "60°",
                highTemperature = "75°",
                weatherCondition = "Sunny"
            )
        ).build()
    }
)
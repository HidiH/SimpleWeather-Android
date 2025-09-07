package com.thewizrd.simpleweather.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.thewizrd.common.controls.ForecastItemViewModel
import com.thewizrd.shared_resources.designer.initializeDependencies
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.utils.StringUtils.removeDigitChars

@Composable
fun ForecastItem(
    modifier: Modifier = Modifier,
    model: ForecastItemViewModel,
    iconProvider: String? = null
) {
    ForecastItem(
        modifier = modifier,
        date = model.date.removeDigitChars(),
        weatherIcon = model.weatherIcon,
        iconProvider = iconProvider,
        hiTemp = model.hiTemp,
        loTemp = model.loTemp
    )
}

@Composable
fun ForecastItem(
    modifier: Modifier = Modifier,
    date: String,
    weatherIcon: String?,
    iconProvider: String? = null,
    hiTemp: String,
    loTemp: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(2.dp),
            maxLines = 1,
            text = date,
            style = MaterialTheme.typography.bodyLarge
        )
        WeatherIcon(
            modifier = Modifier
                .padding(2.dp)
                .size(36.dp),
            weatherIcon = weatherIcon,
            iconProvider = iconProvider,
            shouldAnimate = true
        )
        Text(
            modifier = Modifier.padding(2.dp),
            maxLines = 1,
            text = hiTemp,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            modifier = Modifier.padding(2.dp),
            maxLines = 1,
            text = loTemp,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.LARGE_ROUND,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.SQUARE,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.SMALL_ROUND,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun PreviewForecastItem() {
    val context = LocalContext.current.also {
        it.initializeDependencies(isPhone = false)
    }

    Box(
        modifier = Modifier.fillMaxRectangle(),
        contentAlignment = Alignment.Center
    ) {
        ForecastItem(
            date = "Fri",
            weatherIcon = WeatherIcons.DAY_CLOUDY,
            hiTemp = "83°",
            loTemp = "64°"
        )
    }
}
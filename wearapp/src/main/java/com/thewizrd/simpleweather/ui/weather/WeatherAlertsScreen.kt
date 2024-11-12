@file:OptIn(ExperimentalHorologistApi::class)

package com.thewizrd.simpleweather.ui.weather

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.thewizrd.common.controls.WeatherAlertViewModel
import com.thewizrd.shared_resources.designer.initializeDependencies
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlert
import com.thewizrd.simpleweather.ui.ScalingLazyListStateViewModel
import com.thewizrd.simpleweather.ui.components.WeatherAlertPanel
import com.thewizrd.simpleweather.ui.compose.tools.WearPreviewDevices
import com.thewizrd.simpleweather.ui.utils.rememberFocusRequester
import java.time.ZonedDateTime

@Composable
fun WeatherAlertsScreen(
    backStackEntry: NavBackStackEntry,
    focusRequester: FocusRequester,
    alerts: List<WeatherAlertViewModel>
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollStateViewModel: ScalingLazyListStateViewModel = viewModel(backStackEntry)

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .rotaryWithScroll(scrollStateViewModel.scrollState, focusRequester),
        state = scrollStateViewModel.scrollState,
        anchorType = ScalingLazyListAnchorType.ItemCenter,
        autoCentering = AutoCenteringParams(itemIndex = 0)
    ) {
        items(alerts) { alert ->
            WeatherAlertPanel(alert)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            focusRequester.requestFocus()
        }
    }
}

@WearPreviewDevices
@Composable
private fun PreviewWeatherAlertsScreen() {
    val context = LocalContext.current.also {
        it.initializeDependencies(isPhone = false)
    }
    val scrollState = rememberScalingLazyListState()
    val focusRequester = rememberFocusRequester()
    val alerts = remember {
        List(5) {
            WeatherAlertViewModel(
                WeatherAlert().apply {
                    title = "Title"
                    message = "Message"
                    attribution = "Attribution"
                    date = ZonedDateTime.now()
                    expiresDate = ZonedDateTime.now().plusDays(1)
                }
            )
        }
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .rotaryWithScroll(scrollState, focusRequester),
        state = scrollState,
        anchorType = ScalingLazyListAnchorType.ItemCenter,
        autoCentering = AutoCenteringParams(itemIndex = 0)
    ) {
        items(alerts) { alert ->
            WeatherAlertPanel(alert)
        }
    }
}
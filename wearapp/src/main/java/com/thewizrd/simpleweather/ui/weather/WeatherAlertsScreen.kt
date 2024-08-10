@file:OptIn(ExperimentalHorologistApi::class)

package com.thewizrd.simpleweather.ui.weather

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.thewizrd.common.controls.WeatherAlertViewModel
import com.thewizrd.simpleweather.ui.ScalingLazyListStateViewModel
import com.thewizrd.simpleweather.ui.components.WeatherAlertPanel

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
        anchorType = if (alerts.size > 1) ScalingLazyListAnchorType.ItemStart else ScalingLazyListAnchorType.ItemCenter,
        autoCentering = AutoCenteringParams(itemIndex = if (alerts.size > 1) 1 else 0)
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
package com.thewizrd.simpleweather.ui.setup

import android.app.RemoteInput
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.thewizrd.common.viewmodels.LocationSearchViewModel
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.locationdata.LocationQuery
import com.thewizrd.shared_resources.weatherdata.WeatherAPI
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.ui.compose.tools.WearPreviewDevices
import com.thewizrd.simpleweather.ui.theme.activityViewModel
import com.thewizrd.simpleweather.ui.utils.rememberFocusRequester
import com.thewizrd.weather_api.weatherModule

private const val REMOTE_TEXT = "remote_text"

@Composable
fun SetupLocationSearchScreen(focusRequester: FocusRequester) {
    val context = LocalContext.current
    val locationSearchViewModel = activityViewModel<LocationSearchViewModel>()
    val isLoading by locationSearchViewModel.isLoading.collectAsState()
    val locations by locationSearchViewModel.locations.collectAsState()
    val locationSource = remember {
        val locationAPI =
            weatherModule.weatherManager.getLocationProvider().getLocationAPI()
        val entry = WeatherAPI.LocationAPIs.find { lapi -> locationAPI == lapi.value }
        entry?.toString() ?: WeatherIcons.EM_DASH
    }

    val voiceOrTextLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        activityResult.data?.let { data ->
            if (data.hasExtra(RecognizerIntent.EXTRA_RESULTS)) {
                // Result from voice input
                val voiceResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                if (!voiceResults.isNullOrEmpty()) {
                    val text = voiceResults[0]
                    if (!text.isNullOrEmpty()) {
                        // If we're using search make sure gps feature is off
                        if (settingsManager.useFollowGPS()) {
                            settingsManager.setFollowGPS(false)
                        }

                        locationSearchViewModel.fetchLocations(text)
                    }
                }
            } else {
                // Result from remote input
                val results = RemoteInput.getResultsFromIntent(data)

                if (!results.isEmpty) {
                    val text = results.getCharSequence(REMOTE_TEXT) as? String
                    if (!text.isNullOrEmpty()) {
                        // If we're using search make sure gps feature is off
                        if (settingsManager.useFollowGPS()) {
                            settingsManager.setFollowGPS(false)
                        }

                        locationSearchViewModel.fetchLocations(text)
                    }
                }
            }
        }
    }

    SetupLocationSearchScreen(
        focusRequester = focusRequester,
        isLoading = isLoading,
        locations = locations,
        locationSource = locationSource,
        onVoiceSearch = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    context.getString(R.string.location_search_hint)
                )
            }

            voiceOrTextLauncher.launch(intent)
        },
        onTextSearch = {
            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs = listOf(
                RemoteInput.Builder(REMOTE_TEXT)
                    .setLabel(context.getString(R.string.location_search_hint))
                    .setAllowFreeFormInput(true)
                    .wearableExtender {
                        setEmojisAllowed(false)
                        setInputActionType(EditorInfo.IME_ACTION_SEARCH)
                    }
                    .build()
            )
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            voiceOrTextLauncher.launch(intent)
        },
        onLocationSelected = {
            if (it != LocationQuery.EMPTY) {
                locationSearchViewModel.onLocationSelected(it)
            }
        }
    )
}

@Composable
private fun SetupLocationSearchScreen(
    focusRequester: FocusRequester,
    isLoading: Boolean = false,
    locationSource: String = "Weather Provider",
    locations: List<LocationQuery> = emptyList(),
    onVoiceSearch: () -> Unit = {},
    onTextSearch: () -> Unit = {},
    onLocationSelected: ((LocationQuery) -> Unit) = {}
) {
    val context = LocalContext.current

    if (isLoading) {
        ScreenScaffold {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    } else {
        if (locations.isNotEmpty()) {
            val scrollState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()

            ScreenScaffold(scrollState = scrollState) { contentPadding ->
                TransformingLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotaryScrollable(
                            RotaryScrollableDefaults.behavior(scrollState),
                            focusRequester
                        ),
                    state = scrollState,
                    contentPadding = contentPadding,
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .animateItem(),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = stringResource(id = R.string.label_nav_locations))
                        }
                    }

                    items(items = locations) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .animateItem(),
                            transformation = SurfaceTransformation(transformationSpec),
                            onClick = {
                                onLocationSelected(it)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            label = {
                                Text(text = it.locationName ?: "", maxLines = 3)
                            },
                            secondaryLabel = it.locationCountry?.let { country ->
                                {
                                    Text(text = country)
                                }
                            },
                            icon = if (it.locationQuery.isNullOrBlank() && it.locationCountry.isNullOrBlank()) {
                                null
                            } else {
                                {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_place_white_24dp),
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }

                    item {
                        ListSubHeader(
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .animateItem(),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = remember(context, locationSource) {
                                buildString {
                                    append(context.getString(R.string.credit_prefix))
                                    append(" ")
                                    append(locationSource)
                                }
                            }, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            ScreenScaffold { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    ListHeader(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.location_search_hint))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentSize(align = Alignment.Center)
                                .align(Alignment.Center)
                        ) {
                            IconButton(
                                colors = IconButtonDefaults.filledIconButtonColors(),
                                onClick = onVoiceSearch
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_voice_black_24dp),
                                    contentDescription = stringResource(R.string.abc_searchview_description_voice)
                                )
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            IconButton(
                                colors = IconButtonDefaults.filledIconButtonColors(),
                                onClick = onTextSearch
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_black_24dp),
                                    contentDescription = stringResource(R.string.abc_searchview_description_search)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
private fun PreviewSetupLocationSearchScreen() {
    SetupLocationSearchScreen(
        focusRequester = rememberFocusRequester(),
        isLoading = false,
        locations = emptyList()
    )
}
package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thewizrd.common.controls.WeatherUiModel
import com.thewizrd.common.controls.toUiModel
import com.thewizrd.common.helpers.locationPermissionEnabled
import com.thewizrd.common.location.LocationProvider
import com.thewizrd.common.location.LocationResult
import com.thewizrd.common.performance.PerfTrace
import com.thewizrd.common.utils.ErrorMessage
import com.thewizrd.common.weatherdata.WeatherDataLoader
import com.thewizrd.common.weatherdata.WeatherRequest
import com.thewizrd.common.weatherdata.WeatherResult
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.exceptions.ErrorStatus
import com.thewizrd.shared_resources.exceptions.WeatherException
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.locationdata.buildEmptyGPSLocation
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.shared_resources.weatherdata.model.LocationType
import com.thewizrd.simpleweather.BuildConfig
import com.thewizrd.simpleweather.wearable.WearableListenerActions.ACTION_REQUESTSYNCWEATHER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface WeatherNowState {
    val weather: WeatherUiModel?
    val isLoading: Boolean
    val errorMessages: List<ErrorMessage>
    val isGPSLocation: Boolean
    val locationData: LocationData?
    val noLocationAvailable: Boolean

    data class NoWeather(
        override val weather: WeatherUiModel? = null,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val isGPSLocation: Boolean,
        override val locationData: LocationData? = null,
        override val noLocationAvailable: Boolean = false
    ) : WeatherNowState

    data class HasWeather(
        override val weather: WeatherUiModel,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val isGPSLocation: Boolean,
        override val locationData: LocationData? = null,
        override val noLocationAvailable: Boolean = false
    ) : WeatherNowState
}

private data class WeatherNowViewModelState(
    val weather: WeatherUiModel? = null,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val isGPSLocation: Boolean = false,
    val locationData: LocationData? = null,
    val noLocationAvailable: Boolean = false,
    val isInitialized: Boolean = false
) {
    fun toWeatherNowState(): WeatherNowState {
        return if (weather?.isValid == true) {
            WeatherNowState.HasWeather(
                weather = weather,
                isLoading = isLoading,
                errorMessages = errorMessages,
                isGPSLocation = isGPSLocation,
                locationData = locationData,
                noLocationAvailable = noLocationAvailable
            )
        } else {
            WeatherNowState.NoWeather(
                isLoading = isLoading,
                errorMessages = errorMessages,
                isGPSLocation = isGPSLocation,
                locationData = locationData,
                noLocationAvailable = noLocationAvailable
            )
        }
    }
}

class WeatherNowViewModel(private val app: Application) : AndroidViewModel(app) {
    private val viewModelState =
        MutableStateFlow(WeatherNowViewModelState(isLoading = true, noLocationAvailable = true))

    private val weatherDataLoader = WeatherDataLoader()

    private val locationProvider = LocationProvider(app)

    private val _eventsFlow = MutableSharedFlow<WearableEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<WearableEvent> = _eventsFlow

    val uiState = viewModelState.map {
        it.toWeatherNowState()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value.toWeatherNowState()
    )

    val weather = viewModelState.mapNotNull {
        it.weather
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value.weather ?: WeatherUiModel()
    )

    val errorMessages = viewModelState.map {
        it.errorMessages
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        viewModelState.value.errorMessages
    )

    val isInitialized = viewModelState.map {
        it.isInitialized
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value.isInitialized
    )

    private fun getLocationData(): LocationData? {
        return viewModelState.value.locationData
    }

    fun initialize(locationData: LocationData? = null) {
        viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            var locData = locationData ?: settingsManager.getHomeData()

            if (settingsManager.useFollowGPS()) {
                if (locData != null && settingsManager.getAPI() != locData.weatherSource) {
                    settingsManager.updateLocation(buildEmptyGPSLocation())
                }

                val result = updateLocation()

                if (result is LocationResult.Changed) {
                    settingsManager.updateLocation(result.data)
                    locData = result.data
                }
            }

            updateLocation(locData)

            viewModelState.update {
                it.copy(isInitialized = true)
            }
        }
    }

    fun refreshWeather(forceRefresh: Boolean = false) {
        val isDataSync =
            !BuildConfig.IS_NONGMS && settingsManager.getDataSync() != WearableDataSync.OFF

        val trace = PerfTrace("wnow_refreshWeather").apply {
            putAttribute("forceRefresh", forceRefresh.toString())
            putAttribute("isDataSync", isDataSync.toString())
            startTrace()
        }

        viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            if (isDataSync) {
                loadSavedWeather(forceSync = forceRefresh)
                trace.stopTrace()
            } else {
                if (settingsManager.useFollowGPS()) {
                    val result = updateLocation()

                    if (result is LocationResult.Changed) {
                        settingsManager.updateLocation(result.data)
                        weatherDataLoader.updateLocation(result.data)
                    } else if (result is LocationResult.NotChanged) {
                        result.data?.takeIf { it.isValid }?.let { data ->
                            if (!weatherDataLoader.isLocationValid()) {
                                weatherDataLoader.updateLocation(data)
                                viewModelState.update { it.copy(locationData = data) }
                            }
                        }
                    }
                }

                val result = if (weatherDataLoader.isLocationValid()) {
                    weatherDataLoader.loadWeatherResult(
                        WeatherRequest.Builder()
                            .forceRefresh(forceRefresh)
                            .loadAlerts()
                            .apply {
                                if (forceRefresh) {
                                    loadForecasts()
                                }
                            }
                            .build()
                    )
                } else {
                    WeatherResult.NoWeather()
                }

                updateWeatherState(result)
            }

            trace.stopTrace()
        }
    }

    private suspend fun loadSavedWeather(forceSync: Boolean = false) {
        val result = weatherDataLoader.loadWeatherResult(
            WeatherRequest.Builder()
                .forceLoadSavedData()
                .loadAlerts()
                .build()
        )

        if (forceSync && (result !is WeatherResult.Success && result !is WeatherResult.WeatherWithError)) {
            _eventsFlow.tryEmit(WearableEvent(ACTION_REQUESTSYNCWEATHER))
        } else {
            updateWeatherState(result)
        }
    }

    private fun updateWeatherState(result: WeatherResult) {
        when (result) {
            is WeatherResult.Error -> {
                viewModelState.update { state ->
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(result.exception)
                    state.copy(
                        errorMessages = errorMessages,
                        isLoading = false,
                        noLocationAvailable = false
                    )
                }
            }

            is WeatherResult.NoWeather -> {
                viewModelState.update { state ->
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(WeatherException(ErrorStatus.NOWEATHER))
                    state.copy(
                        errorMessages = errorMessages,
                        isLoading = false,
                        noLocationAvailable = false
                    )
                }
            }

            is WeatherResult.Success -> {
                val weatherData = result.data.toUiModel()

                viewModelState.update { state ->
                    state.copy(
                        weather = weatherData,
                        isLoading = false,
                        noLocationAvailable = false,
                        isGPSLocation = state.locationData?.locationType == LocationType.GPS
                    )
                }
            }

            is WeatherResult.WeatherWithError -> {
                val weatherData = result.data.toUiModel()

                viewModelState.update { state ->
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(result.exception)
                    state.copy(
                        weather = weatherData,
                        errorMessages = errorMessages,
                        isLoading = false,
                        noLocationAvailable = false,
                        isGPSLocation = state.locationData?.locationType == LocationType.GPS
                    )
                }
            }
        }
    }

    fun setErrorMessageShown(error: ErrorMessage) {
        viewModelState.update { state ->
            state.copy(
                errorMessages = state.errorMessages.filterNot { it == error }
            )
        }
    }

    private suspend fun updateLocation(): LocationResult {
        var locationData = getLocationData()

        if ((BuildConfig.IS_NONGMS || settingsManager.getDataSync() == WearableDataSync.OFF) && settingsManager.useFollowGPS() && (locationData == null || locationData.locationType == LocationType.GPS)) {
            if (!app.locationPermissionEnabled()) {
                return LocationResult.NotChanged(locationData)
            }

            val locMan = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

            if (locMan == null || !LocationManagerCompat.isLocationEnabled(locMan)) {
                locationData = settingsManager.getHomeData()
                return LocationResult.NotChanged(locationData)
            }

            return locationProvider.getLatestLocationData(locationData)
        }

        return LocationResult.NotChanged(locationData)
    }

    fun updateLocation(locationData: LocationData?) {
        viewModelState.update {
            it.copy(locationData = locationData)
        }

        if (locationData?.isValid == true) {
            viewModelState.update {
                it.copy(locationData = locationData, noLocationAvailable = false)
            }
            weatherDataLoader.updateLocation(locationData)
            refreshWeather(false)
        } else {
            checkInvalidLocation(locationData)

            viewModelState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun checkInvalidLocation(locationData: LocationData?) {
        if (locationData == null || !locationData.isValid) {
            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    Logger.writeLine(
                        Log.WARN,
                        "Location: %s",
                        JSONParser.serializer(locationData, LocationData::class.java)
                    )
                    Logger.writeLine(
                        Log.WARN,
                        "Home: %s",
                        JSONParser.serializer(
                            settingsManager.getHomeData(),
                            LocationData::class.java
                        )
                    )

                    Logger.writeLine(Log.WARN, IllegalStateException("Invalid location data"))
                }

                viewModelState.update {
                    it.copy(noLocationAvailable = true, isLoading = false)
                }
            }
        }
    }
}
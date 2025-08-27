package com.thewizrd.simpleweather.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.common.weatherdata.WeatherDataLoader
import com.thewizrd.common.weatherdata.WeatherRequest
import com.thewizrd.common.weatherdata.WeatherResult
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.exceptions.ErrorStatus
import com.thewizrd.shared_resources.exceptions.WeatherException
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.locationdata.buildEmptyGPSLocation
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.wearable.WearableDataSync
import com.thewizrd.shared_resources.weatherdata.model.LocationType
import com.thewizrd.simpleweather.BuildConfig
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.wearable.WearableWorker
import com.thewizrd.simpleweather.wearable.WearableWorkerActions
import com.thewizrd.simpleweather.wearable.WeatherDataSyncWorker
import com.thewizrd.weather_api.weatherModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    val showDisconnectedView: Boolean

    data class NoWeather(
        override val weather: WeatherUiModel? = null,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val isGPSLocation: Boolean,
        override val locationData: LocationData? = null,
        override val noLocationAvailable: Boolean = false,
        override val showDisconnectedView: Boolean = false
    ) : WeatherNowState

    data class HasWeather(
        override val weather: WeatherUiModel,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val isGPSLocation: Boolean,
        override val locationData: LocationData? = null,
        override val noLocationAvailable: Boolean = false,
        override val showDisconnectedView: Boolean = false
    ) : WeatherNowState
}

private data class WeatherNowViewModelState(
    val weather: WeatherUiModel? = null,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val isGPSLocation: Boolean = false,
    val locationData: LocationData? = null,
    val noLocationAvailable: Boolean = false,
    val showDisconnectedView: Boolean = false
) {
    fun toWeatherNowState(): WeatherNowState {
        return if (weather?.isValid == true) {
            WeatherNowState.HasWeather(
                weather = weather,
                isLoading = isLoading,
                errorMessages = errorMessages,
                isGPSLocation = isGPSLocation,
                locationData = locationData,
                noLocationAvailable = noLocationAvailable,
                showDisconnectedView = showDisconnectedView
            )
        } else {
            WeatherNowState.NoWeather(
                isLoading = isLoading,
                errorMessages = errorMessages,
                isGPSLocation = isGPSLocation,
                locationData = locationData,
                noLocationAvailable = noLocationAvailable,
                showDisconnectedView = showDisconnectedView
            )
        }
    }
}

class WeatherNowViewModel(private val app: Application) : AndroidViewModel(app),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val viewModelState =
        MutableStateFlow(WeatherNowViewModelState(isLoading = true, noLocationAvailable = true))
    private val weatherDataLoader = WeatherDataLoader()
    private val wm = weatherModule.weatherManager

    private val locationProvider = LocationProvider(app)
    private val dataSyncWorker = WeatherDataSyncWorker(
        app,
        onLoadData = {
            viewModelScope.launch {
                // We got all our data; now load the weather
                val locationData = settingsManager.getHomeData()

                viewModelState.update {
                    it.copy(isLoading = true, locationData = locationData)
                }

                if (locationData?.isValid == true) {
                    weatherDataLoader.updateLocation(locationData)
                    loadSavedWeather()
                } else {
                    cancelDataSync()
                }
            }
        },
        onCancelSync = {
            cancelDataSync()
        }
    )

    init {
        if (!BuildConfig.IS_NONGMS) {
            dataSyncWorker.registerSyncReceiver()
        }
        appLib.registerAppSharedPreferenceListener(this)

        initializeWeatherState()
    }

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

    private fun getLocationData(): LocationData? {
        return viewModelState.value.locationData
    }

    private fun initializeWeatherState() {
        viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            var locData = settingsManager.getHomeData()

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

            if (locData?.isValid == true) {
                viewModelState.update {
                    it.copy(locationData = locData, noLocationAvailable = false)
                }
                weatherDataLoader.updateLocation(locData)
                refreshWeather(false)
            } else {
                checkInvalidLocation(locData)

                viewModelState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun refreshWeather(forceRefresh: Boolean = false) {
        val trace = PerfTrace("wnow_refreshWeather").apply {
            putAttribute("forceRefresh", forceRefresh.toString())
            startTrace()
        }

        viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            if (BuildConfig.IS_NONGMS || settingsManager.getDataSync() == WearableDataSync.OFF) {
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
            } else {
                syncWeather(forceRefresh)
            }

            trace.stopTrace()
        }
    }

    private fun loadSavedWeather(forceSync: Boolean = false) {
        viewModelScope.launch {
            val result = weatherDataLoader.loadWeatherResult(
                WeatherRequest.Builder()
                    .forceLoadSavedData()
                    .loadAlerts()
                    .build()
            )

            if (forceSync && (result !is WeatherResult.Success && result !is WeatherResult.WeatherWithError)) {
                syncWeather(true)
            } else {
                updateWeatherState(result)
            }
        }
    }

    private fun updateWeatherState(result: WeatherResult) {
        viewModelState.update { state ->
            when (result) {
                is WeatherResult.Error -> {
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(result.exception)
                    state.copy(
                        errorMessages = errorMessages,
                        isLoading = false,
                        noLocationAvailable = false
                    )
                }
                is WeatherResult.NoWeather -> {
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(WeatherException(ErrorStatus.NOWEATHER))
                    state.copy(
                        errorMessages = errorMessages,
                        isLoading = false,
                        noLocationAvailable = false
                    )
                }
                is WeatherResult.Success -> {
                    state.copy(
                        weather = result.data.toUiModel(),
                        isLoading = false,
                        noLocationAvailable = false,
                        isGPSLocation = state.locationData?.locationType == LocationType.GPS
                    )
                }
                is WeatherResult.WeatherWithError -> {
                    val errorMessages =
                        state.errorMessages + ErrorMessage.WeatherError(result.exception)
                    state.copy(
                        weather = result.data.toUiModel(),
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

    /* Wearable Data Sync */
    private fun cancelDataSync() {
        if (!BuildConfig.IS_NONGMS) {
            dataSyncWorker.cancelDataSync()

            if (settingsManager.getDataSync() != WearableDataSync.OFF) {
                viewModelScope.launch {
                    var locationData = getLocationData()

                    if (locationData == null) {
                        locationData = settingsManager.getHomeData()
                    }

                    viewModelState.update {
                        it.copy(locationData = locationData)
                    }

                    if (locationData?.isValid == true) {
                        weatherDataLoader.updateLocation(locationData)
                        loadSavedWeather()
                    } else {
                        viewModelState.update {
                            it.copy(isLoading = false)
                        }

                        if (locationData != null) {
                            checkInvalidLocation(locationData)
                        } else {
                            viewModelState.update {
                                val errorMessages =
                                    it.errorMessages + ErrorMessage.Resource(R.string.error_syncing)
                                it.copy(errorMessages = errorMessages)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncWeather(forceRefresh: Boolean = false) {
        if (!BuildConfig.IS_NONGMS) {
            if (forceRefresh) {
                // Request update from connected device
                WearableWorker.enqueueAction(app, WearableWorkerActions.ACTION_REQUESTUPDATE, true)
                dataSyncWorker.startSyncTimer()
            } else {
                val locationData = settingsManager.getHomeData()

                viewModelState.update {
                    it.copy(locationData = locationData)
                }

                if (locationData?.isValid == true) {
                    viewModelState.update {
                        it.copy(noLocationAvailable = false)
                    }
                    weatherDataLoader.updateLocation(locationData)
                    loadSavedWeather(true)
                } else {
                    checkInvalidLocation(locationData)

                    viewModelState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun updateConnectionStatus(connectionStatus: WearConnectionStatus) {
        viewModelState.update {
            it.copy(showDisconnectedView = settingsManager.getDataSync() != WearableDataSync.OFF && connectionStatus != WearConnectionStatus.CONNECTED)
        }
    }

    override fun onCleared() {
        super.onCleared()

        dataSyncWorker.close()
        appLib.unregisterAppSharedPreferenceListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (key.isNullOrBlank()) return

        when (key) {
            SettingsManager.KEY_DATASYNC -> {
                // If data sync settings changes,
                // reset so we can properly reload
                viewModelState.update {
                    it.copy(locationData = null)
                }
            }
            SettingsManager.KEY_TEMPUNIT,
            SettingsManager.KEY_DISTANCEUNIT,
            SettingsManager.KEY_PRECIPITATIONUNIT,
            SettingsManager.KEY_PRESSUREUNIT,
            SettingsManager.KEY_SPEEDUNIT,
            SettingsManager.KEY_ICONSSOURCE -> {
                refreshWeather(false)
            }
        }
    }
}
package com.thewizrd.simpleweather.ui.navigation

/**
 * Represent all Screens (Composables) in the app.
 */
sealed class Screen(
    val route: String
) {
    // Weather Screens
    object WeatherNow : Screen("weathernow")
    object Alerts : Screen("alerts")
    object Details : Screen("weatherdetails")
    object Forecast : Screen("forecast")
    object HourlyForecast : Screen("hourlyforecast")
    object Precipitation : Screen("precipitation")

    // Weather Preferences
    object DetailsTileEditor : Screen("detailstileeditor")

    // Setup
    object Setup : Screen("setup")
    object SetupLocationSearch : Screen("setup_locationSearch")
    object SetupSync : Screen("setup_sync")

    // Settings
    object SettingsGeneral : Screen("pref_general")
    object SettingsUnits : Screen("pref_units")
    object SettingsIcons : Screen("pref_icons")
    object SettingsAboutApp : Screen("pref_aboutapp")
    object SettingsCredits : Screen("pref_credits")
    object SettingsOSSCredits : Screen("pref_oslibs")
}

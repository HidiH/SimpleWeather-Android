package com.thewizrd.simpleweather.controls.graphs

import com.thewizrd.shared_resources.icons.WeatherIcons

class ForecastRangeBarEntry {
    var date: CharSequence? = null
    var weatherIcon: String = WeatherIcons.NA
    var iconRotation: Int = 0

    var hiTempData: YEntryData? = null
    var loTempData: YEntryData? = null

    var pop: Int? = null
}
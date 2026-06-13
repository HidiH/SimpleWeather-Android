package com.thewizrd.simpleweather.controls.graphs

import androidx.annotation.ColorInt

class ForecastRangeBarEntry : GraphEntry {
    var hiTempData: YEntryData? = null
    var loTempData: YEntryData? = null

    var pop: Int? = null

    var fillColors: IntArray? = null

    constructor()

    constructor(
        label: CharSequence,
        hiTempData: YEntryData,
        loTempData: YEntryData,
        pop: Int? = null,
        weatherIcon: String? = null
    ) {
        this.xLabel = label
        this.hiTempData = hiTempData
        this.loTempData = loTempData
        this.pop = pop
        this.xWeatherIcon = weatherIcon
    }

    fun setFillColors(@ColorInt hiColor: Int, @ColorInt loColor: Int) {
        fillColors = intArrayOf(hiColor, loColor)
    }

    fun setFillColor(@ColorInt color: Int) {
        fillColors = intArrayOf(color, color)
    }
}
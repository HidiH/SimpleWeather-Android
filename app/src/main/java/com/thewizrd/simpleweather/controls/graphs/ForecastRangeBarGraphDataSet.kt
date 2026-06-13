package com.thewizrd.simpleweather.controls.graphs

class ForecastRangeBarGraphDataSet : GraphDataSet<ForecastRangeBarEntry> {

    var label: String? = null

    var min: Float? = null
    var max: Float? = null

    constructor(entries: List<ForecastRangeBarEntry>) : super(entries) {
        this.label = null
    }

    constructor(label: String?, entries: List<ForecastRangeBarEntry>) : this(entries) {
        this.label = label
    }

    @JvmOverloads
    fun setMinMax(min: Float? = null, max: Float? = null) {
        this.min = min
        this.max = max
    }

    override fun calcMinMax(entry: ForecastRangeBarEntry) {
        if (min == null) {
            if (entry.hiTempData?.y != null && entry.hiTempData!!.y < yMin) {
                yMin = entry.hiTempData!!.y
            }

            if (entry.loTempData?.y != null && entry.loTempData!!.y < yMin) {
                yMin = entry.loTempData!!.y
            }
        }

        if (max == null) {
            if (entry.hiTempData?.y != null && entry.hiTempData!!.y > yMax) {
                yMax = entry.hiTempData!!.y
            }

            if (entry.loTempData?.y != null && entry.loTempData!!.y > yMax) {
                yMax = entry.loTempData!!.y
            }
        }
    }
}
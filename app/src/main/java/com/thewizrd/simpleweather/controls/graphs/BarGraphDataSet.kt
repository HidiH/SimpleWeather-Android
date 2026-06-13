package com.thewizrd.simpleweather.controls.graphs

class BarGraphDataSet : GraphDataSet<BarGraphEntry> {

    var label: String? = null

    var min: Float? = null
    var max: Float? = null

    constructor(entries: List<BarGraphEntry>) : super(entries) {
        this.label = null
    }

    constructor(label: String?, entries: List<BarGraphEntry>) : this(entries) {
        this.label = label
    }

    @JvmOverloads
    fun setMinMax(min: Float? = null, max: Float? = null) {
        this.min = min
        this.max = max
    }

    override fun calcMinMax(entry: BarGraphEntry) {
        if (min == null) {
            if (entry.entryData?.y != null && entry.entryData!!.y < yMin) {
                yMin = entry.entryData!!.y
            }
        }

        if (max == null) {
            if (entry.entryData?.y != null && entry.entryData!!.y > yMax) {
                yMax = entry.entryData!!.y
            }
        }
    }
}
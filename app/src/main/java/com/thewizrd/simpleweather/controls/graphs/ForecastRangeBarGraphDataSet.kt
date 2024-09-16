package com.thewizrd.simpleweather.controls.graphs

class ForecastRangeBarGraphDataSet {
    var entryData: MutableList<ForecastRangeBarEntry>
        private set

    var yMax = -Float.MAX_VALUE
        private set
    var yMin = Float.MAX_VALUE
        private set

    constructor() {
        this.entryData = ArrayList()
    }

    constructor(entries: List<ForecastRangeBarEntry>) {
        this.entryData = entries as? MutableList ?: entries.toMutableList() ?: ArrayList()
        calcMinMax()
    }

    fun calcMinMax() {
        yMax = -Float.MAX_VALUE
        yMin = Float.MAX_VALUE

        if (entryData.isNullOrEmpty()) {
            return
        }

        for (entry in entryData) {
            calcMinMax(entry)
        }
    }

    private fun calcMinMax(entry: ForecastRangeBarEntry) {
        if (entry.hiTempData?.y != null && entry.hiTempData!!.y < yMin) {
            yMin = entry.hiTempData!!.y
        }

        if (entry.hiTempData?.y != null && entry.hiTempData!!.y > yMax) {
            yMax = entry.hiTempData!!.y
        }

        if (entry.loTempData?.y != null && entry.loTempData!!.y < yMin) {
            yMin = entry.loTempData!!.y
        }

        if (entry.loTempData?.y != null && entry.loTempData!!.y > yMax) {
            yMax = entry.loTempData!!.y
        }
    }

    fun setEntries(entries: List<ForecastRangeBarEntry>) {
        this.entryData = entries as? MutableList ?: entries.toMutableList() ?: ArrayList()
        notifyDataSetChanged()
    }

    val dataCount: Int
        get() = entryData.size

    val isEmpty: Boolean
        get() = entryData.isEmpty()

    fun clear() {
        entryData.clear()
        notifyDataSetChanged()
    }

    fun addEntry(entry: ForecastRangeBarEntry): Boolean {
        calcMinMax(entry)
        return entryData.add(entry)
    }

    fun removeEntry(entry: ForecastRangeBarEntry): Boolean {
        val removed = entryData.remove(entry)

        if (removed) {
            calcMinMax()
        }

        return removed
    }

    fun getEntryIndex(entry: ForecastRangeBarEntry): Int {
        return entryData.indexOf(entry)
    }

    fun getEntryForIndex(idx: Int): ForecastRangeBarEntry {
        return entryData[idx]
    }

    fun notifyDataSetChanged() {
        calcMinMax()
    }
}
package com.thewizrd.simpleweather.controls.graphs

class ForecastRangeBarGraphData : GraphData<ForecastRangeBarGraphDataSet> {
    constructor() : super()
    constructor(set: ForecastRangeBarGraphDataSet) : super(set)

    fun setDataSet(set: ForecastRangeBarGraphDataSet) {
        dataSets.clear()
        dataSets.add(set)
        notifyDataChanged()
    }

    fun getDataSet(): ForecastRangeBarGraphDataSet? {
        return dataSets.firstOrNull()
    }

    override fun calcMinMax(set: ForecastRangeBarGraphDataSet) {
        if (set.min == null && set.max == null) {
            if (yMax < set.yMax) {
                yMax = set.yMax
            }
            if (yMin > set.yMin) {
                yMin = set.yMin
            }
        } else {
            if (set.max != null) {
                if (yMax < set.max!!) {
                    yMax = set.max!!
                }
            } else if (yMax < set.yMax) {
                yMax = set.yMax
            }

            if (set.min != null) {
                if (yMin > set.min!!)
                    yMin = set.min!!
            } else if (yMin > set.yMin) {
                yMin = set.yMin
            }
        }
    }
}
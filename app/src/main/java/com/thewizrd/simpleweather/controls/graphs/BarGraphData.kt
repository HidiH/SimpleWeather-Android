package com.thewizrd.simpleweather.controls.graphs

class BarGraphData : GraphData<BarGraphDataSet> {
    constructor() : super()
    constructor(set: BarGraphDataSet) : super(set)
    constructor(label: CharSequence, set: BarGraphDataSet) : super(set) {
        this.graphLabel = label
    }

    fun setDataSet(set: BarGraphDataSet) {
        dataSets.clear()
        dataSets.add(set)
        notifyDataChanged()
    }

    fun getDataSet(): BarGraphDataSet? {
        return dataSets.firstOrNull()
    }

    override fun calcMinMax(set: BarGraphDataSet) {
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
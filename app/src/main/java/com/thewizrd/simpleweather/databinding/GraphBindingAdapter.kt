package com.thewizrd.simpleweather.databinding

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.thewizrd.shared_resources.weatherdata.model.Forecast
import com.thewizrd.shared_resources.weatherdata.model.HourlyForecast
import com.thewizrd.shared_resources.weatherdata.model.MinutelyForecast
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.ForecastBarGraphView
import com.thewizrd.simpleweather.controls.ForecastRangeBarGraphView
import com.thewizrd.simpleweather.controls.graphs.BarGraphData
import com.thewizrd.simpleweather.controls.graphs.BarGraphPanel
import com.thewizrd.simpleweather.controls.graphs.ForecastGraphPanel
import com.thewizrd.simpleweather.controls.graphs.GraphData
import com.thewizrd.simpleweather.controls.graphs.LineViewData
import com.thewizrd.simpleweather.controls.graphs.RangeBarGraphData
import com.thewizrd.simpleweather.controls.graphs.RangeBarGraphPanel
import com.thewizrd.simpleweather.controls.viewmodels.ForecastGraphViewModel
import com.thewizrd.simpleweather.controls.viewmodels.ForecastGraphViewModel.GraphType
import com.thewizrd.simpleweather.controls.viewmodels.ForecastType
import com.thewizrd.simpleweather.controls.viewmodels.GraphModelUtils
import com.thewizrd.simpleweather.controls.viewmodels.RangeBarGraphMapper.createForecastGraphData
import com.thewizrd.simpleweather.controls.viewmodels.RangeBarGraphMapper.createGraphData

object GraphBindingAdapter {
    @JvmStatic
    @BindingAdapter("graphData")
    fun updateForecastGraph(view: ForecastGraphPanel, graphData: GraphData<*>?) {
        view.setGraphData(graphData as LineViewData?)
        view.setDrawSeriesLabels(graphData?.dataSets?.any { !it.seriesLabel.isNullOrEmpty() } == true)
    }

    @JvmStatic
    @BindingAdapter("graphData")
    fun updateForecastGraph(view: RangeBarGraphPanel, graphData: RangeBarGraphData?) {
        view.setGraphData(graphData)
    }

    @JvmStatic
    @BindingAdapter("minForecastData")
    fun updateMinForecastGraph(view: ForecastGraphPanel, forecastData: List<MinutelyForecast>?) {
        if (!forecastData.isNullOrEmpty()) {
            val vm = ForecastGraphViewModel(view.context)
            vm.setMinutelyForecastData(forecastData, GraphType.Line)
            view.setGraphData(vm.graphData as LineViewData?)
            view.setDrawSeriesLabels((vm.graphData as? LineViewData)?.dataSets?.any { !it.seriesLabel.isNullOrEmpty() } == true)
        } else {
            view.setGraphData(null)
        }
    }

    @JvmStatic
    @BindingAdapter("minForecastData")
    fun updateMinForecastGraph(view: ForecastBarGraphView, forecastData: List<MinutelyForecast>?) {
        if (!forecastData.isNullOrEmpty()) {
            val vm = ForecastGraphViewModel(view.context)
            vm.setMinutelyForecastData(forecastData, GraphType.Bar)
            view.setData(vm.graphData as BarGraphData?, vm.forecastType)
        } else {
            view.setData(null)
        }
    }

    @JvmStatic
    @BindingAdapter("forecastData")
    fun updateForecastGraph(view: ForecastGraphPanel, forecastData: List<HourlyForecast>?) {
        if (!forecastData.isNullOrEmpty()) {
            val vm = ForecastGraphViewModel(view.context).apply {
                setForecastData(
                    forecastData,
                    forecastData.getRecommendedForecastType(),
                    GraphType.Line
                )
            }
            view.setGraphData(vm.graphData as? LineViewData)
            view.setDrawSeriesLabels((vm.graphData as? LineViewData)?.dataSets?.any { !it.seriesLabel.isNullOrEmpty() } == true)
        } else {
            view.setGraphData(null)
        }
    }

    @JvmStatic
    @BindingAdapter("forecastData")
    fun updateForecastGraph(view: ForecastBarGraphView, forecastData: List<HourlyForecast>?) {
        if (!forecastData.isNullOrEmpty()) {
            val vm = ForecastGraphViewModel(view.context).apply {
                setForecastData(
                    forecastData,
                    forecastData.getRecommendedForecastType(),
                    GraphType.Bar
                )
            }
            view.setData(vm.graphData as? BarGraphData, vm.forecastType)
        } else {
            view.setData(null)
        }
    }

    @JvmStatic
    @BindingAdapter("forecastDataLabel")
    fun updateForecastGraphLabel(view: TextView, forecastData: List<HourlyForecast>?) {
        if (!forecastData.isNullOrEmpty()) {
            view.text = GraphModelUtils.getLabelForGraphType(
                view.context,
                forecastData.getRecommendedForecastType()
            )
        } else {
            view.text = ""
        }
    }

    private fun List<HourlyForecast>.getRecommendedForecastType(): ForecastType {
        return if (this.firstOrNull()?.extras?.pop != null && this.lastOrNull()?.extras?.pop != null) {
            ForecastType.PRECIPITATION
        } else if (this.firstOrNull()?.extras?.qpfRainMm != null && this.lastOrNull()?.extras?.qpfRainMm != null) {
            ForecastType.RAIN
        } else {
            ForecastType.WIND
        }
    }

    @JvmStatic
    @BindingAdapter("forecastData")
    fun updateForecastGraph(view: RangeBarGraphPanel, forecastData: List<Forecast>?) {
        val maxForecasts = view.context.resources.getInteger(R.integer.weathernow_max_forecasts)
        view.setGraphData(createGraphData(view.context, forecastData?.take(maxForecasts)))
    }

    @JvmStatic
    @BindingAdapter("forecastData")
    fun updateForecastGraph(view: ForecastRangeBarGraphView, forecastData: List<Forecast>?) {
        view.setData(createForecastGraphData(forecastData))
    }

    @JvmStatic
    @BindingAdapter("graphData")
    fun updateBarGraph(view: BarGraphPanel, graphData: BarGraphData?) {
        view.setGraphData(graphData)
    }

    @JvmStatic
    @BindingAdapter(value = ["graphData", "forecastType"], requireAll = true)
    fun updateBarGraph(
        view: ForecastBarGraphView,
        graphData: BarGraphData?,
        forecastType: ForecastType? = null
    ) {
        view.setData(graphData, forecastType)
    }

    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter("graphLabel")
    fun updateBarGraph(view: TextView, graphData: BarGraphData?) {
        val graphLabel = graphData?.graphLabel
        val dataSetLabel = graphData?.getDataSet()?.label

        if (graphLabel != null && dataSetLabel != null) {
            view.text = "$graphLabel ($dataSetLabel)"
        } else {
            view.text = graphLabel
        }
    }
}

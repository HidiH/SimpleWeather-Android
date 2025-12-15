package com.thewizrd.simpleweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.graphs.BarGraphData
import com.thewizrd.simpleweather.controls.viewmodels.ForecastGraphViewModel
import com.thewizrd.simpleweather.databinding.ChartsBargraphpanelBinding
import com.thewizrd.simpleweather.databinding.ChartsForecastgraphpanelBinding
import java.util.Objects

class ChartsItemAdapter : ListAdapter<ForecastGraphViewModel, RecyclerView.ViewHolder> {
    constructor() : super(diffCallback)
    constructor(config: AsyncDifferConfig<ForecastGraphViewModel>) : super(config)

    private class ChartType {
        companion object {
            const val LineView = 0
            const val BarChart = 1
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ForecastGraphViewModel>() {
            override fun areItemsTheSame(oldItem: ForecastGraphViewModel, newItem: ForecastGraphViewModel): Boolean {
                return Objects.equals(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItem: ForecastGraphViewModel, newItem: ForecastGraphViewModel): Boolean {
                return Objects.equals(oldItem.graphData, newItem.graphData)
            }
        }

        private const val CORNERS_FULL = 0
        private const val CORNERS_TOP = 1
        private const val CORNERS_CENTER = 2
        private const val CORNERS_BOTTOM = 3
    }

    @IntDef(value = [CORNERS_FULL, CORNERS_TOP, CORNERS_CENTER, CORNERS_BOTTOM])
    @Retention(AnnotationRetention.SOURCE)
    private annotation class CornersType

    inner class LineViewViewHolder(private val binding: ChartsForecastgraphpanelBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.forecastGraphPanel.setDrawIconLabels(false)
        }

        fun bind(model: ForecastGraphViewModel) {
            binding.graphModel = model
            binding.executePendingBindings()
        }
    }

    inner class BarChartViewViewHolder(private val binding: ChartsBargraphpanelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ForecastGraphViewModel) {
            binding.forecastType = model.forecastType
            binding.graphData = model.graphData as BarGraphData?
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ChartType.BarChart) {
            BarChartViewViewHolder(ChartsBargraphpanelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LineViewViewHolder(ChartsForecastgraphpanelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)

        if (holder is LineViewViewHolder) {
            holder.bind(model)
        } else if (holder is BarChartViewViewHolder) {
            holder.bind(model)
        }

        if (holder.itemView is MaterialCardView) {
            updateItemCorners(holder.itemView as MaterialCardView, getCornersType(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).graphData) {
            is BarGraphData -> ChartType.BarChart
            else -> ChartType.LineView
        }
    }

    @CornersType
    private fun getCornersType(position: Int): Int {
        return when {
            itemCount <= 1 -> CORNERS_FULL
            position == 0 -> CORNERS_TOP
            position == itemCount - 1 -> CORNERS_BOTTOM
            else -> CORNERS_CENTER
        }
    }

    private fun updateItemCorners(cardView: MaterialCardView, @CornersType cornersType: Int) {
        val baseShapeModel =
            ShapeAppearanceModel.builder(cardView.context, null, 0, R.style.WeatherNow_CardView)
        val smallCornerSize = cardView.context.dpToPx(8f)

        when (cornersType) {
            CORNERS_BOTTOM -> {
                baseShapeModel.setTopLeftCornerSize(smallCornerSize)
                baseShapeModel.setTopRightCornerSize(smallCornerSize)
            }

            CORNERS_CENTER -> {
                baseShapeModel.setTopLeftCornerSize(smallCornerSize)
                baseShapeModel.setTopRightCornerSize(smallCornerSize)
                baseShapeModel.setBottomLeftCornerSize(smallCornerSize)
                baseShapeModel.setBottomRightCornerSize(smallCornerSize)
            }

            CORNERS_FULL -> {}

            CORNERS_TOP -> {
                baseShapeModel.setBottomLeftCornerSize(smallCornerSize)
                baseShapeModel.setBottomRightCornerSize(smallCornerSize)
            }
        }

        cardView.shapeAppearanceModel = baseShapeModel.build()
    }
}
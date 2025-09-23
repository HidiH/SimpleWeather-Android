package com.thewizrd.simpleweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.view.updateMarginsRelative
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.graphs.BarGraphData
import com.thewizrd.simpleweather.controls.graphs.ForecastRangeBarGraphData
import com.thewizrd.simpleweather.controls.graphs.GraphData
import com.thewizrd.simpleweather.controls.viewmodels.ForecastType
import com.thewizrd.simpleweather.databinding.ChartsBargraphpanelBinding
import com.thewizrd.simpleweather.databinding.ChartsRangebargraphpanelBinding
import java.util.Objects

class AQIForecastGraphAdapter : ListAdapter<GraphData<*>, RecyclerView.ViewHolder> {
    constructor() : super(diffCallback)
    constructor(config: AsyncDifferConfig<GraphData<*>>) : super(config)

    private class ChartType {
        companion object {
            const val BarChart = 0
            const val RangeBarChart = 1
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<GraphData<*>>() {
            override fun areItemsTheSame(oldItem: GraphData<*>, newItem: GraphData<*>): Boolean {
                return Objects.equals(oldItem.graphLabel, newItem.graphLabel)
            }

            override fun areContentsTheSame(oldItem: GraphData<*>, newItem: GraphData<*>): Boolean {
                return Objects.equals(oldItem, newItem)
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

    inner class BarGraphViewHolder(internal val binding: ChartsBargraphpanelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindModel(aqiData: GraphData<*>) {
            binding.forecastType = ForecastType.AIRQUALITY
            binding.graphData = aqiData as BarGraphData?
            binding.executePendingBindings()
        }
    }

    inner class RangeBarGraphViewHolder(internal val binding: ChartsRangebargraphpanelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindModel(aqiData: GraphData<*>) {
            binding.forecastType = ForecastType.AIRQUALITY
            binding.graphData = aqiData as ForecastRangeBarGraphData?
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val horizontalMargin = parent.context.dpToPx(6f).toInt()
        val verticalMargin = parent.context.dpToPx(2f).toInt()
        val layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        ).apply {
            updateMarginsRelative(
                start = horizontalMargin,
                end = horizontalMargin,
                top = verticalMargin,
                bottom = verticalMargin
            )
        }

        return if (viewType == ChartType.RangeBarChart) {
            RangeBarGraphViewHolder(
                ChartsRangebargraphpanelBinding.inflate(LayoutInflater.from(parent.context)).apply {
                    this.root.layoutParams = layoutParams
                })
        } else {
            BarGraphViewHolder(
                ChartsBargraphpanelBinding.inflate(LayoutInflater.from(parent.context)).apply {
                    this.root.layoutParams = layoutParams
                })
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)


        if (holder is BarGraphViewHolder) {
            holder.bindModel(model)
        } else if (holder is RangeBarGraphViewHolder) {
            holder.bindModel(model)
        }

        if (holder.itemView is MaterialCardView) {
            updateItemCorners(holder.itemView as MaterialCardView, getCornersType(position))
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ForecastRangeBarGraphData -> ChartType.RangeBarChart
            else -> ChartType.BarChart
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
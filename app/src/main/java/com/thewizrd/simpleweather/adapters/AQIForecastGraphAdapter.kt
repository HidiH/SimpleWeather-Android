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
import com.thewizrd.shared_resources.utils.ContextUtils.isLargeTablet
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.graphs.BarGraphData
import com.thewizrd.simpleweather.databinding.ChartsBargraphpanelBinding
import java.util.Objects

class AQIForecastGraphAdapter : ListAdapter<BarGraphData, AQIForecastGraphAdapter.ViewHolder> {
    constructor() : super(diffCallback)
    constructor(config: AsyncDifferConfig<BarGraphData>) : super(config)

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BarGraphData>() {
            override fun areItemsTheSame(oldItem: BarGraphData, newItem: BarGraphData): Boolean {
                return Objects.equals(oldItem.graphLabel, newItem.graphLabel)
            }

            override fun areContentsTheSame(oldItem: BarGraphData, newItem: BarGraphData): Boolean {
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

    inner class ViewHolder(internal val binding: ChartsBargraphpanelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.barGraphPanel.setScrollingEnabled(false)
            binding.barGraphPanel.setDrawIconLabels(false)
            binding.barGraphPanel.setFillParentWidth(!itemView.context.isLargeTablet())
            binding.barGraphPanel.setGraphMaxWidth(
                if (itemView.context.isLargeTablet()) {
                    -1
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.graph_max_width)
                }
            )
        }

        fun bindModel(aqiData: BarGraphData) {
            binding.graphData = aqiData
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ChartsBargraphpanelBinding.inflate(LayoutInflater.from(parent.context)).apply {
                this.root.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply {
                    val horizontalMargin = parent.context.dpToPx(6f).toInt()
                    val verticalMargin = parent.context.dpToPx(2f).toInt()
                    updateMarginsRelative(
                        start = horizontalMargin,
                        end = horizontalMargin,
                        top = verticalMargin,
                        bottom = verticalMargin
                    )
                }
            })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(getItem(position))

        if (holder.itemView is MaterialCardView) {
            updateItemCorners(holder.itemView as MaterialCardView, getCornersType(position))
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.binding.barGraphPanel.requestGraphLayout()
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
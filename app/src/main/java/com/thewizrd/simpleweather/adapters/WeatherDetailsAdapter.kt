package com.thewizrd.simpleweather.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.util.ObjectsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.ShapeAppearanceModel
import com.thewizrd.common.controls.BaseForecastItemViewModel
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.WeatherDetailItem

class WeatherDetailsAdapter<T : BaseForecastItemViewModel> :
    PagingDataAdapter<T, WeatherDetailsAdapter<T>.ViewHolder>(diffCallback as DiffUtil.ItemCallback<T>) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BaseForecastItemViewModel>() {
            override fun areItemsTheSame(
                oldItem: BaseForecastItemViewModel,
                newItem: BaseForecastItemViewModel
            ): Boolean {
                return ObjectsCompat.equals(oldItem.date, newItem.date)
            }

            override fun areContentsTheSame(
                oldItem: BaseForecastItemViewModel,
                newItem: BaseForecastItemViewModel
            ): Boolean {
                return ObjectsCompat.equals(oldItem, newItem)
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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class ViewHolder(val detailPanel: WeatherDetailItem) :
        RecyclerView.ViewHolder(detailPanel) {
        fun bind(model: BaseForecastItemViewModel?) {
            detailPanel.bind(model)
            detailPanel.setOnToggleListener {
                detailPanel.postOnAnimation {
                    notifyItemChanged(bindingAdapterPosition, "animation")
                }
            }
        }
    }

    @SuppressLint("NewApi")  // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(WeatherDetailItem(parent.context))
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(getItem(position))
        updateItemCorners(holder.detailPanel, getCornersType(position))
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

    private fun updateItemCorners(detailPanel: WeatherDetailItem, @CornersType cornersType: Int) {
        val baseShapeModel =
            ShapeAppearanceModel.builder(detailPanel.context, null, 0, R.style.WeatherNow_CardView)
        val smallCornerSize = detailPanel.context.dpToPx(8f)

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

        detailPanel.shapeAppearanceModel = baseShapeModel.build()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (!payloads.contains("animation")) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}
package com.thewizrd.simpleweather.controls

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.res.use
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.thewizrd.shared_resources.helpers.RecyclerOnClickListenerInterface
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.graphs.BarGraphData
import com.thewizrd.simpleweather.controls.viewmodels.ForecastType
import com.thewizrd.simpleweather.databinding.LayoutBarBinding
import com.thewizrd.simpleweather.databinding.LayoutBarViewBinding
import kotlin.math.max

class ForecastBarGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val binding: LayoutBarViewBinding

    private val graphViewHeight: Int
    private val zeroValueItemHeight: Int
    private var bottomTextHeights: Int = 0

    private var graphData: BarGraphData? = null
    private var forecastType: ForecastType? = null

    // Event listeners
    private var onClickListener: RecyclerOnClickListenerInterface? = null

    fun setOnClickPositionListener(onClickListener: RecyclerOnClickListenerInterface?) {
        this.onClickListener = onClickListener
    }

    init {
        orientation = VERTICAL

        graphViewHeight =
            context.obtainStyledAttributes(attrs, R.styleable.ForecastBarGraphView).use {
                it.getDimensionPixelSize(
                    R.styleable.ForecastBarGraphView_graphHeight,
                    context.resources.getDimensionPixelSize(R.dimen.bargraph_panel_height)
                )
            }
        zeroValueItemHeight = context.dpToPx(1f).toInt()

        val inflater = LayoutInflater.from(context)
        binding = LayoutBarViewBinding.inflate(inflater, this)

        minimumHeight = graphViewHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        binding.innerLayout.getChildAt(0)?.also { v ->
            graphData?.let {
                val barBinding = DataBindingUtil.getBinding<LayoutBarBinding>(v)
                if (barBinding != null) {
                    val measuredBottomTextHeights =
                        barBinding.barValue.measuredHeight + barBinding.barDate.measuredHeight
                    if (bottomTextHeights != measuredBottomTextHeights) {
                        bottomTextHeights = measuredBottomTextHeights
                        minimumHeight = graphViewHeight + measuredBottomTextHeights
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun setData(graphData: BarGraphData?, forecastType: ForecastType? = null) {
        this.graphData = graphData
        this.forecastType = forecastType

        val dataSet = graphData?.getDataSet()

        if (dataSet != null && !dataSet.isEmpty) {
            binding.innerLayout.run {
                val itemCount = dataSet.dataCount
                val layoutInflater = LayoutInflater.from(context)

                val max = graphData.yMax
                val min = graphData.yMin

                for (i in 0 until itemCount) {
                    val view = getChildAt(i)
                    val data = dataSet.getEntryForIndex(i)

                    val item: LayoutBarBinding = if (view != null) {
                        DataBindingUtil.getBinding(view) ?: LayoutBarBinding.bind(view)
                    } else {
                        LayoutBarBinding.inflate(layoutInflater).apply {
                            val barIconHeight = context.dpToPx(32f).toInt()

                            root.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                                val shouldBeGone = innerBar.measuredHeight <= barIconHeight

                                if (shouldBeGone != barIcon.isGone) {
                                    barIcon.isGone = shouldBeGone
                                    innerBar.requestLayout()
                                }
                            }
                        }
                    }

                    item.data = data
                    item.root.setOnClickListener { v ->
                        onClickListener?.onClick(v, max(indexOfChild(v), 0))
                    }

                    item.innerBar.updateLayoutParams {
                        val normalizedValue = when {
                            min == max -> {
                                1f
                            }

                            else -> {
                                val dataRange = max - min

                                data.entryData?.y?.let {
                                    if (dataRange == 0f) {
                                        0f
                                    } else {
                                        (it.coerceIn(min, max) - min) / dataRange
                                    }
                                } ?: 0f
                            }
                        }
                        height =
                            zeroValueItemHeight + (normalizedValue * (graphViewHeight + item.barValue.measuredHeight * 2)).toInt()
                    }

                    // Update icon
                    item.barIcon.rotation = data.xIconRotation.toFloat()
                    item.barIcon.setImageResource(getIconResourceFromForecastType(forecastType))

                    if (getChildAt(i) == null) {
                        addView(
                            item.root,
                            LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                                .apply {
                                    gravity = Gravity.BOTTOM
                                })
                    }
                }

                removeViews(itemCount, childCount - itemCount)
            }
        } else {
            binding.innerLayout.removeAllViews()
        }
    }

    @DrawableRes
    private fun getIconResourceFromForecastType(forecastType: ForecastType?): Int {
        return when (forecastType) {
            ForecastType.TEMPERATURE -> 0
            ForecastType.MINUTELY -> R.drawable.wi_raindrop
            ForecastType.PRECIPITATION -> R.drawable.wi_raindrop
            ForecastType.WIND -> R.drawable.wi_direction_up_2x
            ForecastType.HUMIDITY -> R.drawable.material_humidity_percentage
            ForecastType.UVINDEX -> 0
            ForecastType.RAIN -> R.drawable.material_water_drop
            ForecastType.SNOW -> R.drawable.wi_snowflake_cold
            ForecastType.AIRQUALITY -> 0
            null -> 0
        }
    }
}
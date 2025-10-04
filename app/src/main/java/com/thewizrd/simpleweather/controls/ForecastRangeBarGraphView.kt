package com.thewizrd.simpleweather.controls

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.use
import androidx.core.view.forEachIndexed
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.thewizrd.shared_resources.helpers.RecyclerOnClickListenerInterface
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.graphs.ForecastRangeBarEntry
import com.thewizrd.simpleweather.controls.graphs.ForecastRangeBarGraphData
import com.thewizrd.simpleweather.controls.viewmodels.ForecastType
import com.thewizrd.simpleweather.databinding.LayoutBarViewBinding
import com.thewizrd.simpleweather.databinding.LayoutRangebarBinding
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UseKtx")
class ForecastRangeBarGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val binding: LayoutBarViewBinding

    private var maxItemCount: Int = 7

    private val graphHeight: Int
    private var bottomTextHeights: Int = 0
    private var scale: Float = 1f

    private var graphData: ForecastRangeBarGraphData? = null
    private var forecastType: ForecastType? = null

    // Event listeners
    private var onClickListener: RecyclerOnClickListenerInterface? = null

    fun setOnClickPositionListener(onClickListener: RecyclerOnClickListenerInterface?) {
        this.onClickListener = onClickListener
    }

    init {
        orientation = VERTICAL

        val inflater = LayoutInflater.from(context)
        binding = LayoutBarViewBinding.inflate(inflater, this)

        graphHeight =
            context.obtainStyledAttributes(attrs, R.styleable.ForecastRangeBarGraphView).use {
                it.getDimensionPixelSize(
                    R.styleable.ForecastRangeBarGraphView_graphHeight,
                    context.resources.getDimensionPixelSize(R.dimen.bargraph_panel_height)
                )
            }

        binding.innerLayout.updateLayoutParams {
            height = graphHeight
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var measuredBottomTextHeights = Int.MIN_VALUE
        var newScale = 0f

        binding.innerLayout.forEachIndexed { idx, bar ->
            val barBinding = DataBindingUtil.getBinding<LayoutRangebarBinding>(bar)
            if (barBinding != null) {
                measuredBottomTextHeights = max(
                    measuredBottomTextHeights,
                    barBinding.barPop.measuredHeight +
                            barBinding.barIcon.measuredHeight +
                            barBinding.barDate.measuredHeight
                )
                if (measuredBottomTextHeights != bottomTextHeights) {
                    bottomTextHeights = measuredBottomTextHeights
                }

                graphData?.let {
                    val dataBarHeight = it.yMax - it.yMin

                    newScale = max(
                        if (dataBarHeight == 0f) {
                            1f
                        } else {
                            min(
                                (graphHeight - bottomTextHeights - (barBinding.barHi.measuredHeight + barBinding.barLo.measuredHeight)) / dataBarHeight,
                                1f
                            )
                        } * context.dpToPx(.9f),
                        newScale
                    )
                }
            }
        }

        if (newScale != scale && newScale != 0f) {
            scale = newScale
        }

        binding.innerLayout.forEachIndexed { idx, bar ->
            val barBinding = DataBindingUtil.getBinding<LayoutRangebarBinding>(bar)
            if (barBinding != null) {
                // Update bar measurement
                graphData?.getDataSet()?.getEntryForIndex(idx)?.let { data ->
                    val max = graphData?.yMax
                    val min = graphData?.yMin
                    if (min != null && max != null) {
                        updateInnerBarMargins(barBinding.innerBar, min, max, data)
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun setData(graphData: ForecastRangeBarGraphData?, forecastType: ForecastType? = null) {
        this.graphData = graphData
        this.forecastType = forecastType

        val dataSet = graphData?.getDataSet()

        if (dataSet != null && !dataSet.isEmpty) {
            binding.innerLayout.run {
                val itemCount = min(dataSet.dataCount, maxItemCount)
                val layoutInflater = LayoutInflater.from(context)

                val max = graphData.yMax
                val min = graphData.yMin

                for (i in 0 until itemCount) {
                    val view = getChildAt(i)
                    val data = dataSet.getEntryForIndex(i)

                    val item: LayoutRangebarBinding = if (view != null) {
                        DataBindingUtil.getBinding(view) ?: LayoutRangebarBinding.bind(view)
                    } else {
                        LayoutRangebarBinding.inflate(layoutInflater)
                    }

                    item.forecastType = forecastType
                    item.data = data
                    item.root.setOnClickListener { v ->
                        onClickListener?.onClick(v, max(indexOfChild(v), 0))
                    }

                    if (data.hiTempData == null || data.loTempData == null) {
                        item.rangebar.updateLayoutParams<LayoutParams> {
                            height = width
                            weight = 0f
                        }
                    } else {
                        item.rangebar.updateLayoutParams<LayoutParams> {
                            height = 0
                            weight = 1f
                        }
                    }

                    data.fillColors?.let { item.rangebar.setColors(it) }
                        ?: item.rangebar.setColors()

                    updateInnerBarMargins(item.innerBar, min, max, data)

                    val barSize = when (forecastType) {
                        ForecastType.TEMPERATURE -> context.dpToPx(6f)
                        else -> context.dpToPx(24f)
                    }.toInt()

                    if (barSize != item.rangebar.layoutParams.width) {
                        item.rangebar.updateLayoutParams {
                            width = barSize
                        }
                    }

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
                this@ForecastRangeBarGraphView.requestLayout()
            }
        } else {
            binding.innerLayout.removeAllViews()
        }
    }

    private fun updateInnerBarMargins(
        innerBar: View,
        min: Float,
        max: Float,
        data: ForecastRangeBarEntry
    ) {
        innerBar.updateLayoutParams<LayoutParams> {
            topMargin = data.hiTempData?.y?.let { (max - it) * scale }?.toInt() ?: 0
            bottomMargin = data.loTempData?.y?.minus(min)?.times(scale)?.toInt() ?: 0
        }
    }
}
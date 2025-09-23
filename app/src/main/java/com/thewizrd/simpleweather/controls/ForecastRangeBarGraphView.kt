package com.thewizrd.simpleweather.controls

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.use
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.thewizrd.shared_resources.helpers.RecyclerOnClickListenerInterface
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
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

    private var graphViewHeight: Int
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

        graphViewHeight =
            context.obtainStyledAttributes(attrs, R.styleable.ForecastRangeBarGraphView).use {
                it.getDimensionPixelSize(
                    R.styleable.ForecastRangeBarGraphView_graphHeight,
                    context.resources.getDimensionPixelSize(R.dimen.bargraph_panel_height)
                )
            }

        val inflater = LayoutInflater.from(context)
        binding = LayoutBarViewBinding.inflate(inflater, this)

        minimumHeight = graphViewHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        binding.innerLayout.getChildAt(0)?.also { v ->
            graphData?.let {
                val barBinding = DataBindingUtil.getBinding<LayoutRangebarBinding>(v)
                if (barBinding != null) {
                    val dataBarHeight = it.yMax - it.yMin

                    val measuredBottomTextHeights =
                        barBinding.barPop.measuredHeight + barBinding.barIcon.measuredHeight + barBinding.barDate.measuredHeight
                    if (measuredBottomTextHeights != bottomTextHeights) {
                        bottomTextHeights = measuredBottomTextHeights
                        minimumHeight = graphViewHeight + measuredBottomTextHeights
                    }

                    val newScale = if (dataBarHeight == 0f) {
                        1f
                    } else {
                        min(graphViewHeight / dataBarHeight, 1f)
                    } * context.dpToPx(1f)

                    if (newScale != scale) {
                        scale = newScale
                        postOnAnimation { setData(graphData, forecastType) }
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

                    item.innerBar.updateLayoutParams<LayoutParams> {
                        topMargin = data.hiTempData?.y?.let { (max - it) * scale }?.toInt() ?: 0
                        bottomMargin = data.loTempData?.y?.minus(min)?.times(scale)?.toInt() ?: 0
                    }

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
            }
        } else {
            binding.innerLayout.removeAllViews()
        }
    }
}
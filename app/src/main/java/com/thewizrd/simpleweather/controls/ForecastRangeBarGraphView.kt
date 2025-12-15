package com.thewizrd.simpleweather.controls

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
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
import com.thewizrd.simpleweather.controls.graphs.isNullOrEmpty
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
        refreshLayout()
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

                    updateBarSizing(item, min, max, data)

                    data.fillColors?.let { item.rangebar.setColors(it) }
                        ?: item.rangebar.setColors()

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

    private fun refreshLayout() {
        val dataSet = graphData?.getDataSet()
        if (dataSet.isNullOrEmpty()) return

        val max = graphData?.yMax ?: return
        val min = graphData?.yMin ?: return

        binding.innerLayout.forEachIndexed { idx, bar ->
            dataSet?.getEntryForIndex(idx)?.let { data ->
                DataBindingUtil.getBinding<LayoutRangebarBinding>(bar)?.run {
                    updateBarSizing(this, min, max, data)
                }
            }
        }
    }

    private fun updateBarSizing(
        bar: LayoutRangebarBinding,
        min: Float,
        max: Float,
        data: ForecastRangeBarEntry
    ) {
        // We compute margins as a proportion of available graphable height.
        // Reserve a space for labels/icons inside the control
        var bottomTextHeights =
            bar.barHi.measuredHeight + bar.barLo.measuredHeight + bar.barPop.measuredHeight + bar.barIcon.measuredHeight + bar.barDate.measuredHeight
        if (bottomTextHeights <= 0) bottomTextHeights = bar.root.context.dpToPx(100f).toInt()
        var availableHeight = graphHeight - bottomTextHeights

        var range = max - min
        if (range <= 0) range = 1f

        val top = data.hiTempData?.y?.let { ((max - it) / range) * availableHeight }?.toInt() ?: 0
        val bottom =
            data.loTempData?.y?.let { ((it - min) / range) * availableHeight }?.toInt() ?: 0

        // apply margins on the InnerBarView so the inner range gradient stretches between hi and lo
        bar.innerBar.updateLayoutParams<LayoutParams> {
            topMargin = top
            bottomMargin = bottom
        }

        // Set the RangeBarView.Height so the visible gradient fills the remaining space
        if (data.hiTempData == null || data.loTempData == null) {
            bar.rangebar.updateLayoutParams<LayoutParams> {
                height = width
                weight = 0f
            }
        } else {
            bar.rangebar.updateLayoutParams<LayoutParams> {
                height = 0
                weight = 1f
            }
        }
    }
}
package com.thewizrd.simpleweather.controls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.thewizrd.shared_resources.helpers.RecyclerOnClickListenerInterface
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.controls.graphs.RangeBarGraphDataSet
import com.thewizrd.simpleweather.databinding.LayoutRangebarBinding
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SimpleRangeBarGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private var maxItemCount: Int = 7
    private var scale: Float = 1f
    private var isMeasured: Boolean = false

    private var dataSet: RangeBarGraphDataSet? = null

    // Event listeners
    private var onClickListener: RecyclerOnClickListenerInterface? = null

    fun setOnClickPositionListener(onClickListener: RecyclerOnClickListenerInterface?) {
        this.onClickListener = onClickListener
    }

    init {
        orientation = HORIZONTAL
        minimumHeight = context.dpToPx(250f).toInt()
        removeAllViews()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        maxItemCount = min(7f, (measuredWidth / context.dpToPx(70f))).roundToInt()

        getChildAt(0)?.also { v ->
            dataSet?.let {
                val binding = DataBindingUtil.getBinding<LayoutRangebarBinding>(v)
                if (binding != null) {
                    val remainingSpace = v.measuredHeight - binding.innerBar.measuredHeight
                    if (remainingSpace > 0) {
                        val dataBarHeight = it.yMax - it.yMin
                        scale = min(remainingSpace / dataBarHeight, 1f) * context.dpToPx(2f)

                        if (!isMeasured) {
                            postOnAnimation { setData(dataSet) }
                        }

                        isMeasured = true
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isMeasured = false
    }

    fun setData(dataSet: RangeBarGraphDataSet?) {
        this.dataSet = dataSet

        if (dataSet != null && !dataSet.isEmpty) {
            val itemCount = min(dataSet.dataCount, maxItemCount)
            val layoutInflater = LayoutInflater.from(context)

            val max = dataSet.yMax
            val min = dataSet.yMin

            for (i in 0 until itemCount) {
                val view = getChildAt(i)
                val data = dataSet.getEntryForIndex(i)

                val item: LayoutRangebarBinding = if (view != null) {
                    DataBindingUtil.getBinding(view) ?: LayoutRangebarBinding.bind(view)
                } else {
                    LayoutRangebarBinding.inflate(layoutInflater)
                }

                item.data = data
                item.root.setOnClickListener { v ->
                    onClickListener?.onClick(v, max(indexOfChild(v), 0))
                }

                if (data.hiTempData == null || data.loTempData == null) {
                    item.rangebar.updateLayoutParams<LayoutParams> {
                        height = width
                        weight = 0f
                    }
                    if (data.hiTempData == null) {
                        item.rangebar.setColors(
                            intArrayOf(
                                Colors.LIGHTSKYBLUE,
                                Colors.LIGHTSKYBLUE
                            )
                        )
                    } else if (data.loTempData == null) {
                        item.rangebar.setColors(intArrayOf(Colors.ORANGERED, Colors.ORANGERED))
                    }
                } else {
                    item.rangebar.updateLayoutParams<LayoutParams> {
                        height = 0
                        weight = 1f
                    }
                    item.rangebar.setColors()
                }

                item.innerBar.layoutParams =
                    LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                        topMargin = data.hiTempData?.y?.let { (max - it) * scale }?.toInt() ?: 0
                        bottomMargin = data.loTempData?.y?.minus(min)?.times(scale)?.toInt() ?: 0
                    }

                if (getChildAt(i) == null) {
                    addView(item.root, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
                }
            }

            removeViews(itemCount, childCount - itemCount)
        } else {
            removeAllViews()
        }
    }
}
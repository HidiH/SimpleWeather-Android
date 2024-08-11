package com.thewizrd.simpleweather.controls

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx

class GradientBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private val DEFAULT_COLORS = intArrayOf(Colors.ORANGERED, Colors.LIGHTSKYBLUE)
    }

    init {
        setColors(DEFAULT_COLORS)
    }

    fun setColors(colors: IntArray = DEFAULT_COLORS) {
        GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, colors
        ).apply {
            cornerRadius = context.dpToPx(24f)
        }.also {
            background = it
        }
    }
}
package com.thewizrd.simpleweather.controls

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.search.SearchView
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R

open class CustomSearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SearchView(context, attrs) {
    private val elevationOverlayProvider: ElevationOverlayProvider =
        ElevationOverlayProvider(context)

    fun setBackgroundOverlayColor(@ColorInt backgroundColor: Int) {
        // 6dp
        val elevation = context.dpToPx(6f)
        val backgroundColorWithOverlay =
            elevationOverlayProvider.compositeOverlayIfNeeded(backgroundColor, elevation)
        findViewById<View>(R.id.open_search_view_background)?.setBackgroundColor(
            backgroundColorWithOverlay
        )
    }
}
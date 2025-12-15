package com.thewizrd.simpleweather.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

internal interface NoBackground
internal interface Selectable

open class SelectableBackgroundPreference : Preference, NoBackground, Selectable {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)
}


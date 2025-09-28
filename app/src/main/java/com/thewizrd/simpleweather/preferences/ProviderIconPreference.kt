package com.thewizrd.simpleweather.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.thewizrd.simpleweather.R

class ProviderIconPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.providerIconPreferenceStyle,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
}
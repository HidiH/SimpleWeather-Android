package com.thewizrd.simpleweather.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.controls.WearChipButton

class ProviderIconPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.WearPreference_ProviderIconPreference
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val chipButton = holder.itemView as? WearChipButton
        if (chipButton != null) {
            chipButton.setPrimaryText(title)
            chipButton.setSecondaryText(summary)

            chipButton.findViewById<TextView?>(R.id.wear_chip_secondary_text)?.apply {
                maxLines = 10
            }

            chipButton.setContentViewVisibility(
                if (widgetLayoutResource != 0) View.VISIBLE else View.GONE
            )

            chipButton.findViewById<View?>(android.R.id.widget_frame)?.apply {
                val verticalPadding = chipButton.context.dpToPx(8f).toInt()
                updatePaddingRelative(top = verticalPadding)
            }
        }
    }
}
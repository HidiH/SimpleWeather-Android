package com.thewizrd.common.controls

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import com.thewizrd.shared_resources.R
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.sharedDeps
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.weatherdata.model.Pollen

class PollenViewModel(pollenData: Pollen) {
    var treePollenDesc: CharSequence
        private set
    var treePollenShortDesc: CharSequence
        private set

    var grassPollenDesc: CharSequence
        private set
    var grassPollenShortDesc: CharSequence
        private set

    var ragweedPollenDesc: CharSequence
        private set
    var ragweedPollenShortDesc: CharSequence
        private set

    var treePollenProgress: Int = pollenData.treePollenCount?.ordinal ?: 0
        private set
    var grassPollenProgress: Int = pollenData.grassPollenCount?.ordinal ?: 0
        private set
    var ragweedPollenProgress: Int = pollenData.ragweedPollenCount?.ordinal ?: 0
        private set

    var treePollenProgressColor: Int = pollenData.treePollenCount.toColor()
        private set
    var grassPollenProgressColor: Int = pollenData.grassPollenCount.toColor()
        private set
    var ragweedPollenProgressColor: Int = pollenData.ragweedPollenCount.toColor()
        private set

    val progressMax: Int = Pollen.PollenCount.VERY_HIGH.ordinal

    init {
        getPollenCountDescription(pollenData.treePollenCount).run {
            treePollenDesc = first
            treePollenShortDesc = second
        }

        getPollenCountDescription(pollenData.grassPollenCount).run {
            grassPollenDesc = first
            grassPollenShortDesc = second
        }

        getPollenCountDescription(pollenData.ragweedPollenCount).run {
            ragweedPollenDesc = first
            ragweedPollenShortDesc = second
        }
    }

    private fun getPollenCountDescription(pollenCount: Pollen.PollenCount?): Pair<CharSequence, CharSequence> {
        val context = sharedDeps.context

        val description = when (pollenCount) {
            Pollen.PollenCount.LOW -> SpannableString(context.getString(R.string.label_count_low)) to SpannableString(
                context.getString(R.string.label_count_low_short)
            )

            Pollen.PollenCount.MODERATE -> SpannableString(context.getString(R.string.label_count_moderate)) to SpannableString(
                context.getString(R.string.label_count_moderate_short)
            )

            Pollen.PollenCount.HIGH -> SpannableString(context.getString(R.string.label_count_high)) to SpannableString(
                context.getString(R.string.label_count_high_short)
            )

            Pollen.PollenCount.VERY_HIGH -> SpannableString(context.getString(R.string.label_count_veryhigh)) to SpannableString(
                context.getString(R.string.label_count_veryhigh_short)
            )

            else -> WeatherIcons.EM_DASH to WeatherIcons.EM_DASH
        }

        return description.apply {
            first.setDescriptionSpan(pollenCount)
            second.setDescriptionSpan(pollenCount)
        }
    }

    @ColorInt
    private fun Pollen.PollenCount?.toColor(): Int = when (this) {
        Pollen.PollenCount.LOW -> Colors.LIMEGREEN
        Pollen.PollenCount.MODERATE -> Colors.ORANGE
        Pollen.PollenCount.HIGH -> Colors.ORANGERED
        Pollen.PollenCount.VERY_HIGH -> Colors.RED
        else -> Colors.TRANSPARENT
    }

    private fun CharSequence.setDescriptionSpan(pollenCount: Pollen.PollenCount?) {
        if (this is Spannable) {
            this.setSpan(
                ForegroundColorSpan(pollenCount.toColor()),
                0,
                this.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}
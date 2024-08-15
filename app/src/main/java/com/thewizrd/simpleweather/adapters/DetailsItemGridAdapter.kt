package com.thewizrd.simpleweather.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.thewizrd.common.controls.DetailItemViewModel
import com.thewizrd.common.controls.WeatherDetailsType
import com.thewizrd.shared_resources.utils.ContextUtils.dpToPx
import com.thewizrd.shared_resources.utils.sequenceEqual
import com.thewizrd.simpleweather.controls.DetailCard
import com.thewizrd.simpleweather.preferences.FeatureSettings

class DetailsItemGridAdapter(private val itemType: ItemType = ItemType.WEATHER_NOW) :
    BaseAdapter() {
    enum class ItemType {
        WEATHER_NOW,
        FORECAST
    }

    private var mDataset: List<DetailItemViewModel>? = null

    override fun getCount(): Int {
        return mDataset?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return mDataset?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mDataset?.get(position)?.detailsType?.value?.toLong() ?: 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView as? DetailCard ?: DetailCard(parent.context).apply {
            if (itemType == ItemType.FORECAST) {
                setStrokeWidth(1f)
                elevation = parent.context.dpToPx(4f)
            }
        }

        view.bindModel(mDataset!![position])

        return view
    }

    fun updateItems(dataset: Collection<DetailItemViewModel>?) {
        if (mDataset == null || !sequenceEqual(mDataset, dataset)) {
            mDataset = dataset?.filterNot {
                ((it.detailsType == WeatherDetailsType.SUNRISE || it.detailsType == WeatherDetailsType.SUNSET) && FeatureSettings.isSunPhaseEnabled) ||
                        (it.detailsType == WeatherDetailsType.MOONRISE || it.detailsType == WeatherDetailsType.MOONSET || it.detailsType == WeatherDetailsType.MOONPHASE) && FeatureSettings.isMoonPhaseEnabled
            }
            notifyDataSetChanged()
        }
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
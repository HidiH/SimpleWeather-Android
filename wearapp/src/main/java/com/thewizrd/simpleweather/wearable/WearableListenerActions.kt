package com.thewizrd.simpleweather.wearable

import com.thewizrd.common.wearable.WearConnectionStatus
import com.thewizrd.simpleweather.viewmodels.WearableListenerViewModel

object WearableListenerActions {
    // Actions
    const val ACTION_OPENONPHONE = "SimpleWeather.Droid.Wear.action.OPEN_APP_ON_PHONE"
    const val ACTION_SHOWSTORELISTING = "SimpleWeather.Droid.Wear.action.SHOW_STORE_LISTING"
    const val ACTION_SENDCONNECTIONSTATUS = "SimpleWeather.Droid.Wear.action.SEND_CONNECTION_STATUS"
    const val ACTION_UPDATECONNECTIONSTATUS =
        "SimpleWeather.Droid.Wear.action.UPDATE_CONNECTION_STATUS"
    const val ACTION_REQUESTSETUPSTATUS = "SimpleWeather.Droid.Wear.action.REQUEST_SETUP_STATUS"

    const val ACTION_REQUESTSYNCWEATHER = "SimpleWeather.Droid.Wear.action.REQUEST_SYNC_WEATHER"
    const val ACTION_UPDATESYNCSTATUS = "SimpleWeather.Droid.Wear.action.UPDATE_SYNC_STATUS"

    const val ACTION_SYNCSETTINGUPDATED = "SimpleWeather.Droid.Wear.action.SYNC_SETTING_UPDATED"
    const val ACTION_REQUESTREFRESHWEATHER =
        "SimpleWeather.Droid.Wear.action.REQUEST_REFRESH_WEATHER"

    // Extras
    /**
     * Extra contains success flag for open on phone action.
     *
     * @see .ACTION_OPENONPHONE
     */
    const val EXTRA_SUCCESS = "SimpleWeather.Droid.Wear.extra.SUCCESS"

    /**
     * Extra contains flag for whether or not to show the animation for the open on phone action.
     *
     * @see .ACTION_OPENONPHONE
     */
    const val EXTRA_SHOWANIMATION = "SimpleWeather.Droid.Wear.extra.SHOW_ANIMATION"

    /**
     * Extra contains connection status for WearOS device and connected phone
     *
     * @see WearConnectionStatus
     *
     * @see WearableListenerViewModel
     */
    const val EXTRA_CONNECTIONSTATUS = "SimpleWeather.Droid.Wear.extra.CONNECTION_STATUS"
    const val EXTRA_DEVICESETUPSTATUS = "SimpleWeather.Droid.Wear.extra.DEVICE_SETUP_STATUS"
}
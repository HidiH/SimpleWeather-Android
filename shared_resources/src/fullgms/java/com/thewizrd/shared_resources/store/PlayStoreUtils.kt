package com.thewizrd.shared_resources.store

import android.net.Uri
import androidx.core.net.toUri

object PlayStoreUtils {
    // Link to Play Store listing
    const val PLAY_STORE_APP_URI = "market://details?id=com.thewizrd.simpleweather"
    const val PLAY_STORE_APP_WEBURI = "https://play.google.com/store/apps/details?id=com.thewizrd.simpleweather"

    fun getPlayStoreURI(): Uri {
        return PLAY_STORE_APP_URI.toUri()
    }

    fun getPlayStoreWebURI(): Uri {
        return PLAY_STORE_APP_WEBURI.toUri()
    }
}
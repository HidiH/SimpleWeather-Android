package com.thewizrd.common.wearable

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.PutDataRequest
import com.thewizrd.shared_resources.BuildConfig
import com.thewizrd.shared_resources.utils.Logger

object WearableHelper {
    // Name of capability listed in Phone app's wear.xml
    const val CAPABILITY_PHONE_APP = "com.thewizrd.simpleweather_phone_app"

    // Name of capability listed in Wear app's wear.xml
    const val CAPABILITY_WEAR_APP = "com.thewizrd.simpleweather_wear_app"

    const val PACKAGE_NAME = "com.thewizrd.simpleweather"

    // For WearableListenerService
    const val StartActivityPath = "/start-activity"
    const val SettingsPath = "/settings"
    const val LocationPath = "/data/location"
    const val WeatherPath = "/data/weather"
    const val ErrorPath = "/error"
    const val IsSetupPath = "/isweatherloaded"
    const val PingPath = "/ping"

    // For Activity Launcher
    private const val SCHEME_APP = "simpleweather"
    private const val PATH_REMOTE_LAUNCH = "launch-activity"
    const val URI_PARAM_PKGNAME = "package"
    const val URI_PARAM_ACTIVITYNAME = "activity"

    fun isGooglePlayServicesInstalled(context: Context): Boolean {
        val gPlayAvailability = GoogleApiAvailability.getInstance()

        val queryResult = gPlayAvailability.isGooglePlayServicesAvailable(context)
        if (queryResult == ConnectionResult.SUCCESS) {
            Logger.writeLine(Log.INFO, "App: Google Play Services is installed on this device.")
            return true
        }

        if (gPlayAvailability.isUserResolvableError(queryResult)) {
            val errorString = gPlayAvailability.getErrorString(queryResult)
            Logger.writeLine(
                Log.INFO,
                "App: There is a problem with Google Play Services on this device: %s - %s",
                queryResult, errorString
            )
        }

        return false
    }

    fun getWearDataUri(NodeId: String?, Path: String?): Uri {
        return Uri.Builder()
            .scheme(PutDataRequest.WEAR_URI_SCHEME)
            .authority(NodeId)
            .path(Path)
            .build()
    }

    fun getWearDataUri(Path: String?): Uri {
        return Uri.Builder()
            .scheme(PutDataRequest.WEAR_URI_SCHEME)
            .path(Path)
            .build()
    }

    fun getLaunchActivityUri(packageName: String, activityName: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME_APP)
            .authority(PATH_REMOTE_LAUNCH)
            .appendQueryParameter(URI_PARAM_PKGNAME, packageName)
            .appendQueryParameter(URI_PARAM_ACTIVITYNAME, activityName)
            .build()
    }

    fun createRemoteActivityIntent(packageName: String, activityName: String): Intent {
        return Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(getLaunchActivityUri(packageName, activityName))
    }

    fun isRemoteLaunchUri(uri: Uri): Boolean {
        return uri.scheme == SCHEME_APP && uri.host == PATH_REMOTE_LAUNCH &&
                !uri.getQueryParameter(URI_PARAM_PKGNAME).isNullOrEmpty() &&
                !uri.getQueryParameter(URI_PARAM_ACTIVITYNAME).isNullOrEmpty()
    }

    fun Uri.toLaunchIntent(): Intent {
        return Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setComponent(
                ComponentName(
                    this.getQueryParameter(URI_PARAM_PKGNAME)!!,
                    this.getQueryParameter(URI_PARAM_ACTIVITYNAME)!!
                )
            )
    }

    fun getPackageName(): String {
        var packageName = PACKAGE_NAME
        if (BuildConfig.DEBUG) packageName += ".debug"
        return packageName
    }
}
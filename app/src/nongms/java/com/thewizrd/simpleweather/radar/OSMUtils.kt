package com.thewizrd.simpleweather.radar

import android.content.Context
import android.content.pm.PackageInfo
import org.osmdroid.config.Configuration
import java.io.File

private var mapInitialized: Boolean = false

fun Context.initializeMap() {
    if (!mapInitialized) {
        val version = runCatching {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            String.format("v%s", packageInfo.versionName)
        }.getOrDefault("")

        Configuration.getInstance().userAgentValue =
            String.format("SimpleWeather (thewizrd.dev+SimpleWeatherAndroid@gmail.com) %s", version)
        Configuration.getInstance().osmdroidTileCache = File(cacheDir, "tiles")
        Configuration.getInstance().osmdroidBasePath = File(noBackupFilesDir, "osmdroid")

        mapInitialized = true
    }
}
package com.thewizrd.simpleweather.wearable.tiles.layouts

import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.expression.VersionBuilders.VersionInfo

fun DeviceParameters.supportsTransformation(): Boolean {
    // @RequiresSchemaVersion(major = 1, minor = 400)
    val supportedVersion = VersionInfo.Builder()
        .setMajor(1).setMinor(400)
        .build()

    return this.rendererSchemaVersion >= supportedVersion
}
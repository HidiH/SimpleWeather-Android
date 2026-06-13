package com.thewizrd.shared_resources.utils

import com.thewizrd.shared_resources.locationdata.LocationData

fun createUnsupportedLocationException(weatherApi: String, location: LocationData): Exception {
    return Exception("Unsupported location - weatherapi: ${weatherApi}, countryCode: ${location.countryCode}, query: [${location.query}]")
}
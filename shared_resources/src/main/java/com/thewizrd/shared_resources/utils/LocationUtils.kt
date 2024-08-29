@file:Suppress("PropertyName")

package com.thewizrd.shared_resources.utils

import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.locationdata.LocationQuery

object LocationUtils {
    // Source: https://gist.github.com/graydon/11198540
    private val US_BOUNDING_BOX = BoundingBox(24.9493, 49.5904, -125.0011, -66.9326)
    private val USCA_BOUNDING_BOX =
        BoundingBox(24.4825578966, 71.7611572494, -168.9184947286, -52.2436900411)

    // France
    private val FR_BOUNDING_BOX = BoundingBox(41.2632185, 51.268318, -5.4534286, 9.8678344)

    // Puerto Rico
    private val PR_BOUNDING_BOX =
        BoundingBox(17.7306659963, 18.6663824908, -68.1108798087, -65.1100910828)

    // US Virgin Islands
    private val VI_BOUNDING_BOX =
        BoundingBox(17.6234681162, 18.4649841585, -65.1541175321, -64.512674287)

    // Guam & The Northern Mariana Islands
    private val GU_MP_BOUNDING_BOX =
        BoundingBox(13.019485113, 20.7560506513, 144.3987098565, 146.3240638604)

    // American Somoa
    private val AS_BOUNDING_BOX =
        BoundingBox(-14.6018129466, -10.9972026743, -171.141907163, -168.1016121805)

    // Germany
    private val DE_BOUNDING_BOX =
        BoundingBox(47.2701114, 55.099161, 5.8663153, 15.0419319)

    private val NWS_SUPPORTED_COUNTRIES = setOf("US", "AS", "UM", "GU", "MP", "PR", "VI")
    private val NWS_SUPPORTED_LOCATIONS = listOf(
        US_BOUNDING_BOX,
        PR_BOUNDING_BOX,
        VI_BOUNDING_BOX,
        GU_MP_BOUNDING_BOX,
        AS_BOUNDING_BOX
    )

    fun isUS(countryCode: String?): Boolean {
        return if (countryCode.isNullOrBlank()) {
            false
        } else {
            countryCode.equals("us", ignoreCase = true) || countryCode.equals(
                "usa",
                ignoreCase = true
            ) || countryCode.lowercase().contains("united states")
        }
    }

    fun isUS(location: LocationData): Boolean {
        return if (!location.countryCode.isNullOrBlank()) {
            isUS(location.countryCode)
        } else {
            US_BOUNDING_BOX.intersects(location.latitude, location.longitude)
        }
    }

    fun isUS(location: LocationQuery): Boolean {
        return if (!location.locationCountry.isNullOrBlank()) {
            isUS(location.locationCountry)
        } else {
            US_BOUNDING_BOX.intersects(location.locationLat, location.locationLong)
        }
    }

    fun isUSorCanada(countryCode: String?): Boolean {
        return if (countryCode.isNullOrBlank()) {
            false
        } else {
            isUS(countryCode) || countryCode.equals(
                "CA",
                ignoreCase = true
            ) || countryCode.lowercase().contains("canada")
        }
    }

    fun isUSorCanada(location: LocationData): Boolean {
        return if (!location.countryCode.isNullOrBlank()) {
            isUSorCanada(location.countryCode)
        } else {
            USCA_BOUNDING_BOX.intersects(location.latitude, location.longitude)
        }
    }

    fun isUSorCanada(location: LocationQuery): Boolean {
        return if (!location.locationCountry.isNullOrBlank()) {
            isUSorCanada(location.locationCountry)
        } else {
            USCA_BOUNDING_BOX.intersects(location.locationLat, location.locationLong)
        }
    }

    fun isFrance(countryCode: String?): Boolean {
        return if (countryCode.isNullOrBlank()) {
            false
        } else {
            countryCode.equals("fr", ignoreCase = true) || countryCode.equals(
                "france",
                ignoreCase = true
            )
        }
    }

    fun isFrance(location: LocationData): Boolean {
        return if (!location.countryCode.isNullOrBlank()) {
            isFrance(location.countryCode)
        } else {
            FR_BOUNDING_BOX.intersects(location.latitude, location.longitude)
        }
    }

    fun isFrance(location: LocationQuery): Boolean {
        return if (!location.locationCountry.isNullOrBlank()) {
            isFrance(location.locationCountry)
        } else {
            FR_BOUNDING_BOX.intersects(location.locationLat, location.locationLong)
        }
    }

    fun isNWSSupported(countryCode: String?): Boolean {
        return NWS_SUPPORTED_COUNTRIES.contains(countryCode?.uppercase())
    }

    fun isNWSSupported(location: LocationData): Boolean {
        return if (!location.countryCode.isNullOrBlank()) {
            isNWSSupported(location.countryCode)
        } else {
            NWS_SUPPORTED_LOCATIONS.any { it.intersects(location.latitude, location.longitude) }
        }
    }

    fun isNWSSupported(location: LocationQuery): Boolean {
        return if (!location.locationCountry.isNullOrBlank()) {
            isNWSSupported(location.locationCountry)
        } else {
            NWS_SUPPORTED_LOCATIONS.any {
                it.intersects(
                    location.locationLat,
                    location.locationLong
                )
            }
        }
    }

    fun isGermany(countryCode: String?): Boolean {
        return if (countryCode.isNullOrBlank()) {
            false
        } else {
            countryCode.equals("de", ignoreCase = true) || countryCode.equals(
                "germany",
                ignoreCase = true
            )
        }
    }

    fun isGermany(location: LocationData): Boolean {
        return if (!location.countryCode.isNullOrBlank()) {
            isGermany(location.countryCode)
        } else {
            DE_BOUNDING_BOX.intersects(location.latitude, location.longitude)
        }
    }

    fun isGermany(location: LocationQuery): Boolean {
        return if (!location.locationCountry.isNullOrBlank()) {
            isGermany(location.locationCountry)
        } else {
            DE_BOUNDING_BOX.intersects(location.locationLat, location.locationLong)
        }
    }

    private data class BoundingBox(
        val lat_min: Double,
        val lat_max: Double,
        val lon_min: Double,
        val lon_max: Double,
    ) {
        fun intersects(lat: Double, lon: Double): Boolean {
            return (lat in lat_min..lat_max) && (lon in lon_min..lon_max)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BoundingBox

            if (lat_min != other.lat_min) return false
            if (lat_max != other.lat_max) return false
            if (lon_min != other.lon_min) return false
            if (lon_max != other.lon_max) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lat_min.hashCode()
            result = 31 * result + lat_max.hashCode()
            result = 31 * result + lon_min.hashCode()
            result = 31 * result + lon_max.hashCode()
            return result
        }
    }
}

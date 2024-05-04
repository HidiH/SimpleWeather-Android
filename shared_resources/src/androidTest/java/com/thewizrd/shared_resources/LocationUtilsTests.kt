package com.thewizrd.shared_resources

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.utils.LocationUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationUtilsTests {
    @Test
    fun isNYC_US() {
        val location = LocationData().apply {
            this.tzLong = "America/New_York"
        }
        val location2 = LocationData().apply {
            this.latitude = 40.7484445
            this.longitude = -73.9882393
        }

        Assert.assertTrue(LocationUtils.isUS(location))
        Assert.assertTrue(LocationUtils.isUS(location2))
    }

    @Test
    fun isPuertoRico() {
        val location = LocationData().apply {
            this.tzLong = "America/Puerto_Rico"
        }
        val location2 = LocationData().apply {
            this.latitude = 40.7484445
            this.longitude = -73.9882393
        }

        Assert.assertTrue(LocationUtils.isNWSSupported(location))
        Assert.assertTrue(LocationUtils.isNWSSupported(location2))
    }

    @Test
    fun isUSVI() {
        val location = LocationData().apply {
            this.tzLong = "America/St_Thomas"
        }
        val location2 = LocationData().apply {
            this.latitude = 17.726257
            this.longitude = -64.835823
        }

        Assert.assertTrue(LocationUtils.isNWSSupported(location))
        Assert.assertTrue(LocationUtils.isNWSSupported(location2))
    }

    @Test
    fun isGuam_US() {
        val location = LocationData().apply {
            this.tzLong = "Pacific/Guam"
        }
        val location2 = LocationData().apply {
            this.latitude = 13.4623618
            this.longitude = 144.7946835
        }

        Assert.assertTrue(LocationUtils.isNWSSupported(location))
        Assert.assertTrue(LocationUtils.isNWSSupported(location2))
    }

    @Test
    fun isAmericaSomoa_US() {
        val location = LocationData().apply {
            this.tzLong = "Pacific/Pago_Pago"
        }
        val location2 = LocationData().apply {
            this.latitude = 17.726257
            this.longitude = -64.835823
        }

        Assert.assertTrue(LocationUtils.isNWSSupported(location))
        Assert.assertTrue(LocationUtils.isNWSSupported(location2))
    }

    @Test
    fun isCanada() {
        val location = LocationData().apply {
            this.tzLong = "America/Montreal"
        }
        val location2 = LocationData().apply {
            this.latitude = 45.5593046
            this.longitude = -73.8766794
        }

        Assert.assertTrue(LocationUtils.isUSorCanada(location))
        Assert.assertTrue(LocationUtils.isUSorCanada(location2))
    }

    @Test
    fun isFrance() {
        val location = LocationData().apply {
            this.tzLong = "Europe/Paris"
        }
        val location2 = LocationData().apply {
            this.latitude = 48.8589384
            this.longitude = 2.2646349
        }

        Assert.assertTrue(LocationUtils.isFrance(location))
        Assert.assertTrue(LocationUtils.isFrance(location2))
    }
}
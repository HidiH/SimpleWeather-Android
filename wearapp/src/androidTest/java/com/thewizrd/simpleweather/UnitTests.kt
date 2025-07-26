package com.thewizrd.simpleweather

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thewizrd.common.CommonModule
import com.thewizrd.common.commonModule
import com.thewizrd.shared_resources.AppState
import com.thewizrd.shared_resources.ApplicationLib
import com.thewizrd.shared_resources.DateTimeConstants
import com.thewizrd.shared_resources.SharedModule
import com.thewizrd.shared_resources.appLib
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.preferences.SettingsManager
import com.thewizrd.shared_resources.sharedDeps
import com.thewizrd.shared_resources.utils.DateTimeUtils
import com.thewizrd.shared_resources.utils.ZoneIdCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class UnitTests {
    private lateinit var context: Context
    private var wasUsingPersonalKey = false

    @Before
    fun init() {
        // Context of the app under test.
        context = ApplicationProvider.getApplicationContext()

        appLib = object : ApplicationLib() {
            override val context: Context
                get() = this@UnitTests.context.applicationContext
            override val preferences: SharedPreferences
                get() = PreferenceManager.getDefaultSharedPreferences(context)

            override fun registerAppSharedPreferenceListener() {}
            override fun unregisterAppSharedPreferenceListener() {}
            override fun registerAppSharedPreferenceListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
            override fun unregisterAppSharedPreferenceListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}

            override val appState: AppState
                get() = AppState.BACKGROUND
            override val isPhone = false
            override val properties = Bundle()
            override val settingsManager = SettingsManager(context)
        }

        // Needs to be called on main thread
        runBlocking(Dispatchers.Main.immediate) {
            sharedDeps = object : SharedModule() {
                override val context = this@UnitTests.context.applicationContext
            }
        }

        commonModule = object : CommonModule() {
            override val context = appLib.context
        }

        runBlocking {
            settingsManager.loadIfNeeded()
        }

        settingsManager.getAPI()?.let { api ->
            if (settingsManager.usePersonalKey(api)) {
                settingsManager.setPersonalKey(api, false)
                wasUsingPersonalKey = true
            }
        }
    }

    @After
    fun destroy() {
        settingsManager.getAPI()?.let { api ->
            if (wasUsingPersonalKey) {
                settingsManager.setPersonalKey(api, true)
                wasUsingPersonalKey = false
            }
        }
    }

    @Test
    fun tzdbTest() {
        val tzLong = "Asia/Qostanay" // tzdb - 2018h

        val zId = ZoneIdCompat.of(tzLong)
        Assert.assertNotNull(zId)

        val zDT = ZonedDateTime.now(zId)
        val zStr = zDT.format(
            DateTimeUtils.ofPatternForUserLocale(DateTimeConstants.TIMEZONE_NAME)
        )
        Log.d("tzdbtest", "DT = ${zDT.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}")
        Log.d("tzdbtest", "Z = $zStr")

        Assert.assertTrue(zStr == "Asia/Qostanay" || zStr == "GMT+06:00")
    }
}
package com.thewizrd.simpleweather.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.thewizrd.shared_resources.utils.UserThemeMode
import com.thewizrd.shared_resources.utils.UserThemeMode.OnThemeChangeListener
import com.thewizrd.simpleweather.helpers.WindowColorManager
import com.thewizrd.simpleweather.locale.UserLocaleActivity
import kotlinx.coroutines.launch

abstract class WindowColorActivity : UserLocaleActivity(), OnThemeChangeListener,
    WindowColorManager {
    private var prevConfig: Configuration? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                updateWindowColors()
            }
        }
    }

    abstract override fun onThemeChanged(mode: UserThemeMode)
    abstract override fun updateWindowColors()

    @CallSuper
    override fun onStart() {
        super.onStart()
        prevConfig = Configuration(this.resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (prevConfig == null || (newConfig.diff(prevConfig) and ActivityInfo.CONFIG_ORIENTATION) != 0) {
            updateWindowColors()
        }

        prevConfig = Configuration(newConfig)
    }
}
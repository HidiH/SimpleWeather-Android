package com.thewizrd.simpleweather.ui.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.TimeSource
import com.thewizrd.shared_resources.utils.ZoneIdCompat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Based on [androidx.wear.compose.material3.DefaultTimeSource]
 */
class ZonedTimeSource constructor(timeFormat: String, private val timeZone: String? = null) :
    TimeSource {
    private val _timeFormat = timeFormat

    @Composable
    override fun currentTime(): String = currentTime({ Instant.now() }, _timeFormat, timeZone).value
}

@Composable
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun currentTime(
    instant: () -> Instant,
    timeFormat: String,
    timeZone: String? = null
): State<String> {
    val context = LocalContext.current
    val config = LocalConfiguration.current

    var zoneId: ZoneId by remember {
        mutableStateOf(timeZone?.let { ZoneIdCompat.of(it) } ?: ZoneId.systemDefault())
    }
    var currentInstant by remember { mutableStateOf(instant()) }
    val locale by remember(config) { mutableStateOf(config.locales[0]) }

    val timeText = remember {
        derivedStateOf { formatInstant(currentInstant, timeFormat, zoneId, locale) }
    }

    val updatedInstantLambda by rememberUpdatedState(instant)

    DisposableEffect(context, updatedInstantLambda) {
        val receiver = TimeBroadcastReceiver(
            onTimeChanged = { currentInstant = updatedInstantLambda() },
            onTimeZoneChanged = { if (timeZone == null) zoneId = ZoneId.systemDefault() }
        )
        receiver.register(context)
        onDispose {
            receiver.unregister(context)
        }
    }
    return timeText
}

/**
 * A [BroadcastReceiver] to receive time tick, time change, and time zone change events.
 */
private class TimeBroadcastReceiver(
    val onTimeChanged: () -> Unit,
    val onTimeZoneChanged: () -> Unit,
) : BroadcastReceiver() {
    private var registered = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            onTimeZoneChanged()
        } else {
            onTimeChanged()
        }
    }

    fun register(context: Context) {
        if (!registered) {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_TIME_CHANGED)
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            context.registerReceiver(this, filter)
            registered = true
        }
    }

    fun unregister(context: Context) {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }
}

private fun formatInstant(
    currentInstant: Instant,
    timeFormat: String,
    zoneId: ZoneId,
    locale: Locale
): String {
    return ZonedDateTime.ofInstant(currentInstant, zoneId)
        .format(DateTimeFormatter.ofPattern(timeFormat, locale))
}

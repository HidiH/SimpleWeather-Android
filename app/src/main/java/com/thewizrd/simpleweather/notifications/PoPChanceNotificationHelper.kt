package com.thewizrd.simpleweather.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.thewizrd.common.helpers.areNotificationsEnabled
import com.thewizrd.shared_resources.DateTimeConstants
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.helpers.toImmutableCompatFlag
import com.thewizrd.shared_resources.icons.WeatherIcons
import com.thewizrd.shared_resources.icons.WeatherIconsEFProvider
import com.thewizrd.shared_resources.locationdata.LocationData
import com.thewizrd.shared_resources.sharedDeps
import com.thewizrd.shared_resources.utils.DateTimeUtils
import com.thewizrd.shared_resources.utils.Logger
import com.thewizrd.shared_resources.weatherdata.model.HourlyForecast
import com.thewizrd.shared_resources.weatherdata.model.MinutelyForecast
import com.thewizrd.simpleweather.R
import com.thewizrd.simpleweather.main.MainActivity
import com.thewizrd.simpleweather.services.WidgetUpdaterWorker
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object PoPChanceNotificationHelper {
    // Sets an ID for the notification
    private const val NOT_CHANNEL_ID = "SimpleWeather.chancenotification"

    suspend fun postNotification(context: Context) {
        if (!settingsManager.isPoPChanceNotificationEnabled() || !settingsManager.isWeatherLoaded() || !context.areNotificationsEnabled()) return

        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val lastPostTime = settingsManager.getLastPoPChanceNotificationTime()

        // We already posted recently; skip for now
        if (now.isBefore(lastPostTime)) return

        // Get the forecast for the next 12 hours
        val location = settingsManager.getHomeData() ?: return
        val minForecasts =
            settingsManager.getWeatherForecastData(location.query)?.minForecast?.filter {
                !it.date.isBefore(now.withZoneSameInstant(location.tzOffset))
            }

        // Create minutely precipitation notification if possible
        if (!createMinutelyNotification(context, location, minForecasts, now)) {
            // If not fallback to PoP% notification
            val nowHour = now.withZoneSameInstant(location.tzOffset).truncatedTo(ChronoUnit.HOURS)
            val hrForecasts =
                settingsManager.getHourlyForecastsByQueryOrderByDateByLimitFilterByDate(
                    location.query,
                    12,
                    nowHour
                )

            if (!createPoPNotification(context, location, hrForecasts, now)) {
                return
            }
        }
    }

    private fun createPoPNotification(
        context: Context,
        location: LocationData,
        hrForecasts: List<HourlyForecast>?,
        now: ZonedDateTime
    ): Boolean {
        if (hrForecasts.isNullOrEmpty()) return false

        // Find the next hour with a 60% or higher chance of precipitation
        val forecast =
            hrForecasts.find { it.extras?.pop != null && it.extras.pop >= settingsManager.getPoPChanceMinimumPercentage() }

        // Proceed if within the next 2hrs
        if (forecast == null || Duration.between(now.truncatedTo(ChronoUnit.HOURS), forecast.date)
                .toHours() >= 2
        ) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannel(context)
        }

        val wim = sharedDeps.weatherIconsManager

        // Should be within 0-3 hours
        val dt =
            forecast.date.truncatedTo(ChronoUnit.HOURS).withZoneSameInstant(ZoneId.systemDefault())
        val time = if (DateFormat.is24HourFormat(context)) {
            val skeleton = DateTimeConstants.SKELETON_24HR
            dt.format(
                DateTimeUtils.ofPatternForUserLocale(
                    DateTimeUtils.getBestPatternForSkeleton(
                        skeleton
                    )
                )
            )
        } else {
            val pattern = DateTimeConstants.ABBREV_12HR_AMPM
            dt.format(DateTimeUtils.ofPatternForUserLocale(pattern))
        }
        val duraStr =
            context.getString(R.string.precipitation_likely_text_format, time, forecast.extras.pop)

        val notifBuilder = NotificationCompat.Builder(context, NOT_CHANNEL_ID)
            //.setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getOnClickIntent(context, location))
            .setContentTitle(duraStr)
            .setContentText(location.name)
            .setTimeoutAfter(TimeUnit.HOURS.toMillis(3))

        if (wim.isFontIcon) {
            notifBuilder.setSmallIcon(wim.getWeatherIconResource(WeatherIcons.UMBRELLA))
        } else {
            // Use default icon pack here; animated icons are not supported here
            val wip = wim.getIconProvider(WeatherIconsEFProvider.KEY)
            notifBuilder.setSmallIcon(wip.getWeatherIconResource(WeatherIcons.UMBRELLA))
        }

        val notifMgr = context.getSystemService<NotificationManager>()!!
        notifMgr.notify(NOT_CHANNEL_ID, location.hashCode(), notifBuilder.build())

        // Find the next hour with < 60% or higher chance of precipitation
        val stopForecast =
            hrForecasts.find { it.extras?.pop == null || it.extras.pop < settingsManager.getPoPChanceMinimumPercentage() }
        // Delay further notifications until this time
        val nextTime = stopForecast?.date?.truncatedTo(ChronoUnit.HOURS) ?: now.plusHours(2)
        settingsManager.setLastPoPChanceNotificationTime(nextTime.minusMinutes(5))

        return true
    }

    private suspend fun createMinutelyNotification(
        context: Context,
        location: LocationData,
        minForecasts: List<MinutelyForecast>?,
        now: ZonedDateTime
    ): Boolean {
        if (minForecasts.isNullOrEmpty()) return false

        val isRainingMinute = minForecasts.firstOrNull {
            Duration.between(
                now.truncatedTo(ChronoUnit.MINUTES),
                it.date.truncatedTo(ChronoUnit.MINUTES)
            ).abs().toMinutes() <= 5 && it.rainMm > 0
        }

        val minute = if (isRainingMinute != null) {
            // Find minute where rain stops
            minForecasts.firstOrNull {
                it.date.truncatedTo(ChronoUnit.MINUTES)
                    .isAfter(isRainingMinute.date) && it.rainMm <= 0
            }
        } else {
            // Find minute where rain starts
            minForecasts.firstOrNull {
                it.date.truncatedTo(ChronoUnit.MINUTES)
                    .isAfter(now.truncatedTo(ChronoUnit.MINUTES)) && it.rainMm > 0
            }
        } ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannel(context)
        }

        val wim = sharedDeps.weatherIconsManager

        val formatStrResId = if (isRainingMinute != null) {
            R.string.precipitation_likely_minutely_stopping_text_format
        } else {
            R.string.precipitation_likely_minutely_starting_text_format
        }
        val dt =
            minute.date.truncatedTo(ChronoUnit.MINUTES).withZoneSameInstant(ZoneId.systemDefault())
        val time = if (DateFormat.is24HourFormat(context)) {
            dt.format(DateTimeUtils.ofPatternForUserLocale(DateTimeConstants.CLOCK_FORMAT_24HR))
        } else {
            dt.format(DateTimeUtils.ofPatternForUserLocale(DateTimeConstants.CLOCK_FORMAT_12HR_AMPM))
        }
        val formatStr = context.getString(formatStrResId, time)

        val duration = Duration.between(now, minute.date).abs()

        val notifBuilder = NotificationCompat.Builder(context, NOT_CHANNEL_ID)
            //.setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getOnClickIntent(context, location))
            .setContentTitle(formatStr)
            .setContentText(location.name)
            .setTimeoutAfter(duration.multipliedBy(2).toMillis())

        if (wim.isFontIcon) {
            notifBuilder.setSmallIcon(wim.getWeatherIconResource(WeatherIcons.UMBRELLA))
        } else {
            // Use default icon pack here; animated icons are not supported here
            val wip = wim.getIconProvider(WeatherIconsEFProvider.KEY)
            notifBuilder.setSmallIcon(wip.getWeatherIconResource(WeatherIcons.UMBRELLA))
        }

        val notifMgr = context.getSystemService<NotificationManager>()!!
        notifMgr.notify(NOT_CHANNEL_ID, location.hashCode(), notifBuilder.build())

        // Delay further notifications until this time
        val nextTime = minute.date.truncatedTo(ChronoUnit.MINUTES)
        settingsManager.setLastPoPChanceNotificationTime(nextTime)

        if (duration.toMinutes() < 60) {
            runCatching {
                WidgetUpdaterWorker.schedulePoPNotification(context, duration)
            }.onFailure {
                Logger.error("PoPChanceNotificationHelper", it)
            }
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initChannel(context: Context) {
        // Gets an instance of the NotificationManager service
        val mNotifyMgr = context.getSystemService(NotificationManager::class.java)
        var mChannel = mNotifyMgr.getNotificationChannel(NOT_CHANNEL_ID)
        val notChannelName = context.getString(R.string.not_channel_name_precipnotification)
        if (mChannel == null) {
            mChannel = NotificationChannel(
                NOT_CHANNEL_ID,
                notChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }

        // Configure the notification channel.
        mChannel.name = notChannelName
        mNotifyMgr.createNotificationChannel(mChannel)
    }

    private fun getOnClickIntent(context: Context, location: LocationData): PendingIntent {
        // When user clicks on widget, launch to WeatherNow page
        val onClickIntent = Intent(context.applicationContext, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        return PendingIntent.getActivity(
            context, location.hashCode(),
            onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT.toImmutableCompatFlag()
        )
    }
}
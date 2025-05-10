package com.thewizrd.simpleweather.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class PowerUtils {
    companion object {
        const val KEY_REQUESTIGNOREBATOPTS = "key_request_ignorebatopts"

        @JvmStatic
        fun checkBackgroundOptimizationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        @RequiresApi(Build.VERSION_CODES.M)
        @JvmStatic
        fun isBackgroundOptimizationDisabled(context: Context): Boolean {
            val pwrMan = context.getSystemService(PowerManager::class.java)
            return pwrMan.isIgnoringBatteryOptimizations(context.packageName)
        }

        @SuppressLint("BatteryLife")
        @RequiresApi(Build.VERSION_CODES.M)
        @JvmStatic
        fun canStartIgnoreBatteryOptActivity(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).also {
                it.data = Uri.Builder().scheme("package").authority(context.packageName).build()
            }
            return intent.resolveActivity(context.packageManager) != null
        }

        @SuppressLint("BatteryLife")
        @RequiresApi(Build.VERSION_CODES.M)
        @JvmStatic
        fun startIgnoreBatteryOptActivity(context: Context) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).also {
                it.data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        }
    }
}
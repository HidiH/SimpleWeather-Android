package com.thewizrd.simpleweather.ui.helpers

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberLocationPermissionLauncher(
    locationCallback: ((Boolean) -> Unit)? = null,
    bgLocationCallback: ((Boolean) -> Unit)? = null
): LocationPermissionLauncher {
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
            locationCallback?.invoke(it[Manifest.permission.ACCESS_COARSE_LOCATION] == true || it[Manifest.permission.ACCESS_FINE_LOCATION] == true)
        }

    val bgLocationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            bgLocationCallback?.invoke(it)
        }

    return remember {
        object : LocationPermissionLauncher {
            override fun requestLocationPermission() {
                locationPermissionLauncher.launch(
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                )
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun requestBackgroundLocationPermission() {
                bgLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }
}

interface LocationPermissionLauncher {
    fun requestLocationPermission()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission()
}
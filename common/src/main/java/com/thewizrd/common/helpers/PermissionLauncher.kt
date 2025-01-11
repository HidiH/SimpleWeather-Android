package com.thewizrd.common.helpers

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PermissionLauncher {
    constructor(
        activity: ComponentActivity,
        callback: ((Map<String, Boolean>) -> Unit)? = null
    ) {
        permissionsLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                callback?.invoke(result)
            }
    }

    constructor(
        fragment: Fragment,
        callback: ((Map<String, Boolean>) -> Unit)? = null
    ) {
        permissionsLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                callback?.invoke(result)
            }
    }

    private val permissionsLauncher: ActivityResultLauncher<Array<String>>

    fun requestPermission(permission: String) {
        requestPermissions(arrayOf(permission))
    }

    fun requestPermissions(permissions: List<String>) {
        requestPermissions(permissions.toTypedArray())
    }

    fun requestPermissions(permissions: Array<String>) {
        permissionsLauncher.launch(permissions)
    }
}
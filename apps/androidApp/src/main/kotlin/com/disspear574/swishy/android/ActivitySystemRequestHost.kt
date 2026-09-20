package com.disspear574.swishy.android

import android.app.Activity
import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.disspear574.swishy.media.SystemRequestHost
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ActivitySystemRequestHost(activity: ComponentActivity) : SystemRequestHost {

    private var pendingSender: ((Boolean) -> Unit)? = null
    private var pendingPermissions: ((Boolean) -> Unit)? = null

    private val senderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            pendingSender?.invoke(result.resultCode == Activity.RESULT_OK)
            pendingSender = null
        }

    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            pendingPermissions?.invoke(grants.values.any { granted -> granted })
            pendingPermissions = null
        }

    override suspend fun launch(intentSender: IntentSender): Boolean =
        suspendCancellableCoroutine { continuation ->
            pendingSender = { confirmed -> continuation.resume(confirmed) }
            continuation.invokeOnCancellation { pendingSender = null }
            senderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }

    override suspend fun requestPermissions(permissions: Array<String>): Boolean =
        suspendCancellableCoroutine { continuation ->
            pendingPermissions = { granted -> continuation.resume(granted) }
            continuation.invokeOnCancellation { pendingPermissions = null }
            permissionLauncher.launch(permissions)
        }
}

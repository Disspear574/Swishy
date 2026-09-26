package com.disspear574.swishy.media

import android.content.IntentSender

/** Shows Activity-owned system dialogs from code that has no Activity. */
interface SystemRequestHost {

    suspend fun launch(intentSender: IntentSender): Boolean

    suspend fun requestPermissions(permissions: Array<String>): Boolean
}

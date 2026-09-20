package com.disspear574.swishy.media

import android.content.IntentSender

interface SystemRequestHost {

    suspend fun launch(intentSender: IntentSender): Boolean

    suspend fun requestPermissions(permissions: Array<String>): Boolean
}
